# Milestone 10: Extensive BM Logging Implementation Plan

## Objective
Implement an "Extensive Mode" for Bowel Movements, allowing users to track consistency (Bristol Scale), pain levels, and duration.

## Tasks
1. Update `BowelMovement` entity with new fields: `consistency`, `painLevel`, `durationMinutes`.
2. Increment Room Database version to 2 and implement migration.
3. Implement `updateBowelMovement` in `MealViewModel`.
4. Create a `BMDetailDialog` in Compose to capture extensive data.
5. Update the Main Feed to show icons/details for extensive BM logs.
6. Make feed items clickable to edit existing BM entries.
7. Write Unit and Robolectric tests for the new logic and UI.

## Verification
- Users can log a BM with consistency and pain level.
- Database persists the new fields across app restarts.
- The UI displays the extensive data correctly in the history.
