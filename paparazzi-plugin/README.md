# Composable Preview Paparazzi Plugin

A Gradle plugin that automatically generates and runs Paparazzi test files for your Composable Previews using
the [ComposablePreviewScanner](https://github.com/sergio-sastre/ComposablePreviewScanner) library.

## Intended Purpose

Having a Gradle plugin helps you avoid duplicating boilerplate code across multiple modules where you want to generate Paparazzi screenshot tests from `@Preview` composables.
This approach is similar in spirit to what Roborazzi provides, but tailored specifically for Paparazzi.

That said, please note that this plugin is intended primarily as a showcase.
It is not designed to support use cases specific to individual projects.

The author does not plan to provide support or accept feature requests for project-specific adaptations.
However, customizing the plugin to fit your needs should be straightforward: simply modify the implementation of the `GenerateComposablePreviewPaparazziTestTask` class to suit your requirements.

## Features

- 🎯 **Automatic Test Generation**: Generates Paparazzi test files for all your `@Preview` annotated
  composables
- 🔧 **Configurable**: Customize package scanning, test class names, and more
- 📱 **Configuration**: Proper configuration based on `@Preview` parameters. Supports:
    - `device`
    - `locale`
    - `fontScale`
    - `uiMode`
    - `width`
    - `height`
    - `showBackground`
    - `backgroundColor`
    - `showSystemUi`
    - `apiLevel`
- 🔒 **Private Preview Support**: Option to include private `@Preview`

## Usage

Copy-paste this gradle plugin folder into your project, and add the following into your project `settings.gradle.kts` file
```gradle
includeBuild("paparazzi-plugin")
```

### Apply the Plugin

Add this to the gradle file of the module you want to apply this plugin

```kotlin
plugins {
    id("app.cash.paparazzi")
    id("io.github.sergio-sastre.composable-preview-scanner.paparazzi-plugin")
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
  testImplementation("io.github.sergio-sastre.ComposablePreviewScanner:android:0.9.0")
  testImplementation("junit:junit:4.13.2")
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
| `generatedTestClassCount` | `Int` | `maxParallelForks` | Number of test classes to split the generated parameterized tests into |

### Parallel execution
Paparazzi (using JUnit 4) runs parameterized tests sequentially within a single class. To speed up execution, this plugin can split your previews into multiple test classes (shards), allowing Gradle to run them in parallel across multiple worker processes.

1. **`generatedTestClassCount`**: Controls how many test classes are generated. Each class will contain a subset of your previews.
2. **`maxParallelForks`**: A standard Gradle property that defines how many worker processes Gradle can start to run test classes in parallel.

For effective parallelism, you should set both, for instance:

```kotlin
// In your module's build.gradle.kts
composablePreviewPaparazzi {
    // ... other config
    // 1. Generate 4 test classes
    generatedTestClassCount = 4 
}

tasks.withType<Test> {
    // 2. Allow Gradle to run up to 4 test classes at the same time
    maxParallelForks = 4 
}
```

> [!IMPORTANT]
> `generatedTestClassCount` defaults to the same value as `maxParallelForks`. Therefore, if you already use `maxParallelForks` during testing, the behavior may change if your tests are non-deterministic—for example, if they depend on the order in which other tests run.
> 
> However, this option does not modify `maxParallelForks` itself. Following [Roborazzi's policy](https://github.com/takahirom/roborazzi/releases/tag/1.53.0), this plugin **never modifies your AGP/Gradle settings** automatically in order to keep one single source of configuration. You must always configure `maxParallelForks` explicitly in your build script to enable parallel processing.

### Run the Generated Tests
By running any of the following gradle tasks, the tests will be generated AND then executed (both):

```bash
# Record screenshots
./gradlew your_module:recordPaparazziDebug

# Verify screenshots
./gradlew your_module:verifyPaparazziDebug
```

> [!NOTE]  
> You can try it out it in this project by replacing `your_module` with `paparazzi-plugin-tests`

### Generate Tests

In case you only want to generate the tests without running them, execute the generation task:

```bash
./gradlew your_module:generateComposablePreviewPaparazziTests
```

This will ONLY generate the corresponding test file in `src/test/kotlin` which will be automatically
included in your test source set.

## Verifying changes
In case you want to adjust the plugin to your specific requirements, this project contains a module that
allows you to easily verify your changes.

Clone this project and adjust the `GenerateComposablePreviewPaparazziTestTask` class.

Then verify your changes by executing:

```bash
./gradlew your_module:recordPaparazziDebug
```

Finally, take a look at the generated test class and screenshots to verify if the outputs are as expected.

## License

This plugin is released under the same license as the ComposablePreviewScanner project.
