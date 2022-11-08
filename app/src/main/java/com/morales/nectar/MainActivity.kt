package com.morales.nectar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.morales.nectar.data.remote.requests.PlantData
import com.morales.nectar.screens.NectarViewModel
import com.morales.nectar.screens.auth.NotificationMessage
import com.morales.nectar.screens.auth.ProfileScreen
import com.morales.nectar.screens.auth.SignUpScreen
import com.morales.nectar.screens.feed.FeedScreen
import com.morales.nectar.screens.login.LoginScreen
import com.morales.nectar.screens.plants.*
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
    object NewPost : DestinationScreen("newPost/{imageUri}") {
        fun createRoute(uri: String) = "newPost/$uri"
    }

    object CreateNewPlantScreen : DestinationScreen("addPlant")
    object EditPlantScreen : DestinationScreen("editPlant")
    object SinglePlant : DestinationScreen("singlePlant")
    object CareLogEntries : DestinationScreen("care/{plantId}") {
        fun createRoute(plantId: String) = "care/$plantId"
    }
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
        composable(DestinationScreen.Feed.route) {
            FeedScreen(navController = navController, vm = vm)
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
        composable(DestinationScreen.EditPlantScreen.route) {
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
        composable(DestinationScreen.CareLogEntries.route) { navBackStackEntry ->
            val plantId = navBackStackEntry.arguments?.getString("plantId")
            plantId?.let {
                CareLogEntriesScreen(navController = navController, vm = vm, plantId = it)
            }
        }
        composable(DestinationScreen.CreateNewPlantScreen.route) {
            CreateNewPlantScreen(navController = navController, vm = vm)
        }
    }
}