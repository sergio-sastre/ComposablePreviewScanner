package sergio.sastre.composable.preview.scanner.tests.logic

import android.content.res.Configuration
import org.junit.Assert.assertEquals
import sergio.sastre.composable.preview.scanner.StringProvider
import sergio.sastre.composable.preview.scanner.customextraannotation.Device
import sergio.sastre.composable.preview.scanner.customextraannotation.ScreenshotTestConfig
import sergio.sastre.composable.preview.scanner.excluded.ExcludeScreenshot
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.Assert.assertTrue
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.core.scanresult.RequiresLargeHeap
import sergio.sastre.composable.preview.scanner.core.preview.getAnnotation
import sergio.sastre.composable.preview.scanner.core.scanner.classpath.SourceSetClasspath.MAIN_COMPILED_CLASSES_PATH
import sergio.sastre.composable.preview.scanner.core.scanner.classpath.SourceSetClasspath.SCREENSHOT_TEST_COMPILED_CLASSES_PATH
import sergio.sastre.composable.preview.scanner.core.utils.testFilePath
import java.io.FileNotFoundException

// TODO: Test cases:
// - Example that Combines CrossModuleCustomPreviews(custom/notCustom) + SameModuleCustomPreviews + just Previews + PreviewParameters
// - Source Sets: AndroidTest, ScreenshotTest(done), Main(done) and different variant?
// - Allow multiple source sets
//      1. add source set to the package as suffix e.g. sergio.sastre.composable.preview.scanner.androidtest
// - Add special method to read classpath in instrumentation tests
// - Add this functionality to scanResultDumper
class AndroidComposablePreviewScannerTest {

    @Test
    fun `GIVEN Previews from 'main' classpath THEN they are the same as Previews at buildTime`() {
        val mainClasspathPreviews =
            AndroidComposablePreviewScanner()
                .overrideClasspath(MAIN_COMPILED_CLASSES_PATH)
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .getPreviews()
                .map { it.previewInfo.toString() }

        val buildTimePreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .getPreviews()
                .map { it.previewInfo.toString() }

        assertTrue(mainClasspathPreviews.containsAll(buildTimePreviews))
        assertTrue(buildTimePreviews.containsAll(mainClasspathPreviews))
    }

    @Test
    fun `GIVEN Previews from 'screenshotTest' classpath THEN they exist and are not empty`() {
        val screenshotTestClasspathPreviews =
            AndroidComposablePreviewScanner()
                .overrideClasspath(SCREENSHOT_TEST_COMPILED_CLASSES_PATH)
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .getPreviews()
                .map { it.previewInfo.toString() }

        assertEquals(1, screenshotTestClasspathPreviews.size)
    }

    @Test
    fun `GIVEN several package trees THEN previews from all package trees are included`() {
        val includedPlusMultiplePreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees(
                    "sergio.sastre.composable.preview.scanner.included",
                    "sergio.sastre.composable.preview.scanner.multiplepreviews"
                )
                .getPreviews()
                .map { it.previewInfo.toString() }

        val includedPreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.included")
                .getPreviews()
                .map { it.previewInfo.toString() }

        assumeTrue(includedPreviews.isNotEmpty())

        val multiplePreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.multiplepreviews")
                .getPreviews()
                .map { it.previewInfo.toString() }

        assumeTrue(includedPlusMultiplePreviews.size > includedPreviews.size)
        assumeTrue(includedPlusMultiplePreviews.size > multiplePreviews.size)

        assert(includedPlusMultiplePreviews.containsAll(includedPreviews + multiplePreviews))
        assert(includedPlusMultiplePreviews.size == (includedPreviews + multiplePreviews).size)
    }

    @Test
    fun `GIVEN same composable with different absolute path THEN their screenshot names are different`(){
        val sameComposable1Preview =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.samecomposable1")
                .getPreviews()
                .first()
                .toString()

        val sameComposable2Preview =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.samecomposable2")
                .getPreviews()
                .first()
                .toString()

        assert(sameComposable1Preview != sameComposable2Preview)
    }

