package com.morales.nectar.screens.care

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.himanshoe.kalendar.Kalendar
import com.himanshoe.kalendar.model.KalendarType
import com.morales.nectar.DestinationScreen
import com.morales.nectar.android.composables.CommonDivider
import com.morales.nectar.android.composables.NectarConfirmDialog
import com.morales.nectar.android.composables.NectarSubmitButton
import com.morales.nectar.android.composables.PlantImage
import com.morales.nectar.data.models.CareLog
import com.morales.nectar.navigation.BottomNavigationItem
import com.morales.nectar.navigation.BottomNavigationMenu
import com.morales.nectar.navigation.NavParam
import com.morales.nectar.navigation.navigateTo
import com.morales.nectar.screens.NectarViewModel
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import java.time.LocalDate

private const val TAG = "CareLogEntriesScreen"

@Composable
fun CareLogEntriesScreen(
    navController: NavController,
    vm: NectarViewModel
) {
    val scrollState = rememberScrollState()
    val selectedDate = remember { mutableStateOf(LocalDate.now()) }
    val allCareLogEntries = vm.allCareLogEntries.value

    fun filterCareLogs(entries: List<CareLog>, date: LocalDate) =
        entries.filter { vm.parseDate(it.careDate) == date }

    val careLogsOnDay = remember {
        mutableStateOf(filterCareLogs(allCareLogEntries, LocalDate.now()))
    }

    val isCalendarMini = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit, block = {
        vm.getAllCareLogEntries()
        careLogsOnDay.value = filterCareLogs(vm.allCareLogEntries.value, LocalDate.now())
    })

    Column {
        NectarSubmitButton(
            buttonText = if (isCalendarMini.value) "Expand Calendar" else "Minimize Calendar",
            onClick = { isCalendarMini.value = isCalendarMini.value.not() })
        Column(modifier = Modifier.weight(1f)) {
            Kalendar(
                kalendarType = if (isCalendarMini.value) KalendarType.Oceanic else KalendarType.Firey,
                onCurrentDayClick = { day, events ->
                    Log.i(TAG, events.toString())
                    selectedDate.value = day.localDate.toJavaLocalDate()
                    careLogsOnDay.value =
                        filterCareLogs(allCareLogEntries, day.localDate.toJavaLocalDate())
                },
                takeMeToDate = selectedDate.value.toKotlinLocalDate()
            )
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
            ) {
                CareLogOnDayList(
                    careLogEntries = careLogsOnDay.value,
                    refreshScreen = {
                        vm.getAllCareLogEntries()
                    },
                    navController = navController,
                    vm = vm
                )
            }
        }

        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.POSTS,
            navController = navController
        )
    }
}

@Composable
fun CareLogOnDayList(
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
            Text(text = "No care log entries on this day")
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
            .padding(top = 5.dp)
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
            PlantImage(imageUri = entry.plantImage)
            Text(text = "Common Name: ${entry.plantName}", fontWeight = FontWeight.Bold)
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