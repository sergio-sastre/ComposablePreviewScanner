package sergio.sastre.composable.preview.scanner.tests.api.main.scanner

import android.content.res.Configuration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import sergio.sastre.composable.preview.scanner.AndroidStringProvider
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import org.junit.Test
import sergio.sastre.composable.preview.scanner.ListProvider
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.customextraannotation.Device
import sergio.sastre.composable.preview.scanner.android.customextraannotation.ScreenshotTestConfig
import sergio.sastre.composable.preview.scanner.android.excluded.ExcludeScreenshot
import sergio.sastre.composable.preview.scanner.android.included.IncludeScreenshot
import sergio.sastre.composable.preview.scanner.core.scanresult.RequiresLargeHeap
import sergio.sastre.composable.preview.scanner.core.preview.getAnnotation
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.exceptions.RepeatableAnnotationNotSupportedException
import sergio.sastre.composable.preview.scanner.core.utils.testFilePath
import sergio.sastre.composable.preview.scanner.utils.ScanResultFileName
import java.io.FileNotFoundException

class AndroidComposablePreviewScannerTest {

    @Test
    fun `GIVEN several package trees THEN previews from all package trees are included`() {
        val includedPlusMultiplePreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees(
                    "sergio.sastre.composable.preview.scanner.android.included",
                    "sergio.sastre.composable.preview.scanner.android.multiplepreviews"
                )
                .getPreviews()
                .map { it.previewInfo.toString() }

        val includedPreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.included")
                .getPreviews()
                .map { it.previewInfo.toString() }

        assumeTrue(includedPreviews.isNotEmpty())

        val multiplePreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.multiplepreviews")
                .getPreviews()
                .map { it.previewInfo.toString() }

        assumeTrue(includedPlusMultiplePreviews.size > includedPreviews.size)
        assumeTrue(includedPlusMultiplePreviews.size > multiplePreviews.size)

        assert(includedPlusMultiplePreviews.containsAll(includedPreviews + multiplePreviews))
        assert(includedPlusMultiplePreviews.size == (includedPreviews + multiplePreviews).size)
    }

    @Test
    fun `GIVEN same composable with different absolute path THEN their screenshot names are different`() {
        val sameComposable1Preview =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.samecomposable1")
                .getPreviews()
                .first()
                .toString()

        val sameComposable2Preview =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.samecomposable2")
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
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.included")
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
                    include = listOf("sergio.sastre.composable.preview.scanner.android.included"),
                    exclude = listOf("sergio.sastre.composable.preview.scanner.android.included.nested"),
                )
                .getPreviews()
                .map { it.toString() }

        val nestedPreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.included.nested")
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
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.multiplepreviews")
                .getPreviews()