    @OptIn(RequiresLargeHeap::class)
    @Test
    fun `GIVEN 'included' package does not contain all previews THEN previews in 'included' are contained in all previews`() {
        val allPreviews =
            AndroidComposablePreviewScanner()
                .scanAllPackages()
                .getPreviews()
                .map { it.toString() }

        val includedPreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.included")
                .getPreviews()
                .map { it.toString() }

        assumeTrue(includedPreviews.size < allPreviews.size)

        assert(allPreviews.containsAll(includedPreviews))
    }

    @Test
    fun `GIVEN 'included nested' package THEN previews in 'included' contain previews in 'nested'`() {
        val includedPreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.included")
                .getPreviews()
                .map { it.toString() }

        val nestedPreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.included.nested")
                .getPreviews()
                .map { it.toString() }

        assumeTrue(nestedPreviews.size < includedPreviews.size)

        assert(includedPreviews.containsAll(nestedPreviews))
    }

    @Test
    fun `GIVEN 'included nested' package WHEN 'nested' excluded THEN don't return previews in 'nested'`() {
        val includedPreviewsWithoutNested =
            AndroidComposablePreviewScanner()
                .scanPackageTrees(
                    include = listOf("sergio.sastre.composable.preview.scanner.included"),
                    exclude = listOf("sergio.sastre.composable.preview.scanner.included.nested"),
                )
                .getPreviews()
                .map { it.toString() }

        val nestedPreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.included.nested")
                .getPreviews()
                .map { it.toString() }

        assumeTrue(nestedPreviews.isNotEmpty())
        assumeTrue(includedPreviewsWithoutNested.isNotEmpty())

        assert(includedPreviewsWithoutNested.none { nestedPreviews.contains(it) })
    }

    @OptIn(RequiresLargeHeap::class)
    @Test
    fun `GIVEN some previews contain api lower than 30 WHEN filtering previews with api 30+ THEN all resulting previews contain api 30+`() {
        val allPreviews =
            AndroidComposablePreviewScanner()
                .scanAllPackages()
                .getPreviews()

        val previews30Plus =
            AndroidComposablePreviewScanner()
                .scanAllPackages()
                .filterPreviews { it.apiLevel >= 30 }
                .getPreviews()

        assumeTrue(allPreviews.any { it.previewInfo.apiLevel < 30 })

        assert(previews30Plus.all { it.previewInfo.apiLevel >= 30 })
    }

    @Test
    fun `GIVEN multiple previews THEN more than 1 preview is included`() {
        val multiplePreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.multiplepreviews")
                .getPreviews()

        assert(multiplePreviews.size > 1)
    }

    @Test
    fun `GIVEN all previews in 'excluded' package contain ExcludeScreenshot annotation WHEN I exclude previews with ExcludeScreenshot THEN no preview is included`() {
        val previewsInExcludedPackage =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.excluded")
                .includeAnnotationInfoForAllOf(ExcludeScreenshot::class.java)
                .getPreviews()

        val previewsWithoutExcludeScreenshotAnnotation =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.excluded")
                .excludeIfAnnotatedWithAnyOf(ExcludeScreenshot::class.java)
                .getPreviews()

        val previewsWithExcludeScreenshotAnnotation =
            previewsInExcludedPackage.filter { it.getAnnotation<ExcludeScreenshot>() != null }

        // All previews contain ExcludedScreenshot
        assumeTrue(previewsWithExcludeScreenshotAnnotation.size == previewsInExcludedPackage.size)
        assumeTrue(previewsInExcludedPackage.size > previewsWithoutExcludeScreenshotAnnotation.size)

        assert(previewsWithoutExcludeScreenshotAnnotation.isEmpty())
    }

    @Test
    fun `WHEN use a custom annotation for previews with night mode THEN those previews are included with night mode`() {
        val customPreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.custompreviewannotation")
                .getPreviews()

        val customPreviewParams = customPreviews.map { it.previewInfo }

        assert(customPreviewParams.all { it.uiMode == Configuration.UI_MODE_NIGHT_YES })
    }

    @Test
    fun `GIVEN private previews WHEN including them, THEN those previews are included`() {
        val privatePreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.privatepreviews")
                .includePrivatePreviews()
                .getPreviews()

        assert(privatePreviews.isNotEmpty())
    }

    @Test
    fun `GIVEN private previews WHEN not implicitly including them THEN those previews are excluded`() {
        val privatePreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.privatepreviews")
                .getPreviews()

        assert(privatePreviews.isEmpty())
    }

