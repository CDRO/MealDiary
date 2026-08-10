package ch.schmidlins.mealdiary.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.schmidlins.mealdiary.data.repository.UserPreferencesRepository
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefsRepo = UserPreferencesRepository(this)

        setContent {
            SettingsScreen(prefsRepo)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(prefsRepo: UserPreferencesRepository) {
    val scope = rememberCoroutineScope()
    val bmInterval by prefsRepo.bmPromptIntervalHours.collectAsState(initial = 24)
    val reminderEnabled by prefsRepo.isReminderEnabled.collectAsState(initial = true)
    val weightEnabled by prefsRepo.isWeightTrackingEnabled.collectAsState(initial = false)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Bowel Movement Prompt Interval", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = bmInterval.toFloat(),
                onValueChange = { scope.launch { prefsRepo.updateBMPromptInterval(it.toInt()) } },
                valueRange = 12f..48f,
                steps = 3
            )
            Text("${bmInterval} hours")
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Enable Meal Reminders")
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = { scope.launch { prefsRepo.updateReminderEnabled(it) } }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Enable Weight Tracking")
                Switch(
                    checked = weightEnabled,
                    onCheckedChange = { scope.launch { prefsRepo.updateWeightTrackingEnabled(it) } }
                )
            }
        }
    }
}
