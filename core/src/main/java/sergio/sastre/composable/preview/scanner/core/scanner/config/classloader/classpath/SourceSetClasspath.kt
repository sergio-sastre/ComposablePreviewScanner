package sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath

import java.util.Locale

object SourceSetClasspath {

    const val MAIN = "tmp/kotlin-classes/debug"
    const val ANDROID_TEST = "tmp/kotlin-classes/debugAndroidTest"
    const val SCREENSHOT_TEST = "intermediates/kotlinc/debugScreenshotTest/compileDebugScreenshotTestKotlin/classes"

    fun androidTestFor(variantName: String) = "tmp/kotlin-classes/${variantName}AndroidTest"

    fun mainFor(variantName: String): String = "tmp/kotlin-classes/$variantName"

    fun screenshotTestFor(variantName: String): String {
        val capitalizedVariantName = variantName.replaceFirstChar { if (it. isLowerCase()) it. titlecase(Locale.ROOT) else it. toString() }
        return "intermediates/kotlinc/${variantName}ScreenshotTest/compile${capitalizedVariantName}ScreenshotTestKotlin/classes"
    }
}