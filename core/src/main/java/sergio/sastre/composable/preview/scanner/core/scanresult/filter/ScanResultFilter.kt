package sergio.sastre.composable.preview.scanner.core.scanresult.filter

import io.github.classgraph.ScanResult
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreviewWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.PreviewsFinderWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.logger.PreviewScanningLogger
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.exceptions.RepeatableAnnotationNotSupportedException

/**
 * The reason for these interfaces is to avoid API misuse:
 * If includeIfAnnotatedWithAnyOf() is called, then excludeIfAnnotatedWithAnyOf() cannot be called,
 * and vice versa.
 *
 * They are mutually exclusive by their API design
 */
interface PreviewProviderWithResult<T, R> {
    fun getPreviews(): List<ComposablePreviewWithResult<T, R>>
}

interface PreviewProvider<T> : PreviewProviderWithResult<T, Unit> {
    override fun getPreviews(): List<ComposablePreview<T>>
}

interface GeneralScanResultFilterWithResult<T, R> : PreviewProviderWithResult<T, R> {
    fun excludeIfAnnotatedWithAnyOf(vararg annotations: Class<out Annotation>): ExclusiveFilterWithResult<T, R>
    fun includeIfAnnotatedWithAnyOf(vararg annotations: Class<out Annotation>): InclusiveFilterWithResult<T, R>
    fun includeAnnotationInfoForAllOf(vararg annotations: Class<out Annotation>): GeneralScanResultFilterWithResult<T, R>
    fun includePrivatePreviews(): GeneralScanResultFilterWithResult<T, R>
    fun filterPreviews(predicate: (T) -> Boolean): GeneralScanResultFilterWithResult<T, R>
}

interface GeneralScanResultFilter<T> : GeneralScanResultFilterWithResult<T, Unit>, PreviewProvider<T> {
    override fun excludeIfAnnotatedWithAnyOf(vararg annotations: Class<out Annotation>): ExclusiveFilter<T>
    override fun includeIfAnnotatedWithAnyOf(vararg annotations: Class<out Annotation>): InclusiveFilter<T>
    override fun includeAnnotationInfoForAllOf(vararg annotations: Class<out Annotation>): GeneralScanResultFilter<T>
    override fun includePrivatePreviews(): GeneralScanResultFilter<T>
    override fun filterPreviews(predicate: (T) -> Boolean): GeneralScanResultFilter<T>
}

interface ExclusiveFilterWithResult<T, R> : PreviewProviderWithResult<T, R> {
    fun includeAnnotationInfoForAllOf(vararg annotations: Class<out Annotation>): ExclusiveFilterWithResult<T, R>
    fun includePrivatePreviews(): ExclusiveFilterWithResult<T, R>
    fun filterPreviews(predicate: (T) -> Boolean): ExclusiveFilterWithResult<T, R>
}

interface ExclusiveFilter<T> : ExclusiveFilterWithResult<T, Unit>, PreviewProvider<T> {
    override fun includeAnnotationInfoForAllOf(vararg annotations: Class<out Annotation>): ExclusiveFilter<T>
    override fun includePrivatePreviews(): ExclusiveFilter<T>
    override fun filterPreviews(predicate: (T) -> Boolean): ExclusiveFilter<T>
}

interface InclusiveFilterWithResult<T, R> : PreviewProviderWithResult<T, R> {
    fun includeAnnotationInfoForAllOf(vararg annotations: Class<out Annotation>): InclusiveFilterWithResult<T, R>
    fun includePrivatePreviews(): InclusiveFilterWithResult<T, R>
    fun filterPreviews(predicate: (T) -> Boolean): InclusiveFilterWithResult<T, R>
}

interface InclusiveFilter<T> : InclusiveFilterWithResult<T, Unit>, PreviewProvider<T> {
    override fun includeAnnotationInfoForAllOf(vararg annotations: Class<out Annotation>): InclusiveFilter<T>
    override fun includePrivatePreviews(): InclusiveFilter<T>
    override fun filterPreviews(predicate: (T) -> Boolean): InclusiveFilter<T>
}

/**
 * Filter the ComposablePreviews of a given ScanResult.
 */
