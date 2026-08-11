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
                Text("Consistency (Bristol Scale: ${consistency.toInt()})")
                Slider(
                    value = consistency,
                    onValueChange = { consistency = it },
                    valueRange = 1f..7f,
                    steps = 5
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Pain Level: ${painLevel.toInt()}")
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
