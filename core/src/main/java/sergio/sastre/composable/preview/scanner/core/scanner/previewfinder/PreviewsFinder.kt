package sergio.sastre.composable.preview.scanner.core.scanner.previewfinder

import io.github.classgraph.ClassInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.ScanResultFilterState

interface PreviewsFinder<T> {

   fun hasPreviewsIn(classInfo: ClassInfo): Boolean

   fun findPreviewsFor(
        clazz: Class<*>,
        classInfo: ClassInfo,
        scanResultFilterState: ScanResultFilterState<T>,
    ): List<ComposablePreview<T>>
}