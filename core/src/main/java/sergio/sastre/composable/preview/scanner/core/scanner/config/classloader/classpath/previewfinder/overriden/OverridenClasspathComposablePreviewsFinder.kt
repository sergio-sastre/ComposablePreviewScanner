package sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.previewfinder.overriden

import io.github.classgraph.ClassInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreator
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.previewfinder.overriden.annotationloader.CustomPreviewAnnotationLoader
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.previewfinder.PreviewsFinder
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.previewfinder.buildtime.ComposablePreviewsAtBuildTimeFinder
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.ClassLoader
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.ScanResultFilterState

internal class OverridenClasspathComposablePreviewsFinder<T>(
    override val annotationToScanClassName: String,
    previewInfoMapper: ComposablePreviewInfoMapper<T>,
    previewMapperCreator: ComposablePreviewMapperCreator<T>,
    classLoader: ClassLoader,
    crossModuleCustomPreviewAnnotationLoader: CustomPreviewAnnotationLoader
) : PreviewsFinder<T> {

    private val composableAnnotationFinders =
        listOf(
            ComposablePreviewsAtBuildTimeFinder(
                annotationToScanClassName,
                previewInfoMapper,
                previewMapperCreator,
                classLoader
            ),

            CrossModuleComposableWithCustomPreviewsFinder(
                annotationToScanClassName,
                previewInfoMapper,
                previewMapperCreator,
                classLoader,
                crossModuleCustomPreviewAnnotationLoader
            ),

            SameModuleComposableWithCustomPreviewsFinder(
                annotationToScanClassName,
                previewInfoMapper,
                previewMapperCreator,
                classLoader
            )
        )

    override fun findPreviewsFor(
        classInfo: ClassInfo,
        scanResultFilterState: ScanResultFilterState<T>,
    ): List<ComposablePreview<T>> {
        val composablePreviews: MutableList<ComposablePreview<T>> = mutableListOf()
        composableAnnotationFinders.forEach {
            val previews = it.findPreviewsFor(classInfo, scanResultFilterState)
            composablePreviews.addAll(previews)
        }
        return composablePreviews
    }
}