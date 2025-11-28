# Compose Multiplatform Previews Support

## Common Previews

You can find executable examples here:
- [Roborazzi](https://github.com/sergio-sastre/ComposablePreviewScanner/blob/master/tests/src/test/java/sergio/sastre/composable/preview/scanner/tests/roborazzi/RoborazziCommonComposablePreviewInvokeTests.kt)
- [Roborazzi via its Gradle plugin](https://github.com/sergio-sastre/roborazzi/tree/droidcon/preview_tests/sample-generate-preview-tests-multiplatform)
- [Paparazzi](https://github.com/sergio-sastre/ComposablePreviewScanner/blob/master/tests/src/test/java/sergio/sastre/composable/preview/scanner/tests/paparazzi/PaparazziCommonComposablePreviewInvokeTests.kt)

> [!NOTE]
> Executable examples with Instrumentation-based screenshot testing libraries are coming soon.

### Overview

Since Compose Multiplatform 1.6.0, JetBrains has added support for `@Preview`s in `common`. ComposablePreviewScanner can also scan such Previews when running on any jvm-target, like:
- Android
- Desktop
- Jvm

ComposablePreviewScanner provides a `CommonComposablePreviewScanner` for that purpose.

### Prerequisites

Assuming that you have:
- some Compose Multiplatform `@Previews` defined in `common`
- some jvm-target module (i.e. Android or Desktop) where you want to run screenshot tests for those `@Previews`. That's because ComposablePreviewScanner only works in jvm-targets for now.

### Setup Steps

Here is how you could also run screenshot tests for those Compose Multiplatform `@Previews` together, for instance, with Roborazzi (would also work with Paparazzi or any Instrumentation-based library).

#### 1. Add `:common` dependency

```kotlin
testImplementation("io.github.sergio-sastre.ComposablePreviewScanner:common:<version>")
```

#### 2. Add an additional Parameterized screenshot test

Add an additional Parameterized screenshot test for these Compose Multiplatform `@Previews`. This is basically the same as in the corresponding [Paparazzi](paparazzi-guide.md), [Roborazzi](roborazzi-guide.md), or [Instrumentation screenshot tests](instrumentation-guide.md) sections, but use `CommonComposablePreviewScanner<CommonPreviewInfo>` and `CommonPreviewScreenshotIdBuilder`.

#### 3. Run the screenshot tests

Run these screenshot tests by executing the corresponding command e.g. for android:

```bash
./gradlew yourModule:recordRoborazziDebug
```

---

## Desktop Previews

You can find [executable examples with Roborazzi here](https://github.com/sergio-sastre/roborazzi/tree/demo/kug_munich_presentation/sample-compose-desktop-multiplatform).

### Background

As we've seen in the "How it works" section of the main README, Compose-Desktop previews are still not visible to ClassGraph since they use `AnnotationRetention.SOURCE`.

There is [already an open issue](https://youtrack.jetbrains.com/issue/CMP-5675) to change it to `AnnotationRetention.BINARY`, which would allow ClassGraph to find them.

In the meanwhile, it is also possible to workaround this limitation with ComposablePreviewScanner as follows.

### Setup Steps

#### 1. Add `:jvm` dependency

Add `:jvm` dependency from ComposablePreviewScanner 0.2.0+ and use Roborazzi, since it is the only Screenshot Testing Library that supports Compose-Desktop:

```kotlin
testImplementation("io.github.sergio-sastre.ComposablePreviewScanner:jvm:<version>")
```

#### 2. Configure Roborazzi

Configure Roborazzi as described [in the corresponding "Multiplatform support" section](https://github.com/takahirom/roborazzi?tab=readme-ov-file#multiplatform-support)

#### 3. Define your own annotation

Define your own annotation. The only condition is not to define `AnnotationRetention.SOURCE`:

```kotlin
package my.package.path

@Target(AnnotationTarget.FUNCTION)
annotation class DesktopScreenshot
```

#### 4. Annotate the Desktop Composables

Annotate the Desktop Composables you want to generate screenshot tests for with this annotation, e.g.

```kotlin
@DesktopScreenshot
@Preview // It'd also work without this annotation
@Composable
fun MyDesktopComposable() { 
    // Composable code here
}
```

#### 5. Create the parameter provider for the Parameterized test

We can use [TestParameterInjector](https://github.com/google/TestParameterInjector) for that:

```kotlin
class DesktopPreviewProvider : TestParameterValuesProvider() {
  @OptIn(RequiresShowStandardStreams::class)
  override fun provideValues(context: Context?): List<ComposablePreview<JvmAnnotationInfo>> =
    JvmAnnotationScanner("my.package.path.DesktopScreenshot")
      .enableScanningLogs()
      .scanPackageTrees("previews")
      .getPreviews()
}
```

#### 6. Write the Parameterized test itself

```kotlin
fun screenshotNameFor(preview: ComposablePreview<JvmAnnotationInfo>): String =
   "$DEFAULT_ROBORAZZI_OUTPUT_DIR_PATH/${preview.declaringClass}.${preview.methodName}.png"

@RunWith(TestParameterInjector::class)
class DesktopPreviewTest(
   @TestParameter(valuesProvider = DesktopPreviewProvider::class)
   val preview: ComposablePreview<JvmAnnotationInfo>
) {
   @OptIn(ExperimentalTestApi::class)
   @Test
   fun test() {
      ROBORAZZI_DEBUG = true
      runDesktopComposeUiTest {
         setContent { preview() }
         onRoot().captureRoboImage(
            filePath = screenshotNameFor(preview),
         )
      }
   }
}
```

#### 7. Run the tests

Run these Roborazzi tests by executing the corresponding command:

```bash
./gradlew yourModule:recordRoborazziJvm
```

(if using the Kotlin Jvm Plugin)

---

[← Back to Main README](../README.md)
