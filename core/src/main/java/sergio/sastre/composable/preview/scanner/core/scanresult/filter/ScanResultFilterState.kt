package sergio.sastre.composable.preview.scanner.core.scanresult.filter

import io.github.classgraph.MethodInfo

data class ScanResultFilterState<T>(
    val overriddenClasspath: Boolean = false,
    val excludedAnnotations: List<Class<out Annotation>> = emptyList(),
    val includedAnnotations: List<Class<out Annotation>> = emptyList(),
    val namesOfIncludeAnnotationsInfo: Set<String> = emptySet(),
    val meetsPreviewCriteria: (T) -> Boolean = { true },
    val includesPrivatePreviews: Boolean = false,
) {
    fun shouldIncludeMethod(methodInfo: MethodInfo): Boolean =
        (hasExcludedAnnotation(methodInfo) || excludesMethod(methodInfo)).not() && hasIncludedAnnotation(methodInfo)

    private fun excludesMethod(methodInfo: MethodInfo): Boolean =
        !includesPrivatePreviews && methodInfo.isPrivate

    private fun hasExcludedAnnotation(methodInfo: MethodInfo): Boolean =
        when (excludedAnnotations.isNotEmpty()) {
            true -> excludedAnnotations.any { methodInfo.getAnnotationInfo(it) != null }
            false -> false
        }

    private fun hasIncludedAnnotation(methodInfo: MethodInfo): Boolean =
        when (includedAnnotations.isNotEmpty()) {
            true -> includedAnnotations.any { methodInfo.getAnnotationInfo(it) != null }
            false -> true
        }
}
