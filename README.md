[![](https://jitpack.io/v/sergio-sastre/ComposablePreviewScanner.svg)](https://jitpack.io/#sergio-sastre/ComposablePreviewScanner)</br>
<a href="https://androidweekly.net/issues/issue-628">
<img src="https://androidweekly.net/issues/issue-628/badge">
</a>

# <p align="center">Composable Preview Scanner</p>


<p align="center">
<img width="400" src="https://github.com/sergio-sastre/ComposablePreviewScanner/assets/6097181/63d9676d-22c4-4bd1-8680-3fcf1a72e001">
</p>

A library to help auto-generate screenshot tests from Composable Previews with any screenshot testing library:
JVM-based (i.e. Paparazzi, Roborazzi) as well as Instrumentation-based (i.e. Shot, Dropshots, Android-Testify, etc.)

# Comparison with other solutions
|                                           | Composable Preview Scanner | Showkase      | Compose Preview Screenshot Testing |
|-------------------------------------------|------------------------------------|---------------|------------------------------------|
| Independent of AGP version                | ✅                                  | ✅             | ❌                                  |
| Library-agnostic solution                 | ✅                                  | ✅             | ❌<sup>1</sup>                      |
| Scans previews in "main" source           | ✅                                  | ✅             | ❌<sup>2</sup>                      |
| Preview Infos available                   | ✅                                  | ❌<sup>3</sup> | ✅                                  |
| Specific Config (e.g. for Libs) available | ✅<sup>4</sup>                      | ❌             | ❌                                  |
| Compose-Desktop support                   | *✅<sup>5</sup>                     | ❌<sup>6</sup> | ❌                                  |

<sup>1</sup> Compose Preview Screenshot Testing is a standalone solution based on LayoutLib, whereas ComposablePreviewScanner and Showkase provide Composables' infos so you can run screenshot tests with your favourite screenshot testing library.</br></br>
<sup>2</sup> Compose Preview Screenshot Testing requires to put the previews in a brand-new "screenshotTest" source. ComposablePreviewScanner and Showkase only work with previews in the "main" source, so if you want to have the previews used for screenshot tests separate, I recommend to create an extra module for them and their screenshot tests and do not include that module in :app.</br></br>
<sup>3</sup> Showkase components only hold information about the Composable, but not about the Preview Info (i.e. ApiLevel, Locale, UiMode, FontScale...).</br></br>
<sup>4</sup> ComposablePreviewScanner supports adding extra lib-config (e.g. Paparazzi's Rendering Mode or Roborazzi's compare options) in the form of annotations that are additionally added to the preview. You can check how in the examples below in [Jvm Screenshot Tests](#jvm-screenshot-tests) and [Instrumentation Screenshot Tests](#instrumentation-screenshot-tests) respectively.</br></br>
<sup>5</sup> ComposablePreviewScanner can also be used in JVM-targets like Compose-Desktop. However, the Previews might need to be extra-annotated to be visible to ClassGraph. Check the corresponding [Compose-Desktop Support](#compose-desktop-support) section.</br></br>
<sup>6</sup> [Showkase: Compose Multiplatform Support](https://github.com/airbnb/Showkase/issues/364)

ComposablePreviewScanner also works with:
- `@PreviewParameters`
- Multi-Previews, including  `@PreviewScreenSizes`, `@PreviewFontScales`, `@PreviewLightDark`, and `@PreviewDynamicColors`.
- private `@Previews` (from version 0.1.3 on)

but does not work with
- `@Previews` that are not located in the "main" source
- `@Previews` inside classes, as reported in [this issue](https://github.com/sergio-sastre/ComposablePreviewScanner/issues/4)


# How to set up
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
   // jvm tests
   testImplementation("com.github.sergio-sastre.ComposablePreviewScanner:android:<version>")

   // instrumentation tests
   debugImplementation("com.github.sergio-sastre.ComposablePreviewScanner:android:<version>")
}
```
For compose-desktop support, check [this issue](https://github.com/sergio-sastre/ComposablePreviewScanner/issues/3) and its workaround. </br></br>

Direct links
1. [Jvm Screenshot Tests](#jvm-screenshot-tests)</br>
   1.1  [Paparazzi](#paparazzi)</br>
   1.2  [Roborazzi](#roborazzi)</br>
2. [Instrumentation Screenshot Tests](#instrumentation-screenshot-tests)
3. [Compose Desktop Support](#compose-desktop-support)

# How to use
The API is pretty simple:

```kotlin
AndroidComposablePreviewScanner()
    .scanPackageTrees("your.package", "your.package2")
    // options to filter scanned previews
    .excludeIfAnnotatedWithAnyOf(
        ExcludeForScreenshot::class.java, 
        ExcludeForScreenshot2::class.java
    )
    .includeAnnotationInfoForAllOf(
        ScreenshotConfig::class.java,
        ScreenshotConfig2::class.java
    )
    .includePrivatePreviews() // Otherwise they are ignored
    .filterPreviews { 
        // filter by any previewInfo: name, group, apiLevel, locale, uiMode, fontScale...
        previewInfo ->  previewInfo.apiLevel == 30 
    }
    // ---
    .getPreviews()
```

There are 2 more options to scan previews:
1. All Packages: `scanAllPackages()`. This might require a huge amount of memory since it would scan not only in a set of packages, but in all packages used in your app/module (i.e. also in its transitive dependencies). This is in 99% of the cases unnecessary, and scanning the main package trees of your module should be sufficient. 
2. From a file for faster times: </br>
   2.1. `scanFile(jsonFile: File)`. The file had been previously generated by using ScanResultDumper().dumpScanResultToFile(jsonFile)</br>
   2.2. `scanFile(inputStream: InputStream)`. This is useful for Instrumentation tests as you can see in its corresponding section.

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
    ...
}
```

3. Include your annotation info in the Preview
```kotlin
object ComposablePreviewProvider : TestParameter.TestParameterValuesProvider {
    override fun provideValues(): List<ComposablePreview<AndroidPreviewInfo>> =
        AndroidComposablePreviewScanner()
            .scanPackageTrees("my.package", "my.package2")
            .includeAnnotationInfoForAllOf(PaparazziConfig::class.java)
            ... // any other filtering option
            .getPreviews()
}
```

4. Map the PreviewInfo and PaparazziConfig values. For instance, you can use a custom class for that.
```kotlin
object PaparazziPreviewRule {
    fun createFor(preview: ComposablePreview<AndroidPreviewInfo>): Paparazzi =
        Paparazzi(
            deviceConfig = PIXEL_4A.copy(
                nightMode =
                   when(preview.previewInfo.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                      true -> NightMode.NIGHT
                      false -> NightMode.NOTNIGHT
                }
                ... // other configurations
            ),
            maxPercentDifference = preview.getAnnotation<PaparazziConfig>()?.maxPercentDifference ?: 0F
        )
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
        paparazzi.snapshot {
            preview()
        }
    }
}
```

6. Run these Paparazzi tests together with the existing ones by executing the corresponding command e.g. ./gradlew yourModule:recordPaparazziDebug

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
    ...
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

object RobolectricPreviewInfosApplier {
    fun applyFor(preview: ComposablePreview<AndroidPreviewInfo>) {
        val uiMode = nightMode =
           when(preview.previewInfo.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
              true -> "+night"
              false -> "+notnight"
           }
        RuntimeEnvironment.setQualifiers(uiMode)
       ... // other configurations
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
        RobolectricPreviewInfosApplier.applyFor(preview)
        
        captureRoboImage(
           roborazziOptions = RoborazziOptionsMapper.createFor(preview)
        ) {
            preview()
        }
    }
}
```

5. Run these Roborazzi tests together with the existing ones by executing the corresponding command e.g. ./gradlew yourModule:recordRoborazziDebug

## Instrumentation Screenshot Tests
Android does not use the standard Java bytecode format and does not actually even have a runtime classpath.
Therefore, the simplest way to support instrumentation tests, is to...
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
   fun MyComposable(){
      ...
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
         val uiMode = nightMode = when(preview.previewInfo.uiMode) {
            Configuration.UI_MODE_NIGHT_YES -> UiMode.NIGHT
            else -> UiMode.DAY
         }
         return ActivityScenarioForComposableRule(
            config = ComposableConfigItem(
                uiMode = uiMode
                ... // other configurations
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
         @JvmStatic
         @ParameterizedTestRunner.Parameters
         fun values(): List<AndroidComposablePreview<PreviewInfo>> =
               AndroidComposablePreviewScanner()
                  .scanFile(getInstrumentation().context.assets.open("scan_result.json"))
                  .includeAnnotationInfoForAllOf(DropshotsConfig::class.java)
                  .getPreviews()
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
   - Run these Dropshots tests together with the existing ones by executing the corresponding command e.g. ./gradlew yourModule:connectedAndroidTest -Pdropshots.record

> WARNING
> Beware that Locale Strings in Preview Infos, unlike AndroidUiTestingUtils, use The BCP-47 tag but with + instead of - as separators, and have the prefix b+. Therefore, the BCP-47 tag "zh-Hans-CN" would be written as "b+zh+Hans+CN" instead. 
> So for this case, you'd have to convert locale "b+zh+Hans+CN" to "zh-Hans-CN" in order to use it with AndroidUiTestingUtils

## Advanced Usage
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
        ...
    }
}
```
createScreenshotIdFor(preview) will generate the following id: "MyClass.MyComposable.FONT_1_5f_WITHOUT_BACKGROUND"

## How It works
This library is written on top of [ClassGraph](https://github.com/classgraph/classgraph), an uber-fast parallelized classpath scanner.

Classpath can scan everything that is available either at bytecode level or at runtime.
This is also the case of annotations without retention or with either `AnnotationRetention.BINARY` or `AnnotationRetention.RUNTIME`, like Android Composable Previews
```kotlin
package androidx.compose.ui.tooling.preview
...
@Retention(AnnotationRetention.BINARY)
annotation class Preview(
...
)
```

However, those with `AnnotationRetention.SOURCE` are not visible to Classgraph. Such annotations are mainly used for IDE tooling, and that is the case for the Compose-Desktop Preview annotation.
```kotlin
package androidx.compose.desktop.ui.tooling.preview
...
@Retention(AnnotationRetention.SOURCE)
annotation class Preview
```

## Compose-Desktop Support
As we've seen in the previous section [How it works](#how-it-works), Compose-Desktop previews are not visible to ClassGraph.
The best solution would be to create a KSP able to read Compose-Desktop Preview annotations.

However, it is also possible to workaround this limitation with ComposablePreviewScanner as follows.

1. You'll have to copy `:core` and `:jvm` code from ComposablePreviewScanner into your module due to [this issue](https://github.com/sergio-sastre/ComposablePreviewScanner/issues/3) and use Roborazzi, since it is the only Screenshot Testing Library that supports Compose-Desktop

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
            ...
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

6. Run these Roborazzi tests by executing the corresponding command e.g. ./gradlew yourModule:recordRoborazziJvm (if using the Kotlin Jvm Plugin)


</br></br>
<a href="https://www.flaticon.com/free-icons/magnify" title="magnify icons">Composable Preview Scanner logo modified from one by Freepik - Flaticon</a>

