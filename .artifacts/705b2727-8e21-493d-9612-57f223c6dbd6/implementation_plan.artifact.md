# Implementation Plan - Milestone 2: History & Suppression Logic

This plan focuses on implementing a unified history feed and the smart suppression/prompt logic for bowel movement tracking.

## User Review Required

> [!IMPORTANT]
> **BM Prompt Logic**: After the initial 24h suppression period (from the first meal), the app will actively ask "Have you had a bowel movement?" if none have been logged in the last 24 hours.

> [!TIP]
> **Configurability**: While Milestone 2 focuses on the hardcoded 24h interval, Milestone 3 will introduce settings to adjust this interval (12h - 48h or disabled).

## Proposed Changes

### Data Layer

#### [MODIFY] [MealDao.kt](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/app/src/main/java/ch/schmidlins/mealdiary/data/dao/MealDao.kt)
- Add a query to fetch the timestamp of the earliest meal entry.

#### [MODIFY] [BowelMovementDao.kt](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/app/src/main/java/ch/schmidlins/mealdiary/data/dao/BowelMovementDao.kt)
- Add a query to fetch the timestamp of the most recent BM entry.

### View Model

#### [MODIFY] [MealViewModel.kt](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/app/src/main/java/ch/schmidlins/mealdiary/ui/MealViewModel.kt)
- Implement a `unifiedFeed` Flow that combines Meals and Bowel Movements.
- Implement logic for `shouldAskAboutBM`:
    - `false` if first meal was < 24h ago.
    - `true` if first meal was > 24h ago AND last BM was > 24h ago (or doesn't exist).
- Expose `timeSinceLastBM` state.

### UI Layer

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/app/src/main/java/ch/schmidlins/mealdiary/MainActivity.kt)
- Add a "BM Prompt" section (e.g., a card or banner) that appears when `shouldAskAboutBM` is true.
- Refactor the feed to use a unified list with chronological sorting.

## Verification Plan

### Automated Tests
- **Unit Tests**:
    - Verify `shouldAskAboutBM` is `false` if first meal is < 24h old.
    - Verify `shouldAskAboutBM` is `true` if first meal is > 24h old and no BM exists.
    - Verify `shouldAskAboutBM` becomes `false` immediately after a BM is logged.
- **Interaction Tests**:
    - Verify the BM Prompt UI is not present in the "suppression" phase.
    - Verify the unified feed order.

### Manual Verification
- Deploy to emulator.
- Log a meal. Verify no BM prompt appears.
- Log a BM. Verify it appears in the feed.
- Adjust system clock forward by 25 hours. Verify the app *asks* about the past BM.

> [!IMPORTANT]
> All manual verification steps in this milestone are slated to be fully automated in the next milestone once the infrastructure is stable.
