package ch.schmidlins.mealdiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import ch.schmidlins.mealdiary.ui.MealViewModel
import ch.schmidlins.mealdiary.ui.MealViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(this)
        val mealRepository = MealRepository(database.mealDao())
        val bmRepository = BMRepository(database.bowelMovementDao())
        val viewModelFactory = MealViewModelFactory(mealRepository, bmRepository)

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
    val meals by viewModel.meals.observeAsState(emptyList())

    Scaffold(
        topBar = { TopAppBar(title = { Text("MealDiary") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
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
                items(meals) { meal ->
                    Text("${meal.description} at ${meal.timestamp}")
                }
            }
        }
    }
}
