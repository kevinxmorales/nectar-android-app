package com.morales.nectar.screens.plants

import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.morales.nectar.composables.CommonDivider
import com.morales.nectar.composables.ProgressSpinner
import com.morales.nectar.screens.NectarViewModel

@Composable
fun NewPostScreen(
    navController: NavController,
    encodedUri: String,
    vm: NectarViewModel,
) {
    val isLoading = vm.isLoading.value
    val imageUri by remember { mutableStateOf(encodedUri) }
    var description by rememberSaveable { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    if (isLoading) {
        ProgressSpinner()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
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
                    navController.popBackStack()
                    vm.onNewPost(listOf(Uri.parse(imageUri)), description)
                }
            )
        }

        CommonDivider()

        Image(
            contentDescription = "An image of a plant to be uploaded",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 150.dp),
            painter = rememberImagePainter(imageUri)
        )

        Row(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    textColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                onValueChange = { description = it },
                singleLine = false,
                value = description,
                label = {
                    Text(text = "Description")
                }
            )
        }
    }
}