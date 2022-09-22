package com.morales.nectar.repository

import android.util.Log
import com.morales.nectar.data.remote.PlantsApi
import com.morales.nectar.data.remote.responses.Plant
import com.morales.nectar.data.remote.responses.PlantList
import com.morales.nectar.util.Resource
import dagger.hilt.android.scopes.ActivityScoped
import java.lang.Exception
import javax.inject.Inject

private const val TAG = "PlantsRepository"

@ActivityScoped
class PlantsRepository
    @Inject constructor(private val api: PlantsApi){

    suspend fun getPlantsListByUserId(id: Int): Resource<PlantList> {
        val response = try {
            api.getPlantListByUserId(id)
        } catch(e: Exception) {
            Log.i(TAG, e.toString())
            return Resource.Error("An unknown error occurred")
        }
        return Resource.Success(response)
    }

    suspend fun getPlantById(id: Int): Resource<Plant> {
        val response = try {
            api.getPlantById(id)
        } catch(e: Exception) {
            return Resource.Error("An unknown error occurred")
        }
        return Resource.Success(response)
    }
}