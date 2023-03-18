package com.morales.nectar.screens

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.morales.nectar.data.Event
import com.morales.nectar.data.models.CareLog
import com.morales.nectar.data.models.PlantData
import com.morales.nectar.data.models.UserData
import com.morales.nectar.data.remote.requests.care.CareLogRequest
import com.morales.nectar.data.remote.requests.care.UpdateCareLogRequest
import com.morales.nectar.data.remote.requests.user.CreateUserRequest
import com.morales.nectar.data.remote.requests.user.UpdateUserRequest
import com.morales.nectar.data.remote.responses.FileUploadResponse
import com.morales.nectar.exceptions.UnauthorizedException
import com.morales.nectar.repository.CareLogRepository
import com.morales.nectar.repository.FileRepository
import com.morales.nectar.repository.PlantsRepository
import com.morales.nectar.repository.UserRepository
import com.morales.nectar.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private const val TAG = "NectarViewModel"
private val FILLER_WORDS = listOf("the", "be", "to", "is", "of", "and", "or", "a", "in", "it")

val DATE_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME

@HiltViewModel
class NectarViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val sharedPreferences: SharedPreferences,
    private val userRepository: UserRepository,
    private val plantsRepository: PlantsRepository,
    private val careLogRepository: CareLogRepository,
    private val fileRepository: FileRepository,
) : ViewModel() {
    val signedIn = mutableStateOf(false)
    val isLoading = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)
    val popupNotification = mutableStateOf<Event<String>?>(null)
    val plants = mutableStateOf<List<PlantData>>(listOf())
    val searchedPlants = mutableStateOf<List<PlantData>>(listOf())
    val plantsFeed = mutableStateOf<List<PlantData>>(listOf())
    val careLogEntries = mutableStateOf<List<CareLog>>(listOf())
    val allCareLogEntries = mutableStateOf<List<CareLog>>(listOf())
    private val currentPlant = mutableStateOf<PlantData?>(null)
    val currentCareLog = mutableStateOf<CareLog?>(null)

    init {
        val currentUser = auth.currentUser
        signedIn.value = currentUser != null
        if (currentUser != null) {
            getUserData(id = currentUser.uid)
            getAllCareLogEntries()
        }
        withAuth { token ->
            viewModelScope.launch { setValueInSharePrefs("token", token) }
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

    fun onLogin2(email: String, pass: String) {
        if (email.isEmpty() or pass.isEmpty()) {
            handleException(message = "Please fill in all fields")
            return
        }
        isLoading.value = true
        viewModelScope.launch {

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
            userData.value = createUserResponse.data?.user
            val sessionToken = createUserResponse.data?.sessionToken!!
            setValueInSharePrefs("token", sessionToken)
            onLogin(email, password)
        }
    }

    fun withAuth(execute: (token: String) -> Job) {
        if (auth.currentUser == null) {
            onLogout()
            return
        }
        auth.currentUser!!.getIdToken(true)
            .addOnSuccessListener {
                val token = it.token.toString()
                Log.i(TAG, token)
                execute("Bearer $token")
            }.addOnFailureListener {
                Log.e(TAG, "could not get authentication token")
                onLogout()
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

    fun updatePosts() {
        viewModelScope.launch {
            refreshPosts()
        }
    }

    private fun refreshPosts() {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            handleException(message = "Error: username unavailable, unable to refresh posts")
            onLogout()
            return
        }
        isLoading.value = true
        getPlantsByUserId(currentUid) { plantList ->
            plants.value = plantList
            isLoading.value = false
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
                    isLoading.value = false
                    return@launch
                }
                val plantsRes = res.data
                if (plantsRes == null) {
                    handleException(null, "could not fetch your plant collection")
                    isLoading.value = false
                    return@launch
                }
                onSuccess(plantsRes.plants)
            }
        }
    }

    fun uploadProfileImage(file: File) {
        val id = auth.currentUser?.uid!!
        isLoading.value = true
        withAuth { token ->
            viewModelScope.launch {
                val res = userRepository.updateProfileImage(token, file, id)
                if (res is Resource.Error) {
                    handleException(res.exception, "Unable to update profile image")
                    isLoading.value = false
                    return@launch
                }
                getUserData(id)
            }
        }
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

    suspend fun fetchPlantById(token: String, id: String?): PlantData? {
        Log.i(TAG, "Fetching plant by id $id")
        val deferredPlantData = viewModelScope.async {
            isLoading.value = true
            if (id == null) {
                currentPlant.value = null
                return@async null
            }
            val res = plantsRepository.getPlantById(token, id)
            Log.i(TAG, res.toString())
            if (res is Resource.Error) {
                handleException(null, "Unable to fetch plant data")
                isLoading.value = false
                return@async null
            }
            isLoading.value = false
            Log.i(TAG, res.data.toString())
            return@async res.data
        }
        refreshPosts()
        val plantData = deferredPlantData.await()
        if (plantData == null) {
            handleException(null, "Could not update plant image")
            return null
        }
        return plantData
    }

    fun deletePlantImage(plant: PlantData, imageUrl: String) {
        isLoading.value = true
        withAuth { token ->
            viewModelScope.launch {
                val res = plantsRepository.deleteImage(token, plant.plantId!!, imageUrl)
                if (res is Resource.Error) {
                    handleException(res.exception, "Unable to update plant")
                    isLoading.value = false
                    return@launch
                }
                popupNotification.value = Event("Picture successfully deleted")
                isLoading.value = false
                refreshPosts()
            }
        }
    }

    fun onUpdatePlant(
        originalPlant: PlantData,
        newCommonName: String,
        newScientificName: String?,
        newToxicity: String?,
    ) {
        isLoading.value = true
        withAuth { token: String ->
            viewModelScope.launch(Dispatchers.Main) {
                val updatedPlant = originalPlant.copy()
                updatedPlant.commonName = newCommonName
                updatedPlant.scientificName = newScientificName
                updatedPlant.toxicity = newToxicity

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

    suspend fun deleteImage(plantId: String, uri: String?) {
        if (uri.isNullOrEmpty()) return
        withAuth { token ->
            viewModelScope.launch {
                isLoading.value = true
                val res = fileRepository.deleteImage(token, plantId, uri)
                if (res is Resource.Error) {
                    handleException(
                        res.data as Exception?,
                        "Unable to delete plant, please try again"
                    )
                    isLoading.value = false
                    return@launch
                }
            }
        }
    }

    suspend fun uploadImageAsync(
        token: String,
        plantId: String,
        imageFile: File
    ): FileUploadResponse? {
        val deferredResult = viewModelScope.async {
            val res = fileRepository.uploadImage(token, plantId, imageFile)
            if (res is Resource.Error) {
                handleException(res.exception, "unable to upload image")
                isLoading.value = false
                return@async null
            }
            val imageUri = res.data?.imageUrl
            val updatedPlant = res.data?.plant
            if (imageUri == null || updatedPlant == null) {
                handleException(res.exception, "unable to upload image")
                isLoading.value = false
                return@async null
            }
            return@async res.data
        }
        val response = deferredResult.await()
        if (response == null) {
            handleException(null, "Could not update plant image")
            return null
        }
        return response
    }

    fun onAddNewPlant(
        images: List<File>,
        commonName: String,
        scientificName: String?,
        toxicity: String?
    ) {
        withAuth { token: String ->
            viewModelScope.launch(Dispatchers.Main) {
                isLoading.value = true

                val currentUid = auth.currentUser?.uid
                if (currentUid == null) {
                    handleException(message = "Error: username unavailable. unable to create post")
                    onLogout()
                    isLoading.value = false
                    return@launch
                }

                //upload images
                val resultUris = mutableListOf<String>()
                for (image in images) {
                    val res = fileRepository.uploadImage(token, image)
                    if (res is Resource.Error) {
                        handleException(res.exception, "unable to create plant")
                        isLoading.value = false
                        return@launch
                    }
                    val imageUri = res.data?.imageUrl
                    if (imageUri == null) {
                        handleException(res.exception, "unable to create plant")
                        isLoading.value = false
                        return@launch
                    }
                    resultUris.add(imageUri)
                }

                val newPlant = PlantData(
                    commonName = commonName,
                    scientificName = scientificName,
                    toxicity = toxicity,
                    userId = currentUid,
                    images = resultUris

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

    fun onLogout() {
        auth.signOut()
        signedIn.value = false
        userData.value = null
        popupNotification.value = Event("Logged out")
        searchedPlants.value = listOf()
        plantsFeed.value = listOf()
        plants.value = listOf()
        careLogEntries.value = listOf()
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

    fun handleException(e: Exception? = null, message: String = "") {
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
        isLoading.value = true
        getCareLogEntries(plantId) { logs ->
            careLogEntries.value = logs
        }

    }

    fun getCareLogEntries(plantId: String, onSuccess: ((logs: List<CareLog>) -> Unit)?) {
        withAuth { token: String ->
            isLoading.value = true
            viewModelScope.launch(Dispatchers.Main) {
                val res = careLogRepository.getCareLogsByPlantId(token, plantId)
                isLoading.value = false
                if (res is Resource.Error) {
                    handleException(res.exception, "Could not get care log entries")
                    return@launch
                }
                if (onSuccess == null) {
                    isLoading.value = false
                    careLogEntries.value = res.data ?: listOf()
                    return@launch
                }
                isLoading.value = false
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
                currentCareLog.value = res.data
            }
        }
    }


    fun createCareLogEntry(
        plantId: String,
        wasWatered: Boolean,
        wasFertilized: Boolean,
        notes: String?,
        careDate: LocalDate?
    ) {
        Log.i(TAG, careDate.toString())

        val careLogRequest = CareLogRequest(
            plantId = plantId,
            wasWatered = wasWatered,
            wasFertilized = wasFertilized,
            notes = notes,
            careDate = careDate.toString()
        )
        Log.i(TAG, careLogRequest.toString())
        withAuth { token: String ->
            isLoading.value = true
            viewModelScope.launch(Dispatchers.Main) {
                Log.i(TAG, careLogRequest.toString())
                val res = careLogRepository.createCareLogEntry(token, careLogRequest)
                isLoading.value = false
                if (res is Resource.Error) {
                    handleException(res.exception, "Could not create care log entry, please try again")
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

    fun updateCareLogEntry(id: String, careLog: UpdateCareLogRequest) {
        withAuth { token: String ->
            viewModelScope.launch(Dispatchers.Main) {
                isLoading.value = true
                val res = careLogRepository.updateCareLogEntry(token, id, careLog)
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

    fun getAllCareLogEntries() {
        val userId = auth.currentUser?.uid ?: return
        withAuth { token ->
            viewModelScope.launch {
                isLoading.value = true
                val res = careLogRepository.getCareLogsByUserId(token, userId)
                isLoading.value = false
                if (res is Resource.Error) {
                    handleException(res.exception, "Could not get care log entries")
                    return@launch
                }
                allCareLogEntries.value = res.data ?: listOf()
            }
        }
    }

    fun parseDate(dateString: String): LocalDate =
        LocalDate.parse(dateString, DateTimeFormatter.RFC_1123_DATE_TIME)

    fun setValueInSharePrefs(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getValueFromSharedPrefs(key: String): String? = sharedPreferences.getString(key, null)
}