package com.morales.nectar.navigation

import android.os.Parcelable
import androidx.navigation.NavController
import com.morales.nectar.DestinationScreen

data class NavParam(val name: String, val value: Parcelable)

fun navigateTo(navController: NavController, dest: DestinationScreen, vararg params: NavParam) {
    for(param in params) {
        navController.currentBackStackEntry?.arguments?.putParcelable(param.name, param.value)
    }

    navController.navigate(dest.route) {
        popUpTo(dest.route)
        launchSingleTop = true
    }
}