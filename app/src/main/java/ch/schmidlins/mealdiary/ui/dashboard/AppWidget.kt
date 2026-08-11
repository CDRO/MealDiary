package ch.schmidlins.mealdiary.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.schmidlins.mealdiary.ui.MealViewModel

sealed class AppWidget(val id: String, val title: String) {
    @Composable
    abstract fun Content(viewModel: MealViewModel)

    object BMFrequency : AppWidget("bm_freq", "Avg. BM Frequency") {
        @Composable
        override fun Content(viewModel: MealViewModel) {
            val stats = viewModel.statistics.observeAsState().value
            stats?.let {
                ch.schmidlins.mealdiary.BMFrequencyChart(it.avgBMFrequency)
            }
        }
    }

    object WeightTrend : AppWidget("weight_trend", "Weight Trend") {
        @Composable
        override fun Content(viewModel: MealViewModel) {
            val stats = viewModel.statistics.observeAsState().value
            stats?.let {
                if (it.weightHistory.size >= 2) {
                    ch.schmidlins.mealdiary.WeightTrendChart(it.weightHistory)
                } else {
                    Text("Not enough weight data", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    object TopFoods : AppWidget("top_foods", "Top Foods") {
        @Composable
        override fun Content(viewModel: MealViewModel) {
            val stats = viewModel.statistics.observeAsState().value
            stats?.let { s ->
                Column {
                    s.topFoods.forEach { (food, count) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(food, style = MaterialTheme.typography.bodySmall)
                            Text("$count logs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                    if (s.topFoods.isEmpty()) {
                        Text("No foods logged yet", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }

    object Insights : AppWidget("insights", "Smart Insights") {
        @Composable
        override fun Content(viewModel: MealViewModel) {
            val insights = viewModel.insights.observeAsState(emptyList()).value
            if (insights.isNotEmpty()) {
                insights.forEach { insight ->
                    Text("✨ $insight", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                Text("Keep logging to see insights", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
