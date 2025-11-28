# Troubleshooting

Common issues and solutions when using ComposablePreviewScanner.

---

## java.io.FileNotFoundException (File name too long)

**Error:**
```
java.io.FileNotFoundException: ... (File name too long)
```

**Explanation:**

This is more likely to happen when using Paparazzi. By default, Paparazzi additionally prefixes the screenshot file named internally instead of just using the `name` we pass to its `snapshot()` method, and this results sometimes in the final screenshot file name being longer than allowed.

That is why it is recommended to [set a custom SnapshotHandler](paparazzi-guide.md#step-3-create-custom-record-and-verify-snapshothandlers) in the Paparazzi Test Rule.

**Solutions:**

But if you're still experiencing such issues, consider:

1. Using `AndroidPreviewScreenshotIdBuilder` methods like `ignoreIdFor()` or `overrideDefaultIdFor()` to shorten the given name. See [Advanced Usage - Screenshot File Names](advanced-usage.md#screenshot-file-names).
2. Avoid `AndroidPreviewScreenshotIdBuilder` and use `paparazzi.snapshot {}` instead of `paparazzi.snapshot(name = screenshotId)`

---

## java.lang.IllegalArgumentException: Generated method name contains invalid characters

**Explanation:**

Some libraries restrict the characters allowed in filenames and may alter the provided screenshot name (e.g., Paparazzi 1.3.5+ like reported in this issue [here](https://github.com/cashapp/paparazzi/issues/1963)).

This is especially problematic when the `TestParameterInjector` test runner is used.

**Solution:**

To avoid issues, `ComposablePreviewScanner`s ScreenshotIdBuilders should be used with the standard JUnit4 `Parameterized` test runner, and invalid characters should be encoded manually if needed, for example:

```kotlin
AndroidPreviewScreenshotIdBuilder(preview)
    ...
    .build()
    .replace("<", "%3C")
    .replace(">", "%3E")
    .replace("?", "%3F")
```

> [!NOTE]
> When using `TestParameterInjector` invalid characters may persist.
> Use the JUnit4 `Parameterized` test runner for valid results.

---

## Cannot inline bytecode built with JVM target 17

**Error:**
```text
Task compileDebugUnitTestKotlin FAILED
e: file:... Cannot inline bytecode built with JVM target 17 into bytecode that is being built with JVM target 11. Specify proper '-jvm-target' option.
```

**Solution:**

You need to upgrade the 'jvmTarget' in the gradle build file of the module where it is failing like this:

```kotlin
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17 // or higher
    }
}
```

---

## GooglePlayServicesMissingManifestValueException

**Error:**
```text
com.google.android.gms.common.GooglePlayServicesMissingManifestValueException: A required meta-data tag in your app's AndroidManifest.xml does not exist.  You must have the following declaration within the <application> element:     <meta-data android:name="com.google.android.gms.version" android:value="@integer/google_play_services_version" />
```

**Solution:**

If you are using 'GoogleMap' composable, then you will need to wrap your composable preview content with `CompositionLocalProvider(LocalInspectionMode provides true)`

```kotlin
@Preview
@Composable
internal fun MapScreenPreview() {
    CompositionLocalProvider(LocalInspectionMode provides true) {
        MapScreen()
    }
}
```

---

[← Back to Main README](../README.md)
