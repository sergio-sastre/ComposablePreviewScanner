package sergio.sastre.composable.preview.scanner.core.scanresult.filter

import io.github.classgraph.MethodInfo

data class ScanResultFilterState<T>(
    val overridenClasspath: Boolean = false,
    val excludedAnnotations: List<Class<out Annotation>> = emptyList(),
    val namesOfIncludeAnnotationsInfo: Set<String> = emptySet(),
    val meetsPreviewCriteria: (T) -> Boolean = { true },
    val includesPrivatePreviews: Boolean = false,
) {
    fun excludesMethod(methodInfo: MethodInfo): Boolean =
        !includesPrivatePreviews && methodInfo.isPrivate

    fun hasExcludedAnnotation(methodInfo: MethodInfo): Boolean =
        when (excludedAnnotations.isNotEmpty()) {
            true -> excludedAnnotations.any { methodInfo.getAnnotationInfo(it) != null }
            false -> false
        }
}
