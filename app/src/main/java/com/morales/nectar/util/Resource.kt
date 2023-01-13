package com.morales.nectar.util

import com.morales.nectar.exceptions.UnsuccessfulRequestException

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null,
    val exception: Exception? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(
        message: String,
        data: T? = null,
        exception: Exception = UnsuccessfulRequestException()
    ) :
        Resource<T>(data, message, exception)
}