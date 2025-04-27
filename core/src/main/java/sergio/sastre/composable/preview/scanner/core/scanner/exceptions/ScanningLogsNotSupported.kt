package sergio.sastre.composable.preview.scanner.core.scanner.exceptions

class ScanningLogsNotSupported: Exception(
    "You can only use .enableScanningLogs() within non-Instrumentation tests.\n" +
        "Feel free to open an issue if you need this feature in Instrumentation tests."
)