# Milestone 2: History & Suppression Logic Implementation Plan

## Objective
Implement a unified history feed and smart suppression/prompt logic for bowel movement (BM) tracking.

## User Review Required
> [!IMPORTANT]
> **BM Prompt Logic**: After the initial 24h suppression period (from the first meal), the app will actively ask "Have you had a bowel movement?" if none have been logged in the last 24 hours.

## Tasks
1. **Data Layer Updates**:
   - `MealDao`: Fetch earliest meal timestamp.
   - `BowelMovementDao`: Fetch most recent BM timestamp.
2. **ViewModel Implementation**:
   - `unifiedFeed` Flow: Combine Meals and BMs sorted chronologically.
   - `shouldAskAboutBM` Logic: 24h suppression from first meal, 24h interval from last BM.
3. **UI Implementation**:
   - BM Prompt: Banner/Card asking about past BMs when `shouldAskAboutBM` is true.
   - Unified History: List displaying all entry types with distinct visuals.
4. **Testing**:
   - Unit tests for suppression logic.
   - Interaction tests for feed order and prompt visibility.

## Verification Plan
### Automated Tests
- Unit tests for `shouldAskAboutBM` states.
- Interaction tests for UI prompt suppression and feed sorting.
### Manual Verification
- Verify prompt appearance after 24h of "first meal" and no BMs.
