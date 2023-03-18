package com.morales.nectar.screens.plants

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.morales.nectar.DestinationScreen
import com.morales.nectar.R
import com.morales.nectar.android.composables.CommonDivider
import com.morales.nectar.android.composables.PlantImage
import com.morales.nectar.android.composables.ProgressSpinner
import com.morales.nectar.data.models.PlantData
import com.morales.nectar.navigation.NavParam
import com.morales.nectar.navigation.navigateTo
import com.morales.nectar.screens.NectarViewModel
import kotlinx.coroutines.launch
import java.io.File
import kotlin.io.path.outputStream

private const val TAG = "EditPlantsScreen"

@Composable
fun UpdateImageCard(imageUri: String, onDelete: (uri: String) -> Unit) {
    Box {
        PlantImage(imageUri = imageUri)
        if (imageUri.isNotBlank()) {
            Card(
                shape = CircleShape,
                border = BorderStroke(width = 2.dp, color = Color.White),
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .clickable { onDelete.invoke(imageUri) }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = "delete this plant image",
                    modifier = Modifier.background(Color.White)
                )
            }
        }
    }
}

@Composable
fun EditPlantScreen(
    navController: NavController,
    vm: NectarViewModel,
    plant: PlantData,
) {
    val isLoading = vm.isLoading.value
    val numImages = if (plant.images != null) plant.images!!.size else 0
    var imagesUrl1 by rememberSaveable {
        mutableStateOf(
            if (numImages >= 1 && plant.images != null) plant.images!![0] else ""
        )
    }
    var imagesUrl2 by rememberSaveable {
        mutableStateOf(
            if (numImages >= 2 && plant.images != null) plant.images!![1] else ""
        )
    }
    var imagesUrl3 by rememberSaveable {
        mutableStateOf(
            if (numImages >= 3 && plant.images != null) plant.images!![2] else ""
        )
    }
    var plantName by rememberSaveable { mutableStateOf(plant.commonName ?: "") }
    var scientificName by rememberSaveable { mutableStateOf(plant.scientificName ?: "") }
    var toxicity by rememberSaveable { mutableStateOf(plant.toxicity ?: "") }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    fun getFileFromUri(uri: Uri?, onSuccess: (imageFile: File) -> Unit) {
        if (uri == null) return
        val inputStream = context.contentResolver.openInputStream(uri) ?: return
        val path = kotlin.io.path.createTempFile()
        inputStream.use { input ->
            path.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        onSuccess.invoke(path.toFile())
    }

    fun refreshScreen(updatedPlant: PlantData?) {
        navController.popBackStack()
        navigateTo(
            navController,
            DestinationScreen.EditPlant,
            NavParam("plant", updatedPlant!!)
        )
    }

    fun onUpdateImage(
        newUri: Uri?,
        oldUriString: String,
        updateState: (resultUri: String?) -> Unit
    ) {
        if (newUri == null) return
        getFileFromUri(newUri) { imageFile: File ->
            vm.withAuth { token ->
                coroutineScope.launch {
                    val result = vm.uploadImageAsync(token, plant.plantId!!, imageFile)
                    val resultUri = result?.imageUrl
                    val updatedPlant = result?.plant
                    vm.deleteImage(plant.plantId, oldUriString)
                    updateState.invoke(resultUri)
                    Log.i(TAG, updatedPlant.toString())
                    refreshScreen(updatedPlant)
                }
            }
        }
    }

    val newPostImageLauncher1 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        onUpdateImage(uri, imagesUrl3) { resultUri: String? ->
            imagesUrl3 = resultUri!!
        }

    }

    val newPostImageLauncher2 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        onUpdateImage(uri, imagesUrl2) { resultUri: String? ->
            imagesUrl2 = resultUri!!
        }
    }
    val newPostImageLauncher3 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        onUpdateImage(uri, imagesUrl2) { resultUri: String? ->
            imagesUrl2 = resultUri!!
        }
    }

    val onBackPress: () -> Unit = {
        val newPlantData = plant.copy()
        vm.updatePosts()
        navController.popBackStack()
        navController.popBackStack()
        navigateTo(
            navController,
            DestinationScreen.SinglePlant,
            NavParam("plant", newPlantData)
        )
    }

    val onSubmit: () -> Unit = {
        focusManager.clearFocus()
        vm.onUpdatePlant(
            originalPlant = plant,
            plantName,
            scientificName,
            toxicity
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

        OptionsBar(onBackPress, onSubmit)

        CommonDivider()

        Text(
            text = "Add up to three images of your new plant",
            modifier = Modifier.padding(start = 8.dp)
        )
        Row(modifier = Modifier.horizontalScroll(scrollState)) {
            Column(Modifier.clickable { newPostImageLauncher1.launch("image/*") }) {
                UpdateImageCard(imageUri = imagesUrl1) {
                    vm.deletePlantImage(plant, imagesUrl1)
                    imagesUrl1 = imagesUrl2
                    imagesUrl2 = imagesUrl3
                    imagesUrl3 = ""
                    plant.images = listOf(imagesUrl1, imagesUrl2).filter { it.isNotBlank() }
                }
            }
            Column(Modifier.clickable { newPostImageLauncher2.launch("image/*") }) {
                UpdateImageCard(imageUri = imagesUrl2) {
                    vm.deletePlantImage(plant, imagesUrl2)
                    imagesUrl2 = imagesUrl3
                    imagesUrl3 = ""
                    plant.images = listOf(imagesUrl1, imagesUrl2).filter { it.isNotBlank() }
                }
            }
            Column(Modifier.clickable { newPostImageLauncher3.launch("image/*") }) {
                UpdateImageCard(imageUri = imagesUrl3) {
                    vm.deletePlantImage(plant, imagesUrl3)
                    imagesUrl3 = ""
                    plant.images = listOf(imagesUrl1, imagesUrl2).filter { it.isNotBlank() }
                }
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
                value = plantName,
                focusManager = focusManager
            )
            PlantTextFieldRow(
                label = "Scientific Name: e.g. Monstera deliciosa ",
                text = "Scientific Name",
                onValueChange = { scientificName = it },
                value = scientificName,
                focusManager = focusManager
            )
            PlantTextFieldRow(
                label = "Toxicity: e.g. Toxic to Pets",
                text = "Toxicity",
                onValueChange = { toxicity = it },
                value = toxicity,
                focusManager = focusManager
            )
        }
    }
}

@Composable
fun OptionsBar(
    onBackPress: () -> Unit,
    onSubmit: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = "Back",
            modifier = Modifier
                .clickable {
                    onBackPress.invoke()
                }
        )
        Text(
            text = "Submit",
            modifier = Modifier
                .clickable { onSubmit() }
        )
    }
}
