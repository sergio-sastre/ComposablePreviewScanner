package sergio.sastre.composable.preview.scanner.core.scanresult.dump

import io.github.classgraph.ClassGraph
import io.github.classgraph.ScanResult
import sergio.sastre.composable.preview.scanner.core.scanresult.RequiresLargeHeap
import java.io.File
import java.io.FileNotFoundException
import java.util.Locale

/**
 * Dumps the ScanResult of the target package trees into a file
 */
class ScanResultDumper {
    private var updatedClassGraph = ClassGraph()
        .enableClassInfo()
        .enableMethodInfo()
        .enableAnnotationInfo()
        .enableMemoryMapping()

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

        fun dumpScanResultToFileInAssets(
            fileName: String,
            flavourName: String = ""
        ): ScanResultProcessor = apply {
            val path = System.getProperty("user.dir")
            val directoryName = "$path/src/androidTest${flavourName.capitalize(Locale.ROOT)}/assets"
            createDirectory(directoryName)
            val outputFile = File(directoryName, fileName)
            outputFile.bufferedWriter().use { writer ->
                writer.write(scanResult.toJSON())
            }
            println("Scan Results dump to output file path: ${outputFile.absolutePath}")
        }

        fun dumpScanResultToFile(outputFile: File): ScanResultProcessor = apply {
            outputFile.bufferedWriter().use { writer ->
                writer.write(scanResult.toJSON())
            }
            println("Scan Results dump to output file path: ${outputFile.absolutePath}")
        }
    }
}