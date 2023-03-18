package com.morales.nectar.screens.auth

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.morales.nectar.DestinationScreen
import com.morales.nectar.android.composables.CommonDivider
import com.morales.nectar.android.composables.CommonImage
import com.morales.nectar.android.composables.ProgressSpinner
import com.morales.nectar.navigation.navigateTo
import com.morales.nectar.screens.NectarViewModel
import kotlin.io.path.outputStream

const val TAG = "ProfileScreen"

@Composable
fun ProfileScreen(
    navController: NavController,
    vm: NectarViewModel,
) {
    val isLoading = vm.isLoading.value

    if (isLoading) {
        ProgressSpinner()
    } else {
        val userData = vm.userData.value
        var name by rememberSaveable { mutableStateOf(userData?.name ?: "") }
        var username by rememberSaveable { mutableStateOf(userData?.username ?: "") }

        ProfileContent(
            vm = vm,
            name = name,
            username = username,
            onNameChange = { name = it },
            onUsernameChange = { username = it },
            onSave = { vm.updateUserInfo(name = name, username = username) },
            onBack = { navigateTo(navController, DestinationScreen.MyPosts) },
            onLogout = {
                vm.onLogout()
                navigateTo(navController, DestinationScreen.Login)
            },
        )
    }
}

@Composable
fun ProfileContent(
    vm: NectarViewModel,
    name: String,
    username: String,
    onNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit,
) {
    val imageUrl = vm.userData.value?.imageUrl
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = "Back",
                modifier = Modifier.clickable { onBack.invoke() }
            )
            Text(
                text = "Save",
                modifier = Modifier.clickable { onSave.invoke() }
            )
        }
        CommonDivider()

        //User image
        ProfileImage(imageUrl = imageUrl, vm = vm)

        CommonDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Name",
                modifier = Modifier.width(100.dp)
            )
            TextField(
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    textColor = Color.Black
                ),
                onValueChange = onNameChange,
                value = name,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Username",
                modifier = Modifier.width(100.dp)
            )
            TextField(
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    textColor = Color.Black
                ),
                onValueChange = onUsernameChange,
                value = username,
            )
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp)
        ) {
            Text(
                text = "Logout",
                modifier = Modifier.clickable { onLogout.invoke() }
            )
        }
    }

}

@Composable
fun ProfileImage(imageUrl: String?, vm: NectarViewModel) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) {
            Log.e(TAG, "error updating profile image, uri is null")
            return@rememberLauncherForActivityResult
        }
        val inputStream = context.contentResolver.openInputStream(uri)
        if (inputStream == null) {
            Log.e(TAG, "error updating profile image, input stream is null")
            return@rememberLauncherForActivityResult
        }
        val file = kotlin.io.path.createTempFile()
        inputStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        vm.uploadProfileImage(file.toFile())
    }

    Box(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clickable { launcher.launch("image/*") }
        ) {
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .size(100.dp),
                shape = CircleShape
            ) {
                CommonImage(uri = imageUrl, contentDescription = "your current profile image")
            }
            Text(text = "Change profile picture")
        }

        val isLoading = vm.isLoading.value
        if (isLoading) {
            ProgressSpinner()
        }
    }
}
