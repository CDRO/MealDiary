# Implementation Plan - Milestone 9: Recurring Meal Suggestions

This plan focuses on implementing an autocomplete feature for meal descriptions to further reduce the hurdle of logging repetitive food items.

## User Review Required

> [!IMPORTANT]
> **Suggestion Criteria**: Meals will only be suggested if they have been logged **at least 5 times** in the past. This ensures the suggestion list remains relevant and uncluttered.
> **UX Integration**: Suggestions will appear in a dropdown or horizontally scrollable list above the keyboard while typing in the "What did you eat?" field.

## Proposed Changes

### Data Layer

#### [MODIFY] [MealDao.kt](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/app/src/main/java/ch/schmidlins/mealdiary/data/dao/MealDao.kt)
- Add a query to fetch meal descriptions that appear at least 5 times.
- `SELECT description FROM meals GROUP BY description HAVING COUNT(*) >= 5`

### View Model

#### [MODIFY] [MealViewModel.kt](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/app/src/main/java/ch/schmidlins/mealdiary/ui/MealViewModel.kt)
- Expose a `recurringMeals: LiveData<List<String>>` flow.
- Filter the list based on the current `mealText` input.

### UI Layer

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/app/src/main/java/ch/schmidlins/mealdiary/MainActivity.kt)
- Implement an autocomplete dropdown or a suggestion row above the text field.
- When a suggestion is tapped, the text field is populated instantly.

## Verification Plan

### Automated Tests
- **Unit Tests**:
    - Verify the DAO query correctly identifies descriptions with >= 5 entries.
    - Verify the ViewModel correctly filters suggestions based on input prefix.
- **Robolectric Tests**:
    - Verify that suggestions appear when typing a matching prefix.
    - Verify that tapping a suggestion updates the text field.

### Manual Verification
- Deploy to emulator.
- Log "Oatmeal" 5 times.
- Start typing "Oat" in the input field.
- Verify "Oatmeal" appears as a suggestion.
- Tap it and verify the text is completed.
