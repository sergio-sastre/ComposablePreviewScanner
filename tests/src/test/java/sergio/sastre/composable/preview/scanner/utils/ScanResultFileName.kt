package sergio.sastre.composable.preview.scanner.utils

/**
 * Scan Result File names for tests
 * This is used to ensure that Unit Tests running in parallel do not access the same files,
 * what would result in some tests being skipped.
 *
 * NOTE: Every Test Class should use a different [ScanResultFileName].
 * That's because Gradle can only execute tests that are in different classes in parallel.
 */
object ScanResultFileName {
    const val SCAN_RESULT_DUMPER_TEST = "scan_result_1.json"
    const val ANDROID_COMPOSABLE_PREVIEW_SCANNER_TEST = "scan_result_2.json"
    const val SCAN_RESULT_DUMPER_SCANNING_LOGGER_TEST = "scan_result_3.json"

}