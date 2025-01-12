package sergio.sastre.composable.preview.scanner.core.scanner.config

import io.github.classgraph.ClassGraph
import io.github.classgraph.ScanResult
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.previewfinder.ClasspathPreviewsFinder
import sergio.sastre.composable.preview.scanner.core.scanresult.RequiresLargeHeap
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.ScanResultFilter
import java.io.File
import java.io.InputStream

class ClassGraphSourceScanner<T>(
    private val classGraph: ClassGraph,
    private val findComposableWithPreviewsInClass: ClasspathPreviewsFinder<T>,
): SourceScanner<T> {

    private var updatedClassGraph = classGraph

    /**
     * Scan previews in all packages, including those of external dependencies.
     */
    @RequiresLargeHeap
    override fun scanAllPackages(): ScanResultFilter<T> {
        return ScanResultFilter(
            updatedClassGraph.scan(),
            findComposableWithPreviewsInClass,
        )
    }

    /**
     * Scan previews in the given packageTrees
     *
     * @param packageTrees where we want to scan previews
     */
    override fun scanPackageTrees(vararg packageTrees: String): ScanResultFilter<T> {
        if (packageTrees.isEmpty()) {
            throw IllegalArgumentException("packages must not be empty. For that, use scanAllPackages() instead")
        }
        updatedClassGraph = updatedClassGraph.acceptPackages(*packageTrees)
        return ScanResultFilter(
            updatedClassGraph.scan(),
            findComposableWithPreviewsInClass,
        )
    }

    /**
     * Scan previews in the given ‘include‘ packageTrees, excluding the 'exclude' packageTrees
     *
     * @param include where we want to scan previews
     * @param exclude where we do not want to scan previews, even though they were inside the included packageTrees
     */
    override fun scanPackageTrees(include: List<String>, exclude: List<String>): ScanResultFilter<T> {
        if (include.isEmpty()) {
            throw IllegalArgumentException("include packages must not be empty. For that, use scanAllPackages() instead")
        }
        updatedClassGraph = updatedClassGraph
            .acceptPackages(*include.toTypedArray())
            .rejectPackages(*exclude.toTypedArray())
        return ScanResultFilter(
            updatedClassGraph.scan(),
            findComposableWithPreviewsInClass,
        )
    }

    /**
     * Scan previews in the given file
     *
     * @param jsonFile a json file that was generated by using ScanResultDump.dumpScanResultToFile(fileName)
     */
    override fun scanFile(jsonFile: File): ScanResultFilter<T> {
        val scanResult = ScanResult.fromJSON(jsonFile.bufferedReader().use { it.readLine() })
        return ScanResultFilter(
            scanResult,
            findComposableWithPreviewsInClass,
        )
    }

    /**
     * Scan for previews in the given InputStream.
     *
     * @param targetInputStream to make them accessible in instrumentation tests, like getInstrumentation().context.assets.open(fileName).
     * Such file was generated previously by using ScanResultDump.dumpScanResultToFileInAssets(fileName)
     *
     * @param customPreviewsInfoInputStream to make custom previews defined in a dependency or module,
     * like those in "androidx.compose.ui.tooling.preview", available in instrumentation tests.
     * Such file was generated previously by using ScanResultDump.dumpScanResultToFileInAssets(customPreviewsPackageTrees, customPreviewsFileName)
     */
    override fun scanFile(
        targetInputStream: InputStream,
        customPreviewsInfoInputStream: InputStream
    ): ScanResultFilter<T> {
        val scanResult =
            ScanResult.fromJSON(targetInputStream.bufferedReader().use { it.readLine() })
        findComposableWithPreviewsInClass
            .applyCustomPreviewsScanResult(
                ScanResult.fromJSON(
                    customPreviewsInfoInputStream.bufferedReader().use { it.readLine() })
            )
        return ScanResultFilter(
            scanResult,
            findComposableWithPreviewsInClass,
        )
    }
}