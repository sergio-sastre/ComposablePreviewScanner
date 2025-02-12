package sergio.sastre.composable.preview.scanner.core.preview.exception

class PreviewParameterIsNotFirstArgumentException(
    className: String,
    methodName: String,
): RuntimeException(
    "The Preview $className.$methodName contains @PreviewParameter placed after some default parameters.\n Please place @PreviewParameter as first argument of your Preview for ComposablePreviewScanner to support it."
)