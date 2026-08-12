# Contributing to MealDiary

We welcome contributions that add high value to the project. However, please be aware that this project uses **automated AI-led governance**.

## 🛑 Critical Rules

1.  **AI-Led Review:** Every Pull Request is reviewed by a methodical AI Senior Engineer.
2.  **Strict File Protection:** PRs touching any of the following files will be **automatically rejected**:
    *   `.geminirules`
    *   `.ai_state.json`
    *   `CONTRIBUTING.md`
    *   `docs/artifacts/milestones/**/*`
3.  **High Value Only:** We only accept features or fixes that significantly improve the "zero-hurdle" UX or app performance.
4.  **Documentation Requirement:** Every PR **must** include its own documentation folder:
    *   `docs/artifacts/contributions/cXX/implementation_plan.md`
    *   `docs/artifacts/contributions/cXX/tasks.md`
    *   *(Where XX is the ID of your PR)*

## 🛠 Contribution Process

1.  **Open an Issue:** Describe the feature or fix you want to implement.
2.  **Create a Branch:** Use a descriptive name (e.g., `feature/my-new-widget`).
3.  **Implement Changes:** Follow the project's architecture (MVVM, Clean Architecture).
4.  **Add Documentation:** Create the required `implementation_plan.md` and `tasks.md` in the contributions folder.
5.  **Open a Pull Request:** Ensure your PR ID matches your documentation folder.

## ⚠️ Warning: Hostile Reviews

If you are not the primary maintainer (`tizian.schmidlin@gmail.com`), the AI agent will perform a **hostile review**. This means:
*   Your code will be scrutinized for even the smallest architectural deviations.
*   Lack of comprehensive unit/Robolectric tests will result in immediate rejection.
*   Any attempt to bypass governance rules will be blocked.

If your PR passes these strict checks, the agent will mention `@CDRO` for final human approval.
