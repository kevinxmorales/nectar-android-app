package com.morales.nectar.screens.plants

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.morales.nectar.composables.CommonDivider
import com.morales.nectar.composables.PlantImage
import com.morales.nectar.composables.ProgressSpinner
import com.morales.nectar.data.remote.requests.PlantData
import com.morales.nectar.screens.NectarViewModel
import okhttp3.internal.toImmutableList

@Composable
fun EditPlantScreen(
    navController: NavController,
    vm: NectarViewModel,
    plant: PlantData,
) {
    val isLoading = vm.isLoading.value
    val numImages = if (plant.images != null) plant.images.size else 0
    var imagesUrl1 by rememberSaveable {
        mutableStateOf(
            if (numImages >= 1 && plant.images != null) plant.images[0] else ""
        )
    }
    var wasImage1Changed by rememberSaveable { mutableStateOf(false) }
    var imagesUrl2 by rememberSaveable {
        mutableStateOf(
            if (numImages >= 2 && plant.images != null) plant.images[1] else ""
        )
    }
    var wasImage2Changed by rememberSaveable { mutableStateOf(false) }
    var imagesUrl3 by rememberSaveable {
        mutableStateOf(
            if (numImages >= 3 && plant.images != null) plant.images[2] else ""
        )
    }
    var wasImage3Changed by rememberSaveable { mutableStateOf(false) }
    var plantName by rememberSaveable { mutableStateOf(plant.commonName ?: "") }
    var scientificName by rememberSaveable { mutableStateOf(plant.scientificName ?: "") }
    var toxicity by rememberSaveable { mutableStateOf(plant.toxicity ?: "") }
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val newPostImageLauncher1 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imagesUrl1 = uri.toString()
            wasImage1Changed = true
        }
    }
    val newPostImageLauncher2 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imagesUrl2 = uri.toString()
            wasImage2Changed = true
        }
    }
    val newPostImageLauncher3 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imagesUrl3 = uri.toString()
            wasImage3Changed = true
        }
    }

    val onSubmit: () -> Unit = {
        val changedImages = mutableListOf<Uri?>()
        changedImages.add(if (wasImage1Changed) Uri.parse(imagesUrl1) else null)
        changedImages.add(if (wasImage2Changed) Uri.parse(imagesUrl2) else null)
        changedImages.add(if (wasImage3Changed) Uri.parse(imagesUrl3) else null)

        focusManager.clearFocus()
        vm.onEditPost(
            plant,
            changedImages.toImmutableList(),
            plantName,
            scientificName,
            toxicity,
        )
        vm.fetchPlantById(plant.plantId)
        navController.popBackStack()
    }

    if (isLoading) {
        ProgressSpinner()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {

        PlantCreateOptionsBar(navController, onSubmit)

        CommonDivider()

        Text(
            text = "Add up to three images of your new plant",
            modifier = Modifier.padding(start = 8.dp)
        )
        Row(modifier = Modifier.horizontalScroll(scrollState)) {
            Column(Modifier
                .clickable { newPostImageLauncher1.launch("image/*") }) {
                PlantImage(imageUri = imagesUrl1)
            }
            Column(Modifier
                .clickable { newPostImageLauncher2.launch("image/*") }) {
                PlantImage(imageUri = imagesUrl2)
            }
            Column(Modifier
                .clickable { newPostImageLauncher3.launch("image/*") }) {
                PlantImage(imageUri = imagesUrl3)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PlantTextFieldRow(
                label = "Common Name: e.g. Monstera Albo",
                text = "Common Name",
                onValueChange = { plantName = it },
                value = plantName
            )
            PlantTextFieldRow(
                label = "Scientific Name: e.g. Monstera deliciosa ",
                text = "Scientific Name",
                onValueChange = { scientificName = it },
                value = scientificName
            )
            PlantTextFieldRow(
                label = "Toxicity: e.g. Toxic to Pets",
                text = "Toxicity",
                onValueChange = { toxicity = it },
                value = toxicity
            )
        }
    }
}
