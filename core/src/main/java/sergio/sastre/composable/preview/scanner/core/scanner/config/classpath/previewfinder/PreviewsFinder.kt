package sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder

import io.github.classgraph.ClassInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreviewWithResult
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.ScanResultFilterState

interface PreviewsFinderWithResult<T, R> {

    val annotationToScanClassName: String

    fun findPreviewsFor(
        classInfo: ClassInfo,
        scanResultFilterState: ScanResultFilterState<T>,
    ): List<ComposablePreviewWithResult<T, R>>
}

interface PreviewsFinder<T> : PreviewsFinderWithResult<T, Unit> {
    override fun findPreviewsFor(
        classInfo: ClassInfo,
        scanResultFilterState: ScanResultFilterState<T>,
    ): List<ComposablePreview<T>>
}
