# Implementation Plan - Milestone 12: Project Governance & Infrastructure

This plan outlines the addition of project documentation, contribution guidelines, automated product page generation, and strict governance rules for AI-led PR reviews.

## User Review Required

> [!IMPORTANT]
> **Hostile Review Logic**: The AI agent will now proactively check for PRs from external contributors. It will strictly reject any changes to core configuration files (`.geminirules`, `.ai_state.json`, `CONTRIBUTING.md`) or milestone artifacts. External contributions must be "high value" and accompanied by documentation in `docs/artifacts/contributions/cXX`.

> [!NOTE]
> **Product Page**: A GitHub Action will be implemented to generate a static "Product Page" accessible via GitHub Pages. It will include a rendered README, a menu for browsing contributions/milestones, and a link to the latest APK.

## Proposed Changes

### Project Documentation
#### [NEW] [README.md](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/README.md)
- Project vision: "Zero-hurdle" logging.
- Tech stack overview.
- Links to milestones and product page.

#### [NEW] [CONTRIBUTING.md](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/CONTRIBUTING.md)
- Process for opening PRs.
- Requirement for `docs/artifacts/contributions/cXX/implementation_plan.md` and `tasks.md`.
- Warning about the automated AI "hostile review" for external code.

### Governance & Security
#### [MODIFY] [.geminirules](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/.geminirules)
- Add "Governance & External Contributions" section.
- Instruction to check `gh pr list` for authors other than `tizian.schmidlin@gmail.com`.
- Hard rejection rules for core files.
- Command to mention `@CDRO` upon validation success.

### Automation (CI/CD)
#### [NEW] [.github/workflows/product-page.yml](file:///C:/Users/tizia/AndroidStudioProjects/MealDiary/.github/workflows/product-page.yml)
- Trigger on PR merges to `main`.
- Generate HTML from Markdown artifacts.
- Deploy to the `gh-pages` branch.

## Verification Plan

### Automated Tests
- Trigger the GitHub Action by pushing to a branch and merging.
- Verify the action completes successfully and publishes to GitHub Pages.

### Manual Verification
- Attempt a mock review of a simulated "external" PR to verify `.geminirules` rejection logic.
- Browse the generated product page to ensure all links (README, CONTRIBUTING, Milestones) work correctly.
