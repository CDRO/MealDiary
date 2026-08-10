# MealDiary Master Plan

## Project Vision
A "zero-hurdle" meal and bowel movement diary with smart heuristic analysis, weight tracking, and personalized reminders.

## Milestones

### Milestone 1: Core Infrastructure
- **Objective**: Establish the technical foundation.
- **Tasks**:
    - Setup Jetpack Compose.
    - Setup Room Database with models for `Meal`, `BowelMovement`, and `WeightEntry`.
    - Setup Native C++ (CMake) build support.
    - Implement basic "Zero Hurdle" logging UI.
    - Write baseline unit and interaction tests.

### Milestone 2: History & Suppression Logic
- **Objective**: Data persistence and smart visibility.
- **Tasks**:
    - Feed UI showing all entries.
    - Implement 24-hour suppression for BM tracking.
    - Room migrations/schemas for future analysis.

### Milestone 3: Reminders & Widgets
- **Objective**: Reduce logging hurdles via OS integration.
- **Tasks**:
    - Home screen widgets (Glance/RemoteViews).
    - Personalized meal-time reminders (WorkManager).
    - Settings UI for meal-to-time mapping.

### Milestone 4: Weight Tracker & Suggestion Engine
- **Objective**: Incremental feature rollout.
- **Tasks**:
    - Weight entry UI and history.
    - Suggestion logic (trigger after 7 days of use).

### Milestone 5: Heuristic Analysis (C++ Native)
- **Objective**: Data-driven insights.
- **Tasks**:
    - Native C++ correlation engine.
    - Pattern matching (Food vs. BM timing).
    - Insight UI.

### Milestone 6: Final Polish
- **Objective**: Quality assurance and UX refinement.
- **Tasks**:
    - Haptics, animations.
    - Full test suite execution and verification.
