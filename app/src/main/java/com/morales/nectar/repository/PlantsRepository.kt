package com.morales.nectar.repository

import com.morales.nectar.data.models.PlantData
import com.morales.nectar.data.remote.PlantsApi
import com.morales.nectar.data.remote.responses.GetPlantsResponse
import com.morales.nectar.data.remote.responses.NewPlantData
import com.morales.nectar.util.Resource
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

private const val TAG = "PlantsRepository"

@ActivityScoped
class PlantsRepository
@Inject constructor(private val api: PlantsApi) {

    suspend fun getPlantsByUserId(
        token: String,
        id: String
    ): Resource<GetPlantsResponse> {
        return executeRequest { api.getPlantListByUserId(token, id) }
    }

    suspend fun getPlantById(token: String, id: String): Resource<PlantData> {
        return executeRequest { api.getPlantById(token, id) }
    }

    suspend fun searchPlants(
        token: String,
        userId: String,
        params: String
    ): Resource<List<PlantData>> {
        return executeRequest { api.searchPlants(token, params) }
    }

    suspend fun createPlant(
        token: String,
        newPlantData: PlantData
    ): Resource<PlantData> {
        return executeRequest { api.createPlant(token, newPlantData) }
    }

    suspend fun updatePlant(
        token: String,
        updatedPlantData: PlantData,
        plantId: String
    ): Resource<PlantData> {
        return executeRequest { api.updatePlant(token, plantId, updatedPlantData) }
    }

    suspend fun deletePlant(
        token: String,
        plantId: String
    ): Resource<NewPlantData> {
        return executeRequest { api.deletePlant(token, plantId) }
    }
}