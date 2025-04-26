[![](https://jitpack.io/v/sergio-sastre/ComposablePreviewScanner.svg)](https://jitpack.io/#sergio-sastre/ComposablePreviewScanner)
[![](https://img.shields.io/maven-central/v/io.github.sergio-sastre.ComposablePreviewScanner/android)](https://central.sonatype.com/artifact/io.github.sergio-sastre.ComposablePreviewScanner/android)</br>

<a href="https://androidweekly.net/issues/issue-628">
<img src="https://androidweekly.net/issues/issue-628/badge">
</a></br>
<a href="https://jetc.dev/issues/221.html"><img src="https://img.shields.io/badge/As_Seen_In-jetc.dev_Newsletter_Issue_%23221-blue?logo=Jetpack+Compose&amp;logoColor=white" alt="As Seen In - jetc.dev Newsletter Issue #221"></a>

# <p align="center">Composable Preview Scanner</p>


<p align="center">
<img width="400" src="https://github.com/sergio-sastre/ComposablePreviewScanner/assets/6097181/63d9676d-22c4-4bd1-8680-3fcf1a72e001">
</p>

A library to help auto-generate screenshot tests from Composable Previews with any screenshot testing library:
JVM-based (i.e. Paparazzi, Roborazzi) as well as Instrumentation-based (i.e. Shot, Dropshots, Android-Testify, etc.)

#### Provide anonymous feedback
Already using ComposablePreviewScanner?</br>
I'd love to hear your thoughts!</br>
Help shape its future by taking [this quick survey](https://forms.gle/jcvggBxv14CLqjFo6)

# Comparison with other solutions
|                                                      | Composable Preview Scanner                    | Showkase                                      | Compose Preview Screenshot Testing |
|------------------------------------------------------|-----------------------------------------------|-----------------------------------------------|------------------------------------|
| Independent of AGP version                           | ✅                                             | ✅                                             | ❌                                  |
| Library-agnostic solution                            | ✅                                             | ✅                                             | ❌<sup>1</sup>                      |
| Scans previews in different sources sets<sup>2</sup> | ✅ main<br/>✅ screenshotTest<br/>✅ androidTest | ✅ main<br/>❌ screenshotTest<br/>❌ androidTest | ❌ main<br/>✅ screenshotTest<br/>❌ androidTest                     |
| Preview Infos available                              | ✅                                             | ❌<sup>3</sup>                                 | ✅                                  |
| Specific Config (e.g. for Libs) available            | ✅<sup>4</sup>                                 | ❌                                             | ⚠️<sup>5</sup>                               |
| Compose Multiplatform Previews support               | *✅<sup>6</sup>                                | ❌<sup>7</sup>                                 | ❌                                  |

<sup>1</sup> Compose Preview Screenshot Testing is a standalone solution based on LayoutLib, whereas ComposablePreviewScanner and Showkase provide Composables' infos so you can run screenshot tests with your favourite screenshot testing library.</br></br>
<sup>2</sup> From version 0.5.0, ComposablePreviewScanner can scan previews in any source set. Compose Preview Screenshot Testing requires to put the previews in a brand-new "screenshotTest" source.</br></br>
<sup>3</sup> Showkase components only hold information about the Composable, but not about the Preview Info (i.e. ApiLevel, Locale, UiMode, FontScale...).</br></br>
<sup>4</sup> ComposablePreviewScanner supports adding extra lib-config (e.g. Paparazzi's Rendering Mode or Roborazzi's compare options) in the form of annotations that are additionally added to the preview. You can check how in the examples below in [Jvm Screenshot Tests](#jvm-screenshot-tests) and [Instrumentation Screenshot Tests](#instrumentation-screenshot-tests) respectively.</br></br>
<sup>5</sup> Compose Preview Screenshot Testing supports *only general tolerance* via gradle plugin from version [0.0.1-alpha06](https://developer.android.com/studio/preview/compose-screenshot-testing#001-alpha06)</br></br>
<sup>6</sup> ComposablePreviewScanner provides some Compose Multiplatform support: It can scan `@Preview`s in `common`, as described in the [Compose-Multiplatform](#compose-multiplatform-support) section. Compose-Desktop `@Preview`s are still not supported out of the box because of [this issue](https://youtrack.jetbrains.com/issue/CMP-5675), but you can check the [Compose-Desktop support](#compose-desktop-support) section for a workaround.</br></br>
<sup>7</sup> [Showkase: Compose Multiplatform Support](https://github.com/airbnb/Showkase/issues/364)
</br></br></br>
ComposablePreviewScanner also works with:
- `@PreviewParameters` (for Compose Multiplatform since 0.6.0+)
- Multi-Previews, including  `@PreviewScreenSizes`, `@PreviewFontScales`, `@PreviewLightDark`, and `@PreviewDynamicColors`.
- private `@Previews` (from version 0.1.3+)
- `@Previews` inside public classes<sup>1</sup> (from version 0.3.0+), not nested classes though
- `@Previews` located in any source set, like "main", "screenshotTest" and "androidTest" (from version 0.5.0+)
- `@Previews` with default-parameters (from version 0.5.1+)

<sup>1</sup> The [Compose Preview Screenshot Testing tool](https://developer.android.com/studio/preview/compose-screenshot-testing) from Google requires to put your `@Previews` inside a class.

# How to set up
> [!WARNING]  
> Beware the prefixes:</br>
> *Maven Central* -> **io.github**</br>
> *Jitpack* -> **com.github**</br>
## Maven central (since 0.3.2)
```kotlin
dependencies {
   // jvm tests (e.g. with Roborazzi & Paparazzi)
   testImplementation("io.github.sergio-sastre.ComposablePreviewScanner:android:<version>")

   // instrumentation tests (e.g. with Shot, Dropshots & Android-Testify)
   debugImplementation("io.github.sergio-sastre.ComposablePreviewScanner:android:<version>")
   
   // compose multiplatform (jvm-targets)
   testImplementation("io.github.sergio-sastre.ComposablePreviewScanner:jvm:<version>")
}
```

## Jitpack
Add jitpack to your root build.gradle file:
```kotlin
allprojects {
   repositories {
      maven { url 'https://jitpack.io' }
   }
}
```

```kotlin
dependencies {
   // jvm tests (e.g. with Roborazzi & Paparazzi)
   testImplementation("com.github.sergio-sastre.ComposablePreviewScanner:android:<version>")

   // instrumentation tests (e.g. with Shot, Dropshots & Android-Testify)
   debugImplementation("com.github.sergio-sastre.ComposablePreviewScanner:android:<version>")
   
   // compose multiplatform (jvm-targets), from 0.2.0
   testImplementation("com.github.sergio-sastre.ComposablePreviewScanner:jvm:<version>")
}
```

# How to use
## Libraries
1. [Jvm Screenshot Tests](#jvm-screenshot-tests)</br>
   1.1  [Paparazzi](#paparazzi)</br>
   1.2  [Roborazzi](#roborazzi)</br>
2. [Instrumentation Screenshot Tests](#instrumentation-screenshot-tests)
3. [Compose Multiplatform Support](#compose-multiplatform-support)

> [!NOTE]
> [Roborazzi](https://github.com/takahirom/roborazzi) has integrated ComposablePreviewScanner in its plugin since [version 1.22](https://github.com/takahirom/roborazzi/releases/tag/1.22.0)


## API   
The API is pretty simple:

```kotlin
AndroidComposablePreviewScanner()
    // Optional to log scanning info like scanning time or amount of previews found
    .enableScanningLogs()
    // Optional to scan previews in compiled classes of other source sets, like "screenshotTest" or "androidTest"
    // If omitted, it scans previews in 'main' at build time
    .setTargetSourceSet(
       Classpath(SourceSet.SCREENSHOT_TEST) // scan previews under "screenshotTest"
    )
    // Compulsory to define where to scan for Previews.
    // See 'Scanning source Options (packages, files, inputStreams)'
    .scanPackageTrees(
        include = listOf("your.package", "your.package2"),
        exclude = listOf("your.package.subpackage1", "your.package2.subpackage1")
    )
    // Optional to filter out scanned previews with any of the given annotations
    // Warning: this and its 'include' counterpart are mutually exclusive by API design
    .excludeIfAnnotatedWithAnyOf(
        ExcludeForScreenshot::class.java, 
        ExcludeForScreenshot2::class.java
    )
    // Optional to filter in only scanned previews with any of the given annotations
    // Warning: this and its 'exclude' counterpart are mutually exclusive by API design
    .includeIfAnnotatedWithAnyOf(
      IncludeForScreenshot::class.java,
      IncludeForScreenshot2::class.java
    )
    // Optional to include configuration info of the screenshot testing library in use
    // See 'How to use -> Libraries' above for further info
    .includeAnnotationInfoForAllOf(
        ScreenshotConfig::class.java,
        ScreenshotConfig2::class.java
    )
    // Optional to also provide private Previews
    .includePrivatePreviews()
    // Optional to filter by any previewInfo: name, group, apiLevel, locale, uiMode, fontScale...
    .filterPreviews {
        previewInfo ->  previewInfo.apiLevel == 30 
    }
    // ---
    .getPreviews()
```

## Scanning

### Scanning Source Sets (screenshotTest, androidTest, main)
By default, ComposablePreviewScanner scans `@Preview`s in the `main` Source Set at build time.
However, one can scan previews in other Source Sets different from `main` by using `.setTargetSourceSet(classpath:Classpath)`,
where `classpath` is the local path to the compiled classes of that Source Set.</br>
ComposablePreviewScanner provides some default values to facilitate this:
```kotlin
// Previews under "screenshotTest"
Classpath(SourceSet.SCREENSHOT_TEST)

// Previews under "androidTest"
Classpath(SourceSet.ANDROID_TEST)
```

#### Ensure compiled classes exist
You have to make sure the corresponding compiled classes for that Source Set exist and are up to date.
The simplest way is to execute the corresponding compile task before running your tests or dumping the scan result to a file, namely `<module>:compile<Variant><Sourceset>Kotlin`, for instance
1. ScreenshotTest 
   - Debug ->   `:mymodule:compileDebugScreenshotTestKotlin`
   - Release -> `:mymodule:compileReleaseScreenshotTestKotlin`
2. AndroidTest
   - Debug ->   `:mymodule:compileDebugAndroidTestKotlin`
   - Release -> `:mymodule:compileReleaseAndroidTestKotlin`

To ensure you don't forget it, you can configure gradle accordingly, so those tasks are always executed previously.
For instance, if you're using Roborazzi or Paparazzi and want to scan previews in the `screenshotTest` Source Set for the `debug` variant
```kotlin
// Create Compiled Classes always before unit tests, including Roborazzi/Paparazzi tests
tasks.withType<Test> {
   dependsOn("compileDebugScreenshotTestKotlin")
}
```

#### Ensure Source Set dependencies available in tests
Last but not least, make sure all the code inside the previews of the target Source Set is also
available in `test` (for Roborazzi and Paparazzi) or `android test` (for any instrumentation-based library).</br>
So, let's say that you only have `@Preview`s in `screenshotTest`, and not in `main`. Therefore you've only added that dependency to `screenshotTest`:
```kotlin
screenshotTestImplementation("androidx.compose.ui:ui-tooling-preview:<version>")
```
If you're running Roborazzi or Paparazzi screenshot tests, you'll need to add that dependency to 'test' build Type
```kotlin
screenshotTestImplementation("androidx.compose.ui:ui-tooling-preview:<version>")
testImplementation("androidx.compose.ui:ui-tooling-preview:<version>")
```

> [!WARNING]
> For instrumentation tests and Source Sets different from `main` or `androidTest`, like `screenshotTest`, you'll also need to ensure that the classes of those source sets
> are also included in the .apk installed on the device or emulator, or it will throw ClassNotFoundErrors.
> The easiest way to achieve this is to add the following code snippet to your gradle file:
> ```kotlin
> val includeScreenshotTests = project.hasProperty("includeSourceSetScreenshotTest")
> if (includeScreenshotTests) {
>     sourceSets {
>        getByName("androidTest") {
>           java.srcDir("src/screenshotTest/java") //or kotlin
>           res.srcDir("src/screenshotTest/res")
>        }
>     }
>}
> ```
> And pass that gradle property when executing the screenshot tests via command-line, e.g.:
> `./gradlew :tests:screenshotRecord -PincludeSourceSetScreenshotTest`
> 
> This is NOT necessary for JVM-based screenshot testing libraries like Roborazzi and paparazzi

### Scanning source Options (packages, files, inputStreams)
Apart from `scanPackageTrees(include:List<String>, exclude:List<String>)`, there are 2 more options to scan previews:
1. All Packages: `scanAllPackages()`. This might require a huge amount of memory since it would scan not only in a set of packages, but in all packages used in your app/module (i.e. also in its transitive dependencies). This is in 99% of the cases unnecessary, and scanning the main package trees of your module should be sufficient. 
2. From a file containing the ScanResult. This speeds up your screenshot tests, since avoids the time-consuming process of scanning each time by reusing the previously scanned data: </br>
   2.1. `scanFile(jsonFile: File)`. Use this for JVM-based screenshot testing libraries (i.e. Roborazzi & Paparazzi).</br>
   2.2. `scanFile(targetInputStream: InputStream, customPreviewsInfoInputStream: InputStream)`. This is meant for Instrumentation-based screenshot testing libraries.</br></br>
You can create a unit test for that:
```kotlin
class SaveScanResultInFiles {
    @Test
    fun `task -- save scan result in file`() {
        val scanResultFileName = "scan_result.json"

        ScanResultDumper()
            .setTargetSourceSet(Classpath(SourceSet.ANDROID_TEST)) // optional
            .scanPackageTrees("my.package")
            // for unit tests
            .dumpScanResultToFile(scanResultFileName)
            // for instrumentation tests
            .dumpScanResultToFileInAssets(
               flavourName = "myFlavour",
               fileName = scanResultFileName
            )
    }
}
```

## JVM Screenshot Tests

### Paparazzi
You can find [executable examples here](https://github.com/sergio-sastre/Android-screenshot-testing-playground/tree/master/lazycolumnscreen-previews/paparazzi/src)

Let's say we want to enable some custom Paparazzi Config for some Previews, for instance a maxPercentDifferent value

1. Define your own annotation for the Lib config.
```kotlin
annotation class PaparazziConfig(val maxPercentDifferent: Double)
```

2. Annotate the corresponding Previews accordingly (you do not need to annotate all):
```kotlin
@PaparazziConfig(maxPercentDifferent = 0.1F)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MyComposable(){
   // Composable code here
}
```

3. Include your annotation info in the Preview
```kotlin
object ComposablePreviewProvider : TestParameter.TestParameterValuesProvider {
    override fun provideValues(): List<ComposablePreview<AndroidPreviewInfo>> =
        AndroidComposablePreviewScanner()
            .scanPackageTrees("my.package", "my.package2")
            .includeAnnotationInfoForAllOf(PaparazziConfig::class.java)
            // any other filtering option ...
            .getPreviews()
}
```

4. Map the PreviewInfo and PaparazziConfig values. For instance, you can use a custom class for that.
```kotlin

// The DevicePreviewInfoParser used in this method is available since ComposablePreviewScanner 0.4.0
object DeviceConfigBuilder {
    fun build(preview: AndroidPreviewInfo): DeviceConfig {
        val parsedDevice =
            DevicePreviewInfoParser.parse(preview.device)?.inPx() ?: return DeviceConfig()
        val conversionFactor = parsedDevice.densityDpi / 160f
        val previewWidthInPx = ceil(preview.widthDp * conversionFactor).toInt()
        val previewHeightInPx = ceil(preview.heightDp * conversionFactor).toInt()

        return DeviceConfig(
            screenHeight = when (preview.heightDp > 0) {
                true -> previewHeightInPx
                false -> parsedDevice.dimensions.height.toInt()
            },
            screenWidth = when (preview.widthDp > 0) {
                true -> previewWidthInPx
                false -> parsedDevice.dimensions.width.toInt()
            },
            density = Density(parsedDevice.densityDpi),
            xdpi = parsedDevice.densityDpi, // not 100% precise
            ydpi = parsedDevice.densityDpi, // not 100% precise
            size = ScreenSize.valueOf(parsedDevice.screenSize.name),
            ratio = ScreenRatio.valueOf(parsedDevice.screenRatio.name),
            screenRound = ScreenRound.valueOf(parsedDevice.shape.name),
            orientation = when (parsedDevice.orientation) {
                Orientation.PORTRAIT -> ScreenOrientation.PORTRAIT
                Orientation.LANDSCAPE -> ScreenOrientation.LANDSCAPE
            },
            locale = preview.locale.ifBlank { "en" },
            nightMode =
            when (preview.uiMode and UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES) {
                true -> NightMode.NIGHT
                false -> NightMode.NOTNIGHT
            },
        )
    }
}

object PaparazziPreviewRule {
    fun createFor(preview: ComposablePreview<AndroidPreviewInfo>): Paparazzi {
        val previewInfo = preview.previewInfo
        return Paparazzi(
            deviceConfig = DeviceConfigBuilder.build(preview.previewInfo),
            supportsRtl = true,
            showSystemUi = previewInfo.showSystemUi,
            renderingMode = when {
                previewInfo.widthDp > 0 && previewInfo.heightDp > 0 -> SessionParams.RenderingMode.FULL_EXPAND
                previewInfo.heightDp > 0 -> SessionParams.RenderingMode.V_SCROLL
                else -> SessionParams.RenderingMode.SHRINK
            },
            // other configurations...
            maxPercentDifference = preview.getAnnotation<PaparazziConfig>()?.maxPercentDifference ?: 0F
        )
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

5. Create the corresponding Parameterized Test:
```kotlin
@RunWith(TestParameterInjector::class)
class PreviewTestParameterTests(
    @TestParameter(valuesProvider = ComposablePreviewProvider::class)
    val preview: ComposablePreview<AndroidPreviewInfo>,
) {

    @get:Rule
    val paparazzi: Paparazzi = PaparazziPreviewRule.createFor(preview)

    @Test
    fun snapshot() {
        // Recommended for more meaningful screenshot file names. See #Advanced Usage
        val screenshotId = AndroidPreviewScreenshotIdBuilder(preview).ignoreClassName().build()
        paparazzi.snapshot(name = screenshotId) {
            PreviewBackground(
               showBackground = preview.previewInfo.showBackground,
               backgroundColor = preview.previewInfo.backgroundColor,
            ) {
               preview()
            }
        }
    }
}
```

6. Run these Paparazzi tests together with the existing ones by executing the corresponding command e.g. `./gradlew yourModule:recordPaparazziDebug`

### Roborazzi
You can find [executable examples here](https://github.com/sergio-sastre/Android-screenshot-testing-playground/tree/master/lazycolumnscreen-previews/roborazzi/src)

Let's say we want to enable some custom Roborazzi Config for some Previews, for instance a maxPercentDifferent value

1. Define your own annotation for the Lib Config.
```kotlin
annotation class RoborazziConfig(val comparisonThreshold: Double)
```

2. Annotate the corresponding Previews accordingly:
```kotlin
@RoborazziConfig(comparisonThreshold = 0.1)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MyComposable(){
    // Composable code here
}
```

3. Map the PreviewInfo and RoborazziConfig values. For instance, you can use a custom class for that.
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

4. Create the corresponding Parameterized Test:
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

    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(sdk = [30]) // same as the previews we've filtered
    @Test
    fun snapshot() {
        // Recommended for more meaningful screenshot file names. See #Advanced Usage
        val screenshotId = AndroidPreviewScreenshotIdBuilder(preview).build()
        captureRoboImage(
           filePath = "${screenshotId}.png",
           roborazziOptions = RoborazziOptionsMapper.createFor(preview),
           roborazziComposeOptions = RoborazziComposeOptionsMapper.createFor(preview)
        ) {
            preview()
        }
    }
}
```

5. Run these Roborazzi tests together with the existing ones by executing the corresponding command e.g. `./gradlew yourModule:recordRoborazziDebug`

## Instrumentation Screenshot Tests
You can find executable examples that use ComposablePreviewScanner with the different instrumentation-based libraries in the corresponding links below:
- [Dropshots](https://github.com/sergio-sastre/Android-screenshot-testing-playground/tree/master/recyclerviewscreen-previews/dropshots)
- [Shot](https://github.com/sergio-sastre/Android-screenshot-testing-playground/tree/master/recyclerviewscreen-previews/shot)
- [Android-Testify](https://github.com/sergio-sastre/Android-screenshot-testing-playground/tree/master/recyclerviewscreen-previews/android-testify)</br>

Android does not use the standard Java bytecode format and does not actually even have a runtime classpath.
Moreover, the "build" folders, where the compiled classes are located, are not accessible from instrumentation tests.
Therefore, the current way to support instrumentation tests, is by previously dumping the relevant classes into a file and moving it into a folder that can be accessed while running instrumentation tests.
1. run the scan in a unit test & save it in a file accessible by instrumentation tests e.g. in assets
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
Ideally, this scanning could be done via a Gradle Plugin in the future instead of by running it in a unit test.

2. Now proceed to prepare your Composable Preview Tests with, for instance, Dropshots.
Let's say we want to enable some custom Dropshots Config for some Previews, for instance a maxPercentDifferent value.
   - Define your own annotation
      ```kotlin
      annotation class DropshotsConfig(val comparisonThreshold: Double)
      ```
   - Annotate the corresponding Previews accordingly:
   ```kotlin
   @DropshotsConfig(comparisonThreshold = 0.15)
   @Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
   @Composable
   fun MyComposable() {
      // Composable code here
   }
   ```
   - Map the PreviewInfo and DropshotsConfig values. For instance, you can use a custom class for that. To map the Preview Info values, I recommend to use the ActivityScenarioForComposableRule of [AndroidUiTestingUtils](https://github.com/sergio-sastre/AndroidUiTestingUtils)
   ```kotlin
   object DropshotsPreviewRule {
      fun createFor(preview: ComposablePreview<AndroidPreviewInfo>): Dropshots =
         preview.getAnnotation<DropshotsConfig>()?.let { config ->
            Dropshots(
               resultValidator = ThresholdValidator(config.comparisonThreshold))
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
- Create the corresponding Parameterized Test:
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
         fun values(): List<AndroidComposablePreview<PreviewInfo>> = cachedPreviews   
      }
   
      @get:Rule
      val dropshots: Dropshots = DropshotsPreviewRule.createFor(preview)
   
      @get:Rule
      val activityScenarioForComposable: ActivityScenarioForComposableTestRule = 
            ActivityScenarioForComposablePreviewTestRule.createFor(preview)

      @Test
      fun snapshot() {
        activityScenarioForComposableRule.onActivity {
            it.setContent {
                preview()
            }
        }

        dropshots.assertSnapshot(
            view = activityScenarioForComposable.activity.waitForComposeView()
        )
      }
   }
   ```
   - Run these Dropshots tests together with the existing ones by executing the corresponding command e.g. `./gradlew yourModule:connectedAndroidTest -Pdropshots.record`

> [!WARNING]
> Beware that Locale Strings in Preview Infos, unlike AndroidUiTestingUtils, use The BCP-47 tag but with + instead of - as separators, and have the prefix b+. Therefore, the BCP-47 tag "zh-Hans-CN" would be written as "b+zh+Hans+CN" instead. 
> So for this case, you'd have to convert locale "b+zh+Hans+CN" to "zh-Hans-CN" in order to use it with AndroidUiTestingUtils, for instance as showcased above: </br>
> `val locale = preview.previewInfo.locale.removePrefix("b+").replace("+", "-").ifBlank { "en" }`

## Advanced Usage
### Screenshot File Names
ComposablePreviewScanner also provides a class to customize the name of the generated screenshots based on its Preview Info.
By default, it does not include the Preview Info in the screenshot file name if it is the same as its default value, but it can be configured to behave differently.
That means, for @Preview(showBackground = false), showBackground would not be included in the screenshot file name since it is the default.

```kotlin
fun createScreenshotIdFor(preview: ComposablePreview<AndroidPreviewInfo>) = 
    AndroidPreviewScreenshotIdBuilder(preview)
        // Paparazzi screenshot names already include className and methodName
        // so ignore them to avoid them duplicated what might throw a FileNotFoundException
        // due to the longName
       .ignoreClassName()
       .ignoreMethodName()
       // use this if you have previews in the same file with the same method name but different signature
       .doNotignoreMethodParametersType() 
       .ignoreForId("heightDp")
       .ignoreForId("widthDp")
       .overrideDefaultIdFor(
           previewInfoName = "showBackground",
           applyInfoValue = {
               when (it.showBackground) {
                   true -> "WITH_BACKGROUND"
                   false -> "WITHOUT_BACKGROUND"
               }
           }
       )
       .build()
```

and then in your test
```kotlin
@Test
fun snapshot() {
    paparazzi.snapshot(
        name = createScreenshotIdFor(preview)
    ) {
        preview()
    }
}
```

So, for the following Preview
```kotlin
class MyClass {
    
    @Preview(widthDp = 33, heightDp = 33, fontScale = 1.5f)
    @Composable
    fun MyComposable(){
        // Composable code here
    }
}
```
createScreenshotIdFor(preview) will generate the following id: `"MyClass.MyComposable.FONT_1_5f_WITHOUT_BACKGROUND"`

### Parsing Preview Device String
Since 0.4.0, ComposablePreviewScanner also provides `DevicePreviewInfoParser.parse(device: String)`
which returns a `Device` object containing all the necessary information to support different devices in your Roborazzi & Paparazzi screenshot tests!

It can parse ALL possible combinations of "device strings" up to Android Studio Lady Bug, namely:
```kotlin
// The over 80 devices supported either by id and/or name, for instance:
@Preview(device = "id:pixel_9_pro")
@Preview(device = "name:Pixel 9 Pro")
@Preview(device = "spec:parent=pixel_9_pro, orientation=landscape, navigation=buttons")

// And custom devices
@Preview(device = "spec:width = 411dp, height = 891dp, orientation = landscape, dpi = 420, isRound = false, chinSize = 0dp, cutout = corner")
@Preview(device = "spec:id=reference_desktop,shape=Normal,width=1920,height=1080,unit=px,dpi=160") // in pixels
@Preview(device = "spec:id=reference_desktop,shape=Normal,width=1920,height=1080,unit=dp,dpi=160") // in dp
```

For further info on how to use them, see [Roborazzi](#roborazzi) and [Paparazzi](#paparazzi) sections respectively.

## How It works
This library is written on top of [ClassGraph](https://github.com/classgraph/classgraph), an uber-fast parallelized classpath scanner.

Classpath can scan everything that is available either at bytecode level or at runtime.
This is also the case of annotations without retention or with either `AnnotationRetention.BINARY` or `AnnotationRetention.RUNTIME`, like Android Composable Previews
```kotlin
package androidx.compose.ui.tooling.preview

@Retention(AnnotationRetention.BINARY)
annotation class Preview(
   // Preview code here ...
)
```

However, those with `AnnotationRetention.SOURCE` are not visible to Classgraph. Such annotations are mainly used for IDE tooling, and that is the case for the Compose-Desktop Preview annotation.
```kotlin
package androidx.compose.desktop.ui.tooling.preview

@Retention(AnnotationRetention.SOURCE)
annotation class Preview
```

### Compose Multiplatform Support
Since Compose Multiplatform 1.6.0, Jetbrains has added support for `@Preview`s in `common`. ComposablePreviewScanner can also
scan such Previews when running on any jvm-target, like
- Android
- Desktop
- Jvm

ComposablePreviewScanner provides a `CommonComposablePreviewScanner` for that purpose.

Assuming that you have:
- some Compose Multiplatform `@Previews` defined in `common`
- some Android screenshot tests in place

Here is how you could also run screenshot tests for those Compose Multiplatform `@Previews` together, for instance, with Roborazzi (would also work with Paparazzi or any Instrumentation-based library).

1. Add `:jvm` dependency for ComposablePreviewScanner 0.2.0+
   `testImplementation("io.github.sergio-sastre.ComposablePreviewScanner:jvm:<version>")`

2. Add an additional Parameterized screenshot test for these Compose Multiplatform `@Previews`
```kotlin
@RunWith(ParameterizedRobolectricTestRunner::class)
class PreviewParameterizedTests(
    private val preview: ComposablePreview<CommonPreviewInfo>,
) {

    companion object {
        // Optimization: This avoids scanning for every test
        private val cachedPreviews: List<ComposablePreview<CommonPreviewInfo>> by lazy {
            CommonComposablePreviewScanner()
                .scanPackageTrees("your.package", "your.package2") // those where your common @Previews are located
                .includeAnnotationInfoForAllOf(RoborazziConfig::class.java)
                .getPreviews()
        }

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun values(): List<ComposablePreview<CommonPreviewInfo>> = cachedPreviews
    }

    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(sdk = [30]) // same as the previews we've filtered
    @Test
    fun snapshot() {
        captureRoboImage(
           roborazziOptions = RoborazziOptionsMapper.createFor(preview)
        ) {
            preview()
        }
    }
}
```

3. Run these screenshot tests together with the existing ones by executing the corresponding command e.g. `./gradlew yourModule:recordRoborazziDebug`

### Compose-Desktop Support
As we've seen in the previous section [How it works](#how-it-works), Compose-Desktop previews are still not visible to ClassGraph since they use `AnnotationRetention.SOURCE`.
There is [already an open issue](https://youtrack.jetbrains.com/issue/CMP-5675) to change it to `AnnotationRetention.BINARY`, which would allow ClassGraph to find them.

In the meanwhile, it is also possible to workaround this limitation with ComposablePreviewScanner as follows.

1. Add `:jvm` dependency from ComposablePreviewScanner 0.2.0+ and use Roborazzi, since it is the only Screenshot Testing Library that supports Compose-Desktop
   `testImplementation("com.github.sergio-sastre.ComposablePreviewScanner:jvm:<version>")`

2. Configure Roborazzi as described [in the corresponding "Multiplatform support" section](https://github.com/takahirom/roborazzi?tab=readme-ov-file#multiplatform-support)

3. Define your own annotation. The only condition is not to define `AnnotationRetention.SOURCE`
   ```kotlin
   package my.package.path
      
   @Target(AnnotationTarget.FUNCTION)
   annotation class VisibleForScreenshotTesting
   ```
4. Annotate the Desktop Composables you want to generate screenshot tests for with this annotation, e.g.
```kotlin
   class MyClass {

        @VisibleForScreenshotTesting
        @Preview // It'd also work without this annotation
        @Composable
        fun MyDesktopComposable() { 
            // Composable code here
        }
   }
```

5. Create the test. Unfortunately, `kotlin.test` doesn't provide any means for parameterized tests, so we'll need to iterate through all previews. That has the disadvantage that, if it fails to record/verify one of the preview screenshots, subsequent screenshots won't be recorded/verified (hard asserts).

```kotlin
class DesktopPreviewScreenshotTests {

   @OptIn(ExperimentalTestApi::class)
   @Test
   fun desktopPreviews() {
      ROBORAZZI_DEBUG = true
      val listComposables = JvmAnnotationScanner(
         annotationToScanClassName = "my.package.path.VisibleForScreenshotTesting"
      )
         .scanPackageTrees("my.package.tree1", "my.package.tree2")
         .getPreviews()

      listComposables.forEach { composablePreview ->
         runDesktopComposeUiTest {
            setContent {
               composablePreview()
            }
            onRoot().captureRoboImage(
               filePath = "$composablePreview.png", // optional, works without it too
            )
         }
      }
   }
}
```

6. Run these Roborazzi tests by executing the corresponding command e.g. `./gradlew yourModule:recordRoborazziJvm` (if using the Kotlin Jvm Plugin)

# Tech talks
In these tech-talks have also been mentioned the benefits of using ComposablePreviewScanner:
- DroidKaigi 2024 [in JA 🇯🇵 with EN 🇬🇧 slides]:</br>
[Understand the mechanism! Let's do screenshots testing of Compose Previews with various variations](https://www.youtube.com/watch?app=desktop&v=c4AxUXTQgw4) by [Sumio Toyama](https://x.com/sumio_tym)</br>
- [“Fast Feedback loops & Composable Preview Scanner”](https://www.youtube.com/watch?v=SphQelcGdHk) with the Skool Android Community</br>

# Testing
The core of ComposablePreviewScanner was developed using Test-Driven Development (TDD).</br>
I strongly believe this approach is one of the key reasons the library has very few known bugs although it's widely used with over 150k monthly downloads.

However, some tests have specific preconditions and may be skipped if those aren't met.</br>
For example, when running tests to retrieve @Previews from a SourceSet other than main, such as screenshotTest or androidTest,
the corresponding compiled classes must first be generated via the appropriate Gradle task.</br>

To streamline this process and support my TDD workflow, I’ve created custom Gradle tasks that handle these prerequisites automatically,
saving time and reducing friction during development.</br>
They can also help you in case you fork this library and make some code adjustments, to ensure everything still works as expected.</br>

These custom gradle tasks are the following:</br>
1. API logic tests:`./gradlew :tests:testApi`
2. SourceSet logic tests: `./gradlew :tests:testSourceSets`
3. Paparazzi integration tests: `./gradlew :tests:paparazziPreviews` and `./gradlew :tests:paparazziPreviews -Pverify=true`
4. Roborazzi integration tests: `./gradlew :tests:roborazziPreviews` and `./gradlew :tests:roborazziPreviews -Pverify=true`

Custom gradle tasks for Android-testify integration tests (i.e. instrumentation screenshot testing libraries) coming soon

</br></br>
<a href="https://www.flaticon.com/free-icons/magnify" title="magnify icons">Composable Preview Scanner logo modified from one by Freepik - Flaticon</a>

