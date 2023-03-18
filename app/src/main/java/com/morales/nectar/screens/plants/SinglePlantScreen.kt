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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.himanshoe.kalendar.Kalendar
import com.himanshoe.kalendar.model.KalendarDay
import com.himanshoe.kalendar.model.KalendarEvent
import com.himanshoe.kalendar.model.KalendarType
import com.morales.nectar.DestinationScreen
import com.morales.nectar.R
import com.morales.nectar.android.composables.CommonDivider
import com.morales.nectar.android.composables.CommonImageFull
import com.morales.nectar.android.composables.NectarConfirmDialog
import com.morales.nectar.android.composables.NectarSubmitButton
import com.morales.nectar.android.composables.ProgressSpinner
import com.morales.nectar.data.models.CareLog
import com.morales.nectar.data.models.PlantData
import com.morales.nectar.navigation.NavParam
import com.morales.nectar.navigation.navigateTo
import com.morales.nectar.screens.NectarViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.util.*

private const val TAG = "SinglePlantScreen"
private const val USE_CALENDAR_VIEW = "Use Calendar View"
private const val USE_LIST_VIEW = "Use List View"


@Composable
fun SinglePlantScreen(
    navController: NavController,
    p: PlantData,
    vm: NectarViewModel,
) {
    val scrollState = rememberScrollState()

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

    val onBackPress: () -> Unit = {
        vm.updatePosts()
        navController.popBackStack()
    }

    LaunchedEffect(key1 = Unit) {
        vm.getCareLogEntries(p.plantId!!, null)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.White)

    ) {
        SinglePostHeader(navController, onBackPress, onDelete, onUpdate)

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
    onBackPress: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: () -> Unit,
) {
    val showDeleteDialog = remember { mutableStateOf(false) }
    if (showDeleteDialog.value) {
        NectarConfirmDialog(
            onCancel = { showDeleteDialog.value = false },
            onSubmit = {
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
                .clickable { onBackPress.invoke() },
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
    val careLogEntriesProgress = vm.isLoading.value

    val useCalendarView = remember { mutableStateOf(false) }
    val careLogButtonText = remember { mutableStateOf(USE_CALENDAR_VIEW) }

    val onAddCareLogEntry = {
        navController.navigate(DestinationScreen.CreateCareLogEntry.createRoute(post.plantId!!))
    }

    val onClick = {
        careLogButtonText.value = if (useCalendarView.value) USE_LIST_VIEW else USE_CALENDAR_VIEW
        useCalendarView.value = useCalendarView.value.not()
    }
    val today = LocalDate.parse(java.time.LocalDate.now().format(DateTimeFormatter.ISO_DATE))
    val careLogsOnDay = remember { mutableStateOf<List<CareLog>>(listOf()) }
    val careLogDaySelected = remember { mutableStateOf(KalendarDay(today)) }

    val onCalendarDayClick = { kalendarDay: KalendarDay, kalendarEvents: List<KalendarEvent> ->
        careLogDaySelected.value = kalendarDay
        for (event in kalendarEvents) {
            careLogsOnDay.value =
                careLogEntries.filter { careLogEntry -> java.time.LocalDate.parse(careLogEntry.careDate) == event.date.toJavaLocalDate() }
        }
    }

    val refreshScreen = {
        navController.popBackStack()
        navigateTo(
            navController,
            DestinationScreen.SinglePlant,
            NavParam("plant", post)
        )
    }

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
                    CommonImageFull(
                        contentDescription = "the plant owner's profile picture",
                        uri = post.userImage,
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
                    CommonImageFull(
                        contentDescription = "a picture of the plant",
                        uri = post.images!![page],
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 150.dp)
                    )
                }
            }

        }
        Row(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "Common Name: " + post.commonName,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Row(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "Scientific Name: " + post.scientificName,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Row(modifier = Modifier.padding(8.dp)) {
            Text(text = "Toxicity: " + post.toxicity, modifier = Modifier.padding(start = 8.dp))
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

        NectarSubmitButton(buttonText = USE_CALENDAR_VIEW, onClick = { onClick.invoke() })

        if (careLogEntriesProgress) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ProgressSpinner()
            }
        }
        if (useCalendarView.value) {
            Kalendar(
                kalendarType = KalendarType.Oceanic,
                onCurrentDayClick = { day, events -> onCalendarDayClick.invoke(day, events) }
            )
            CareLogEntryList(
                navController,
                careLogsOnDay.value,
                vm,
                refreshScreen,
            )
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

@Composable
fun CareLogEntryList(
    navController: NavController,
    careLogEntries: List<CareLog>,
    vm: NectarViewModel,
    refreshScreen: () -> Unit
) {

    val onUpdate = { entry: CareLog ->
        navigateTo(
            navController,
            DestinationScreen.EditCareLogEntry,
            NavParam("entry", entry.toCareLogParcel())
        )
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
    entry: CareLog,
    onUpdate: (entry: CareLog) -> Unit,
    onDelete: (id: String) -> Unit,
    refreshScreen: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val showDeleteDialog = remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp)
    ) {
        if (showDeleteDialog.value) {
            NectarConfirmDialog(
                onCancel = { showDeleteDialog.value = false },
                onSubmit = {
                    showDeleteDialog.value = false
                    onDelete.invoke(entry.id)
                    refreshScreen.invoke()
                }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(300.dp)
        ) {
            Text(text = "Date: ${entry.careDate}", fontWeight = FontWeight.Bold)
            Text(text = "Watered: ${if (entry.wasWatered) "Yes" else "No"}")
            Text(text = "Fertilized: ${if (entry.wasFertilized) "Yes" else "No"}")
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
                        contentDescription = "More Options",
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        onClick = { onUpdate.invoke(entry) }
                    ) {
                        Text("Update")
                    }
                    DropdownMenuItem(onClick = { showDeleteDialog.value = true }
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
    CommonDivider()
}