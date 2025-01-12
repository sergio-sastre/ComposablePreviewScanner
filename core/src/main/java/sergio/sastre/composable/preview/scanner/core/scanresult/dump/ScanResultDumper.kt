package sergio.sastre.composable.preview.scanner.core.scanresult.dump

import io.github.classgraph.ClassGraph
import io.github.classgraph.ScanResult
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanresult.RequiresLargeHeap
import java.io.File
import java.io.FileNotFoundException
import java.util.Locale

/**
 * Dumps the ScanResult of the target package trees into a file
 */
class ScanResultDumper {
    private var updatedClassGraph = ClassGraph()
        .ignoreMethodVisibility()
        .enableClassInfo()
        .enableMethodInfo()
        .enableAnnotationInfo()
        .enableMemoryMapping()

    // TODO -> rewrite this to allow api

    /**
     * Prepares the dumper to find previews scanned from a Source Set like 'screenshotTest', 'androidTest', 'main' or a custom one via the given sourceSetClasspath
     * It uses compiled classes of that source set.
     * Check Classpath to find their locations under the /build folder of the corresponding module.
     *
     * Make sure those compiled classes exist and are up to date before scanning them.
     * For that you can execute ./gradlew :<module>:compile<variant><sourceSet>Kotlin,
     * for instance: ./gradlew :mymodule:compileReleaseScreenshotTestKotlin
     *
     * @param sourceSetClasspath the classpath pointing to the package where compiled classes of a Source Set are located
     */
    fun setTargetSourceSet(
        sourceSetClasspath: Classpath,
    ) = apply {
        val absolutePath = File(sourceSetClasspath.rootDir, sourceSetClasspath.packagePath).absolutePath
        updatedClassGraph.overrideClasspath(absolutePath)
    }

    @RequiresLargeHeap
    fun scanAllPackages(): ScanResultProcessor {
        return ScanResultProcessor(updatedClassGraph.scan())
    }

    fun scanPackageTrees(vararg packages: String): ScanResultProcessor {
        updatedClassGraph = updatedClassGraph.acceptPackages(*packages)
        return ScanResultProcessor(updatedClassGraph.scan())
    }

    inner class ScanResultProcessor internal constructor(
        private val scanResult: ScanResult
    ) {

        private fun createDirectory(directoryName: String): File {
            val dir = File(directoryName)
            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    println("Assets Directory created: ${dir.absolutePath}");
                } else {
                    throw FileNotFoundException("Assets Directory has not been created: ${dir.absolutePath}");
                }
            }
            return dir
        }

        /**
         * Dumps the ScanResult of the target package trees for the given variantName (.e.g debug/release) into a file in assets,
         * so it can be read while executing instrumentation tests.
         *
         * @param scanFileName name of the file here the previews info is stored
         * @param variantName usually "debug" or "release" but could be any other custom buildType or flavour.
         * @param packageTreesOfCustomPreviews package where external previews (i.e. previews defined in another dependency or module)
         * different can be found.
         * In most cases, you just need the default "androidx.compose.ui.tooling.preview" package,
         * unless you see some custom-annotated-Previews missing, whose annotation packages should be added here.
         * @param customPreviewsFileName name of the file where the packageTreesOfCustomPreviews info is stored
         */
        fun dumpScanResultToFileInAssets(
            scanFileName: String,
            variantName: String = "",
            packageTreesOfCustomPreviews: List<String> = listOf("androidx.compose.ui.tooling.preview"),
            customPreviewsFileName: String = "custom_previews.json"
        ): ScanResultProcessor = apply {
            val path = System.getProperty("user.dir")
            val capitalizedVariantName =
                variantName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            val directoryName = "$path/src/androidTest${capitalizedVariantName}/assets"
            createDirectory(directoryName)

            val outputScanFile = File(directoryName, scanFileName)
            outputScanFile.bufferedWriter().use { writer ->
                writer.write(scanResult.toJSON())
            }
            println("Scan Results dump to output file path: ${outputScanFile.absolutePath}")

            if (packageTreesOfCustomPreviews.isNotEmpty()) {
                val customPreviewsScanResult = ClassGraph()
                    .acceptPackages(*packageTreesOfCustomPreviews.toTypedArray())
                    .enableAnnotationInfo()
                    .scan()

                val outputCustomPreviewsFile = File(directoryName, customPreviewsFileName)
                outputCustomPreviewsFile.bufferedWriter().use { writer ->
                    writer.write(customPreviewsScanResult.toJSON())
                }

                println("Custom Previews dump to output file path: ${outputCustomPreviewsFile.absolutePath}")
            }
        }

        fun dumpScanResultToFile(
            outputFile: File
        ): ScanResultProcessor = apply {
            outputFile.bufferedWriter().use { writer ->
                writer.write(scanResult.toJSON())
            }
            println("Scan Results dump to output file path: ${outputFile.absolutePath}")
        }
    }
}