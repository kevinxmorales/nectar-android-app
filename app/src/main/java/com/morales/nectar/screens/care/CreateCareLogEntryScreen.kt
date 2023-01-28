package com.morales.nectar.screens.care

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavController
import com.morales.nectar.screens.NectarViewModel
import java.time.LocalDate

@Composable
fun CreateCareLogEntryScreen(
    navController: NavController,
    vm: NectarViewModel,
    plantId: String
) {
    val focusManager = LocalFocusManager.current
    var careNotes by rememberSaveable { mutableStateOf("") }
    val wasWateredSelection = remember { mutableStateOf(false) }
    val wasFertilizedSelection = remember { mutableStateOf(false) }
    var pickedDate by remember { mutableStateOf(LocalDate.now()) }

    val onDatePicked = { newDate: LocalDate ->
        pickedDate = newDate
    }

    val onSubmit = {
        vm.createCareLogEntry(
            plantId = plantId,
            notes = careNotes,
            wasWatered = wasWateredSelection.value,
            wasFertilized = wasFertilizedSelection.value
        )
        focusManager.clearFocus()
        navController.popBackStack()
    }

    CareLogForm(
        submitButtonText = "Add Care Log Entry",
        wasWateredSelection = wasWateredSelection.value,
        setWasWatered = { wasWateredSelection.value = !wasWateredSelection.value },
        wasFertilizedSelection = wasFertilizedSelection.value,
        setWasFertilized = { wasFertilizedSelection.value = !wasFertilizedSelection.value },
        careNotes = careNotes,
        setCareNotes = { notes: String -> careNotes = notes },
        onCancel = { navController.popBackStack() },
        onSubmit = { onSubmit.invoke() },
        pickedDate = pickedDate,
        onDatePicked = { onDatePicked.invoke(it) },
    )

}