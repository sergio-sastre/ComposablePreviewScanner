package sergio.sastre.composable.preview.scanner.tests.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.SourceSet.MAIN
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.SourceSet.SCREENSHOT_TEST
import sergio.sastre.composable.preview.scanner.core.scanresult.RequiresLargeHeap
import java.io.File

class AndroidComposablePreviewScannerSourceSetTest {

    @Test
    fun `GIVEN Previews from 'main' classpath THEN they are the same as Previews at buildTime`() {
        val classpathMain = Classpath(MAIN)
        val file = File(classpathMain.rootDir, classpathMain.packagePath)
        assumeTrue(file.exists())

        val mainClasspathPreviews =
            AndroidComposablePreviewScanner()
                .setTargetSourceSet(
                    sourceSetClasspath = classpathMain,
                    packageTreesOfCrossModuleCustomPreviews = listOf("sergio.sastre.composable.preview.custompreviews")
                )
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
    fun `GIVEN Previews from 'main' release classpath THEN they are the same as Previews at buildTime`() {
        val classpathMainRelease = Classpath(MAIN, "release")
        val file = File(classpathMainRelease.rootDir, classpathMainRelease.packagePath)
        assumeTrue(file.exists())

        val mainReleaseClasspathPreviews =
            AndroidComposablePreviewScanner()
                .setTargetSourceSet(
                    sourceSetClasspath = classpathMainRelease,
                    packageTreesOfCrossModuleCustomPreviews = listOf("sergio.sastre.composable.preview.custompreviews")
                )
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .getPreviews()
                .map { it.previewInfo.toString() }

        val buildTimePreviews =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .getPreviews()
                .map { it.previewInfo.toString() }

        assertTrue(mainReleaseClasspathPreviews.containsAll(buildTimePreviews))
        assertTrue(buildTimePreviews.containsAll(mainReleaseClasspathPreviews))
    }

    @OptIn(RequiresLargeHeap::class)
    @Test
    fun `GIVEN Previews from 'main' classpath for allpackages THEN they are the same as Previews at buildTime`() {
        val classpathMainRelease = Classpath(MAIN)
        val file = File(classpathMainRelease.rootDir, classpathMainRelease.packagePath)
        assumeTrue(file.exists())

        val mainReleaseClasspathPreviews =
            AndroidComposablePreviewScanner()
                .setTargetSourceSet(
                    sourceSetClasspath = classpathMainRelease,
                    packageTreesOfCrossModuleCustomPreviews = listOf("sergio.sastre.composable.preview.custompreviews")
                )
                .scanAllPackages()
                .getPreviews()
                .map { it.previewInfo.toString() }

        val buildTimePreviews =
            AndroidComposablePreviewScanner()
                .scanAllPackages()
                .getPreviews()
                .map { it.previewInfo.toString() }

        assertTrue(mainReleaseClasspathPreviews.containsAll(buildTimePreviews))
        assertTrue(buildTimePreviews.containsAll(mainReleaseClasspathPreviews))
    }

    // run this test after executing ./gradlew tests:compileReleaseScreenshotTestKotlin
    @Test
    fun `GIVEN Previews from 'screenshotTest' debug and release classpaths THEN they are not empty and contain the same previews`() {
        val classpathScreenshotTest = Classpath(SCREENSHOT_TEST)
        val file = File(classpathScreenshotTest.rootDir, classpathScreenshotTest.packagePath)
        assumeTrue(file.exists())

        val classpathScreenshotTestRelease = Classpath(SCREENSHOT_TEST, "release")
        val fileRelease = File(classpathScreenshotTestRelease.rootDir, classpathScreenshotTestRelease.packagePath)
        assumeTrue(fileRelease.exists())

        val screenshotTestClasspathPreviews =
            AndroidComposablePreviewScanner()
                .setTargetSourceSet(
                    sourceSetClasspath = classpathScreenshotTest,
                    packageTreesOfCrossModuleCustomPreviews = listOf("sergio.sastre.composable.preview.custompreviews")
                )
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .getPreviews()
                .map { it.previewInfo.toString() }

        val screenshotTestReleaseClasspathPreviews =
            AndroidComposablePreviewScanner()
                .setTargetSourceSet(
                    sourceSetClasspath = classpathScreenshotTestRelease,
                    packageTreesOfCrossModuleCustomPreviews = listOf("sergio.sastre.composable.preview.custompreviews")
                )
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .getPreviews()
                .map { it.previewInfo.toString() }

        assertTrue(
            screenshotTestClasspathPreviews.containsAll(
                screenshotTestReleaseClasspathPreviews
            )
        )
        assertTrue(
            screenshotTestReleaseClasspathPreviews.containsAll(
                screenshotTestClasspathPreviews
            )
        )
        assertEquals(17, screenshotTestClasspathPreviews.size)
    }
}