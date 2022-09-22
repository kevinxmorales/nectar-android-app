package com.morales.nectar.screens.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.morales.nectar.DestinationScreen
import com.morales.nectar.screens.NectarViewModel

@Composable
fun CheckSignedIn(navController: NavController, vm: NectarViewModel) {
    val alreadyLoggedIn = remember { mutableStateOf(false) }
    val signedIn = vm.signedIn.value
    if (signedIn && !alreadyLoggedIn.value) {
        alreadyLoggedIn.value = true
        navController.navigate(DestinationScreen.MyPosts.route) {
            popUpTo(0)
        }
    }
}