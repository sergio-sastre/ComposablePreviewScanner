package sergio.sastre.composable.preview.scanner.core.scanner.classpath

import java.util.Locale

object SourceSetClasspath {

    const val MAIN_COMPILED_CLASSES_PATH = "tmp/kotlin-classes/debug"
    const val SCREENSHOT_TEST_COMPILED_CLASSES_PATH = "intermediates/kotlinc/debugScreenshotTest/compileDebugScreenshotTestKotlin/classes"

    fun screenshotTestCompiledClassesPathForVariant(variantName: String): String = "tmp/kotlin-classes/$variantName"

    fun mainCompiledClassesPathForVariant(variantName: String): String {
        val capitalizedVariantName = variantName.replaceFirstChar { if (it. isLowerCase()) it. titlecase(Locale.ROOT) else it. toString() }
        return "intermediates/kotlinc/${variantName}ScreenshotTest/compile${capitalizedVariantName}ScreenshotTestKotlin/classes"
    }
}