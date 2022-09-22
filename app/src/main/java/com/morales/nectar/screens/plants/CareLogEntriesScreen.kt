package com.morales.nectar.screens.plants

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.morales.nectar.screens.NectarViewModel

@Composable
fun CareLogEntriesScreen(
    navController: NavController,
    vm: NectarViewModel,
    plantId: String
) {
    var careNotes by rememberSaveable { mutableStateOf("") }
    val wasWateredSelection = remember { mutableStateOf(false) }
    val wasFertilizedSelection = remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

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
        Button(
            modifier = Modifier.padding(8.dp),
            onClick = {
                vm.createCareLogEntry(
                    plantId = plantId,
                    notes = careNotes,
                    wasWatered = wasWateredSelection.value,
                    wasFertilized = wasFertilizedSelection.value
                )
                careNotes = ""
                wasWateredSelection.value = false
                wasFertilizedSelection.value = false
                focusManager.clearFocus()
                navController.popBackStack()
            }
        ) {
            Text(text = "Add Care Log Entry")
        }
    }
}