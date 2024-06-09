package sergio.sastre.composable.preview.scanner.core.scanresult.filter

import io.github.classgraph.ScanResult
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.scanner.ComposablesWithPreviewsFinder

/**
 * Filter the ComposablePreviews of a given ScanResult.
 */
class ScanResultFilter<T> internal constructor(
    private val scanResult: ScanResult,
    private val composablesWithPreviewsFinder: ComposablesWithPreviewsFinder<T>,
) {
    private var scanResultFilterState = ScanResultFilterState<T>()

    fun excludeIfAnnotatedWithAnyOf(vararg annotations: Class<out Annotation>) = apply {
        scanResultFilterState = scanResultFilterState.copy(
            excludedAnnotations = annotations.toList()
        )
    }

    fun includeAnnotationInfoForAllOf(vararg annotations: Class<out Annotation>) = apply {
        scanResultFilterState = scanResultFilterState.copy(
            namesOfIncludeAnnotationsInfo = annotations.map { it.name }.toSet()
        )
    }

    fun filterPreviews(predicate: (T) -> Boolean) = apply {
        scanResultFilterState = scanResultFilterState.copy(
            meetsPreviewCriteria = predicate,
        )
    }

    fun getPreviews(): List<ComposablePreview<T>> =
        scanResult.use { scanResult ->
            scanResult.allClasses.flatMap { classInfo ->
                composablesWithPreviewsFinder.findFor(classInfo, scanResultFilterState)
            }
        }
}