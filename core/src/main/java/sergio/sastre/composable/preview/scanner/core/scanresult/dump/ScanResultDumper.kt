package sergio.sastre.composable.preview.scanner.core.scanresult.dump

import io.github.classgraph.ClassGraph
import io.github.classgraph.ScanResult
import sergio.sastre.composable.preview.scanner.core.scanner.classloader.classpath.Classpath
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

    fun setTargetSourceSet(
        classpath: Classpath,
    ) = apply {
        val absolutePath = File(classpath.buildDir, classpath.packagePath).absolutePath
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

        //TODO - modify to dump also customPreviews
        fun dumpScanResultToFileInAssets(
            scanFileName: String,
            variantName: String = "",
            customPreviewsPackageTrees: List<String> = listOf("androidx.compose.ui.tooling.preview")
            ): ScanResultProcessor = apply {
            val path = System.getProperty("user.dir")
            val capitalizedVariantName = variantName.replaceFirstChar { if (it. isLowerCase()) it. titlecase(Locale.ROOT) else it. toString() }
            val directoryName = "$path/src/androidTest${capitalizedVariantName}/assets"
            createDirectory(directoryName)
            val outputScanFile = File(directoryName, scanFileName)
            outputScanFile.bufferedWriter().use { writer ->
                writer.write(scanResult.toJSON())
            }

            // TODO from here
            val customPreviewsScanResult = ClassGraph()
                .acceptPackages(*customPreviewsPackageTrees.toTypedArray())
                .enableAnnotationInfo()
                .scan()

            val outputCustomPreviewsFile = File(directoryName,"custom_previews.json")
            outputCustomPreviewsFile.bufferedWriter().use { writer ->
                writer.write(customPreviewsScanResult.toJSON())
            }
            // TODO till here

            println("Scan Results dump to output file path: ${outputScanFile.absolutePath}")
        }

        //TODO - check if necessary to change?
        fun dumpScanResultToFile(outputFile: File): ScanResultProcessor = apply {
            outputFile.bufferedWriter().use { writer ->
                writer.write(scanResult.toJSON())
            }
            println("Scan Results dump to output file path: ${outputFile.absolutePath}")
        }
    }
}