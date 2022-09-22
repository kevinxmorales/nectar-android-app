package com.morales.nectar.screens.plants

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.morales.nectar.DestinationScreen
import com.morales.nectar.R
import com.morales.nectar.composables.CommonDivider
import com.morales.nectar.composables.CommonImage
import com.morales.nectar.composables.ProgressSpinner
import com.morales.nectar.data.remote.requests.PlantData
import com.morales.nectar.data.remote.responses.CareLogEntry
import com.morales.nectar.screens.NectarViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SinglePlantScreen(
    navController: NavController,
    plant: PlantData,
    vm: NectarViewModel,
) {
    val careLogEntries = vm.careLogEntries.value
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = Unit) {
        vm.getCareLogEntries(plant.plantId)
    }

    plant.userId?.let {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)

        ) {
            Text(text = "Back", modifier = Modifier.clickable { navController.popBackStack() })

            CommonDivider()

            SinglePostDisplay(navController, vm, plant, careLogEntries.size, scrollState)
        }
    }
}

@Composable
fun SinglePostDisplay(
    navController: NavController,
    vm: NectarViewModel,
    post: PlantData,
    numCareLogEntries: Int,
    scrollState: ScrollState,
) {
    val userData = vm.userData.value
    val careLogEntries = vm.careLogEntries.value
    val careLogEntriesProgress = vm.careLogEntriesProgress.value

    Column {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        shape = CircleShape,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(32.dp)
                    ) {
                        CommonImage(
                            contentDescription = "the plant owner's profile picture",
                            data = post.userImage,
                        )
                    }
                    Text(text = post.username ?: "")
                    Text(text = "", modifier = Modifier.padding(8.dp))
                    val following = userData?.following
                    val postUserId = post.userId
                    if (userData?.authId == postUserId) {
                        // Do not display anything
                    } else if (following != null && following.contains(postUserId)) {
                        Text(text = "Following", color = Color.Gray, modifier = Modifier.clickable {
                            post.userId?.let {
                                vm.onFollowClick(it)
                            }
                        })
                    } else if (following != null && !following.contains(postUserId)) {
                        Text(text = "Follow", color = Color.Blue, modifier = Modifier.clickable {
                            post.userId?.let { vm.onFollowClick(it) }
                        })
                    }
                }
            }

            Box {
                val modifier =
                    Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 150.dp)
                CommonImage(
                    contentDescription = "a picture of the plant",
                    contentScale = ContentScale.FillWidth,
                    data = post.images?.get(0),
                    modifier = modifier,
                )
            }
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (!post.likes.isNullOrEmpty()) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_like),
                        contentDescription = "like button",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(Color.Red)
                    )
                }
                val numLikes: Int = if (post.likes.isNullOrEmpty()) 0 else post.likes!!.size
                Text(text = "$numLikes likes")
            }
            Row(modifier = Modifier.padding(8.dp)) {
                Text(text = post.username ?: "", fontWeight = FontWeight.Bold)
                Text(text = post.commonName ?: "", modifier = Modifier.padding(start = 8.dp))
            }
            CommonDivider()
            if (userData?.authId == post.userId) {
                Row(modifier = Modifier.padding(8.dp)) {
                    if (numCareLogEntries > 0) {
                        Text(text = "$numCareLogEntries Care Log Entries")
                    }
                }
                Row(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "+ Add new care log entry",
                        color = Color.Blue,
                        modifier = Modifier
                            .clickable {
                                post.plantId?.let { plantId ->
                                    navController.navigate(
                                        DestinationScreen.CareLogEntries.createRoute(
                                            plantId
                                        )
                                    )
                                }
                            }
                    )
                }

                if (careLogEntriesProgress) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ProgressSpinner()
                    }
                } else if (careLogEntries.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "No care log entries for this plant")
                    }
                } else {
                    Column {
                        careLogEntries.forEach { entry ->
                            CareLogEntryRow(entry = entry)
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun CareLogEntryRow(entry: CareLogEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column {
            val simpleDateFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault());
            val dateString = simpleDateFormat.format(entry.timestamp)
            Text(text = "Date: $dateString", fontWeight = FontWeight.Bold)
            Text(text = "Watered: ${if (entry.wasWatered == true) "Yes" else "No"}")
            Text(text = "Fertilized: ${if (entry.wasFertilized == true) "Yes" else "No"}")
            Text(text = "Notes: ${entry.notes}", modifier = Modifier.padding(bottom = 8.dp))
            CommonDivider()
        }
    }
}