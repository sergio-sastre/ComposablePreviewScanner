package sergio.sastre.composable.preview.scanner.core.scanner.classpath.overriden

import io.github.classgraph.ClassInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreator
import sergio.sastre.composable.preview.scanner.core.scanner.previewfinder.PreviewsFinder
import sergio.sastre.composable.preview.scanner.core.scanner.classpath.buildtime.ComposablePreviewsAtBuildTimeFinder
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.ScanResultFilterState

class OverridenClasspathComposablePreviewsFinder<T>(
    annotationToScanClassName: String,
    previewInfoMapper: ComposablePreviewInfoMapper<T>,
    previewMapperCreator: ComposablePreviewMapperCreator<T>,
    customPreviewsPackageTrees: List<String>
) : PreviewsFinder<T> {

    private val composableAnnotationFinders = listOf(
        ComposablePreviewsAtBuildTimeFinder(
            annotationToScanClassName, previewInfoMapper, previewMapperCreator
        ),

        CrossModuleComposableWithCustomPreviewsFinder(
            annotationToScanClassName,
            previewInfoMapper,
            previewMapperCreator,
            customPreviewsPackageTrees
        ),

        SameModuleComposableWithCustomPreviewsFinder(
            annotationToScanClassName, previewInfoMapper, previewMapperCreator
        )
    )

    override fun hasPreviewsIn(classInfo: ClassInfo): Boolean =
        composableAnnotationFinders.any { it.hasPreviewsIn(classInfo) }

    override fun findPreviewsFor(
        clazz: Class<*>,
        classInfo: ClassInfo,
        scanResultFilterState: ScanResultFilterState<T>
    ): List<ComposablePreview<T>> {
        val composablePreviews: MutableList<ComposablePreview<T>> = mutableListOf()
        composableAnnotationFinders.forEach {
            composablePreviews.addAll(it.findPreviewsFor(clazz, classInfo, scanResultFilterState))
        }
        return composablePreviews
    }
}