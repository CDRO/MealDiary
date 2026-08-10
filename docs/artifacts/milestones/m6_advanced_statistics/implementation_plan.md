# Milestone 6: Advanced Statistics Implementation Plan

## Objective
Extract quantitative insights from the logged data and visualize them using charts.

## Tasks
1. Implement logic in `MealViewModel` to calculate:
    - Average BM frequency.
    - Weight trends (delta and history).
    - Top logged foods.
2. Build custom visualization components using Compose `Canvas` or basic layouts:
    - **Weight Trend Chart**: A line graph.
    - **BM Frequency Chart**: A bar graph or summary card.
3. Integrate statistics into the `DataOverviewScreen`.
4. Write unit tests for the statistics calculation logic.
5. Verify the visual representation with sample data.

## Verification
- Statistics are calculated accurately based on the logged data.
- Charts are displayed correctly on the Overview screen.
- Weight tracking toggle correctly influences statistics visibility.
