# Implementation Plan - MealDiary Roadmap

This plan outlines the steps to define the project features and establish the initial codebase following the strict workflow in `.geminirules`.

## User Review Required

> [!IMPORTANT]
> The scope has significantly increased. Key highlights:
> 1. **Heuristic Analysis Engine**: A native C++ core will be planned for correlating food keywords with Bowel Movement (BM) timing to identify "accelerators" and "decelerators".
> 2. **Home Screen Widgets**: Quick-action buttons for logging to minimize hurdles further.
> 3. **Smart Weight Tracker**: Hidden by default, suggested after 7 days, supports multi-entry.
> 4. **Flexible Reminders**: Users map specific meals (e.g., "Breakfast") to specific times.

## Proposed Features

- **Instant Logging**: One-tap entry via App and Widgets.
- **Smart Tracking & Logic**:
    - **BM Tracker**: Visible only after 24h of use.
    - **Heuristic Engine**: Analyzes meal text vs. BM timing. C++ implementation for scalability/performance.
- **Configurable Reminders**:
    - Per-meal time configuration (Breakfast, Lunch, Dinner, Snacks).
    - `WorkManager` for reliable background scheduling.
- **Weight Tracking**:
    - Multi-entry per day (timestamped).
    - Opt-in/Suggestion logic (1-week delay for suggestion).
- **Local Persistence**: Room DB for all entries.

## Proposed Roadmap (Milestones)

### Milestone 1: Core Infrastructure
- Project setup with Jetpack Compose, Room, and **Native C++ (CMake)** support.
- Data models for `Meal`, `BowelMovement`, and `WeightEntry`.
- Basic "Zero Hurdle" UI.

### Milestone 2: History & Suppression Logic
- Feed showing all entry types.
- 24-hour suppression for BM tracking.
- Initial Room migrations/schemas for performance analysis.

### Milestone 3: Reminders & Widgets
- **Widgets**: Implementation of `Glance` or `RemoteViews` for quick logging.
- **Reminders**: Configurable meal-time mapping UI and `WorkManager` integration.

### Milestone 4: Weight Tracker & Suggestion Engine
- Weight entry UI and history.
- "1-week usage" logic to trigger the weight tracker suggestion.

### Milestone 5: Heuristic Analysis (C++ Native)
- Implementation of the correlation engine in C++.
- UI for showing "Food Patterns" (e.g., "Coffee usually precedes a BM within 30 mins").

### Milestone 6: UX Polishing & Robustness
- Haptic feedback, animations, and full test suite execution.

## Verification Plan

### Automated Tests
- **Unit Tests**: JVM-based tests for all business logic, suppression timers, and suggestion triggers.
- **Interaction Tests**: Instrumented or Robolectric tests for all UI flows and Widget interactions.
- **C++ Tests**: Native unit tests for the heuristic analysis engine.
- **Performance**: Monitor and optimize test execution time; ensure the suite remains lean and fast.

### Manual Verification
- Test widget logging from the home screen.
- Verify weight suggestion appears exactly after 7 days of first log.
- Verify analysis results match expected patterns from dummy data.
