package ch.schmidlins.mealdiary.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.schmidlins.mealdiary.data.entities.BowelMovement

@Composable
fun BMDetailDialog(
    bm: BowelMovement,
    onDismiss: () -> Unit,
    onSave: (BowelMovement) -> Unit
) {
    var consistency by remember { mutableStateOf((bm.consistency ?: 4).toFloat()) }
    var painLevel by remember { mutableStateOf((bm.painLevel ?: 0).toFloat()) }
    var duration by remember { mutableStateOf((bm.durationMinutes ?: 5).toFloat()) }
    var notes by remember { mutableStateOf(bm.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bowel Movement Details") },
        text = {
            Column {
                val bristolDescription = when (consistency.toInt()) {
                    1 -> "Hard lumps (Constipation)"
                    2 -> "Lumpy sausage"
                    3 -> "Cracked sausage"
                    4 -> "Smooth sausage (Ideal)"
                    5 -> "Soft blobs"
                    6 -> "Mushy/Fluffy"
                    7 -> "Watery (Diarrhea)"
                    else -> ""
                }
                Text("Consistency: $bristolDescription")
                Slider(
                    value = consistency,
                    onValueChange = { consistency = it },
                    valueRange = 1f..7f,
                    steps = 5
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
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
