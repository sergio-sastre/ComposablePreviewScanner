# Glance Previews Support

You can find executable examples in this repo with different screenshot libraries:
- [Roborazzi](../tests/src/test/java/sergio/sastre/composable/preview/scanner/tests/roborazzi/RoborazziGlanceComposablePreviewInvokeTests.kt)
- [Paparazzi](../tests/src/test/java/sergio/sastre/composable/preview/scanner/tests/paparazzi/PaparazziGlanceComposablePreviewInvokeTests.kt)
- [Android-Testify](../tests/src/androidTest/java/sergio/sastre/composable/preview/scanner/screenshots/AndroidTestifyGlanceComposablePreviewScannerInstrumentationTest.kt) (Check the [Instrumentation Screenshot Tests Guide](instrumentation-guide.md) before)

## Setup Steps

To write screenshot tests for Glance previews you have to:

### 1. Add the `:glance` dependency

Add the ComposablePreviewScanner Glance module to your dependencies:

```kotlin
testImplementation("io.github.sergio-sastre.ComposablePreviewScanner:glance:<version>")
```

This contains some utils to correctly set the size of the Composable as well as the size of the device. Take a look at the executable examples above to see how they are used.

### 2. Ensure `targetSdk` is set in your gradle file

Ensure `targetSdk` is set to any value in the gradle file<sup>1</sup>. Otherwise you can see some discrepancies between the Preview and the generated screenshot file for Glance `@Preview`s without `widthDp`.

<sup>1</sup> Unfortunately, Paparazzi is not able to always render screenshots accurately for Glance `@Preview`s without `widthDp`.

### 3. Write the Parameterized screenshot test

Write the Parameterized screenshot test like in the examples above. Use `GlanceComposablePreviewScanner` to scan Glance previews.

---

[← Back to Main README](../README.md)
