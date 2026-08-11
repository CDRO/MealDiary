# Walkthrough - Milestone 8: Data Export (CSV)

Milestone 8 added a critical data portability feature, allowing users to export their entire history of meals, bowel movements, and weight entries to a CSV file.

## Changes Made

### 1. Unified CSV Generation
- Implemented `getCSVData()` in `MealViewModel`.
- Aggregates data from `Meal`, `BowelMovement`, and `WeightEntry` repositories.
- Format: `Timestamp, Type, Value, Notes`.
- Robust escaping for descriptions and notes containing quotes or commas.

### 2. Export UI & Storage Integration
- Added an **"Export Data to CSV"** button in the Settings screen.
- Integrated **Storage Access Framework (SAF)** via `ActivityResultContracts.CreateDocument`.
- The app allows users to choose the save location (Downloads, Cloud storage, etc.) and handles the asynchronous file writing on a background thread (`Dispatchers.IO`).

### 3. High-Speed Quality Assurance
- **Unit Tests**: Added tests in `MealViewModelTest` to verify that the CSV string is generated with correct headers and escaped values.
- **Robolectric Tests**: Added `SettingsActivityTest` to verify the "Export" button is present and functional in the UI.
- All 26 unit and interaction tests are passing in seconds on the JVM.

## How to Verify
1.  Launch the app and log some data (Meal, BM, Weight).
2.  Go to **Settings** (Gear icon in top bar).
3.  Tap **Export Data to CSV**.
4.  The system file picker will appear. Select a location and tap **Save**.
5.  Open the file in a spreadsheet app or text editor to see your data.

## Next Steps
In the final milestone (**Milestone 9: Final Polish**), we will refine the transitions, add haptic feedback to all logging actions, and perform a final verification run.
