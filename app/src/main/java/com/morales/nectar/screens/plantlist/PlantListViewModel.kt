package com.morales.nectar.screens.plantlist

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.morales.nectar.data.models.PlantListEntry
import com.morales.nectar.repository.PlantsRepository
import com.morales.nectar.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "PlantListViewModel"

@HiltViewModel
class PlantListViewModel @Inject constructor(
    private val repository: PlantsRepository
): ViewModel() {

    var plantList = mutableStateOf<List<PlantListEntry>>(listOf())
    var loadError = mutableStateOf("")
    var isLoading = mutableStateOf(false)
    var endReached = mutableStateOf(false)

    fun loadPlantsPaginated() {
        viewModelScope.launch {
            isLoading.value = true
            val result = repository.getPlantsListByUserId(1)

            when(result) {
                is Resource.Success -> {
                    endReached.value = true
                    val plantEntries = result.data?.content?.map { entry ->
                       var url = ""
                       if(entry.images?.isNotEmpty() == true){
                           url = entry.images[0].url
                       }
                        Log.i(TAG, entry.name)
                       PlantListEntry(entry.name, url, entry.id)
                    }
                    loadError.value = ""
                    isLoading.value = false
                    plantList.value += plantEntries!!
                }
                is Resource.Error -> {
                    result.message?.let { Log.i(TAG, it) }
                    loadError.value = result.message.toString()
                    isLoading.value = false
                }
            }
        }
    }

    fun calcDominantColor(drawable: Drawable, onFinish: (Color) -> Unit) {
        val bmp = (drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)

        Palette.from(bmp).generate { palette ->
            palette?.dominantSwatch?.rgb?.let { colorValue ->
                onFinish(Color(colorValue))
            }
        }
    }
}