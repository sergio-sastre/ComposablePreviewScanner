# Contributing to Composable Preview Scanner

Thank you for your interest in contributing to Composable Preview Scanner! We welcome contributions from the community to help make this library better for everyone.

This project is developed using **Test-Driven Development (TDD)** and maintains strict **binary compatibility** using **Metalava**.

## How to Contribute

### Reporting Issues
If you find a bug or have a feature request, please [open an issue](https://github.com/sergio-sastre/ComposablePreviewScanner/issues). When reporting a bug, please include:
- A clear and descriptive title.
- Steps to reproduce the issue.
- Expected vs. actual behavior.
- Relevant environment details (OS, JDK version, Compose version, etc.).

### Submitting Pull Requests
1. **Fork the repository** and create your branch from `master`.
2. **Implement your changes.** If you are adding a new feature or fixing a bug, please include corresponding tests. We follow a TDD approach.
3. **Ensure the build passes.** Run the relevant Gradle tasks (see [Verification](#verification) below).
4. **Follow the code style.** Maintain consistency with the existing codebase.
5. **Update documentation** if your changes introduce new APIs or change existing behavior.
6. **Submit a Pull Request** with a clear description of your changes and a link to the relevant issue.

## Development Setup
- **JDK 17+** is required.
- **Android Studio** (latest stable version) is recommended for development.

## Verification
To maintain high quality, all PRs must pass the following custom Gradle tasks, which are also automatically executed in our **Continuous Integration (CI)** pipeline. We recommend running them locally before submitting your changes:

| Task | Description |
| :--- | :--- |
| `./gradlew :tests:testApi` | Runs API logic tests. |
| `./gradlew :tests:testSourceSets` | Runs SourceSet logic tests. |
| `./gradlew :tests:paparazziPreviews` | Runs Paparazzi integration tests. |
| `./gradlew :tests:roborazziPreviews` | Runs Roborazzi integration tests. |

### Binary Compatibility
We use **Metalava** to track API changes and prevent breaking binary compatibility for our users. If your changes modify public APIs, ensure you've considered the impact and updated the API tracking files accordingly.

## Code of Conduct
Please be respectful and professional in all interactions within this project.

## License
By contributing, you agree that your contributions will be licensed under the project's [LICENSE](https://github.com/sergio-sastre/ComposablePreviewScanner/blob/main/LICENSE).
