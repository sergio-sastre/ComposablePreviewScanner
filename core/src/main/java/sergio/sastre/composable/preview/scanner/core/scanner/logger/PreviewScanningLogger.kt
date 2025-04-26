package sergio.sastre.composable.preview.scanner.core.scanner.logger

import io.github.classgraph.ScanResult
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import kotlin.system.measureTimeMillis

internal class PreviewScanningLogger {

    private var scanningFilesTime: Long = 0
    private var findPreviewsTime: Long = 0
    private var scanningSource: String = ""
    private var annotationName: String = ""
    private var previewsAmount: Int = 0
    private var classpath: Classpath? = null
    private var isLoggingEnabled: Boolean = false

    fun measureScanningTimeAndGetResult(
        actionToMeasure:() -> ScanResult
    ): ScanResult {
        val scanResult: ScanResult
        val durationInMillis = measureTimeMillis {
            scanResult = actionToMeasure()
        }
        this.scanningFilesTime = durationInMillis
        return scanResult
    }

    fun <T> measureFindPreviewsTimeAndGetResult(
        actionToMeasure:() -> List<ComposablePreview<T>>
    ): List<ComposablePreview<T>> {
        val scanResult: List<ComposablePreview<T>>
        val durationInMillis = measureTimeMillis {
            scanResult = actionToMeasure()
        }
        this.findPreviewsTime = durationInMillis
        return scanResult
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

    fun useScanningSourceFile(fileName: String) {
        scanningSource = "Scans from file: $fileName"
    }

    fun addSourceSetInfo(classpath: Classpath) {
        this.classpath = classpath
    }

    fun addPreviewAnnotationName(annotationName: String) {
        this.annotationName = annotationName
    }

    fun addAmountOfPreviews(amount: Int) {
        this.previewsAmount = amount
    }

    fun printFullInfoLog() {
        if (!isLoggingEnabled) return

        println("==============================================================")
        println("Composable Preview Scanner")
        println("==============================================================")
        println(scanningSource)
        classpath?.run {
            println("Source set (compiled classes path): $rootDir/$packagePath")
        }
        println()
        println("@Preview annotation: $annotationName")
        println("Amount of @Previews found: $previewsAmount")
        println()
        println("Time to scan target files: $scanningFilesTime ms")
        println("Time to find @Previews: $findPreviewsTime ms")
        println("--------------------------------------------------------------")
        println("Total time: ${scanningFilesTime + findPreviewsTime} ms")
        println("==============================================================")
        println()
    }
}
