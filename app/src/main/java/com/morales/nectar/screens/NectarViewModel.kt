package com.morales.nectar.screens

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.morales.nectar.data.Event
import com.morales.nectar.data.remote.requests.PlantData
import com.morales.nectar.data.remote.responses.CareLogEntry
import com.morales.nectar.data.remote.responses.UserData
import com.morales.nectar.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject


private const val USERS = "users"
private const val POSTS = "posts"
private const val CARE = "care"
private const val TAG = "NectarViewModel"
private val FILLER_WORDS = listOf("the", "be", "to", "is", "of", "and", "or", "a", "in", "it")


@HiltViewModel
class NectarViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val repository: UserRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    val signedIn = mutableStateOf(false)
    val isLoading = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)
    val popupNotification = mutableStateOf<Event<String>?>(null)

    val refreshPostsProgress = mutableStateOf(false)
    val posts = mutableStateOf<List<PlantData>>(listOf())

    val searchedPosts = mutableStateOf<List<PlantData>>(listOf())
    val searchedPostsProgress = mutableStateOf(false)

    val postsFeed = mutableStateOf<List<PlantData>>(listOf())
    val postsFeedProgress = mutableStateOf(false)

    val careLogEntries = mutableStateOf<List<CareLogEntry>>(listOf())
    val careLogEntriesProgress = mutableStateOf(false)

    val numFollowers = mutableStateOf(0)

    val currentPlant = mutableStateOf<PlantData?>(null)

    init {
        val currentUser = auth.currentUser
        signedIn.value = currentUser != null
        currentUser?.uid?.let { authId ->
            getUserData(authId = authId)
        }
    }

    private fun getUserData(authId: String) {
        isLoading.value = true
        firestore.collection("users").document(authId).get()
            .addOnSuccessListener {
                val user = it.toObject<UserData>()
                userData.value = user
                isLoading.value = false
                refreshPosts()
                getFollowers(user?.authId)
            }
            .addOnFailureListener { exception ->
                handleException(exception, "Cannot retrieve user data")
                isLoading.value = false
            }
    }

    fun onLogin(email: String, pass: String) {
        if (email.isEmpty() or pass.isEmpty()) {
            handleException(customMessage = "Please fill in all fields")
            return
        }
        isLoading.value = true
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    signedIn.value = true
                    isLoading.value = false
                    auth.currentUser?.uid?.let { uid ->
                        getUserData(uid)
                        handleException(customMessage = "Login successful")
                    }
                } else {
                    handleException(task.exception, "Login failed")
                    isLoading.value = false
                }
            }
            .addOnFailureListener { exception ->
                handleException(exception)
                isLoading.value = false
            }
    }

    fun onSignUp(username: String, email: String, password: String) {
        if (username.isEmpty() or email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please fill in all fields")
            return
        }
        viewModelScope.launch {
            isLoading.value = true

            //Check if username is taken
            firestore.collection("users").whereEqualTo("username", username).get()
                .addOnSuccessListener { userList ->
                    if (userList.size() > 0) {
                        handleException(customMessage = "Username already exists")
                        isLoading.value = false
                    } else {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    signedIn.value = true
                                    //Create Profile
                                    createOrUpdateProfile(username = username, email = email)
                                } else {
                                    handleException(task.exception, "Signup Failed")
                                }
                            }
                    }
                }

        }
    }

    private fun createOrUpdateProfile(
        name: String? = null,
        username: String? = null,
        email: String? = null,
        imageUrl: String? = null,
    ) {
        val authId = auth.currentUser?.uid
        val user = UserData(
            authId = authId,
            name = name ?: userData.value?.name,
            username = username ?: userData.value?.username,
            email = email ?: userData.value?.email,
            imageUrl = imageUrl ?: userData.value?.imageUrl
        )

        authId?.let { id ->
            isLoading.value = true
            firestore.collection("users").document(id).get()
                .addOnSuccessListener { fetchedUser ->
                    if (!fetchedUser.exists()) {
                        firestore.collection("users").document(id).set(user)
                        getUserData(id)
                        isLoading.value = false
                    } else {
                        fetchedUser.reference.update(user.toMap())
                            .addOnSuccessListener {
                                this.userData.value = user
                                isLoading.value = false
                            }
                            .addOnFailureListener { e ->
                                handleException(e, "Cannot update user")
                                isLoading.value = false
                            }
                    }
                }.addOnFailureListener { exception ->
                    handleException(e = exception, customMessage = "Cannot Create User")
                    isLoading.value = false
                }
        }
    }

    private fun convertPosts(
        postsSnapshot: QuerySnapshot,
        outState: MutableState<List<PlantData>>
    ) {
        val convertedPosts = postsSnapshot
            .map { p ->
                p.toObject<PlantData>()
            }
            .toMutableList()
        outState.value = convertedPosts
    }

    private fun refreshPosts() {
        val currentUid = auth.currentUser?.uid

        if (currentUid == null) {
            handleException(customMessage = "Error: username unavailable, unable to refresh posts")
            onLogout()
            return
        }

        refreshPostsProgress.value = true
        firestore.collection(POSTS).whereEqualTo("userId", currentUid).get()
            .addOnSuccessListener { docs ->
                convertPosts(docs, posts)
                refreshPostsProgress.value = false
            }
            .addOnFailureListener { e ->
                handleException(e, "Cannot fetch plants")
                refreshPostsProgress.value = false
            }

    }

    fun updateProfile(name: String, username: String) {
        createOrUpdateProfile(name = name, username = username)
    }

    private fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        isLoading.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)

        uploadTask
            .addOnSuccessListener {
                val result = it.metadata?.reference?.downloadUrl
                result?.addOnSuccessListener(onSuccess)
            }
            .addOnFailureListener { e ->
                handleException(e)
                isLoading.value = false
            }

    }

    fun uploadProfileImage(uri: Uri): String? {
        uploadImage(uri) {
            createOrUpdateProfile(imageUrl = it.toString())
        }
        isLoading.value = false
        val updatedUser = userData.value
        return updatedUser?.imageUrl
    }

    fun fetchPlantById(id: String?) {
        isLoading.value = true
        if (id == null) {
            currentPlant.value = null
        }
        viewModelScope.launch(Dispatchers.Main) {
            firestore.collection(POSTS).document(id!!).get()
                .addOnSuccessListener {
                    currentPlant.value = it.toObject<PlantData>()
                    isLoading.value = false
                }
                .addOnFailureListener { e ->
                    handleException(e, "Unable to fetch plant data")
                    isLoading.value = false
                }
        }
    }

    fun onEditPost(
        originalPlant: PlantData,
        uris: List<Uri?>,
        commonName: String,
        scientificName: String?,
        toxicity: String?
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            onEditPostAsync(originalPlant, uris, commonName, scientificName, toxicity)
        }
    }

    private suspend fun onEditPostAsync(
        originalPlant: PlantData,
        uris: List<Uri?>,
        commonName: String,
        scientificName: String?,
        toxicity: String?
    ) {
        isLoading.value = true
        if (originalPlant.plantId == null) {
            handleException(customMessage = "Error: unable to update plant")
            isLoading.value = false
            return
        }
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            handleException(customMessage = "Error: username unavailable. unable to update post")
            onLogout()
            isLoading.value = false
            return
        }

        val originalImages = originalPlant.images
        val resultUris = uploadImages(uris)
        val updatedUris = mutableListOf<String>()
        if (originalImages != null) {
            for (i in resultUris.indices) {
                val currentResultUri = resultUris[i]
                if (currentResultUri != null) {
                    updatedUris.add(currentResultUri.toString())
                } else if (i < originalImages.size) {
                    updatedUris.add(originalImages[i])
                }
            }
        } else {
            updatedUris.addAll(resultUris
                .filterNotNull()
                .map { uri -> uri.toString() }
                .filter { uri -> uri.isNotEmpty() }
                .toMutableList())
        }

        originalPlant.commonName = commonName
        originalPlant.scientificName = scientificName
        originalPlant.toxicity = toxicity
        originalPlant.searchTerms = commonName
            .split(" ", ".", ",", "?", "!", "#")
            .map { it.lowercase() }
            .filter { it.isNotEmpty() and !FILLER_WORDS.contains(it) }
        val plantDataMap = originalPlant.toMap().toMutableMap()

        plantDataMap["images"] = updatedUris

        Log.i(TAG, plantDataMap.toString())

        firestore.collection(POSTS).document(originalPlant.plantId).update(plantDataMap)
            .addOnSuccessListener {
                popupNotification.value = Event("Plant successfully updated")
                isLoading.value = false
                refreshPosts()
            }
            .addOnFailureListener { e ->
                handleException(e, "Unable to update post")
                isLoading.value = false
            }
    }

    fun onNewPost(uris: List<Uri>, commonName: String, scientificName: String?, toxicity: String?) {
        viewModelScope.launch(Dispatchers.Main) {
            onNewPostAsync(uris, commonName, scientificName, toxicity)
        }
    }

    private suspend fun onNewPostAsync(
        uris: List<Uri>,
        commonName: String,
        scientificName: String?,
        toxicity: String?
    ) {
        isLoading.value = true
        val uriList = uris.filter { uri -> uri.toString().isNotEmpty() }
        val resultUris = uploadImages(uriList).filterNotNull()

        val currentUid = auth.currentUser?.uid
        val currentUsername = userData.value?.username
        val currentUserImage = userData.value?.imageUrl

        if (currentUid == null) {
            handleException(customMessage = "Error: username unavailable. unable to create post")
            onLogout()
            isLoading.value = false
            return
        }

        val postUuid = UUID.randomUUID().toString()

        val post = PlantData(
            plantId = postUuid,
            commonName = commonName,
            scientificName = scientificName,
            toxicity = toxicity,
            userId = currentUid,
            username = currentUsername,
            userImage = currentUserImage,
            images = resultUris.map { uri -> uri.toString() },
            likes = listOf(),
            searchTerms = commonName
                .split(" ", ".", ",", "?", "!", "#")
                .map { it.lowercase() }
                .filter { it.isNotEmpty() and !FILLER_WORDS.contains(it) }
        )
        Log.i(TAG, post.toString())
        firestore.collection(POSTS).document(postUuid).set(post)
            .addOnSuccessListener {
                popupNotification.value = Event("Post successfully created")
                isLoading.value = false
                refreshPosts()
            }
            .addOnFailureListener { e ->
                handleException(e, "Unable to create post")
                isLoading.value = false
            }
    }

    private suspend fun uploadAsync(uri: Uri, storageRef: StorageReference): Uri {
        val fileName = UUID.randomUUID()
        val imageRef = storageRef.child("images/$fileName")
        Log.i(TAG, uri.toString())
        return imageRef
            .putFile(uri)
            .await()
            .storage
            .downloadUrl
            .await()
    }

    private suspend fun uploadImages(uris: List<Uri?>): List<Uri?> {
        isLoading.value = true
        val resultUris = mutableListOf<Uri?>()
        for (uri in uris) {
            if (uri == null || uri.toString().isEmpty()) {
                resultUris.add(null)
            } else {
                val storageRef = storage.reference
                resultUris.add(uploadAsync(uri, storageRef))
            }
        }
        Log.i(TAG, resultUris.toString())
        return resultUris
    }


    fun onLogout() {
        auth.signOut()
        signedIn.value = false
        userData.value = null
        popupNotification.value = Event("Logged out")
        searchedPosts.value = listOf()
        postsFeed.value = listOf()
        posts.value = listOf()
        careLogEntries.value = listOf()
        numFollowers.value = 0
    }

    fun searchPosts(searchTerm: String) {
        if (searchTerm.isEmpty()) {
            return
        }
        searchedPostsProgress.value = true
        firestore
            .collection(POSTS)
            .whereArrayContains("searchTerms", searchTerm.trim().lowercase())
            .get()
            .addOnSuccessListener {
                searchedPosts.value = convertPosts(it)
                searchedPostsProgress.value = false
            }
            .addOnFailureListener { e ->
                handleException(e, customMessage = "Cannot search plants")
                searchedPostsProgress.value = false
            }
    }

    private fun convertPosts(postsSnapshot: QuerySnapshot): MutableList<PlantData> {
        return postsSnapshot
            .map { p ->
                p.toObject<PlantData>()
            }
            .toMutableList()
    }

    fun onFollowClick(userId: String) {
        auth.currentUser?.uid?.let { currentUser ->
            val following = arrayListOf<String>()
            userData.value?.following.let {
                if (it != null) {
                    following.addAll(it)
                }
            }
            if (following.contains(userId)) {
                following.remove(userId)
            } else {
                following.add(userId)
            }
            firestore.collection("users").document(currentUser).update("following", following)
                .addOnSuccessListener {
                    getUserData(currentUser)
                }
        }
    }

    private fun handleException(e: Exception? = null, customMessage: String = "") {
        e?.printStackTrace()
        val errMessage = e?.localizedMessage ?: ""
        val message = if (customMessage.isEmpty()) errMessage else "$customMessage $errMessage"
        Log.i(TAG, message)
        popupNotification.value = Event(message)
    }

    fun getPersonalizedFeed() {
        val following = userData.value?.following
        if (!following.isNullOrEmpty()) {
            postsFeedProgress.value = true
            firestore.collection(POSTS).whereIn("userId", following).get()
                .addOnSuccessListener { res ->
                    postsFeed.value = convertPosts(res)
                    if (postsFeed.value.isEmpty()) {
                        getGenericFeed()
                        postsFeedProgress.value = false
                    } else {
                        postsFeedProgress.value = false
                    }
                }
                .addOnFailureListener { e ->
                    handleException(e, "Cannot get personalized feed")
                    postsFeedProgress.value = false
                }
        } else {
            getGenericFeed()
        }
    }

    private fun getGenericFeed() {
        postsFeedProgress.value = true
        val currentTime = System.currentTimeMillis()
        val dayInMills = 24 * 60 * 60 * 1000 * 10
        firestore.collection(POSTS)
            .whereGreaterThan("time", currentTime - dayInMills)
            .get()
            .addOnSuccessListener { res ->
                postsFeed.value = convertPosts(res)
                postsFeedProgress.value = false
            }
            .addOnFailureListener { e ->
                postsFeedProgress.value = false
                handleException(e, "unable to get feed")
            }
    }

    fun onLikePost(plantData: PlantData) {
        auth.currentUser?.uid?.let { currentUserId ->
            plantData.likes?.let { likes ->
                val newLikes = arrayListOf<String>()
                if (likes.contains(currentUserId)) {
                    newLikes.addAll(likes.filter { currentUserId != it })
                } else {
                    newLikes.addAll(likes)
                    newLikes.add(currentUserId)
                }
                plantData.plantId?.let { postId ->
                    firestore
                        .collection(POSTS)
                        .document(postId)
                        .update("likes", newLikes)
                        .addOnSuccessListener {
                            plantData.likes = newLikes
                        }
                        .addOnFailureListener {
                            handleException(it, "unable to like post")
                        }
                }
            }
        }
    }

    fun createCareLogEntry(
        plantId: String,
        wasWatered: Boolean,
        wasFertilized: Boolean,
        notes: String?,
    ) {
        val entryId = UUID.randomUUID().toString()
        val entry = CareLogEntry(
            id = entryId,
            plantId = plantId,
            wasWatered = wasWatered,
            wasFertilized = wasFertilized,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )
        firestore.collection(CARE).document(entryId).set(entry)
            .addOnSuccessListener {
                //Get existing care log entries
                getCareLogEntries(plantId)
            }
            .addOnFailureListener { e ->
                handleException(e, "Cannot create care log entry")
            }
    }

    fun getCareLogEntries(plantId: String?) {
        careLogEntriesProgress.value = true
        firestore.collection(CARE).whereEqualTo("plantId", plantId).get()
            .addOnSuccessListener { documents ->
                val entries = mutableListOf<CareLogEntry>()
                documents.forEach { document ->
                    Log.i("getCareLogEntries", document.toString())
                    val entry = document.toObject<CareLogEntry>()
                    entries.add(entry)
                }
                val sortedEntries = entries.sortedByDescending { it.timestamp }
                careLogEntries.value = sortedEntries
                careLogEntriesProgress.value = false
            }
            .addOnFailureListener { e ->
                handleException(e, "cannot retrieve care log entries")
                careLogEntriesProgress.value = false
            }
    }

    private fun getFollowers(uid: String?) {
        firestore.collection(USERS).whereArrayContains("following", uid ?: "").get()
            .addOnSuccessListener { documents ->
                numFollowers.value = documents.size()
            }
            .addOnFailureListener { e ->
                handleException(e)
            }
    }

}