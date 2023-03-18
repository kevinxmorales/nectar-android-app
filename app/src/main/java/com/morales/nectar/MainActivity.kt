package com.morales.nectar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.morales.nectar.android.composables.NotificationMessage
import com.morales.nectar.data.models.CareLogParcel
import com.morales.nectar.data.models.PlantData
import com.morales.nectar.navigation.navigateTo
import com.morales.nectar.screens.NectarViewModel
import com.morales.nectar.screens.auth.LoginScreen
import com.morales.nectar.screens.auth.ProfileScreen
import com.morales.nectar.screens.auth.SignUpScreen
import com.morales.nectar.screens.care.CareLogEntriesScreen
import com.morales.nectar.screens.care.CreateCareLogEntryScreen
import com.morales.nectar.screens.care.EditCareLogEntryScreen
import com.morales.nectar.screens.plants.CreateNewPlantScreen
import com.morales.nectar.screens.plants.EditPlantScreen
import com.morales.nectar.screens.plants.MyPlantsScreen
import com.morales.nectar.screens.plants.PlantSearchScreen
import com.morales.nectar.screens.plants.SinglePlantScreen
import com.morales.nectar.ui.theme.NectarTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NectarTheme {
                NectarApp()
            }
        }
    }
}

sealed class DestinationScreen(val route: String) {
    object SignUp : DestinationScreen("signUp")
    object Login : DestinationScreen("login")
    object Feed : DestinationScreen("feed")
    object Search : DestinationScreen("search")
    object MyPosts : DestinationScreen("myPosts")
    object Profile : DestinationScreen("profile")
    object CreateNewPlant : DestinationScreen("addPlant")
    object EditPlant : DestinationScreen("editPlant")
    object SinglePlant : DestinationScreen("singlePlant")
    object CareLogEntries : DestinationScreen("careLogs")
    object CreateCareLogEntry : DestinationScreen("care/{plantId}") {
        fun createRoute(plantId: String) = "care/$plantId"
    }

    object EditCareLogEntry : DestinationScreen("care/edit")
}

@Composable
fun NectarApp() {
    val navController = rememberNavController()
    val vm = hiltViewModel<NectarViewModel>()

    NotificationMessage(vm)
    NavHost(navController = navController, startDestination = DestinationScreen.SignUp.route) {
        composable(DestinationScreen.SignUp.route) {
            SignUpScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.Login.route) {
            LoginScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.Search.route) {
            PlantSearchScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.MyPosts.route) {
            MyPlantsScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.Profile.route) {
            ProfileScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.EditPlant.route) {
            val plantData = navController
                .previousBackStackEntry
                ?.arguments
                ?.getParcelable<PlantData>("plant")
            plantData?.let {
                EditPlantScreen(
                    navController = navController,
                    plant = plantData,
                    vm = vm
                )
            }
        }
        composable(DestinationScreen.SinglePlant.route) {
            val plantData = navController
                .previousBackStackEntry
                ?.arguments
                ?.getParcelable<PlantData>("plant")
            plantData?.let {
                SinglePlantScreen(
                    navController = navController,
                    p = plantData,
                    vm = vm
                )
            }
        }
        composable(DestinationScreen.CareLogEntries.route) {
            CareLogEntriesScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.CreateCareLogEntry.route) { navBackStackEntry ->
            val plantId = navBackStackEntry.arguments?.getString("plantId")
            plantId?.let {
                CreateCareLogEntryScreen(navController = navController, vm = vm, plantId = it)
            }
        }
        composable(DestinationScreen.EditCareLogEntry.route) {
            val careLog = navController
                .previousBackStackEntry
                ?.arguments
                ?.getParcelable<CareLogParcel>("entry")
            careLog?.let {
                EditCareLogEntryScreen(
                    navController = navController,
                    vm = vm,
                    entryParcel = careLog
                )
            }
        }
        composable(DestinationScreen.CreateNewPlant.route) {
            CreateNewPlantScreen(navController = navController, vm = vm)
        }
    }
    if (!vm.signedIn.value) {
        navigateTo(navController, DestinationScreen.Login)
    }
}