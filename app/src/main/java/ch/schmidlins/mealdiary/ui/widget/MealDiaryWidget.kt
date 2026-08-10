package ch.schmidlins.mealdiary.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.Text

import ch.schmidlins.mealdiary.data.AppDatabase
import ch.schmidlins.mealdiary.data.entities.BowelMovement
import ch.schmidlins.mealdiary.data.entities.Meal

class MealDiaryWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent()
        }
    }

    @Composable
    private fun WidgetContent() {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text("Log Meal", modifier = GlanceModifier.clickable(actionRunCallback<LogAction>(actionParametersOf(ActionTypeKey to "MEAL"))))
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text("Log BM", modifier = GlanceModifier.clickable(actionRunCallback<LogAction>(actionParametersOf(ActionTypeKey to "BM"))))
        }
    }
}

private val ActionTypeKey = ActionParameters.Key<String>("type")

class LogAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val type = parameters[ActionTypeKey] ?: return
        val database = AppDatabase.getDatabase(context)
        
        when (type) {
            "MEAL" -> {
                database.mealDao().insertMeal(Meal(description = "Logged from Widget", timestamp = System.currentTimeMillis()))
            }
            "BM" -> {
                database.bowelMovementDao().insertBM(BowelMovement(timestamp = System.currentTimeMillis()))
            }
        }
        
        MealDiaryWidget().update(context, glanceId)
    }
}
