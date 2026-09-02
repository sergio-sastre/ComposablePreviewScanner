package sergio.sastre.composable.preview.scanner.core.scanner.logger

import io.github.classgraph.ScanResult
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreviewWithResult
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

    fun <T, R> measureFindPreviewsTimeAndGetResult(
        actionToMeasure:() -> List<ComposablePreviewWithResult<T, R>>
    ): List<ComposablePreviewWithResult<T, R>> {
        val scanResult: List<ComposablePreviewWithResult<T, R>>
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
        val logBuilder = StringBuilder()

        logBuilder.run {
            appendLine("==============================================================")
            appendLine("Composable Preview Scanner")
            appendLine("==============================================================")
            appendLine(scanningSource)
        }
        classpath?.run {
            logBuilder.appendLine("Source set (compiled classes path): $rootDir/$packagePath")
        }
        logBuilder.run {
            appendLine()
            appendLine("@Preview annotation: $annotationName")
            appendLine("Amount of @Previews found: $previewsAmount")
            appendLine()
            appendLine("Time to scan target files: $scanningFilesTime ms")
            appendLine("Time to find @Previews: $findPreviewsTime ms")
            appendLine("--------------------------------------------------------------")
            appendLine("Total time: ${scanningFilesTime + findPreviewsTime} ms")
            appendLine("==============================================================")
            appendLine()
        }
        println(logBuilder.toString())
    }
}
