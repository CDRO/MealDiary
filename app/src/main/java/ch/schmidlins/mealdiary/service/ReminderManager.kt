package ch.schmidlins.mealdiary.service

import android.content.Context
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

class ReminderManager(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    fun scheduleMealReminders() {
        // Example: Schedule reminders at 8am, 14pm, 8pm
        scheduleReminder("8am", 8, 0)
        scheduleReminder("2pm", 14, 0)
        scheduleReminder("8pm", 20, 0)
    }

    private fun scheduleReminder(name: String, hour: Int, minute: Int) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        val initialDelay = target.timeInMillis - now.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "reminder_$name",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelAllReminders() {
        workManager.cancelAllWorkByTag("reminder")
    }
}
