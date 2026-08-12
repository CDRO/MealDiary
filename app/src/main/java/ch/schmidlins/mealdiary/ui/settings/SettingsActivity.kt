package ch.schmidlins.mealdiary.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.schmidlins.mealdiary.data.AppDatabase
import ch.schmidlins.mealdiary.data.repository.BMRepository
import ch.schmidlins.mealdiary.data.repository.MealRepository
import ch.schmidlins.mealdiary.data.repository.UserPreferencesRepository
import ch.schmidlins.mealdiary.data.repository.WeightRepository
import ch.schmidlins.mealdiary.ui.MealViewModel
import ch.schmidlins.mealdiary.ui.MealViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getDatabase(this)
        val mealRepository = MealRepository(database.mealDao())
        val bmRepository = BMRepository(database.bowelMovementDao())
        val weightRepository = WeightRepository(database.weightEntryDao())
        val prefsRepo = UserPreferencesRepository(this)
        val viewModelFactory = MealViewModelFactory(mealRepository, bmRepository, weightRepository, prefsRepo)
        val viewModel = androidx.lifecycle.ViewModelProvider(this, viewModelFactory).get(MealViewModel::class.java)

        setContent {
            SettingsScreen(prefsRepo, viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(prefsRepo: UserPreferencesRepository, viewModel: MealViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val bmInterval by prefsRepo.bmPromptIntervalHours.collectAsState(initial = 24)
    val reminderEnabled by prefsRepo.isReminderEnabled.collectAsState(initial = true)
    val weightEnabled by prefsRepo.isWeightTrackingEnabled.collectAsState(initial = false)
    val enabledWidgets by prefsRepo.enabledWidgets.collectAsState(initial = setOf("insights", "bm_freq", "weight_trend", "top_foods"))

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri ->
            uri?.let {
                scope.launch {
                    val csvData = viewModel.getCSVData()
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(it)?.use { outputStream ->
                            OutputStreamWriter(outputStream).use { writer ->
                                writer.write(csvData)
                            }
                        }
                    }
                }
            }
        }
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
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

            Spacer(modifier = Modifier.height(32.dp))

            Text("Dashboard Widgets", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            val allWidgetIds = listOf(
                "insights" to "Smart Insights",
                "bm_freq" to "BM Frequency",
                "weight_trend" to "Weight Trend",
                "top_foods" to "Top Foods"
            )

            allWidgetIds.forEach { (id, label) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(label)
                    Switch(
                        checked = id in enabledWidgets,
                        onCheckedChange = { isChecked ->
                            val newSet = enabledWidgets.toMutableSet()
                            if (isChecked) newSet.add(id) else newSet.remove(id)
                            scope.launch { prefsRepo.updateEnabledWidgets(newSet) }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Data Management", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { exportLauncher.launch("meal_diary_export.csv") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export Data to CSV")
            }
        }
    }
}
