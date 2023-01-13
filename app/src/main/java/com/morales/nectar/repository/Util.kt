package com.morales.nectar.repository

import com.morales.nectar.data.remote.responses.NectarResponseEntity
import com.morales.nectar.exceptions.UnauthorizedException
import com.morales.nectar.util.Resource
import retrofit2.Response

enum class HttpStatus(val code: Int) {
    OK(200),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    INTERNAL_ERROR(500),

}

private const val internalErrorMessage = "An unexpected error occurred on our side"

suspend fun <T> executeRequest(apiFunc: suspend () -> Response<NectarResponseEntity<T>>): Resource<T> {
    val response = try {
        apiFunc.invoke()
    } catch (e: Exception) {
        return Resource.Error(internalErrorMessage, exception = e)
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
            return Resource.Error(internalErrorMessage)
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
        else -> {
            if (response.body() == null) return Resource.Error(internalErrorMessage)
            return Resource.Success(response.body()!!.content)
        }
    }
}