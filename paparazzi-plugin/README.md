# Composable Preview Paparazzi Plugin

A Gradle plugin that automatically generates and runs Paparazzi test files for your Composable Previews using
the [ComposablePreviewScanner](https://github.com/sergio-sastre/ComposablePreviewScanner) library.

## Intended Purpose

Having a Gradle plugin helps you avoid duplicating boilerplate code across multiple modules where you want to generate Paparazzi screenshot tests from `@Preview` composables.
This approach is similar in spirit to what Roborazzi provides, but tailored specifically for Paparazzi.

That said, please note that this plugin is intended primarily as a showcase.
It is not designed to support use cases specific to individual projects.

The author does not plan to provide support or accept feature request for project-specific adaptations.
However, customizing the plugin to fit your needs should be straightforward: simply modify the implementation of the `GenerateComposablePreviewPaparazziTestTask` class to suit your requirements.

## Features

- 🎯 **Automatic Test Generation**: Generates Paparazzi test files for all your `@Preview` annotated
  composables
- 🔧 **Configurable**: Customize package scanning, test class names, and more
- 📱 **Configuration**: Proper configuration based on `@Preview` parameters. Supports:
    - `device`
    - `locale`
    - `fontSize`
    - `uiMode`
    - `width`
    - `height`
    - `showBackground`
    - `backgroundColor`
    - `showSystemUi`
- 🔒 **Private Preview Support**: Option to include private `@Preview`

## Usage

Copy-paste this gradle plugin folder into your project, and add
```gradle
includeBuild("paparazzi-plugin")
```
into your project `settings.gradle.kts` file

### Apply the Plugin

```kotlin
plugins {
    id("app.cash.paparazzi")
    id("io.github.sergio-sastre.composable-preview-scanner.paparazzi") version "1.0.0"
}
```

### Configure the Plugin

```kotlin
composablePreviewPaparazzi {
    enable = true
    packages = listOf("com.example.ui.previews")
    includePrivatePreviews = false
    testClassName = "MyGeneratedPaparazziTests"
    testPackageName = "com.example.generated.tests"
}
```

### Add the necessary dependencies

These are necessary, because the generated test file requires imports from them:
```gradle
dependencies {
  testImplementation("io.github.sergio-sastre.ComposablePreviewScanner:android:0.6.1")
  testImplementation("com.google.testparameterinjector:test-parameter-injector:1.16")
}
```

### Configuration Options

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enable` | `Boolean` | `false` | Whether to enable test generation |
| `packages` | `List<String>` | `[]` | Package names to scan for `@Preview` composables |
| `includePrivatePreviews` | `Boolean` | `false` | Include private preview functions |
| `testClassName` | `String` | `"GeneratedComposablePreviewPaparazziTests"` | Name of the generated test class |
| `testPackageName` | `String` | `"generated.paparazzi.tests"` | Package name for generated tests |

### Run the Generated Tests
By running any of the following gradle tasks, the tests will be generated and then executed:

```bash
# Record screenshots
./gradlew recordPaparazziDebug

# Verify screenshots
./gradlew verifyPaparazziDebug
```

### Generate Tests

In case you only want to generate the tests without running them, execute the generation task:

```bash
./gradlew generateComposablePreviewPaparazziTests
```

This will generate the corresponding test file in `src/test/` which will be automatically
included in your test source set.

## License

This plugin is released under the same license as the ComposablePreviewScanner project.