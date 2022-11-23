package com.morales.nectar.screens.plants

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.runtime.*
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
import com.morales.nectar.navigation.NavParam
import com.morales.nectar.navigation.navigateTo
import com.morales.nectar.screens.NectarViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SinglePlantScreen(
    navController: NavController,
    p: PlantData,
    vm: NectarViewModel,
) {
    val currentPlant = vm.currentPlant.value
    val careLogEntries = vm.careLogEntries.value
    val scrollState = rememberScrollState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val openDrawer = {
        scope.launch {
            drawerState.open()
        }
    }
    val showDeleteDialog = remember { mutableStateOf(false) }

    val getPlant = {
        vm.fetchPlantById(p.plantId)
        vm.getCareLogEntries(p.plantId)
    }

    val optionsMap = mapOf(
        "Update" to {
            navigateTo(
                navController,
                DestinationScreen.EditPlantScreen,
                NavParam("plant", currentPlant ?: p)
            )
        },
        "Delete" to {
            showDeleteDialog.value = true
        }
    )

    val optionsDrawerFS = true

    LaunchedEffect(key1 = Unit) {
        getPlant()
    }

    if (currentPlant != null) {
        if (showDeleteDialog.value) {
            ConfirmDeleteDialog(
                onCancel = { showDeleteDialog.value = false },
                onDelete = {
                    vm.onDeletePlant(currentPlant)
                    showDeleteDialog.value = false
                    navController.popBackStack()
                }
            )
        }

        currentPlant.userId?.let {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color.White)

            ) {
                SinglePostHeader(
                    currentPlant,
                    getPlant,
                    navController,
                    vm,
                    optionsDrawerFS,
                    optionsMap
                )

                CommonDivider()

                SinglePostDisplay(
                    navController,
                    vm,
                    currentPlant,
                    careLogEntries.size,
                    scrollState,
                    onPressDelete = { showDeleteDialog.value = true }
                )
            }
        }
    }
}

@Composable
fun SinglePostHeader(
    currentPlant: PlantData,
    getPlant: () -> Unit,
    navController: NavController,
    vm: NectarViewModel,
    optionsDrawerFS: Boolean,
    optionsMap: Map<String, () -> Unit>
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Text(
            text = "Back",
            modifier = Modifier.clickable {
                navController.popBackStack()
                vm.currentPlant.value = null
            })

        Image(
            painter = painterResource(id = R.drawable.ic_refresh),
            contentDescription = "refresh button",
            modifier = Modifier
                .size(24.dp)
                .clickable { getPlant() },
            colorFilter = ColorFilter.tint(Color.Black)
        )

        if (optionsDrawerFS) {
            OptionsDrawer(optionsMap = optionsMap)
        } else {
            Text(
                text = "Update",
                modifier = Modifier.clickable {
                    navigateTo(
                        navController,
                        DestinationScreen.EditPlantScreen,
                        NavParam("plant", currentPlant)
                    )
                }
            )
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
    onPressDelete: () -> Unit
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
                    verticalAlignment = Alignment.CenterVertically,
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
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
            val simpleDateFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
            val dateString = simpleDateFormat.format(entry.timestamp)
            Text(text = "Date: $dateString", fontWeight = FontWeight.Bold)
            Text(text = "Watered: ${if (entry.wasWatered == true) "Yes" else "No"}")
            Text(text = "Fertilized: ${if (entry.wasFertilized == true) "Yes" else "No"}")
            Text(text = "Notes: ${entry.notes}", modifier = Modifier.padding(bottom = 8.dp))
            CommonDivider()
        }
    }
}

@Composable
fun ConfirmDeleteDialog(
    onCancel: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            onCancel.invoke()
        },
        title = {
            Text(text = "Are you sure you want to delete?")
        },
        buttons = {
            Column(
                modifier = Modifier.padding(all = 8.dp),
                verticalArrangement = Arrangement.Center

            ) {
                Button(
                    modifier = Modifier,
                    onClick = { onDelete.invoke() }
                ) {
                    Text("Confirm")
                }
                Button(
                    modifier = Modifier,
                    onClick = { onCancel.invoke() }
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun OptionsDrawer(optionsMap: Map<String, () -> Unit>) {
    TopAppBar(
        backgroundColor = Color.White, modifier = Modifier
            .size(50.dp),
        elevation = 0.dp,
        title = {
            Text(text = "")
        },
        actions = {
            OverflowMenu {
                DropdownMenuItem(onClick = { optionsMap["Update"]?.invoke() }) {
                    Text("Update")
                }
                DropdownMenuItem(onClick = { optionsMap["Delete"]?.invoke() }) {
                    Text("Delete")
                }
            }
        }
    )
}

@Composable
fun OverflowMenu(content: @Composable () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    IconButton(onClick = {
        showMenu = !showMenu
    }) {
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            contentDescription = null,
        )
    }
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
    ) {
        content()
    }
}

