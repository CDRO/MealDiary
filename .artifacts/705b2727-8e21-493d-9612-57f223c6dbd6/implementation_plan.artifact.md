# Implementation Plan - Milestone 4: Weight Tracker & Suggestion Engine

This plan introduces weight tracking capabilities and a smart suggestion engine that triggers after one week of app usage.

## User Review Required

> [!IMPORTANT]
> **Weight Tracking Opt-in**: By default, weight tracking is disabled. It will be suggested via a card in the main feed exactly 7 days after the user logs their first meal.
> **Multi-Entry Support**: Users can log their weight multiple times per day. Each entry is timestamped.

## Proposed Changes

### Data Layer

#### [MODIFY] [UserPreferencesRepository.kt](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/app/src/main/java/ch/schmidlins/mealdiary/data/repository/UserPreferencesRepository.kt)
- Add `IS_WEIGHT_TRACKING_ENABLED` preference.
- Add `WEIGHT_SUGGESTION_DISMISSED` preference.

### View Model

#### [MODIFY] [MealViewModel.kt](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/app/src/main/java/ch/schmidlins/mealdiary/ui/MealViewModel.kt)
- Add `WeightItem` to `FeedItem` sealed class.
- Update `unifiedFeed` to include `WeightEntry` items.
- Expose `isWeightTrackingEnabled` state.
- Expose `shouldShowWeightSuggestion`:
    - `true` if weight tracking is disabled AND NOT dismissed AND first meal was > 7 days ago.
- Implement `addWeightEntry(weight: Double)`.

### UI Layer

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/app/src/main/java/ch/schmidlins/mealdiary/MainActivity.kt)
- Show a **Suggestion Card** for weight tracking when `shouldShowWeightSuggestion` is true.
- If weight tracking is enabled:
    - Add a "Log Weight" button (or a numeric input field) to the main screen.
    - Display weight entries in the unified feed with a distinct icon (⚖️).
- Refactor the input section to accommodate weight entry (keeping it zero-hurdle).

#### [MODIFY] [SettingsActivity.kt](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/app/src/main/java/ch/schmidlins/mealdiary/ui/settings/SettingsActivity.kt)
- Add a toggle for "Weight Tracking".

## Verification Plan

### Automated Tests
- **Unit Tests**:
    - Verify `shouldShowWeightSuggestion` logic (0 days, 6 days, 8 days).
    - Verify `WeightEntry` items are correctly sorted and displayed in the unified feed.
- **Interaction Tests**:
    - Verify the suggestion card appears after simulated 7 days.
    - Verify logging a weight entry adds it to the feed correctly.

### Manual Verification
- Deploy to emulator.
- Adjust system clock forward by 8 days after first meal. Verify suggestion appears.
- Enable weight tracking in settings. Verify the "Log Weight" UI appears.
- Log weight and verify it shows up in the feed with the correct timestamp.
