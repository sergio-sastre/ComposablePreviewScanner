package sergio.sastre.composable.preview.scanner.core.scanner.classloader.classpath.previewfinder

import io.github.classgraph.ClassInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.ScanResultFilterState

interface PreviewsFinder<T> {

   fun findPreviewsFor(
       classInfo: ClassInfo,
       scanResultFilterState: ScanResultFilterState<T>,
    ): List<ComposablePreview<T>>
}