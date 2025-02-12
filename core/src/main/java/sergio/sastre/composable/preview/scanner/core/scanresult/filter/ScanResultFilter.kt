package sergio.sastre.composable.preview.scanner.core.scanresult.filter

import io.github.classgraph.ScanResult
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.PreviewsFinder
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.exceptions.RepeatableAnnotationNotSupportedException

/**
 * Filter the ComposablePreviews of a given ScanResult.
 */
class ScanResultFilter<T> internal constructor(
    private val scanResult: ScanResult,
    private val previewsFinder: PreviewsFinder<T>,
) {
    private var scanResultFilterState = ScanResultFilterState<T>()

    /**
     * Excludes previews which use any of the given annotations, so they will not be returned
     *
     * WARNING: throws a [RepeatableAnnotationNotSupportedException] if any of the annotations is repeatable
     */
    fun excludeIfAnnotatedWithAnyOf(vararg annotations: Class<out Annotation>) = apply {
        throwExceptionIfAnyAnnotationIsRepeatable(
            methodName = "excludeIfAnnotatedWithAnyOf()",
            annotations = annotations.toList()
        )
        scanResultFilterState = scanResultFilterState.copy(
            excludedAnnotations = annotations.toList()
        )
    }

    /**
     * Relevant info for screenshot testing a given preview
     * (e.g. tolerance, renderingMode, etc.) can be passed via annotations. For instance
     *
     * @ScreenshotTestConfig(tolerance = 0.85f)
     * @Preview
     * fun MyComposable() { ... }
     *
     * By default, that info is ignored.
     *
     * This makes that info in the annotations accessible in your screenshot tests
     * via (following the previous example) ComposablePreview.getAnnotation<ScreenshotTestConfig>()
     *
     * WARNING: throws a [RepeatableAnnotationNotSupportedException] if any of the annotations is repeatable
     */
    fun includeAnnotationInfoForAllOf(vararg annotations: Class<out Annotation>) = apply {
        throwExceptionIfAnyAnnotationIsRepeatable(
            methodName = "includeAnnotationInfoForAllOf()",
            annotations = annotations.toList()
        )
        scanResultFilterState = scanResultFilterState.copy(
            namesOfIncludeAnnotationsInfo = annotations.map { it.name }.toSet()
        )
    }

    /**
     * By default, private previews are filtered out. You can use this option to also return them
     */
    fun includePrivatePreviews() = apply {
        scanResultFilterState = scanResultFilterState.copy(
            includesPrivatePreviews = true
        )
    }

    /**
     * Filter only previews whose info meets the predicate, for instance
     * apiLevel >= 30 or group == "IncludeForScreenshotTests"
     */
    fun filterPreviews(predicate: (T) -> Boolean) = apply {
        scanResultFilterState = scanResultFilterState.copy(
            meetsPreviewCriteria = predicate,
        )
    }

    fun getPreviews(): List<ComposablePreview<T>> =
        scanResult.use { scanResult ->
            scanResult
                .allClasses
                .asSequence()
                .flatMap { classInfo ->
                    previewsFinder.findPreviewsFor(
                        classInfo,
                        scanResultFilterState,
                    )
                }
                .toList()
        }

    private fun throwExceptionIfAnyAnnotationIsRepeatable(
        methodName: String,
        annotations: List<Class<out Annotation>>
    ) {
        val repeatableAnnotations = annotations.filter { it.isAnnotationPresent(Repeatable::class.java) }
        if (repeatableAnnotations.isNotEmpty()){
            throw RepeatableAnnotationNotSupportedException(
                methodName = methodName,
                repeatableAnnotations = repeatableAnnotations
            )
        }
    }
}