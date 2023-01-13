package com.morales.nectar.repository

import com.morales.nectar.data.models.CareLogEntry
import com.morales.nectar.data.remote.CareLogApi
import com.morales.nectar.data.remote.requests.care.CareLogRequest
import com.morales.nectar.util.Resource
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

private const val TAG = "CareLogRepository"

@ActivityScoped
class CareLogRepository
@Inject constructor(private val api: CareLogApi) {

    suspend fun getCareLogsByPlantId(
        token: String,
        id: String
    ): Resource<List<CareLogEntry>> {
        return executeRequest { api.getCareLogsByPlantId(token, id) }
    }

    suspend fun updateCareLogEntry(
        token: String,
        id: String,
        careLogEntry: CareLogEntry
    ): Resource<Any> {
        return executeRequest { api.updateCareLogEntry(token, id, careLogEntry) }
    }

    suspend fun deleteCareLogEntry(token: String, id: String): Resource<Any> {
        return executeRequest { api.deleteCareLogEntry(token, id) }
    }

    suspend fun createCareLogEntry(
        token: String,
        careLogEntry: CareLogRequest
    ): Resource<Any> {
        return executeRequest { api.createCareLogEntry(token, careLogEntry) }
    }

}