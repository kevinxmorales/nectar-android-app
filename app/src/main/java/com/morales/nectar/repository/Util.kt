package com.morales.nectar.repository

import android.util.Log
import com.morales.nectar.data.remote.responses.NectarResponseEntity
import com.morales.nectar.exceptions.UnauthorizedException
import com.morales.nectar.util.Resource
import retrofit2.Response

enum class HttpStatus(val code: Int) {
    OK(200),
    CREATED(201),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    INTERNAL_ERROR(500),
}

private const val TAG = "Util.executeRequest"

private const val InternalErrorMessage = "An unexpected error occurred on our side"

suspend fun <T> executeRequest(apiFunc: suspend () -> Response<NectarResponseEntity<T>>): Resource<T> {
    val response = try {
        apiFunc.invoke()
    } catch (e: Exception) {
        return Resource.Error(InternalErrorMessage, exception = e)
    }
    when (response.code()) {
        HttpStatus.BAD_REQUEST.code -> {
            return Resource.Error(
                "Could not complete request: %s".format(
                    response.body()?.message ?: "bad request"
                )
            )
        }
        HttpStatus.INTERNAL_ERROR.code -> {
            return Resource.Error(InternalErrorMessage)
        }
        HttpStatus.UNAUTHORIZED.code -> {
            return Resource.Error(
                message = "You are not authenticated",
                exception = UnauthorizedException()
            )
        }
        HttpStatus.FORBIDDEN.code -> {
            return Resource.Error(
                message = "You are not authorized to do this action",
                exception = UnauthorizedException()
            )
        }
        HttpStatus.OK.code, HttpStatus.CREATED.code -> {
            Log.i("Util.executeRequest", response.toString())
            if (response.body() == null) return Resource.Error(InternalErrorMessage)
            return Resource.Success(response.body()!!.content)
        }
        else -> {
            Log.i(TAG, response.toString())
            return Resource.Error(
                "Could not complete request: %s".format(
                    response.body()?.message ?: InternalErrorMessage
                )
            )
        }
    }
}