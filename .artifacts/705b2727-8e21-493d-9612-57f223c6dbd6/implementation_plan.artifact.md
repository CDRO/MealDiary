# Implementation Plan - Milestone 8: CSV Data Export

This plan outlines the implementation of a data export feature, allowing users to save their diary entries as a CSV file using the Storage Access Framework (SAF).

## User Review Required

> [!IMPORTANT]
> **Export Format**: We will provide a single CSV file containing all entries (Meals, Bowel Movements, and Weight) with a "Type" column to distinguish them, or separate files? I am proposing a **single unified CSV** for simplicity, with columns: `Timestamp`, `Type`, `Description/Weight`, `Notes`.

## Proposed Changes

### 1. Data Layer

#### [MODIFY] [MealViewModel.kt](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/app/src/main/java/ch/schmidlins/mealdiary/ui/MealViewModel.kt)
- Add a function to generate a CSV string from all existing database entries.
- Format: `Date,Time,Type,Value,Notes`

### 2. UI Layer

#### [MODIFY] [SettingsActivity.kt](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/app/src/main/java/ch/schmidlins/mealdiary/ui/settings/SettingsActivity.kt)
- Add an **"Export Data to CSV"** button.
- Integrate `ActivityResultContracts.CreateDocument` to allow the user to choose a save location.
- Handle writing the CSV string to the selected URI using a `ContentResolver`.

### 3. CSV Generation Logic
- **Meals**: `timestamp, MEAL, description, notes`
- **BMs**: `timestamp, BM, , notes`
- **Weight**: `timestamp, WEIGHT, weight value, unit`

## Verification Plan

### Automated Tests
- **Unit Tests**:
    - Verify the CSV string generation logic handles empty lists.
    - Verify correct escaping of commas in meal descriptions (if any).
- **Robolectric Tests**:
    - Verify that clicking the export button triggers the file picker intent.

### Manual Verification
- Deploy to emulator.
- Log several entries of each type.
- Go to Settings -> Export Data.
- Select a location (e.g., Downloads).
- Open the resulting file on the computer or device and verify the data matches.

---

# Master Plan Update

I will also update `MASTER_PLAN.md` to reflect the new milestone sequence:
- **Milestone 8**: Data Export (CSV)
- **Milestone 9**: Final Polish
