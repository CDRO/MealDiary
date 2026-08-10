# Implementation Plan - Test Optimization & Milestone 6: Advanced Statistics

This plan aims to significantly speed up the test suite and implement quantitative data insights.

## User Review Required

> [!IMPORTANT]
> **Test Speed**: I am proposing to migrate instrumented UI tests (on-device) to JVM-based tests using **Robolectric**. This will allow interaction tests to run on the host machine, reducing execution time from minutes to seconds.
> **Visual Statistics**: We will add a "Statistics" section to the Overview screen featuring charts for weight trends and frequency analysis.

## Proposed Changes

### 1. Test Optimization

#### [MODIFY] [libs.versions.toml](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/gradle/libs.versions.toml)
- Add `robolectric` version and library.

#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/app/build.gradle.kts)
- Add `testImplementation(libs.robolectric)`.
- Enable `includeAndroidResources = true` in `testOptions`.

#### [MOVE] [MealLogTest.kt](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/app/src/androidTest/java/ch/schmidlins/mealdiary/ui/MealLogTest.kt) to `app/src/test/java/ch/schmidlins/mealdiary/ui/MealLogTest.kt`
- Update to run with `@RunWith(RobolectricTestRunner::class)`.
- This converts the on-device interaction test into a high-speed JVM test.

---

### 2. Milestone 6: Advanced Statistics

#### Data Layer
- **[MODIFY] [MealViewModel.kt](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/app/src/main/java/ch/schmidlins/mealdiary/ui/MealViewModel.kt)**:
    - Implement logic to calculate:
        - Average BM frequency (BMs per day over last 7/30 days).
        - Weight delta (difference between latest and first entry).
        - Top 5 most frequent meal descriptions.

#### UI Layer
- **[MODIFY] [MainActivity.kt](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/app/src/main/java/ch/schmidlins/mealdiary/MainActivity.kt)**:
    - Add a "Statistics" tab or section to `DataOverviewScreen`.
    - Implement a **Weight Trend Chart** using Compose `Canvas`.
    - Implement a **BM Distribution** bar chart.

## Verification Plan

### Automated Tests
- **JVM UI Tests**: Run the migrated `MealLogTest` on the host machine.
- **Unit Tests**: Verify the math behind BM frequency and weight trends in `MealViewModelTest`.

### Manual Verification
- Deploy to emulator.
- Navigate to Overview and verify that the new charts represent the logged data accurately.
- Verify that tests now run significantly faster via `.\gradlew.bat test`.
