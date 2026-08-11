package ch.schmidlins.mealdiary

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ch.schmidlins.mealdiary.data.AppDatabase
import ch.schmidlins.mealdiary.data.repository.BMRepository
import ch.schmidlins.mealdiary.data.repository.MealRepository
import ch.schmidlins.mealdiary.data.repository.UserPreferencesRepository
import ch.schmidlins.mealdiary.data.repository.WeightRepository
import ch.schmidlins.mealdiary.ui.FeedItem
import ch.schmidlins.mealdiary.ui.MealViewModel
import ch.schmidlins.mealdiary.ui.MealViewModelFactory
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(this)
        val mealRepository = MealRepository(database.mealDao())
        val bmRepository = BMRepository(database.bowelMovementDao())
        val weightRepository = WeightRepository(database.weightEntryDao())
        val prefsRepo = UserPreferencesRepository(this)
        val viewModelFactory = MealViewModelFactory(mealRepository, bmRepository, weightRepository, prefsRepo)

        setContent {
            val navController = rememberNavController()
            val viewModel: MealViewModel = viewModel(factory = viewModelFactory)
            
            NavHost(navController = navController, startDestination = "main") {
                composable("main") {
                    MealDiaryApp(viewModel, onNavigateToOverview = { navController.navigate("overview") })
                }
                composable("overview") {
                    DataOverviewScreen(viewModel, onBack = { navController.popBackStack() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealDiaryApp(viewModel: MealViewModel, onNavigateToOverview: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val haptic = LocalHapticFeedback.current
    var mealText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    val feedItems by viewModel.unifiedFeed.observeAsState(emptyList())
    val shouldAskBM by viewModel.shouldAskAboutBM.observeAsState(false)
    val shouldShowWeightSuggestion by viewModel.shouldShowWeightSuggestion.observeAsState(false)
    val isWeightTrackingEnabled by viewModel.isWeightTrackingEnabled.observeAsState(false)
    val timeSinceBM by viewModel.timeSinceLastBM.observeAsState(null)
    val analysisEngine = remember { AnalysisEngine() }
    val patternResult = remember { analysisEngine.getPatternResult() }
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("MealDiary - $patternResult") },
                actions = {
                    IconButton(onClick = onNavigateToOverview) {
                        Icon(Icons.Default.Info, contentDescription = "Overview")
                    }
                    IconButton(onClick = { 
                        val intent = Intent(context, ch.schmidlins.mealdiary.ui.settings.SettingsActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            ) 
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            timeSinceBM?.let {
                Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            androidx.compose.animation.AnimatedVisibility(visible = shouldAskBM) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Have you had a bowel movement in the last 24h?", style = MaterialTheme.typography.bodyLarge)
                        Button(
                            onClick = { 
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                viewModel.addBowelMovement() 
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Yes, Log now")
                        }
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(visible = shouldShowWeightSuggestion) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("You've been using MealDiary for a week! Would you like to track your weight as well?", style = MaterialTheme.typography.bodyLarge)
                        Row(modifier = Modifier.padding(top = 8.dp)) {
                            Button(onClick = { 
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                viewModel.enableWeightTracking() 
                            }) {
                                Text("Yes, Enable")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = { viewModel.dismissWeightSuggestion() }) {
                                Text("Not now")
                            }
                        }
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(visible = isWeightTrackingEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
                            label = { Text("Weight (kg)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            weightText.toDoubleOrNull()?.let {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                viewModel.addWeightEntry(it)
                                weightText = ""
                            }
                        }) {
                            Text("Log")
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
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        viewModel.addMeal(mealText)
                        mealText = ""
                    }
                }) {
                    Text("Log Meal")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { 
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    viewModel.addBowelMovement() 
                }) {
                    Text("Log BM")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(feedItems, key = { "${it.javaClass.simpleName}-${it.id}" }) { item ->
                    val timeStr = dateFormat.format(Date(item.timestamp))
                    Box(modifier = Modifier.animateItem()) {
                        when (item) {
                            is FeedItem.MealItem -> {
                                ListItem(
                                    headlineContent = { Text(item.meal.description) },
                                    leadingContent = { Text("🍴") },
                                    trailingContent = {
                                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                            Text(timeStr, style = MaterialTheme.typography.labelSmall)
                                            IconButton(onClick = { 
                                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                                viewModel.deleteMeal(item.meal) 
                                            }) {
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
                                            IconButton(onClick = { 
                                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                                viewModel.deleteBM(item.bm) 
                                            }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                )
                            }
                            is FeedItem.WeightItem -> {
                                ListItem(
                                    headlineContent = { Text("Weight: ${item.weightEntry.weight} ${item.weightEntry.unit}", color = MaterialTheme.colorScheme.secondary) },
                                    leadingContent = { Text("⚖️") },
                                    trailingContent = {
                                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                            Text(timeStr, style = MaterialTheme.typography.labelSmall)
                                            IconButton(onClick = { 
                                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                                viewModel.deleteWeight(item.weightEntry) 
                                            }) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataOverviewScreen(viewModel: MealViewModel, onBack: () -> Unit) {
    val summaries by viewModel.dailySummaries.observeAsState(emptyList())
    val todayItems by viewModel.todayTimeline.observeAsState(emptyList())
    val weeklySummary by viewModel.weeklySummary.observeAsState()
    val statistics by viewModel.statistics.observeAsState()
    val insights by viewModel.insights.observeAsState(emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Overview") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            item {
                weeklySummary?.let {
                    Text("Weekly Summary (Last 7 Days)", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                Text("Total Meals", style = MaterialTheme.typography.labelMedium)
                                Text("${it.mealCount}", style = MaterialTheme.typography.headlineMedium)
                            }
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                Text("Total BMs", style = MaterialTheme.typography.labelMedium)
                                Text("${it.bmCount}", style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            item {
                Text("Today's Timeline", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                TimelineComponent(todayItems)
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                if (insights.isNotEmpty()) {
                    Text("Smart Insights", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            insights.forEach { insight ->
                                Text("✨ $insight", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            item {
                statistics?.let { stats ->
                    Text("Advanced Statistics", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Avg. BM Frequency", style = MaterialTheme.typography.labelLarge)
                            BMFrequencyChart(stats.avgBMFrequency)
                            
                            stats.avgWeight?.let { avg ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Weight Summary", style = MaterialTheme.typography.labelLarge)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Average: %.1f kg".format(avg), style = MaterialTheme.typography.bodySmall)
                                    stats.weightDelta?.let { delta ->
                                        Text("Change: ${if (delta > 0) "+" else ""}${"%.1f".format(delta)} kg", 
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (delta < 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                            if (stats.topFoods.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Top Foods", style = MaterialTheme.typography.labelLarge)
                                stats.topFoods.forEach { (food, count) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(food, style = MaterialTheme.typography.bodySmall)
                                        Text("$count logs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            }
                            
                            if (stats.weightHistory.size >= 2) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Weight Trend:", style = MaterialTheme.typography.labelLarge)
                                WeightTrendChart(stats.weightHistory)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
            
            item {
                Text("Daily History", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                if (summaries.isEmpty()) {
                    Text("No historical data available", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                }
            }

            items(summaries) { summary ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    ListItem(
                        headlineContent = { Text(summary.date.toString()) },
                        supportingContent = { 
                            Text("🍴 ${summary.mealCount} Meals, 💩 ${summary.bmCount} BMs")
                        },
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                    )
                }
            }
        }
    }
}

@Composable
fun BMFrequencyChart(frequency: Double) {
    val progress = (frequency / 3.0).coerceIn(0.0, 1.0).toFloat()
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(24.dp)) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(12.dp).align(androidx.compose.ui.Alignment.Center),
                color = if (frequency >= 1.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            // Goal marker at 1.0/day
            Box(modifier = Modifier.fillMaxWidth(1f/3f).fillMaxHeight().align(androidx.compose.ui.Alignment.CenterStart)) {
                Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(MaterialTheme.colorScheme.onSurface).align(androidx.compose.ui.Alignment.CenterEnd))
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("0", style = MaterialTheme.typography.labelSmall)
            Text("1 (Goal)", style = MaterialTheme.typography.labelSmall)
            Text("2", style = MaterialTheme.typography.labelSmall)
            Text("3+", style = MaterialTheme.typography.labelSmall)
        }
        Text(
            text = "Current: %.2f BMs/day".format(frequency),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(androidx.compose.ui.Alignment.End).padding(top = 4.dp),
            color = if (frequency >= 1.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun WeightTrendChart(history: List<ch.schmidlins.mealdiary.data.entities.WeightEntry>) {
    val color = MaterialTheme.colorScheme.secondary
    val maxWeight = history.maxOf { it.weight }
    val minWeight = history.minOf { it.weight }
    val range = (maxWeight - minWeight).coerceAtLeast(1.0)
    
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("%.1f kg".format(maxWeight), style = MaterialTheme.typography.labelSmall)
            Text("%.1f kg".format(minWeight), style = MaterialTheme.typography.labelSmall)
        }
        Canvas(modifier = Modifier.fillMaxWidth().height(120.dp).padding(vertical = 8.dp)) {
            val width = size.width
            val height = size.height
            val stepX = width / (history.size - 1).coerceAtLeast(1)
            
            val points = history.mapIndexed { index, entry ->
                val x = index * stepX
                val y = height - ((entry.weight - minWeight) / range * height).toFloat()
                androidx.compose.ui.geometry.Offset(x, y)
            }
            
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = color,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 4f
                )
                drawCircle(color = color, center = points[i], radius = 6f)
            }
            drawCircle(color = color, center = points.last(), radius = 6f)
            
            val fillPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(points.first().x, height)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(color.copy(alpha = 0.3f), androidx.compose.ui.graphics.Color.Transparent)
                )
            )
        }
    }
}

@Composable
fun TimelineComponent(items: List<FeedItem>) {
    val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val dayMillis = 24 * 60 * 60 * 1000L

    Card(modifier = Modifier.fillMaxWidth().height(100.dp)) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Baseline
            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(MaterialTheme.colorScheme.outlineVariant).align(androidx.compose.ui.Alignment.Center))
            
            if (items.isEmpty()) {
                Text("No activities today", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            } else {
                items.forEach { item ->
                    val offset = ((item.timestamp - today).toFloat() / dayMillis).coerceIn(0f, 1f)
                    val color = if (item is FeedItem.MealItem) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    val icon = if (item is FeedItem.MealItem) "🍴" else "💩"
                    
                    Box(modifier = Modifier.fillMaxWidth().align(androidx.compose.ui.Alignment.Center)) {
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            modifier = Modifier.align(androidx.compose.ui.BiasAlignment(horizontalBias = (offset * 2) - 1, verticalBias = 0f))
                        ) {
                            Text(icon, style = MaterialTheme.typography.bodySmall)
                            Box(modifier = Modifier.size(6.dp).background(color, androidx.compose.foundation.shape.CircleShape))
                        }
                    }
                }
            }
        }
    }
}
