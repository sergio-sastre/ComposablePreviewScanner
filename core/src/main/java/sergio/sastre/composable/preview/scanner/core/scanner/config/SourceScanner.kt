package sergio.sastre.composable.preview.scanner.core.scanner.config

import sergio.sastre.composable.preview.scanner.core.scanresult.RequiresLargeHeap
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.ScanResultFilter
import java.io.File
import java.io.InputStream

interface SourceScanner<T> {

    @RequiresLargeHeap
    fun scanAllPackages(): ScanResultFilter<T>

    /**
     * Scan previews in the given packageTrees
     *
     * @param packageTrees where we want to scan previews
     */
    fun scanPackageTrees(vararg packageTrees: String): ScanResultFilter<T>

    /**
     * Scan previews in the given ‘include‘ packageTrees, excluding the 'exclude' packageTrees
     *
     * @param include where we want to scan previews
     * @param exclude where we do not want to scan previews, even though they were inside the included packageTrees
     */
    fun scanPackageTrees(include: List<String>, exclude: List<String>): ScanResultFilter<T>

    /**
     * Scan previews in the given file
     *
     * @param jsonFile a json file that was generated by using ScanResultDump.dumpScanResultToFile(fileName)
     */
    fun scanFile(jsonFile: File): ScanResultFilter<T>

    /**
     * Scan for previews in the given InputStream.
     *
     * @param targetInputStream to make them accessible in instrumentation tests, like getInstrumentation().context.assets.open(fileName).
     * Such file was generated previously by using ScanResultDump.dumpScanResultToFileInAssets(fileName)
     *
     * @param customPreviewsInfoInputStream to make custom previews defined in a dependency or module,
     * like those in "androidx.compose.ui.tooling.preview", available in instrumentation tests.
     * Such file should be generated previously, for instance,
     * by using ScanResultDump.dumpScanResultToFileInAssets(customPreviewsPackageTrees, customPreviewsFileName)
     */
    fun scanFile(
        targetInputStream: InputStream,
        customPreviewsInfoInputStream: InputStream
    ): ScanResultFilter<T>
}