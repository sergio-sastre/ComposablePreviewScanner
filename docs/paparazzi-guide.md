# Paparazzi Setup Guide

You can find [executable examples here](https://github.com/sergio-sastre/Android-screenshot-testing-playground/tree/master/lazycolumnscreen-previews/paparazzi/src)

> [!NOTE]
> You can also find a paparazzi-plugin in this repo that generates all this boilerplate code for you!
> Take a look at [its README.md](../paparazzi-plugin/README.md)

Let's say we want to enable some custom Paparazzi config for some Previews, for instance a maxPercentDifference value

## Step 1: Define your own annotation for the Lib config

```kotlin
annotation class PaparazziConfig(val maxPercentDifference: Double)
```

## Step 2: Annotate the corresponding Previews accordingly

You do not need to annotate all:

```kotlin
@PaparazziConfig(maxPercentDifference = 0.1)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MyComposable(){
   // Composable code here
}
```

## Step 3: Create custom record and verify SnapshotHandlers

Create custom record and verify `SnapshotHandler`s for better control over the screenshot file names.

By default, Paparazzi prefixes all generated screenshot files using its internal `SnapshotHandler`. While this works for most cases, it causes issues in parameterized tests: the default `SnapshotHandler` includes the test [index] in the filename. If the order of your previews changes, filenames no longer match, which can break snapshot verification.

To solve this, we can create custom `SnapshotHandler`s that use a fixed prefix, like "Paparazzi_Preview_Test", instead of a test-index-dependent name. This ensures filenames remain stable regardless of test order.

```kotlin
// Define the prefix = <packageName>_<className>_<methodName>
private val paparazziTestName =
   TestName(packageName = "Paparazzi", className = "Preview", methodName = "Test")

private class PreviewSnapshotVerifier(
   maxPercentDifference: Double
): SnapshotHandler {
   private val snapshotHandler = SnapshotVerifier(
      maxPercentDifference = maxPercentDifference
   )
   override fun newFrameHandler(
      snapshot: Snapshot,
      frameCount: Int,
      fps: Int
   ): SnapshotHandler.FrameHandler {
      val newSnapshot = Snapshot(
         name = snapshot.name,
         testName = paparazziTestName,
         timestamp = snapshot.timestamp,
         tags = snapshot.tags,
         file = snapshot.file,
      )
      return snapshotHandler.newFrameHandler(
         snapshot = newSnapshot,
         frameCount = frameCount,
         fps = fps
      )
   }

   override fun close() {
      snapshotHandler.close()
   }
}

private class PreviewHtmlReportWriter: SnapshotHandler {
   private val snapshotHandler = HtmlReportWriter()
   override fun newFrameHandler(
      snapshot: Snapshot,
      frameCount: Int,
      fps: Int
   ): SnapshotHandler.FrameHandler {
      val newSnapshot = Snapshot(
         name = snapshot.name,
         testName = paparazziTestName,
         timestamp = snapshot.timestamp,
         tags = snapshot.tags,
         file = snapshot.file,
      )
      return snapshotHandler.newFrameHandler(
         snapshot = newSnapshot,
         frameCount = frameCount,
         fps = fps
      )
   }

   override fun close() {
      snapshotHandler.close()
   }
}
```

In the next step, we'll show how to pass these custom SnapshotHandlers to the Paparazzi TestRule to take full control of screenshot filenames.

## Step 4: Map the PreviewInfo and PaparazziConfig values

```kotlin
class Dimensions(
   val screenWidthInPx: Int,
   val screenHeightInPx: Int
)

object ScreenDimensions {
   fun dimensions(
      parsedDevice: Device,
      widthDp: Int,
      heightDp: Int
   ): Dimensions {
      val conversionFactor = parsedDevice.densityDpi / 160f
      val previewWidthInPx = ceil(widthDp * conversionFactor).toInt()
      val previewHeightInPx = ceil(heightDp * conversionFactor).toInt()
      return Dimensions(
         screenHeightInPx = when (heightDp > 0) {
            true -> previewHeightInPx
            false -> parsedDevice.dimensions.height.toInt()
         },
         screenWidthInPx = when (widthDp > 0) {
            true -> previewWidthInPx
            false -> parsedDevice.dimensions.width.toInt()
         }
      )
   }
}

object DeviceConfigBuilder {
   fun build(preview: AndroidPreviewInfo): DeviceConfig {
      val parsedDevice =
         DevicePreviewInfoParser.parse(preview.device)?.inPx() ?: return DeviceConfig()

      val dimensions = ScreenDimensions.dimensions(
         parsedDevice = parsedDevice,
         widthDp = preview.widthDp,
         heightDp = preview.heightDp
      )

      return DeviceConfig(
         screenHeight = dimensions.screenHeightInPx,
         screenWidth = dimensions.screenWidthInPx,
         density = Density(parsedDevice.densityDpi),
         xdpi = parsedDevice.densityDpi, // not 100% precise
         ydpi = parsedDevice.densityDpi, // not 100% precise
         size = ScreenSize.valueOf(parsedDevice.screenSize.name),
         ratio = ScreenRatio.valueOf(parsedDevice.screenRatio.name),
         screenRound = ScreenRound.valueOf(parsedDevice.shape.name),
         orientation = ScreenOrientation.valueOf(parsedDevice.orientation.name),
         locale = preview.locale.ifBlank { "en" },
         nightMode = when (preview.uiMode and UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES) {
            true -> NightMode.NIGHT
            false -> NightMode.NOTNIGHT
         }
      )
   }
}

object PaparazziPreviewRule {
    const val UNDEFINED_API_LEVEL = -1
    const val MAX_API_LEVEL = 36
    
    fun createFor(preview: ComposablePreview<AndroidPreviewInfo>): Paparazzi {
       val previewInfo = preview.previewInfo
       val previewApiLevel = when(previewInfo.apiLevel == UNDEFINED_API_LEVEL) {
          true -> MAX_API_LEVEL
          false -> previewInfo.apiLevel
       }
       // other library configurations...
       val tolerance = preview.getAnnotation<PaparazziConfig>()?.maxPercentDifference ?: 0.0
       return Paparazzi(
            environment = detectEnvironment().copy(compileSdkVersion = previewApiLevel),
            deviceConfig = DeviceConfigBuilder.build(previewInfo),
            supportsRtl = true,
            showSystemUi = previewInfo.showSystemUi,
            renderingMode = when {
                previewInfo.showSystemUi -> SessionParams.RenderingMode.NORMAL
                previewInfo.widthDp > 0 && previewInfo.heightDp > 0 -> SessionParams.RenderingMode.FULL_EXPAND
                else -> SessionParams.RenderingMode.SHRINK
            },
            snapshotHandler = when(System.getProperty("paparazzi.test.verify")?.toBoolean() == true) {
               true -> PreviewSnapshotVerifier(tolerance)
               false -> PreviewHtmlReportWriter()
            },
            maxPercentDifference = tolerance
        )
    }
}

/**
 * A composable function that wraps content inside a Box with a specified size
 * This is used to simulate what previews render when showSystemUi is true:
 * - The Preview takes up the entire screen
 * - The Composable still keeps its original size,
 * - Background color of the Device is white,
 *   but the @Composable background color is the one defined in the Preview
 */
@Composable
fun SystemUiSize(
   widthInDp: Int,
   heightInDp: Int,
   content: @Composable () -> Unit
) {
   Box(Modifier
      .size(
         width = widthInDp.dp,
         height = heightInDp.dp
      )
      .background(Color.White)
   ) {
      content()
   }
}

// Additional to support @Preview's 'showBackground' and 'backgroundColor' properties
@Composable
fun PreviewBackground(
    showBackground: Boolean,
    backgroundColor: Long,
    content: @Composable () -> Unit
) {
    when (showBackground) {
        false -> content()
        true -> {
            val color = when (backgroundColor != 0L) {
                true -> Color(backgroundColor)
                false -> Color.White
            }
            Box(Modifier.background(color)) {
                content()
            }
        }
    }
}
```

## Step 5: Create the corresponding Parameterized Test

```kotlin
@RunWith(Parameterized::class)
class PreviewTestParameterTests(
    val preview: ComposablePreview<AndroidPreviewInfo>,
) {

   companion object {
      // Optimization: This avoids scanning for every test
      private val cachedPreviews: List<ComposablePreview<AndroidPreviewInfo>> by lazy {
         AndroidComposablePreviewScanner()
            .scanPackageTrees("your.package", "your.package2")
            .includeAnnotationInfoForAllOf(PaparazziConfig::class.java)
            .getPreviews()
      }

      @JvmStatic
      @Parameterized.Parameters
      fun values(): List<ComposablePreview<AndroidPreviewInfo>> = cachedPreviews
   }

    @get:Rule
    val paparazzi: Paparazzi = PaparazziPreviewRule.createFor(preview)

    @Test
    fun snapshot() {
        val screenshotId = AndroidPreviewScreenshotIdBuilder(preview).build()
       
        paparazzi.snapshot(name = screenshotId) {
            val previewInfo = preview.previewInfo
            when (previewInfo.showSystemUi) {
                false -> PreviewBackground(
                    showBackground = previewInfo.showBackground,
                    backgroundColor = previewInfo.backgroundColor
                ) {
                    preview()
                }

                true -> {
                    val parsedDevice = (DevicePreviewInfoParser.parse(previewInfo.device) ?: DEFAULT).inDp()
                    SystemUiSize(
                        widthInDp = parsedDevice.dimensions.width.toInt(),
                        heightInDp = parsedDevice.dimensions.height.toInt()
                    ) {
                        PreviewBackground(
                            showBackground = true,
                            backgroundColor = previewInfo.backgroundColor,
                        ) {
                            preview()
                        }
                    }
                }
            }
        }
    }
}
```

## Step 6: Run the tests

Run these Paparazzi tests together with the existing ones by executing the corresponding command:

```bash
./gradlew yourModule:recordPaparazziDebug
```

---

[← Back to Main README](../README.md)
