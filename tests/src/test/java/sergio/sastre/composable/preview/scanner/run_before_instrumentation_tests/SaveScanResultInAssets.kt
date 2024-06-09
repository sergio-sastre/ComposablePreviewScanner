package sergio.sastre.composable.preview.scanner.run_before_instrumentation_tests

import org.junit.Test
import sergio.sastre.composable.preview.scanner.core.scanresult.dump.ScanResultDumper
import sergio.sastre.composable.preview.scanner.core.utils.assetsFilePath

class SaveScanResultInAssets {
    @Test
    fun `task -- save scan result in assets`() {
        val scanResultFileName = "scan_result.json"

        ScanResultDumper()
            .scanPackageTrees("composable.preview.scanner")
            .dumpScanResultToFileInAssets(scanResultFileName)

        assert(
            assetsFilePath(scanResultFileName).exists()
        )
    }
}