package sergio.sastre.composable.preview.scanner.core.scanner.logger

import io.github.classgraph.ScanResult
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import kotlin.system.measureTimeMillis

class ScanningTimeLogger {

    private var scanningFilesTime: Long = 0
    private var findPreviewsTime: Long = 0
    private var scanningSource: String = ""
    private var annotationName: String = ""
    private var previewsAmount: Int = 0
    private var classpath: Classpath? = null
    private var isLoggingEnabled: Boolean = true

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

    fun <T>measureFindPreviewsTimeAndGetResult(
        actionToMeasure:() -> List<ComposablePreview<T>>
    ): List<ComposablePreview<T>> {
        val scanResult: List<ComposablePreview<T>>
        val durationInMillis = measureTimeMillis {
            scanResult = actionToMeasure()
        }
        this.findPreviewsTime = durationInMillis
        return scanResult
    }

    fun setLoggingEnabled(isLoggingEnabled: Boolean) {
        this.isLoggingEnabled = isLoggingEnabled
    }

    fun setFindPreviewsTime(findPreviewsTime: Long) {
        this.findPreviewsTime = findPreviewsTime
    }

    fun setPackageTrees(vararg packageTrees: String) {
        scanningSource = "Package trees: ${packageTrees.joinToString(", ")}"
    }

    fun setPackageTrees(included: List<String>, excluded: List<String>) {
        scanningSource =
            "Included package trees: ${included.joinToString(", ")}" +
                    "\n" +
                    "Excluded package trees: ${excluded.joinToString(", ")}"
    }

    fun setAllPackages() {
        scanningSource = "Scans all packages"
    }

    fun setScanningFromFile(fileName: String) {
        scanningSource = "Scans from file: $fileName"
    }

    fun setSourceSet(classpath: Classpath) {
        this.classpath = classpath
    }

    fun setAnnotationName(annotationName: String) {
        this.annotationName = annotationName
    }

    fun setAmountOfPreviews(amount: Int) {
        this.previewsAmount = amount
    }

    fun printFullInfoLog() {
        if (!isLoggingEnabled) return

        println("Composable Preview Scanner")
        println("===============================")
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
        println("---------------------------------------------------")
        println("Total time: ${scanningFilesTime + findPreviewsTime} ms")
        println("===============================")
        println()
        println()
    }
}