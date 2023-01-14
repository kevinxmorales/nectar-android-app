package com.morales.nectar.screens

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.morales.nectar.data.Event
import com.morales.nectar.data.models.CareLogEntry
import com.morales.nectar.data.models.PlantData
import com.morales.nectar.data.models.UserData
import com.morales.nectar.data.remote.requests.care.CareLogRequest
import com.morales.nectar.data.remote.requests.user.CreateUserRequest
import com.morales.nectar.data.remote.requests.user.UpdateUserRequest
import com.morales.nectar.exceptions.UnauthorizedException
import com.morales.nectar.repository.CareLogRepository
import com.morales.nectar.repository.PlantsRepository
import com.morales.nectar.repository.UserRepository
import com.morales.nectar.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

private const val TAG = "NectarViewModel"
private val FILLER_WORDS = listOf("the", "be", "to", "is", "of", "and", "or", "a", "in", "it")


@HiltViewModel
class NectarViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val userRepository: UserRepository,
    private val plantsRepository: PlantsRepository,
    private val careLogRepository: CareLogRepository,
) : ViewModel() {
    val signedIn = mutableStateOf(false)
    val isLoading = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)
    val popupNotification = mutableStateOf<Event<String>?>(null)

    val refreshPlantsProgress = mutableStateOf(false)
    val plants = mutableStateOf<List<PlantData>>(listOf())

    val searchedPlants = mutableStateOf<List<PlantData>>(listOf())
    val searchedPlantsProgress = mutableStateOf(false)

    val plantsFeed = mutableStateOf<List<PlantData>>(listOf())
    val plantsFeedProgress = mutableStateOf(false)

    val careLogEntries = mutableStateOf<List<CareLogEntry>>(listOf())
    val careLogEntriesProgress = mutableStateOf(false)

    val numFollowers = mutableStateOf(0)

    private val currentPlant = mutableStateOf<PlantData?>(null)

    val currentCareLogEntry = mutableStateOf<CareLogEntry?>(null)

    init {
        val currentUser = auth.currentUser
        signedIn.value = currentUser != null
        Log.i(TAG, "SIGNED IN: ${signedIn.value}")
        if (currentUser != null) {
            getUserData(id = currentUser.uid)
        }
        if (currentUser != null) {
            Log.i(TAG, currentUser.uid)
        } else {
            Log.i(TAG, "CURRENT USER IS NULL")
        }

    }

    private fun getUserData(id: String) {
        isLoading.value = true
        Log.i(TAG, "requesting user info")
        withAuth { token: String ->
            viewModelScope.launch(Dispatchers.Main) {
                val response = userRepository.getUserById(token, id)
                if (response is Resource.Error || response.data == null) {
                    Log.e(TAG, "ERROR: ${response.message}")
                    response.message?.let { handleException(null, it) }
                    isLoading.value = false
                    return@launch
                }
                val user = response.data
                Log.i(TAG, user.toString())
                userData.value = user
                isLoading.value = false
                refreshPosts()
            }
        }
    }

    fun onLogin(email: String, pass: String) {
        if (email.isEmpty() or pass.isEmpty()) {
            handleException(message = "Please fill in all fields")
            return
        }
        isLoading.value = true
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    handleException(task.exception, "Login failed")
                    isLoading.value = false
                    return@addOnCompleteListener
                }
                signedIn.value = true
                isLoading.value = false
                auth.currentUser?.uid?.let { uid ->
                    getUserData(uid)
                    showMessage(message = "Login successful")
                }
            }
            .addOnFailureListener { exception ->
                handleException(exception)
                isLoading.value = false
            }
    }

    fun onSignUp(username: String, email: String, password: String) {
        if (username.isEmpty() or email.isEmpty() or password.isEmpty()) {
            handleException(message = "Please fill in all fields")
            return
        }
        viewModelScope.launch {
            isLoading.value = true
            //Check if username is taken
            val res = userRepository.checkIfUsernameIsTaken(username)
            if (res is Resource.Error) {
                handleException(message = "Username already exists")
                isLoading.value = false
                return@launch
            }
            val req = CreateUserRequest(
                username = username,
                email = email,
                name = "",
                password = password
            )
            val createUserResponse = userRepository.createUser(req)
            if (createUserResponse is Resource.Error) {
                handleException(null, "Signup Failed")
                isLoading.value = false
                return@launch
            }
            isLoading.value = false
            signedIn.value = true
            userData.value = createUserResponse.data
            onLogin(email, password)
        }
    }

    private fun withAuth(execute: (token: String) -> Job) {
        val task = auth.currentUser?.getIdToken(true) ?: throw Exception("Could not get auth token")
        task.addOnSuccessListener {
            val token = it.token.toString()
            execute("Bearer $token")
        }.addOnFailureListener {
            throw Exception("Could not get auth token")
        }
    }

    fun updateUserInfo(
        name: String? = null,
        username: String? = null,
        email: String? = null,
        imageUrl: String? = null,
    ) {
        val currentUser = userData.value
        val id = auth.currentUser?.uid
        if (currentUser == null) {
            Log.e(TAG, "current user is null")
            handleException(message = "An error occurred. Signing you out")
            onLogout()
            return
        }
        if (id == null) {
            Log.e(TAG, "id is null")
            handleException(message = "An error occurred. Signing you out")
            onLogout()
            return
        }
        val req = UpdateUserRequest(
            name = name ?: currentUser.name!!,
            username = username ?: currentUser.username!!,
            email = email ?: currentUser.email!!,
            imageUrl = imageUrl ?: currentUser.imageUrl
        )
        isLoading.value = true
        withAuth { token: String ->
            viewModelScope.launch {
                val res = userRepository.updateUser(token, id, req)
                if (res is Resource.Error) {
                    handleException(res.exception, "An error occurred, could not update user")
                    isLoading.value = false
                    return@launch
                }
                val updatedUser = res.data
                userData.value = updatedUser
                isLoading.value = false
            }
        }
    }

    private fun refreshPosts() {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            handleException(message = "Error: username unavailable, unable to refresh posts")
            onLogout()
            return
        }
        refreshPlantsProgress.value = true
        getPlantsByUserId(currentUid) { plantList ->
            plants.value = plantList
            refreshPlantsProgress.value = false
        }

    }

    private fun getPlantsByUserId(userUid: String, onSuccess: (List<PlantData>) -> Unit) {
        withAuth { token: String ->
            viewModelScope.launch(Dispatchers.Main) {
                val res = plantsRepository.getPlantsByUserId(token, userUid)
                if (res is Resource.Error) {
                    if (res.exception is UnauthorizedException) {
                        onLogout()
                    }
                    handleException(null, "Cannot fetch plants")
                    refreshPlantsProgress.value = false
                    return@launch
                }
                val plantsRes = res.data
                if (plantsRes == null) {
                    handleException(null, "could not fetch your plant collection")
                    refreshPlantsProgress.value = false
                    return@launch
                }
                onSuccess(plantsRes.plants)
            }
        }
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
            updateUserInfo(imageUrl = it.toString())
        }
        isLoading.value = false
        val updatedUser = userData.value
        return updatedUser?.imageUrl
    }

    fun fetchPlantById(id: String?) {
        withAuth { token: String ->
            isLoading.value = true
            viewModelScope.launch(Dispatchers.Main) {
                if (id == null) {
                    currentPlant.value = null
                    return@launch
                }
                val res = plantsRepository.getPlantById(token, id)
                if (res is Resource.Error) {
                    handleException(null, "Unable to fetch plant data")
                    isLoading.value = false
                    return@launch
                }
                currentPlant.value = res.data
                refreshPosts()
                isLoading.value = false
                return@launch
            }
        }
    }

    fun onUpdatePlant(
        originalPlant: PlantData,
        newUris: List<Uri?>,
        newCommonName: String,
        newScientificName: String?,
        newToxicity: String?
    ) {
        isLoading.value = true
        withAuth { token: String ->
            viewModelScope.launch(Dispatchers.Main) {
                val originalImages = originalPlant.images
                val resultUris = uploadImages(newUris)
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
                val updatedPlant = originalPlant
                updatedPlant.commonName = newCommonName
                updatedPlant.scientificName = newScientificName
                updatedPlant.toxicity = newToxicity
                updatedPlant.images = updatedUris

                Log.i(TAG, updatedPlant.toString())
                val res = plantsRepository.updatePlant(token, updatedPlant, originalPlant.plantId!!)
                if (res is Resource.Error) {
                    handleException(null, "Unable to update plant")
                    isLoading.value = false
                    return@launch
                }
                popupNotification.value = Event("Plant successfully updated")
                isLoading.value = false
                refreshPosts()
            }
        }
    }

    fun onDeletePlant(plantToBeDeleted: PlantData) {
        withAuth { token: String ->
            isLoading.value = true
            viewModelScope.launch(Dispatchers.Main) {
                //Delete plant from db
                if (plantToBeDeleted.plantId == null) {
                    handleException(message = "An error was encountered trying to delete plant")
                    return@launch
                }
                val res = plantsRepository.deletePlant(token, plantToBeDeleted.plantId)
                if (res is Resource.Error) {
                    handleException(
                        res.data as Exception?,
                        "Unable to delete plant, please try again"
                    )
                    isLoading.value = false
                    return@launch
                }
                popupNotification.value = Event("Plant successfully deleted")
                isLoading.value = false
                refreshPosts()
            }
        }

    }

    fun onAddNewPlant(
        uris: List<Uri>,
        commonName: String,
        scientificName: String?,
        toxicity: String?
    ) {
        withAuth { token: String ->
            viewModelScope.launch(Dispatchers.Main) {
                isLoading.value = true
                val uriList = uris.filter { uri -> uri.toString().isNotEmpty() }
                val resultUris = uploadImages(uriList).filterNotNull()

                val currentUid = auth.currentUser?.uid

                if (currentUid == null) {
                    handleException(message = "Error: username unavailable. unable to create post")
                    onLogout()
                    isLoading.value = false
                    return@launch
                }
                val searchTerms = commonName
                    .split(" ", ".", ",", "?", "!", "#")
                    .map { it.lowercase() }
                    .filter { it.isNotEmpty() and !FILLER_WORDS.contains(it) }
                val newPlant = PlantData(
                    commonName = commonName,
                    scientificName = scientificName,
                    toxicity = toxicity,
                    userId = currentUid,
                    images = resultUris.map { uri -> uri.toString() }

                )
                Log.i(TAG, newPlant.toString())

                val response = plantsRepository.createPlant(token, newPlant)
                if (response is Resource.Error) {
                    handleException(null, "Unable to create plant")
                    isLoading.value = false
                    return@launch
                }
                popupNotification.value = Event("Plant successfully created")
                isLoading.value = false
                refreshPosts()
            }
        }
    }

    private suspend fun uploadImageAsync(uri: Uri, storageRef: StorageReference): Uri {
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
                resultUris.add(uploadImageAsync(uri, storageRef))
            }
        }
        Log.i(TAG, resultUris.toString())
        isLoading.value = false
        return resultUris
    }


    fun onLogout() {
        auth.signOut()
        signedIn.value = false
        userData.value = null
        popupNotification.value = Event("Logged out")
        searchedPlants.value = listOf()
        plantsFeed.value = listOf()
        plants.value = listOf()
        careLogEntries.value = listOf()
        numFollowers.value = 0
    }

    fun searchPlants(searchTerm: String) {
        val userId = userData.value?.id!!
        if (searchTerm.isEmpty()) {
            searchedPlants.value = listOf()
            return
        }
        withAuth { token: String ->
            isLoading.value = true
            viewModelScope.launch(Dispatchers.Main) {
                val res = plantsRepository.searchPlants(token, userId, searchTerm)
                if (res is Resource.Error) {
                    isLoading.value = false
                    handleException(res.exception, "Could not search plants, try again")
                    return@launch
                }
                isLoading.value = false
                searchedPlants.value = res.data ?: listOf()
            }
        }
    }

    private fun handleException(e: Exception? = null, message: String = "") {
        if (e != null) {
            Log.e(TAG, e.stackTraceToString())
        }
        val errMessage = e?.localizedMessage ?: ""
        val msg = if (message.isEmpty()) errMessage else "$message $errMessage"
        Log.i(TAG, msg)
        showMessage(msg)
    }

    private fun showMessage(message: String) {
        popupNotification.value = Event(message)
    }

    private fun refreshCareLogEntries(plantId: String) {
        refreshPlantsProgress.value = true
        getCareLogEntries(plantId) { logs ->
            careLogEntries.value = logs
        }

    }

    fun getCareLogEntries(plantId: String, onSuccess: ((logs: List<CareLogEntry>) -> Unit)?) {
        withAuth { token: String ->
            careLogEntriesProgress.value = true
            viewModelScope.launch(Dispatchers.Main) {
                val res = careLogRepository.getCareLogsByPlantId(token, plantId)
                careLogEntriesProgress.value = false
                if (res is Resource.Error) {
                    handleException(res.exception, "Could not get care log entries")
                    return@launch
                }
                if (onSuccess == null) {
                    careLogEntriesProgress.value = false
                    careLogEntries.value = res.data ?: listOf()
                    return@launch
                }
                careLogEntriesProgress.value = false
                onSuccess(res.data ?: listOf())
            }
        }
    }

    fun getCareLogEntryById(id: String) {
        withAuth { token: String ->
            isLoading.value = true
            viewModelScope.launch(Dispatchers.Main) {
                val res = careLogRepository.getCareLogsById(token, id)
                isLoading.value = false
                if (res is Resource.Error) {
                    handleException(res.exception, "Could not get care log entry")
                    return@launch
                }
                currentCareLogEntry.value = res.data
            }
        }
    }


    fun createCareLogEntry(
        plantId: String,
        wasWatered: Boolean,
        wasFertilized: Boolean,
        notes: String?,
    ) {
        val careLogRequest = CareLogRequest(
            plantId = plantId,
            wasWatered = wasWatered,
            wasFertilized = wasFertilized,
            notes = notes
        )
        withAuth { token: String ->
            isLoading.value = true
            viewModelScope.launch(Dispatchers.Main) {
                Log.i(TAG, careLogRequest.toString())
                val res = careLogRepository.createCareLogEntry(token, careLogRequest)
                isLoading.value = false
                if (res is Resource.Error) {
                    handleException(
                        res.exception,
                        "Could not create care log entry, please try again"
                    )
                    return@launch
                }
                showMessage("Successfully added care log entry")
            }
        }
    }

    fun deleteCareLogEntry(careLogId: String) {
        withAuth { token: String ->
            viewModelScope.launch(Dispatchers.Main) {
                isLoading.value = true
                val res = careLogRepository.deleteCareLogEntry(token, careLogId)
                isLoading.value = false
                if (res is Resource.Error) {
                    handleException(
                        res.exception,
                        "Could not delete care log entry, please try again"
                    )
                    return@launch
                }
                showMessage("Successfully deleted care log entry")
            }
        }
    }

    fun updateCareLogEntry(id: String, careLogEntry: CareLogEntry) {
        withAuth { token: String ->
            viewModelScope.launch(Dispatchers.Main) {
                isLoading.value = true
                val res = careLogRepository.updateCareLogEntry(token, id, careLogEntry)
                isLoading.value = false
                if (res is Resource.Error) {
                    handleException(
                        res.exception,
                        "Could not update care log entry, please try again"
                    )
                    return@launch
                }
                showMessage("Successfully update care log entry")
            }
        }
    }
}