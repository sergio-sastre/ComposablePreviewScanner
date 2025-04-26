package sergio.sastre.composable.preview.scanner.core.scanresult.logger

import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import java.io.File
import kotlin.system.measureTimeMillis

internal class ScanResultLogger {

    private var dumpScanResultTime: Long = 0
    private var scanningSource: String = ""
    private var classpath: Classpath? = null
    private var scanResultFileName: String? = null
    private var customPreviewsFileName: String? = null
    private var isLoggingEnabled: Boolean = false

    fun measureDumpTime(
        actionToMeasure:() -> Unit
    ) {
        val durationInMillis = measureTimeMillis {
            actionToMeasure()
        }
        this.dumpScanResultTime = durationInMillis
    }

    fun enableLogging(isLoggingEnabled: Boolean) {
        this.isLoggingEnabled = isLoggingEnabled
    }

    fun useScanningSourcePackageTrees(vararg packageTrees: String) {
        scanningSource = "Package trees: ${packageTrees.joinToString(", ")}"
    }

    fun useScanningSourcePackageTrees(included: List<String>, excluded: List<String>) {
        scanningSource =
            "Included package trees: ${included.joinToString(", ")}" +
                    "\n" +
                    "Excluded package trees: ${excluded.joinToString(", ")}"
    }

    fun useScanningSourceAllPackages() {
        scanningSource = "Scans all packages"
    }

    fun addSourceSetInfo(classpath: Classpath) {
        this.classpath = classpath
    }

    fun addScanResultFileName(file: File) {
        this.scanResultFileName = file.absolutePath
    }

    fun addCustomPreviewsFileName(file: File) {
        this.customPreviewsFileName = file.absolutePath
    }

    fun printFullInfoLog() {
        if (!isLoggingEnabled) return

        println("==============================================================")
        println("Scan Result Dumper")
        println("==============================================================")
        println(scanningSource)
        classpath?.run {
            println("Source set (compiled classes path): $rootDir/$packagePath")
        }
        scanResultFileName?.run {
            println("File to dump Scan Result: $this")
        }
        customPreviewsFileName?.run {
            println("File to dump Custom Previews: $this")
        }
        println()
        println("Time to dump scan result: $dumpScanResultTime ms")
        println("==============================================================")
        println()
    }
}
