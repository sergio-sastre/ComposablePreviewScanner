package sergio.sastre.composable.preview.scanner.core.scanner.exceptions

class ScanSourceNotSupported: Exception(
    "You can only use .scanFile(targetInputStream: InputStream, customPreviewsInfoInputStream: InputStream) with Instrumentation tests.\n" +
        "For that, dump the @Preview scanned infos in a json file and place them in the assets folder by using ScanResultDumper in a custom gradle task or in a unit test, as described in the documentation: https://github.com/sergio-sastre/ComposablePreviewScanner?tab=readme-ov-file#instrumentation-screenshot-tests"
)