    @Test
    fun `GIVEN internal previews THEN those previews are included`() {
        val internalPreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.internalpreviews")
                .getPreviews()

        assert(internalPreviews.isNotEmpty())
    }

    @Test
    fun `GIVEN previews are inside a class THEN those previews are included`() {
        val previewsInsideClass =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.previewsinsideclass")
                .getPreviews()

        assert(previewsInsideClass.isNotEmpty())
    }

    @Test
    fun `GIVEN 1 duplicated preview THEN only one is included`() {
        val duplicatedPreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.duplicates")
                .getPreviews()

        assert(duplicatedPreviews.size == 1)
    }

    @Test
    fun `GIVEN 1 preview without preview parameters THEN only one is included`() {
        val preview =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.duplicates") // could take any other without previews
                .getPreviews()

        assumeTrue(preview.size == 1)
        assert(preview.first().previewIndex == null)
    }

    @Test(expected = FileNotFoundException::class)
    fun `GIVEN a scan result file doesn't exist WHEN scan result read from file THEN throw File does not exist exception`() {
        val scanResultFile = testFilePath("scan_result.json")
        assumeFalse(scanResultFile.exists())

        AndroidComposablePreviewScanner()
            .scanFile(scanResultFile)
            .getPreviews()
    }

    @Test
    fun `GIVEN preview parameters with StringProvider but limit 1 THEN it creates only 1 preview with index 0`() {
        val stringProviderValues = StringProvider().values.toList()
        assumeTrue(stringProviderValues.size > 1)

        val previewsWithParameterLimit1 =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.previewparameters")
                .filterPreviews { it.group == "preview-parameter-limit=1" }
                .getPreviews()

        assert(previewsWithParameterLimit1.size == 1)
        assert(previewsWithParameterLimit1.first().previewIndex == 0)
    }

    @Test
    fun `GIVEN preview parameters with StringProvider THEN it creates one preview for every parameter with the index at the end of its name`() {
        val stringProviderValues = StringProvider().values.toList()

        val previewsWithParameterNoLimit =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.previewparameters")
                .filterPreviews { it.group == "no-preview-parameter-limit" }
                .getPreviews()

        assert(previewsWithParameterNoLimit.size == stringProviderValues.size)
        previewsWithParameterNoLimit.onEachIndexed { index, preview ->
            assert(preview.toString().substringAfterLast("_") == index.toString())
        }
    }

    @Test
    fun `GIVEN preview parameters with StringProvider and @PreviewLightDark THEN it creates one preview for every combination`() {
        val stringProviderValuesSize = StringProvider().values.toList().size
        val previewLightDarkValuesSize = 2
        val expectedAmountOfPreviews = stringProviderValuesSize * previewLightDarkValuesSize

        val previews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.multiplepreviewswithpreviewparameters")
                .getPreviews()

        assertEquals(previews.size, expectedAmountOfPreviews)
    }

    @Test
    fun `GIVEN preview annotated with extra custom annotation with params THEN resulting previewParam contains annotation params`() {
        val previewWithExtraAnnotation =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.customextraannotation")
                .includeAnnotationInfoForAllOf(ScreenshotTestConfig::class.java)
                .getPreviews()

        assumeTrue(previewWithExtraAnnotation.size == 1)

        val extraAnnotation =
            previewWithExtraAnnotation.first().getAnnotation<ScreenshotTestConfig>()

        assert(extraAnnotation?.device == Device.PIXEL_XL)
        assert(extraAnnotation?.device?.height == Device.PIXEL_XL.height)
        assert(extraAnnotation?.device?.width == Device.PIXEL_XL.width)
        assert(extraAnnotation?.locale == "ar")
        assert(extraAnnotation?.array?.get(0) == "1")
        assert(extraAnnotation?.array?.get(1) == "2")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `GIVEN scan package trees without arguments, THEN throw IllegalArgumentException`(){
        AndroidComposablePreviewScanner()
            .scanPackageTrees()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `GIVEN scan package trees without include arguments, THEN throw IllegalArgumentException`(){
        AndroidComposablePreviewScanner()
            .scanPackageTrees(
                include = emptyList(),
                exclude = listOf("whatever")
            )
    }
}