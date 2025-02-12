package sergio.sastre.composable.preview.scanner.core.scanner.config.classpath

import java.util.Locale

/**
 * Creates a custom Classpath for a SourceSet unknown to ComposablePreviewScanner
 *
 * @param rootDir path to the root directory where the compiled classes can be found, which is the local "build" folder of this module
 * @param packagePath path to the main package inside rootDir
 */
data class Classpath(
    val packagePath: String,
    val rootDir: String = "${System.getProperty("user.dir")}/build"
) {

    /**
     * Creates a Classpath for the given SourceSet and variant
     *
     * @param sourceSet The sourceSet whose packagePath is known
     * @param variantName The variant of the sourceSet, usually "debug" or "release" but could be a custom one e.g. custom flavours
     */
    constructor(sourceSet: SourceSet, variantName: String = "debug") : this(
        packagePath = when (sourceSet) {
            SourceSet.MAIN -> "tmp/kotlin-classes/$variantName"
            SourceSet.ANDROID_TEST -> "tmp/kotlin-classes/${variantName}AndroidTest"
            SourceSet.SCREENSHOT_TEST -> {
                val capitalizedVariantName = variantName.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                }
                "intermediates/kotlinc/${variantName}ScreenshotTest/compile${capitalizedVariantName}ScreenshotTestKotlin/classes"
            }
        }
    )
}