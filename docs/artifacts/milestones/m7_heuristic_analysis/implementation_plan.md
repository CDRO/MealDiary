# Milestone 7: Heuristic Analysis (C++ Native) Implementation Plan

## Objective
Implement a native C++ engine to correlate food keywords with bowel movement timing and display patterns to the user.

## Tasks
1. Extend the C++ engine (`mealdiary-native.cpp`) to handle more complex pattern matching.
2. Implement a scoring system in C++ to identify "accelerators" (foods often followed by a BM within 4 hours).
3. Implement a scoring system in C++ to identify "decelerators" (foods rarely followed by a BM).
4. Update `AnalysisEngine.kt` to pass historical meal and BM data to the native layer.
5. Build an "Insights" section in the `DataOverviewScreen` to display identified patterns.
6. Write native unit tests (if possible) and Kotlin tests to verify correlation accuracy.

## Verification
- C++ engine accurately identifies "Coffee" as an accelerator (based on sample data).
- The "Insights" section displays meaningful correlations.
- JNI overhead is minimal, and analysis doesn't block the UI thread.
