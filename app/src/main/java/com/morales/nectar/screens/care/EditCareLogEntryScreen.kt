package com.morales.nectar.screens.care

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavController
import com.morales.nectar.data.models.CareLogParcel
import com.morales.nectar.data.remote.requests.care.UpdateCareLogRequest
import com.morales.nectar.screens.NectarViewModel
import java.time.LocalDate

@Composable
fun EditCareLogEntryScreen(
    navController: NavController,
    vm: NectarViewModel,
    entryParcel: CareLogParcel
) {
    val entry = entryParcel.toCareLog()
    var careNotes by rememberSaveable { mutableStateOf(entry.notes ?: "") }
    val wasWateredSelection = remember { mutableStateOf(entry.wasWatered) }
    val wasFertilizedSelection = remember { mutableStateOf(entry.wasFertilized) }
    val focusManager = LocalFocusManager.current
    var pickedDate by remember { mutableStateOf(LocalDate.now()) }

    val onDatePicked = { newDate: LocalDate ->
        pickedDate = newDate
    }
    val onSubmit = {
        val newEntry = UpdateCareLogRequest(
            id = entry.id,
            notes = careNotes,
            wasFertilized = wasFertilizedSelection.value,
            wasWatered = wasWateredSelection.value
        )
        vm.updateCareLogEntry(entry.id, newEntry)
        focusManager.clearFocus()
        navController.popBackStack()
    }

    CareLogForm(
        submitButtonText = "Update Care Log Entry",
        wasWateredSelection = wasWateredSelection.value,
        setWasWatered = { wasWateredSelection.value = !wasWateredSelection.value },
        wasFertilizedSelection = wasFertilizedSelection.value,
        setWasFertilized = { wasFertilizedSelection.value = !wasFertilizedSelection.value },
        careNotes = careNotes,
        setCareNotes = { notes: String -> careNotes = notes },
        onCancel = { navController.popBackStack() },
        onSubmit = { onSubmit.invoke() },
        pickedDate = pickedDate,
        onDatePicked = { onDatePicked.invoke(it) }
    )
}