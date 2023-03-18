package com.morales.nectar.screens.care

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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.morales.nectar.R
import com.morales.nectar.android.composables.CommonDivider
import com.morales.nectar.android.composables.NectarDatePicker
import com.morales.nectar.android.composables.NectarSubmitButton
import com.morales.nectar.android.composables.NectarTextField
import java.time.LocalDate

@Composable
fun CareLogForm(
    submitButtonText: String,
    wasWateredSelection: Boolean,
    setWasWatered: () -> Unit,
    wasFertilizedSelection: Boolean,
    setWasFertilized: () -> Unit,
    pickedDate: LocalDate,
    onDatePicked: (LocalDate) -> Unit,
    careNotes: String,
    setCareNotes: (String) -> Unit,
    onCancel: () -> Unit,
    onSubmit: () -> Unit,
) {

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
                    .clickable { onCancel.invoke() }
            )

            Text(
                text = "Submit",
                modifier = Modifier
                    .clickable { onSubmit.invoke() }
            )
        }

        CommonDivider()

        Row(
            modifier = Modifier
                .padding(8.dp)
                .selectable(
                    selected = wasWateredSelection,
                    onClick = { setWasWatered.invoke() },
                    role = Role.Checkbox
                )
        ) {
            IconToggleButton(
                checked = wasWateredSelection,
                onCheckedChange = { setWasWatered.invoke() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (wasWateredSelection) {
                            R.drawable.ic_checked
                        } else {
                            R.drawable.ic_unchecked
                        }
                    ),
                    tint = Color.Black,
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
                    selected = wasFertilizedSelection,
                    onClick = { setWasFertilized.invoke() },
                    role = Role.Checkbox
                )
        ) {
            IconToggleButton(
                checked = wasFertilizedSelection,
                onCheckedChange = { setWasFertilized.invoke() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (wasFertilizedSelection) {
                            R.drawable.ic_checked
                        } else {
                            R.drawable.ic_unchecked
                        }
                    ),
                    tint = Color.Black,
                    contentDescription = "Button to select if plant was watered today"
                )
            }
            Text(text = "Fertilized?")
        }

        Text(text = "Notes", modifier = Modifier.padding(8.dp))

        NectarTextField(
            label = "Enter any optional notes about this care session",
            onChange = { setCareNotes.invoke(it) },
            value = careNotes,
        )

        NectarDatePicker(pickedDate = pickedDate, onClick = onDatePicked)

        NectarSubmitButton(buttonText = submitButtonText, onClick = { onSubmit.invoke() })
    }
}