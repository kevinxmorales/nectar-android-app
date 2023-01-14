package com.morales.nectar.screens.plants

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
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
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.morales.nectar.DestinationScreen
import com.morales.nectar.R
import com.morales.nectar.composables.CommonDivider
import com.morales.nectar.composables.CommonImage
import com.morales.nectar.composables.ProgressSpinner
import com.morales.nectar.data.models.CareLogEntry
import com.morales.nectar.data.models.PlantData
import com.morales.nectar.navigation.NavParam
import com.morales.nectar.navigation.navigateTo
import com.morales.nectar.screens.NectarViewModel
import java.util.*

private const val TAG = "SinglePlant Screen"

@Composable
fun SinglePlantScreen(
    navController: NavController,
    p: PlantData,
    vm: NectarViewModel,
) {
    val scrollState = rememberScrollState()
    val getPlant = {
        vm.fetchPlantById(p.plantId)
        vm.getCareLogEntries(p.plantId!!, null)
    }

    val onDelete = {
        vm.onDeletePlant(p)
    }

    val onUpdate = {
        navigateTo(
            navController,
            DestinationScreen.EditPlant,
            NavParam("plant", p)
        )
    }

    LaunchedEffect(key1 = Unit) {
        getPlant()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.White)

    ) {
        SinglePostHeader(navController, onDelete, onUpdate)

        CommonDivider()

        SinglePostDisplay(
            navController,
            vm,
            p,
            scrollState
        )
    }

}

@Composable
fun SinglePostHeader(
    navController: NavController,
    onDelete: () -> Unit,
    onUpdate: () -> Unit,
) {
    val showDeleteDialog = remember { mutableStateOf(false) }
    if (showDeleteDialog.value) {
        ConfirmDeleteDialog(
            onCancel = { showDeleteDialog.value = false },
            onDelete = {
                onDelete.invoke()
                showDeleteDialog.value = false
                navController.popBackStack()
            }
        )
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_baseline_arrow_back_ios_24),
            contentDescription = "back button",
            modifier = Modifier
                .size(24.dp)
                .clickable { navController.popBackStack() },
            colorFilter = ColorFilter.tint(Color.Black)
        )
        Image(
            painter = painterResource(id = R.drawable.ic_edit),
            contentDescription = "edit button",
            modifier = Modifier
                .size(24.dp)
                .clickable { onUpdate.invoke() },
            colorFilter = ColorFilter.tint(Color.Black)
        )
        Image(
            painter = painterResource(id = R.drawable.ic_delete),
            contentDescription = "delete button",
            modifier = Modifier
                .size(24.dp)
                .clickable { showDeleteDialog.value = true },
            colorFilter = ColorFilter.tint(Color.Black)
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun SinglePostDisplay(
    navController: NavController,
    vm: NectarViewModel,
    post: PlantData,
    scrollState: ScrollState,
) {
    val careLogEntries = vm.careLogEntries.value
    val careLogEntriesProgress = vm.careLogEntriesProgress.value

    val onAddCareLogEntry = {
        navController.navigate(DestinationScreen.CareLogEntries.createRoute(post.plantId!!))
    }

    val refreshScreen = {
        navController.popBackStack()
        navigateTo(
            navController,
            DestinationScreen.SinglePlant,
            NavParam("plant", post)
        )
    }

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
                post.images?.size?.let { size ->
                    HorizontalPager(
                        count = size,
                        contentPadding = PaddingValues(horizontal = 16.dp),
                    ) { page ->
                        CommonImage(
                            contentDescription = "a picture of the plant",
                            contentScale = ContentScale.FillWidth,
                            data = post.images!![page],
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 150.dp)
                        )
                    }
                }

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

            Row(modifier = Modifier.padding(8.dp)) {
                if (careLogEntries.isNotEmpty()) {
                    Text(text = "${careLogEntries.size} Care Log ${if (careLogEntries.size > 1) "Entries" else "Entry"}")
                }
            }
            Row(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "+ Add new care log entry",
                    color = Color.Blue,
                    modifier = Modifier.clickable { onAddCareLogEntry.invoke() }
                )
            }
            CommonDivider()

            if (careLogEntriesProgress) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ProgressSpinner()
                }
            } else {
                CareLogEntryList(
                    navController,
                    careLogEntries,
                    vm,
                    refreshScreen
                )
            }


        }

    }
}

@Composable
fun CareLogEntryList(
    navController: NavController,
    careLogEntries: List<CareLogEntry>,
    vm: NectarViewModel,
    refreshScreen: () -> Unit
) {

    val onUpdate = { entry: CareLogEntry ->
        navigateTo(navController, DestinationScreen.EditCareLogEntry, NavParam("entry", entry))
    }

    val onDelete = { id: String ->
        Log.i(TAG, id)
        vm.deleteCareLogEntry(id)
    }

    if (careLogEntries.isEmpty()) {
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
                CareLogEntryRow(entry, onUpdate, onDelete, refreshScreen)
            }
        }
    }
}

@Composable
fun CareLogEntryRow(
    entry: CareLogEntry,
    onUpdate: (entry: CareLogEntry) -> Unit,
    onDelete: (id: String) -> Unit,
    refreshScreen: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val showDeleteDialog = remember { mutableStateOf(false) }

    Row(horizontalArrangement = Arrangement.End) {
        if (showDeleteDialog.value) {
            ConfirmDeleteDialog(
                onCancel = { showDeleteDialog.value = false },
                onDelete = {
                    showDeleteDialog.value = false
                    onDelete.invoke(entry.id!!)
                    refreshScreen.invoke()
                }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(300.dp)
        ) {
            Text(text = "Date: ${entry.date?.split("T")?.get(0)}", fontWeight = FontWeight.Bold)
            Text(text = "Watered: ${if (entry.wasWatered == true) "Yes" else "No"}")
            Text(text = "Fertilized: ${if (entry.wasFertilized == true) "Yes" else "No"}")
            Text(text = "Notes: ${entry.notes}", modifier = Modifier.padding(bottom = 8.dp))
        }
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(20.dp),
        ) {
            Box {
                IconButton(onClick = {
                    menuExpanded = !menuExpanded
                    showDeleteDialog.value = false
                }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More",
                    )
                }
                // 5
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    // 6
                    DropdownMenuItem(
                        onClick = {
                            onUpdate.invoke(entry)
                        }
                    ) {
                        Text("Update")
                    }
                    DropdownMenuItem(
                        onClick = {
                            showDeleteDialog.value = true
                        }
                    ) {
                        Text("Delete")
                    }
                }
            }
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
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier
                        .padding(4.dp)
                        .padding(horizontal = 10.dp),
                    onClick = { onDelete.invoke() }
                ) {
                    Text("Confirm")
                }

                Button(
                    modifier = Modifier.padding(4.dp),
                    onClick = { onCancel.invoke() }
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}

