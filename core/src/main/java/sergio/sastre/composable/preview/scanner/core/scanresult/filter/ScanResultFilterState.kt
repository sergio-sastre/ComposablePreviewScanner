package sergio.sastre.composable.preview.scanner.core.scanresult.filter

data class ScanResultFilterState<T>(
    val excludedAnnotations: List<Class<out Annotation>> = emptyList(),
    val namesOfIncludeAnnotationsInfo: Set<String> = emptySet(),
    val meetsPreviewCriteria: (T) -> Boolean = { true },
)
