package sergio.sastre.composable.preview.scanner.core.scanresult.filter

import io.github.classgraph.ScanResult
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.PreviewsFinder
import sergio.sastre.composable.preview.scanner.core.scanner.logger.PreviewScanningLogger
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.exceptions.RepeatableAnnotationNotSupportedException

/**
 * The reason for these interfaces is to avoid API misuse:
 * If includeIfAnnotatedWithAnyOf() is called, then excludeIfAnnotatedWithAnyOf() cannot be called,
 * and vice versa.
 *
 * They are mutually exclusive by their API design
 */
interface BaseScanResultFilter<T> {
    fun includeAnnotationInfoForAllOf(vararg annotations: Class<out Annotation>): ScanResultFilter<T>
    fun includePrivatePreviews(): ScanResultFilter<T>
    fun filterPreviews(predicate: (T) -> Boolean): ScanResultFilter<T>
    fun getPreviews(): List<ComposablePreview<T>>
}

interface InitialScanResultFilter<T> : BaseScanResultFilter<T> {
    fun excludeIfAnnotatedWithAnyOf(vararg annotations: Class<out Annotation>): ExclusiveFilter<T>
    fun includeIfAnnotatedWithAnyOf(vararg annotations: Class<out Annotation>): InclusiveFilter<T>
}

interface ExclusiveFilter<T> : BaseScanResultFilter<T> {
    override fun includeAnnotationInfoForAllOf(vararg annotations: Class<out Annotation>): ScanResultFilter<T>
    override fun includePrivatePreviews(): ScanResultFilter<T>
    override fun filterPreviews(predicate: (T) -> Boolean): ScanResultFilter<T>
}

interface InclusiveFilter<T> : BaseScanResultFilter<T> {
    override fun includeAnnotationInfoForAllOf(vararg annotations: Class<out Annotation>): ScanResultFilter<T>
    override fun includePrivatePreviews(): ScanResultFilter<T>
    override fun filterPreviews(predicate: (T) -> Boolean): ScanResultFilter<T>
}

/**
 * Filter the ComposablePreviews of a given ScanResult.
 */
class ScanResultFilter<T> internal constructor(
    private val scanResult: ScanResult,
    private val previewsFinder: PreviewsFinder<T>,
    private val previewScanningLogger: PreviewScanningLogger,
) : InitialScanResultFilter<T>, ExclusiveFilter<T>, InclusiveFilter<T> {
    private var scanResultFilterState = ScanResultFilterState<T>()

    /**
     * Excludes previews which use any of the given annotations, so they will not be returned
     *
     * WARNING: throws a [RepeatableAnnotationNotSupportedException] if any of the annotations is repeatable
     */
    override fun excludeIfAnnotatedWithAnyOf(
        vararg annotations: Class<out Annotation>
    ): ExclusiveFilter<T> {
        throwExceptionIfAnyAnnotationIsRepeatable(
            methodName = "excludeIfAnnotatedWithAnyOf()",
            annotations = annotations.toList()
        )
        scanResultFilterState = scanResultFilterState.copy(
            excludedAnnotations = annotations.toList()
        )
        return this
    }

    /**
     * Includes previews which use any of the given annotations
     *
     * WARNING: throws a [RepeatableAnnotationNotSupportedException] if any of the annotations is repeatable
     */
    override fun includeIfAnnotatedWithAnyOf(
        vararg annotations: Class<out Annotation>
    ): InclusiveFilter<T> {
        throwExceptionIfAnyAnnotationIsRepeatable(
            methodName = "includeIfAnnotatedWithAnyOf()",
            annotations = annotations.toList()
        )
        scanResultFilterState = scanResultFilterState.copy(
            includedAnnotations = annotations.toList()
        )
        return this
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
    override fun includeAnnotationInfoForAllOf(vararg annotations: Class<out Annotation>): ScanResultFilter<T> {
        throwExceptionIfAnyAnnotationIsRepeatable(
            methodName = "includeAnnotationInfoForAllOf()",
            annotations = annotations.toList()
        )
        scanResultFilterState = scanResultFilterState.copy(
            namesOfIncludeAnnotationsInfo = annotations.map { it.name }.toSet()
        )
        return this
    }

    /**
     * By default, private previews are filtered out. You can use this option to also return them
     */
    override fun includePrivatePreviews(): ScanResultFilter<T> {
        scanResultFilterState = scanResultFilterState.copy(
            includesPrivatePreviews = true
        )
        return this
    }

    /**
     * Filter only previews whose info meets the predicate, for instance
     * apiLevel >= 30 or group == "IncludeForScreenshotTests"
     */
    override fun filterPreviews(predicate: (T) -> Boolean): ScanResultFilter<T> {
        scanResultFilterState = scanResultFilterState.copy(
            meetsPreviewCriteria = predicate,
        )
        return this
    }

    override fun getPreviews(): List<ComposablePreview<T>> =
        scanResult.use { scanResult ->
            previewScanningLogger.measureFindPreviewsTimeAndGetResult {
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
            }.also {
                previewScanningLogger.addAmountOfPreviews(it.size)
                previewScanningLogger.printFullInfoLog()
            }
        }

    private fun throwExceptionIfAnyAnnotationIsRepeatable(
        methodName: String,
        annotations: List<Class<out Annotation>>
    ) {
        val repeatableAnnotations =
            annotations.filter { it.isAnnotationPresent(Repeatable::class.java) }
        if (repeatableAnnotations.isNotEmpty()) {
            throw RepeatableAnnotationNotSupportedException(
                methodName = methodName,
                repeatableAnnotations = repeatableAnnotations
            )
        }
    }
}