# Instrumentation Screenshot Tests Setup Guide

You can find executable examples that use ComposablePreviewScanner with the different instrumentation-based libraries in the corresponding links below:
- [Dropshots](https://github.com/sergio-sastre/Android-screenshot-testing-playground/tree/master/recyclerviewscreen-previews/dropshots)
- [Shot](https://github.com/sergio-sastre/Android-screenshot-testing-playground/tree/master/recyclerviewscreen-previews/shot)
- [Android-Testify](https://github.com/sergio-sastre/Android-screenshot-testing-playground/tree/master/recyclerviewscreen-previews/android-testify)

## Overview

Android does not use the standard Java bytecode format and does not actually even have a runtime classpath.
Moreover, the "build" folders, where the compiled classes are located, are not accessible from instrumentation tests.
Therefore, the current way to support instrumentation tests, is by previously dumping the relevant classes into a file and moving it into a folder that can be accessed while running instrumentation tests.

## Step 1: Run the scan in a unit test & save it in a file accessible by instrumentation tests

For example, save it in assets:

```kotlin
class SaveScanResultInAssets {
    @Test
    fun `task -- save scan result in assets`() {
        val scanResultFileName = "scan_result.json"

        ScanResultDumper()
            .scanPackageTrees("my.package")
            .dumpScanResultToFileInAssets(
                fileName = scanResultFileName
            )

        assert(
           assetsFilePath(
               fileName = scanResultFileName
           ).exists())
    }
}
```

Ensure that the .json with the scan result is up-to-date before executing the instrumentation screenshot tests. For instance, execute that test always before your instrumentation screenshot tests.

> [!NOTE]
> Ideally, this scanning could be done via a Gradle Plugin in the future instead of by running it in a unit test.

## Step 2: Prepare your Composable Preview Tests

Now proceed to prepare your Composable Preview Tests with, for instance, Dropshots.

Let's say we want to enable some custom Dropshots Config for some Previews, for instance a maxPercentDifferent value.

### Define your own annotation

```kotlin
annotation class DropshotsConfig(val comparisonThreshold: Double)
```

### Annotate the corresponding Previews accordingly

```kotlin
@DropshotsConfig(comparisonThreshold = 0.15)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MyComposable() {
   // Composable code here
}
```

### Map the PreviewInfo and DropshotsConfig values

For instance, you can use a custom class for that. To map the Preview Info values, I recommend to use the ActivityScenarioForComposableRule of [AndroidUiTestingUtils](https://github.com/sergio-sastre/AndroidUiTestingUtils)

```kotlin
object DropshotsPreviewRule {
   fun createFor(preview: ComposablePreview<AndroidPreviewInfo>): Dropshots =
      preview.getAnnotation<DropshotsConfig>()?.let { config ->
         Dropshots(
            resultValidator = ThresholdValidator(config.comparisonThreshold)
         )
      } ?: Dropshots()
}

object ActivityScenarioForComposablePreviewRule {
    fun createFor(preview: ComposablePreview<AndroidPreviewInfo>): ActivityScenarioForComposableRule {
        val uiMode =
            when (preview.previewInfo.uiMode and UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES) {
                true -> UiMode.NIGHT
                false -> UiMode.DAY
            }

        val orientation =
            when (DevicePreviewInfoParser.parse(preview.previewInfo.device)?.orientation == Orientation.LANDSCAPE) {
                true -> ComposableConfigOrientation.LANDSCAPE
                false -> ComposableConfigOrientation.PORTRAIT
            }

        val locale =
            preview.previewInfo.locale.removePrefix("b+").replace("+", "-").ifBlank { "en" }

        return ActivityScenarioForComposableRule(
            backgroundColor = Color.TRANSPARENT,
            config = ComposableConfigItem(
                uiMode = uiMode,
                fontSize = FontSizeScale.Value(preview.previewInfo.fontScale),
                orientation = orientation,
                locale = locale
            )
        )
    }
}
```

### Create the corresponding Parameterized Test

```kotlin
@RunWith(ParameterizedTestRunner::class)
class PreviewParameterizedTests(
   private val preview: ComposablePreview<AndroidPreviewInfo>,
) {

   companion object {
     private val cachedPreviews: List<ComposablePreview<AndroidPreviewInfo>> by lazy {
         AndroidComposablePreviewScanner()
               .scanFile(getInstrumentation().context.assets.open("scan_result.json"))
               .includeAnnotationInfoForAllOf(DropshotsConfig::class.java)
               .getPreviews()
     }

      @JvmStatic
      @ParameterizedTestRunner.Parameters
      fun values(): List<ComposablePreview<AndroidPreviewInfo>> = cachedPreviews
   }

   @get:Rule
   val dropshots: Dropshots = DropshotsPreviewRule.createFor(preview)

   @get:Rule
   val activityScenarioForComposableRule: ActivityScenarioForComposableRule = 
         ActivityScenarioForComposablePreviewRule.createFor(preview)

   @Test
   fun snapshot() {
     activityScenarioForComposableRule.onActivity {
         it.setContent {
             preview()
         }
     }

     dropshots.assertSnapshot(
         view = activityScenarioForComposableRule.activity.waitForComposeView()
     )
   }
}
```

## Step 3: Run the tests

Run these Dropshots tests together with the existing ones by executing the corresponding command:

```bash
./gradlew yourModule:connectedAndroidTest -Pdropshots.record
```

## Important Notes

> [!WARNING]
> Beware that Locale Strings in Preview Infos, unlike AndroidUiTestingUtils, use The BCP-47 tag but with + instead of - as separators, and have the prefix b+. Therefore, the BCP-47 tag "zh-Hans-CN" would be written as "b+zh+Hans+CN" instead. 
> 
> So for this case, you'd have to convert locale "b+zh+Hans+CN" to "zh-Hans-CN" in order to use it with AndroidUiTestingUtils, for instance as showcased above:
> 
> `val locale = preview.previewInfo.locale.removePrefix("b+").replace("+", "-").ifBlank { "en" }`

---

[← Back to Main README](../README.md)
