# Milestone 11: In-App Widget Dashboard Implementation Plan

## Objective
Implement a swipeable second pane in the main UI that serves as a customizable dashboard for in-app widgets (Stats, Trends, Quick Actions).

## Tasks
1. Integrate `HorizontalPager` into `MainActivity` to allow swiping between the Feed and the Dashboard.
2. Build a **Widget Framework**:
    - Define a `Widget` interface or sealed class.
    - Implement base widget containers with configurable order.
3. Port existing statistics to the Widget Framework:
    - **Weight Trend Widget**.
    - **BM Frequency Widget**.
    - **Top Foods Widget**.
4. Add customization:
    - Allow users to reorder widgets (potentially simple list for now).
    - Allow toggling widget visibility.
5. Ensure widgets are interactive (e.g., tapping a widget opens its detail view or settings).
6. Write tests for pager navigation and widget rendering.

## Verification
- Swiping right from the main feed opens the Dashboard.
- Dashboard displays stats in a modular "widget" style.
- Customization options (if implemented) correctly update the dashboard layout.
