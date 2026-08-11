package ch.schmidlins.mealdiary.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import ch.schmidlins.mealdiary.data.entities.BowelMovement

@Composable
fun BMDetailDialog(
    bm: BowelMovement,
    onDismiss: () -> Unit,
    onSave: (BowelMovement) -> Unit,
    onDelete: (BowelMovement) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var consistency by remember { mutableStateOf((bm.consistency ?: 4).toFloat()) }
    var painLevel by remember { mutableStateOf((bm.painLevel ?: 0).toFloat()) }
    var duration by remember { mutableStateOf((bm.durationMinutes ?: 5).toFloat()) }
    var notes by remember { mutableStateOf(bm.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bowel Movement Details") },
        text = {
            Column {
                val bristolInfo = when (consistency.toInt()) {
                    1 -> Pair("Hard lumps (Constipation)", "🌰")
                    2 -> Pair("Lumpy sausage", "🥖")
                    3 -> Pair("Cracked sausage", "🌽")
                    4 -> Pair("Smooth sausage (Ideal)", "🐍")
                    5 -> Pair("Soft blobs", "☁️")
                    6 -> Pair("Mushy/Fluffy", "🥞")
                    7 -> Pair("Watery (Diarrhea)", "🌊")
                    else -> Pair("", "")
                }
                Text("Consistency: ${bristolInfo.second} ${bristolInfo.first}", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = consistency,
                    onValueChange = { consistency = it },
                    valueRange = 1f..7f,
                    steps = 5,
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                val painDescription = when (painLevel.toInt()) {
                    0 -> "None"
                    in 1..3 -> "Mild"
                    in 4..6 -> "Moderate"
                    in 7..9 -> "Severe"
                    10 -> "Unbearable"
                    else -> ""
                }
                Text("Pain Level: $painDescription (${painLevel.toInt()})")
                Slider(
                    value = painLevel,
                    onValueChange = { painLevel = it },
                    valueRange = 0f..10f,
                    steps = 9
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Duration: ${duration.toInt()} mins")
                Slider(
                    value = duration,
                    onValueChange = { duration = it },
                    valueRange = 1f..60f
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onSave(bm.copy(
                    consistency = consistency.toInt(),
                    painLevel = painLevel.toInt(),
                    durationMinutes = duration.toInt(),
                    notes = notes.ifBlank { null }
                ))
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (bm.id > 0) {
                    TextButton(onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onDelete(bm)
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