        assert(multiplePreviews.size > 1)
    }

    @Test
    fun `GIVEN all previews in 'excluded' package contain ExcludeScreenshot annotation WHEN I exclude previews with ExcludeScreenshot THEN no preview is included`() {
        val previewsInExcludedPackage =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.excluded")
                .includeAnnotationInfoForAllOf(ExcludeScreenshot::class.java)
                .getPreviews()

        val previewsWithoutExcludeScreenshotAnnotation =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.excluded")
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
    fun `GIVEN some previews in 'included' package contain IncludeScreenshot annotation WHEN I include previews with IncludeScreenshot THEN some are included`() {
        val previewsInIncludedPackage =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.included")
                .includeAnnotationInfoForAllOf(IncludeScreenshot::class.java)
                .getPreviews()

        val previewsWithIncludedScreenshotAnnotation =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.included")
                .includeIfAnnotatedWithAnyOf(IncludeScreenshot::class.java)
                .getPreviews()

        val previewsWithoutIncludeScreenshotAnnotation =
            previewsInIncludedPackage.filter { it.getAnnotation<IncludeScreenshot>() == null }

        // Some previews contain IncludeScreenshot
        assumeTrue(previewsWithoutIncludeScreenshotAnnotation.isNotEmpty())
        assumeTrue(previewsInIncludedPackage.size > previewsWithIncludedScreenshotAnnotation.size)

        assert(previewsWithIncludedScreenshotAnnotation.isNotEmpty())
    }

    @Test
    fun `WHEN use a custom annotation for previews with night mode THEN those previews are included with night mode`() {
        val customPreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.custompreviewannotation")
                .getPreviews()

        val customPreviewParams = customPreviews.map { it.previewInfo }

        assert(customPreviewParams.all { it.uiMode == Configuration.UI_MODE_NIGHT_YES })
    }

    @Test
    fun `GIVEN private previews WHEN including them, THEN those previews are included`() {
        val privatePreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.privatepreviews")
                .includePrivatePreviews()
                .getPreviews()

        assert(privatePreviews.isNotEmpty())
    }

    @Test
    fun `GIVEN private previews WHEN not implicitly including them THEN those previews are excluded`() {
        val privatePreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.privatepreviews")
                .getPreviews()

        assert(privatePreviews.isEmpty())
    }

    @Test
    fun `GIVEN internal previews THEN those previews are included`() {
        val internalPreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.internalpreviews")
                .getPreviews()

        assert(internalPreviews.isNotEmpty())
    }

    @Test
    fun `GIVEN previews are inside a class THEN those previews are included`() {
        val previewsInsideClass =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.previewsinsideclass")
                .getPreviews()

        assert(previewsInsideClass.isNotEmpty())
    }

    @Test
    fun `GIVEN 1 duplicated preview THEN only one is included`() {
        val duplicatedPreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.duplicates")
                .getPreviews()

        assert(duplicatedPreviews.size == 1)
    }

    @Test
    fun `GIVEN 1 preview without preview parameters THEN only one is included`() {
        val preview =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.duplicates") // could take any other without previews
                .getPreviews()

        assumeTrue(preview.size == 1)
        assert(preview.first().previewIndex == null)
    }

    @Test(expected = FileNotFoundException::class)
    fun `GIVEN a scan result file doesn't exist WHEN scan result read from file THEN throw File does not exist exception`() {
        val scanResultFile = testFilePath(ScanResultFileName.ANDROID_COMPOSABLE_PREVIEW_SCANNER_TEST)
        scanResultFile.delete()
        assumeFalse(scanResultFile.exists())

        AndroidComposablePreviewScanner()
            .scanFile(scanResultFile)
            .getPreviews()
    }

    @Test
    fun `GIVEN preview parameters with StringProvider but limit 1 THEN it creates only 1 preview with index 0`() {
        val stringProviderValues = AndroidStringProvider().values.toList()
        assumeTrue(stringProviderValues.size > 1)

        val previewsWithParameterLimit1 =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.previewparameters")
                .filterPreviews { it.group == "preview-parameter-limit=1" }
                .getPreviews()

        assert(previewsWithParameterLimit1.size == 1)
        assert(previewsWithParameterLimit1.first().previewIndex == 0)
    }

    @Test
    fun `GIVEN 2 previews with same method Name but different params are considered different`() {
        val stringProviderValues = AndroidStringProvider().values.toList()
        val listProviderValues = ListProvider().values.toList()
        assumeTrue(stringProviderValues.size == 3)
        assumeTrue(listProviderValues.size == 1)

        val previewsWithSameMethodName =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.samemethodname")
                .getPreviews()

        val previewsAsStrings =
            previewsWithSameMethodName.map { it.toString() }

        // 3 for stringProvider
        // 1 for listProvider
        // 1 without parameter
        assert(previewsWithSameMethodName.size == 5)
        assert(previewsAsStrings.toSet().size == 5)
    }

    @Test
    fun `GIVEN preview parameters with StringProvider THEN it creates one preview for every parameter with the index at the end of its name`() {
        val stringProviderValues = AndroidStringProvider().values.toList()

        val previewsWithParameterNoLimit =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.previewparameters")
                .filterPreviews { it.group == "no-preview-parameter-limit" }
                .getPreviews()

        assert(previewsWithParameterNoLimit.size == stringProviderValues.size)
        previewsWithParameterNoLimit.onEachIndexed { index, preview ->
            assert(preview.toString().substringAfterLast("_") == index.toString())
        }
    }

    @Test
    fun `GIVEN preview parameters with StringProvider and MultiplePreviews THEN it creates one preview for every combination`() {
        val stringProviderValuesSize = AndroidStringProvider().values.toList().size
        /*
        @Preview                 // 1 Preview
        @MyCustomDarkModePreview // 2 Previews
        @PreviewDynamicColors    // 4 Previews
        --------------------------------------
                         TOTAL = // 7 Previews
         */
        val multiplePreviewsAmount = 7
        val expectedAmountOfPreviews = stringProviderValuesSize * multiplePreviewsAmount

        val previews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.multiplepreviewswithpreviewparameters")
                .getPreviews()

        assertEquals(previews.size, expectedAmountOfPreviews)
    }

    @Test
    fun `GIVEN preview annotated with extra custom annotation with params THEN resulting previewParam contains annotation params`() {
        val previewWithExtraAnnotation =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.customextraannotation")
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

    @Repeatable
    annotation class RepeatableAnnotation
    @Test
    fun `GIVEN includeAnnotationInfoForAllOf contains any Repeatable annotation THEN it throws RepeatableAnnotationNotSupportedException`() {
        val exception = assertThrows(RepeatableAnnotationNotSupportedException::class.java) {
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.customextraannotation")
                .includeAnnotationInfoForAllOf(RepeatableAnnotation::class.java)
                .getPreviews()
        }

        assertEquals("includeAnnotationInfoForAllOf() cannot be called with Repeatable annotations 'RepeatableAnnotation'", exception.message)
    }

    @Test
    fun `GIVEN excludeIfAnnotatedWithAnyOf contains any Repeatable annotation THEN it throws RepeatableAnnotationNotSupportedException`() {
        val exception = assertThrows(RepeatableAnnotationNotSupportedException::class.java) {
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.customextraannotation")
                .excludeIfAnnotatedWithAnyOf(RepeatableAnnotation::class.java)
                .getPreviews()
        }

        assertEquals("excludeIfAnnotatedWithAnyOf() cannot be called with Repeatable annotations 'RepeatableAnnotation'", exception.message)
    }

    @Test
    fun `GIVEN includeIfAnnotatedWithAnyOf contains any Repeatable annotation THEN it throws RepeatableAnnotationNotSupportedException`() {
        val exception = assertThrows(RepeatableAnnotationNotSupportedException::class.java) {
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.customextraannotation")
                .includeIfAnnotatedWithAnyOf(RepeatableAnnotation::class.java)
                .getPreviews()
        }

        assertEquals("includeIfAnnotatedWithAnyOf() cannot be called with Repeatable annotations 'RepeatableAnnotation'", exception.message)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `GIVEN scan package trees without arguments, THEN throw IllegalArgumentException`() {
        AndroidComposablePreviewScanner()
            .scanPackageTrees()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `GIVEN scan package trees without include arguments, THEN throw IllegalArgumentException`() {
        AndroidComposablePreviewScanner()
            .scanPackageTrees(
                include = emptyList(),
                exclude = listOf("whatever")
            )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `GIVEN includeIfAnnotatedWithAnyOf without arguments, THEN throw IllegalArgumentException`() {
        AndroidComposablePreviewScanner()
            .scanPackageTrees("sergio.sastre.composable.preview.scanner")
            .includeIfAnnotatedWithAnyOf()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `GIVEN excludeAnnotationInfoForAllOf without arguments, THEN throw IllegalArgumentException`() {
        AndroidComposablePreviewScanner()
            .scanPackageTrees("sergio.sastre.composable.preview.scanner")
            .excludeIfAnnotatedWithAnyOf()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `GIVEN includeAnnotationInfoForAllOf without arguments, THEN throw IllegalArgumentException`() {
        AndroidComposablePreviewScanner()
            .scanPackageTrees("sergio.sastre.composable.preview.scanner")
            .includeAnnotationInfoForAllOf()
    }

}