open class ScanResultFilterWithResult<T, R> internal constructor(
    internal val scanResult: ScanResult,
    internal val previewsFinder: PreviewsFinderWithResult<T, R>,
    internal val previewScanningLogger: PreviewScanningLogger,
) : GeneralScanResultFilterWithResult<T, R>, ExclusiveFilterWithResult<T, R>, InclusiveFilterWithResult<T, R> {
    private var scanResultFilterState = ScanResultFilterState<T>()

    /**
     * Excludes previews which use any of the given annotations, so they will not be returned
     *
     * WARNING: throws a [RepeatableAnnotationNotSupportedException] if any of the annotations is repeatable
     */
    override fun excludeIfAnnotatedWithAnyOf(
        vararg annotations: Class<out Annotation>
    ): ExclusiveFilterWithResult<T, R> {
        require(annotations.isNotEmpty()) {
            "annotations must not be empty. For that, leave it out instead"
        }
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
     * Only includes previews which use any of the given annotations, otherwise they are filtered out
     *
     * WARNING: throws a [RepeatableAnnotationNotSupportedException] if any of the annotations is repeatable
     */
    override fun includeIfAnnotatedWithAnyOf(
        vararg annotations: Class<out Annotation>
    ): InclusiveFilterWithResult<T, R> {
        require(annotations.isNotEmpty()) {
            "annotations must not be empty. For that, leave it out instead"
        }
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
     * Consecutive calls to this method will accumulate (union) the annotation info names.
     *
     * WARNING: throws a [RepeatableAnnotationNotSupportedException] if any of the annotations is repeatable
     */
    override fun includeAnnotationInfoForAllOf(vararg annotations: Class<out Annotation>): ScanResultFilterWithResult<T, R> {
        require(annotations.isNotEmpty()) {
            "annotations must not be empty. For that, leave it out instead"
        }
        throwExceptionIfAnyAnnotationIsRepeatable(
            methodName = "includeAnnotationInfoForAllOf()",
            annotations = annotations.toList()
        )
        scanResultFilterState = scanResultFilterState.copy(
            namesOfIncludeAnnotationsInfo = scanResultFilterState.namesOfIncludeAnnotationsInfo + annotations.map { it.name }
        )
        return this
    }

    /**
     * By default, private previews are filtered out. You can use this option to also return them
     */
    override fun includePrivatePreviews(): ScanResultFilterWithResult<T, R> {
        scanResultFilterState = scanResultFilterState.copy(
            includesPrivatePreviews = true
        )
        return this
    }

    /**
     * Filter only previews whose info meets the predicate, for instance
     * apiLevel >= 30 or group == "IncludeForScreenshotTests"
     */
    override fun filterPreviews(predicate: (T) -> Boolean): ScanResultFilterWithResult<T, R> {
        scanResultFilterState = scanResultFilterState.copy(
            meetsPreviewCriteria = predicate,
        )
        return this
    }

    override fun getPreviews(): List<ComposablePreviewWithResult<T, R>> =
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

class ScanResultFilter<T> internal constructor(
    scanResult: ScanResult,
    previewsFinder: PreviewsFinderWithResult<T, Unit>,
    previewScanningLogger: PreviewScanningLogger,
) : ScanResultFilterWithResult<T, Unit>(scanResult, previewsFinder, previewScanningLogger),
    GeneralScanResultFilter<T>,
    ExclusiveFilter<T>,
    InclusiveFilter<T> {

    override fun excludeIfAnnotatedWithAnyOf(vararg annotations: Class<out Annotation>): ExclusiveFilter<T> {
        super.excludeIfAnnotatedWithAnyOf(*annotations)
        return this
    }

    override fun includeIfAnnotatedWithAnyOf(vararg annotations: Class<out Annotation>): InclusiveFilter<T> {
        super.includeIfAnnotatedWithAnyOf(*annotations)
        return this
    }

    override fun includeAnnotationInfoForAllOf(vararg annotations: Class<out Annotation>): ScanResultFilter<T> {
        super.includeAnnotationInfoForAllOf(*annotations)
        return this
    }

    override fun includePrivatePreviews(): ScanResultFilter<T> {
        super.includePrivatePreviews()
        return this
    }

    override fun filterPreviews(predicate: (T) -> Boolean): ScanResultFilter<T> {
        super.filterPreviews(predicate)
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun getPreviews(): List<ComposablePreview<T>> =
        super.getPreviews() as List<ComposablePreview<T>>
}
