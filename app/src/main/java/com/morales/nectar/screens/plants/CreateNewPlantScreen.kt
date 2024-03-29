package com.morales.nectar.screens.plants

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.morales.nectar.android.composables.CommonDivider
import com.morales.nectar.android.composables.NectarTextField
import com.morales.nectar.android.composables.PlantImage
import com.morales.nectar.android.composables.ProgressSpinner
import com.morales.nectar.screens.NectarViewModel
import java.io.File
import kotlin.io.path.outputStream

private const val TAG = "CreateNewPlantScreen"

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreateNewPlantScreen(
    navController: NavController,
    vm: NectarViewModel
) {
    val isLoading = vm.isLoading.value
    var imagesUrl1 by rememberSaveable { mutableStateOf("") }
    var imagesUrl2 by rememberSaveable { mutableStateOf("") }
    var imagesUrl3 by rememberSaveable { mutableStateOf("") }

    var imageFile1 by rememberSaveable { mutableStateOf<File?>(null) }
    var imageFile2 by rememberSaveable { mutableStateOf<File?>(null) }
    var imageFile3 by rememberSaveable { mutableStateOf<File?>(null) }

    var plantName by rememberSaveable { mutableStateOf("") }
    var scientificName by rememberSaveable { mutableStateOf("") }
    var toxicity by rememberSaveable { mutableStateOf("") }

    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    fun getFileFromUri(uri: Uri?, onSuccess: (uriStr: String, imageFile: File) -> Unit) {
        if (uri == null) {
            Log.e(TAG, "error updating profile image, uri is null")
            return
        }
        val inputStream = context.contentResolver.openInputStream(uri)
        if (inputStream == null) {
            Log.e(TAG, "error updating profile image, input stream is null")
            return
        }
        val path = kotlin.io.path.createTempFile()
        inputStream.use { input ->
            path.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        onSuccess.invoke(uri.toString(), path.toFile())
    }


    val newPostImageLauncher1 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        getFileFromUri(uri) { uriString: String, imageFile: File ->
            imageFile1 = imageFile
            imagesUrl1 = uriString
        }
    }
    val newPostImageLauncher2 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        getFileFromUri(uri) { uriString: String, imageFile: File ->
            imageFile2 = imageFile
            imagesUrl2 = uriString
        }
    }
    val newPostImageLauncher3 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        getFileFromUri(uri) { uriString: String, imageFile: File ->
            imageFile3 = imageFile
            imagesUrl3 = uriString
        }
    }

    val onSubmit: () -> Unit = {
        focusManager.clearFocus()
        val imageFiles = listOfNotNull(imageFile1, imageFile2, imageFile3)

        vm.onAddNewPlant(
            imageFiles,
            plantName,
            scientificName,
            toxicity,
        )
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
        Box(modifier = Modifier.onKeyEvent {
            if (it.key == Key.Enter && it.type == KeyEventType.KeyDown) {
                focusManager.moveFocus(FocusDirection.Next)
                true
            } else {
                false
            }
        }) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                PlantTextFieldRow(
                    label = "Common Name: e.g. Monstera Albo",
                    text = "Common Name",
                    onValueChange = { plantName = it },
                    value = plantName,
                    focusManager = focusManager,
                )

                PlantTextFieldRow(
                    label = "Scientific Name: e.g. Monstera deliciosa ",
                    text = "Scientific Name",
                    onValueChange = { scientificName = it },
                    value = scientificName,
                    focusManager = focusManager,
                )
                PlantTextFieldRow(
                    label = "Toxicity: e.g. Toxic to Pets",
                    text = "Toxicity",
                    onValueChange = { toxicity = it },
                    value = toxicity,
                    focusManager = focusManager,
                )
            }
        }
    }
}

@Composable
fun PlantCreateOptionsBar(navController: NavController, onSubmit: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = "Back",
            modifier = Modifier
                .clickable { navController.popBackStack() }
        )
        Text(
            text = "Submit",
            modifier = Modifier
                .clickable { onSubmit() }
        )
    }
}

@Composable
fun PlantTextFieldRow(
    label: String,
    text: String,
    onValueChange: (String) -> Unit,
    value: String,
    focusManager: FocusManager,
) {

    Text(
        text = text,
        fontSize = 18.sp,
        fontFamily = FontFamily.Serif,
        modifier = Modifier.padding(8.dp)
    )
    NectarTextField(
        label = label,
        onChange = { onValueChange.invoke(it) },
        value = value,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    )

}