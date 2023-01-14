package com.morales.nectar.screens.plants

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Icon
import androidx.compose.material.IconToggleButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.morales.nectar.R
import com.morales.nectar.composables.CommonDivider
import com.morales.nectar.composables.NectarSubmitButton
import com.morales.nectar.data.models.CareLogEntry
import com.morales.nectar.screens.NectarViewModel

@Composable
fun EditCareLogEntryScreen(
    navController: NavController,
    vm: NectarViewModel,
    entry: CareLogEntry
) {
    var careNotes by rememberSaveable { mutableStateOf(entry.notes ?: "") }
    val wasWateredSelection = remember { mutableStateOf(entry.wasWatered ?: false) }
    val wasFertilizedSelection = remember { mutableStateOf(entry.wasFertilized ?: false) }
    val focusManager = LocalFocusManager.current
    val onSubmit = {
        val newEntry = CareLogEntry(
            id = entry.id,
            notes = careNotes,
            wasFertilized = wasFertilizedSelection.value,
            wasWatered = wasWateredSelection.value
        )
        vm.updateCareLogEntry(entry.id!!, newEntry)
        focusManager.clearFocus()
        navController.popBackStack()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = "Cancel",
                modifier = Modifier
                    .clickable { navController.popBackStack() }
            )
        }

        CommonDivider()

        Row(
            modifier = Modifier
                .padding(8.dp)
                .selectable(
                    selected = wasWateredSelection.value,
                    onClick = { wasWateredSelection.value = !wasWateredSelection.value },
                    role = Role.Checkbox
                )
        ) {
            IconToggleButton(
                checked = wasWateredSelection.value,
                onCheckedChange = { wasWateredSelection.value = !wasWateredSelection.value },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (wasWateredSelection.value) {
                            R.drawable.ic_checked
                        } else {
                            R.drawable.ic_unchecked
                        }
                    ),
                    tint = MaterialTheme.colors.primary,
                    contentDescription = "Button to select if plant was watered today"
                )
            }
            Text(text = "Watered?")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(8.dp)
                .selectable(
                    selected = wasFertilizedSelection.value,
                    onClick = { wasFertilizedSelection.value = !wasFertilizedSelection.value },
                    role = Role.Checkbox
                )
        ) {
            IconToggleButton(
                checked = wasFertilizedSelection.value,
                onCheckedChange = {
                    wasFertilizedSelection.value = !wasFertilizedSelection.value
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (wasFertilizedSelection.value) {
                            R.drawable.ic_checked
                        } else {
                            R.drawable.ic_unchecked
                        }
                    ),
                    tint = MaterialTheme.colors.primary,
                    contentDescription = "Button to select if plant was watered today"
                )
            }
            Text(text = "Fertilized?")
        }

        Text(text = "Notes", modifier = Modifier.padding(8.dp))
        TextField(
            value = careNotes,
            onValueChange = { careNotes = it },
            modifier = Modifier
                .padding(8.dp)
                .size(250.dp)
                .border(1.dp, Color.LightGray),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                textColor = Color.Black,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            label = {
                Text(text = "Enter any optional notes about this care session")
            }
        )
        NectarSubmitButton(buttonText = "Update Care Log Entry", onClick = { onSubmit.invoke() })
    }

}