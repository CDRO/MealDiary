# Implementation Plan - Milestone 3: Reminders & Widgets

This plan focuses on enhancing user engagement and accessibility through home screen widgets and personalized reminders.

## User Review Required

> [!IMPORTANT]
> **Widget Integration**: We will use **Jetpack Glance** for the home screen widget. It provides a Compose-like API for building widgets.
> **Settings Persistence**: We will introduce **Jetpack DataStore** to persist user preferences (reminder times, BM interval).

> [!WARNING]
> **Notification Permissions**: On Android 13+ (API 33), the app must explicitly request notification permissions. This will be integrated into the main flow.

## Proposed Changes

### Dependencies
- Add `androidx.glance:glance-appwidget` and `androidx.glance:glance-material3`.
- Add `androidx.work:work-runtime-ktx`.
- Add `androidx.datastore:datastore-preferences`.

### Data Layer
- **[NEW] [UserPreferencesRepository.kt](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/app/src/main/java/ch/schmidlins/mealdiary/data/repository/UserPreferencesRepository.kt)**: Manage user settings like BM prompt interval (12h-48h) and meal reminder times.

### Background Work
- **[NEW] [ReminderWorker.kt](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/app/src/main/java/ch/schmidlins/mealdiary/service/ReminderWorker.kt)**: Worker to trigger system notifications for meals.
- **[NEW] [ReminderManager.kt](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/app/src/main/java/ch/schmidlins/mealdiary/service/ReminderManager.kt)**: Logic to schedule `WorkManager` tasks based on user preferences.

### Widgets
- **[NEW] [MealDiaryWidget.kt](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/app/src/main/java/ch/schmidlins/mealdiary/ui/widget/MealDiaryWidget.kt)**: A home screen widget with quick-action buttons for "Log Meal" and "Log BM".
- **[NEW] [MealDiaryWidgetReceiver.kt](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/app/src/main/java/ch/schmidlins/mealdiary/ui/widget/MealDiaryWidgetReceiver.kt)**: Receiver for updating the Glance widget.

### UI Layer
- **[NEW] [SettingsActivity.kt](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/app/src/main/java/ch/schmidlins/mealdiary/ui/settings/SettingsActivity.kt)** (or a new Screen in MainActivity): A simple UI to configure meal times and the BM prompt interval.
- **[MODIFY] [MealViewModel.kt](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/app/src/main/java/ch/schmidlins/mealdiary/ui/MealViewModel.kt)**: Incorporate settings into the `shouldAskAboutBM` logic.

## Verification Plan

### Automated Tests
- **Unit Tests**:
    - Verify `ReminderManager` correctly calculates the next scheduled time for a given preference.
    - Verify `shouldAskAboutBM` respects the user-configured interval (12h, 48h, etc.).
- **Interaction Tests**:
    - **Glance Unit Tests**: Verify that widget buttons trigger the expected actions.
    - **UI Tests**: Test the settings update flow.

### Manual Verification
- Deploy the app to the emulator.
- Add the "MealDiary Widget" to the home screen.
- Tap "Log Meal" on the widget and verify it appears in the app's history.
- Change the meal reminder time in settings and verify (via logs/notification simulation) that the work is re-scheduled.
- Change the BM prompt interval to 12h and verify the prompt appears earlier than 24h.
