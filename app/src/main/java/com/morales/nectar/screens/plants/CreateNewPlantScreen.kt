package com.morales.nectar.screens.plants

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.morales.nectar.R
import com.morales.nectar.composables.CommonDivider
import com.morales.nectar.composables.CommonImage
import com.morales.nectar.composables.ProgressSpinner
import com.morales.nectar.screens.NectarViewModel

@Composable
fun CreateNewPlantScreen(
    navController: NavController,
    vm: NectarViewModel
) {
    val isLoading = vm.isLoading.value
    var imagesUrl1 by rememberSaveable { mutableStateOf("") }
    var imagesUrl2 by rememberSaveable { mutableStateOf("") }
    var imagesUrl3 by rememberSaveable { mutableStateOf("") }
    var plantName by rememberSaveable { mutableStateOf("") }
    var scientificName by rememberSaveable { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val newPostImageLauncher1 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imagesUrl1 = uri.toString()
        }
    }
    val newPostImageLauncher2 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imagesUrl2 = uri.toString()
        }
    }
    val newPostImageLauncher3 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imagesUrl3 = uri.toString()
        }
    }

    if (isLoading) {
        ProgressSpinner()
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = "Cancel",
                modifier = Modifier
                    .clickable { navController.popBackStack() }
            )
            Text(
                text = "Post",
                modifier = Modifier.clickable {
                    focusManager.clearFocus()
                    vm.onNewPost(
                        listOf(
                            Uri.parse(imagesUrl1),
                            Uri.parse(imagesUrl2),
                            Uri.parse(imagesUrl3)
                        ),
                        plantName
                    )
                    navController.popBackStack()
                }
            )
        }

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
            Text(
                text = "Common Name",
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier
                    .padding(8.dp)
            )
            OutlinedTextField(
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    textColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                onValueChange = { plantName = it },
                value = plantName,
                label = {
                    Text(text = "Common Name: e.g. Monstera Albo")
                }
            )

            Text(
                text = "Scientific Name",
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier
                    .padding(8.dp)
            )
            OutlinedTextField(
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    textColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                onValueChange = { scientificName = it },
                value = scientificName,
                label = {
                    Text(text = "Scientific Name: e.g. Monstera deliciosa ")
                }
            )

            Text(
                text = "Toxicity",
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier
                    .padding(8.dp)
            )
            OutlinedTextField(
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    textColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                onValueChange = { scientificName = it },
                value = scientificName,
                label = {
                    Text(text = "Toxicity: e.g. Toxic to Pets")
                }
            )
        }
    }
}

@Composable
fun PlantImage(imageUri: String) {
    val modifier = Modifier
        .height(150.dp)
        .width(150.dp)
        .padding(8.dp)
    if (imageUri.isNotEmpty()) {
        CommonImage(
            data = imageUri,
            contentDescription = "An image you selected for your plant",
            modifier = modifier
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.placeholder),
            contentDescription = "Placeholder image",
            modifier = modifier
        )
    }
}