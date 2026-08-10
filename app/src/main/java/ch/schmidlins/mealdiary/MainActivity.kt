package ch.schmidlins.mealdiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.schmidlins.mealdiary.data.AppDatabase
import ch.schmidlins.mealdiary.data.repository.BMRepository
import ch.schmidlins.mealdiary.data.repository.MealRepository
import ch.schmidlins.mealdiary.data.repository.WeightRepository
import ch.schmidlins.mealdiary.ui.FeedItem
import ch.schmidlins.mealdiary.ui.MealViewModel
import ch.schmidlins.mealdiary.ui.MealViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(this)
        val mealRepository = MealRepository(database.mealDao())
        val bmRepository = BMRepository(database.bowelMovementDao())
        val weightRepository = WeightRepository(database.weightEntryDao())
        val viewModelFactory = MealViewModelFactory(mealRepository, bmRepository, weightRepository)

        setContent {
            val viewModel: MealViewModel = viewModel(factory = viewModelFactory)
            MealDiaryApp(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealDiaryApp(viewModel: MealViewModel) {
    var mealText by remember { mutableStateOf("") }
    val feedItems by viewModel.unifiedFeed.observeAsState(emptyList())
    val shouldAskBM by viewModel.shouldAskAboutBM.observeAsState(false)
    val timeSinceBM by viewModel.timeSinceLastBM.observeAsState(null)
    val analysisEngine = remember { AnalysisEngine() }
    val patternResult = remember { analysisEngine.getPatternResult() }
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("MealDiary - $patternResult") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            timeSinceBM?.let {
                Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (shouldAskBM) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Have you had a bowel movement in the last 24h?", style = MaterialTheme.typography.bodyLarge)
                        Button(
                            onClick = { viewModel.addBowelMovement() },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Yes, Log now")
                        }
                    }
                }
            }

            OutlinedTextField(
                value = mealText,
                onValueChange = { mealText = it },
                label = { Text("What did you eat?") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Button(onClick = { 
                    if (mealText.isNotBlank()) {
                        viewModel.addMeal(mealText)
                        mealText = ""
                    }
                }) {
                    Text("Log Meal")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { viewModel.addBowelMovement() }) {
                    Text("Log BM")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(feedItems, key = { "${it.javaClass.simpleName}-${it.id}" }) { item ->
                    val timeStr = dateFormat.format(Date(item.timestamp))
                    when (item) {
                        is FeedItem.MealItem -> {
                            ListItem(
                                headlineContent = { Text(item.meal.description) },
                                leadingContent = { Text("🍴") },
                                trailingContent = {
                                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                        Text(timeStr, style = MaterialTheme.typography.labelSmall)
                                        IconButton(onClick = { viewModel.deleteMeal(item.meal) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            )
                        }
                        is FeedItem.BMItem -> {
                            ListItem(
                                headlineContent = { Text("Bowel Movement", color = MaterialTheme.colorScheme.primary) },
                                leadingContent = { Text("💩") },
                                trailingContent = {
                                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                        Text(timeStr, style = MaterialTheme.typography.labelSmall)
                                        IconButton(onClick = { viewModel.deleteBM(item.bm) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
