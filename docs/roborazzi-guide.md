# Roborazzi Setup Guide

You can find [executable examples here](https://github.com/sergio-sastre/Android-screenshot-testing-playground/tree/master/lazycolumnscreen-previews/roborazzi/src)

Let's say we want to enable some custom Roborazzi Config for some Previews, for instance a maxPercentDifferent value

## Step 1: Define your own annotation for the Lib Config

```kotlin
annotation class RoborazziConfig(val comparisonThreshold: Double)
```

## Step 2: Annotate the corresponding Previews accordingly

```kotlin
@RoborazziConfig(comparisonThreshold = 0.1)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MyComposable(){
    // Composable code here
}
```

## Step 3: Map the PreviewInfo and RoborazziConfig values

For instance, you can use a custom class for that.

```kotlin
object RoborazziOptionsMapper {
    fun createFor(preview: ComposablePreview<AndroidPreviewInfo>): RoborazziOptions =
       preview.getAnnotation<RoborazziConfig>()?.let { config ->
          RoborazziOptions(
             compareOptions = CompareOptions(resultValidator = ThresholdValidator(config.comparisonThreshold))
          )
       } ?: RoborazziOptions()
}

object RoborazziComposeOptionsMapper {
    @OptIn(ExperimentalRoborazziApi::class)
    fun createFor(preview: ComposablePreview<AndroidPreviewInfo>): RoborazziComposeOptions =
        RoborazziComposeOptions {
            val previewInfo = preview.previewInfo
            previewDevice(previewInfo.device.ifBlank { Devices.NEXUS_5 } )
            size(
                widthDp = previewInfo.widthDp,
                heightDp = previewInfo.heightDp
            )
            background(
                showBackground = previewInfo.showBackground,
                backgroundColor = previewInfo.backgroundColor
            )
            locale(previewInfo.locale)
            uiMode(previewInfo.uiMode)
            fontScale(previewInfo.fontScale)
        }
}
```

Check the following link for a full list of [Robolectric device qualifiers](https://robolectric.org/device-configuration/) and this blog post on how to [set the cumulative Qualifiers dynamically](https://sergiosastre.hashnode.dev/efficient-testing-with-robolectric-roborazzi-across-many-ui-states-devices-and-configurations)

## Step 4: Create the corresponding Parameterized Test

```kotlin
@RunWith(ParameterizedRobolectricTestRunner::class)
class PreviewParameterizedTests(
    private val preview: ComposablePreview<AndroidPreviewInfo>,
) {

    companion object {
        // Optimization: This avoids scanning for every test
        private val cachedPreviews: List<ComposablePreview<AndroidPreviewInfo>> by lazy {
            AndroidComposablePreviewScanner()
                .scanPackageTrees("your.package", "your.package2")
                .filterPreviews { previewParams -> previewParams.apiLevel == 30 }
                .includeAnnotationInfoForAllOf(RoborazziConfig::class.java)
                .getPreviews()
        }

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun values(): List<ComposablePreview<AndroidPreviewInfo>> = cachedPreviews
    }

    // Recommended for more meaningful screenshot file names. See #Advanced Usage
    fun screenshotNameFor(preview: ComposablePreview<AndroidPreviewInfo>): String =
        "$DEFAULT_ROBORAZZI_OUTPUT_DIR_PATH/${AndroidPreviewScreenshotIdBuilder(preview).build()}.png"

    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(sdk = [30]) // same as the previews we've filtered
    @Test
    fun snapshot() {
        captureRoboImage(
           filePath = screenshotNameFor(preview),
           roborazziOptions = RoborazziOptionsMapper.createFor(preview),
           roborazziComposeOptions = RoborazziComposeOptionsMapper.createFor(preview)
        ) {
            preview()
        }
    }
}
```

## Step 5: Run the tests

Run these Roborazzi tests together with the existing ones by executing the corresponding command:

```bash
./gradlew yourModule:recordRoborazziDebug
```

---

[← Back to Main README](../README.md)
