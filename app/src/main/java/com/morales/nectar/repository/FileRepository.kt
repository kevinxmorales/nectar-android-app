package com.morales.nectar.repository

import com.morales.nectar.data.remote.FileApi
import com.morales.nectar.data.remote.requests.plants.DeleteImageRequest
import com.morales.nectar.data.remote.responses.FileUploadResponse
import com.morales.nectar.util.Resource
import dagger.hilt.android.scopes.ActivityScoped
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

@ActivityScoped
class FileRepository @Inject constructor(private val api: FileApi) {

    suspend fun uploadImage(
        token: String,
        file: File
    ): Resource<FileUploadResponse> {
        val formData = MultipartBody.Part.createFormData("image", file.name, file.asRequestBody())
        return executeRequest { api.uploadImage(token, formData) }
    }

    suspend fun uploadImage(
        token: String,
        plantId: String,
        file: File
    ): Resource<FileUploadResponse> {
        val formData = MultipartBody.Part.createFormData("image", file.name, file.asRequestBody())
        return executeRequest { api.uploadImage(token, plantId, formData) }
    }

    suspend fun deleteImage(token: String, plantId: String, uri: String): Resource<Any> {
        return executeRequest { api.deleteImage(token, plantId, DeleteImageRequest(uri)) }
    }
}