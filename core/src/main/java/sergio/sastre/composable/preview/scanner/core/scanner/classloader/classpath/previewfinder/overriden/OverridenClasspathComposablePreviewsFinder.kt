package sergio.sastre.composable.preview.scanner.core.scanner.classloader.classpath.previewfinder.overriden

import io.github.classgraph.ClassInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreator
import sergio.sastre.composable.preview.scanner.core.scanner.classloader.classpath.previewfinder.PreviewsFinder
import sergio.sastre.composable.preview.scanner.core.scanner.classloader.classpath.previewfinder.buildtime.ComposablePreviewsAtBuildTimeFinder
import sergio.sastre.composable.preview.scanner.core.scanner.classloader.ClassLoader
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.ScanResultFilterState

internal class OverridenClasspathComposablePreviewsFinder<T>(
    annotationToScanClassName: String,
    previewInfoMapper: ComposablePreviewInfoMapper<T>,
    previewMapperCreator: ComposablePreviewMapperCreator<T>,
    customPreviewsPackageTrees: List<String>,
    classLoader: ClassLoader,
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
                customPreviewsPackageTrees,
                classLoader
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
        scanResultFilterState: ScanResultFilterState<T>
    ): List<ComposablePreview<T>> {
        val composablePreviews: MutableList<ComposablePreview<T>> = mutableListOf()
        composableAnnotationFinders.forEach {
            composablePreviews.addAll(it.findPreviewsFor(classInfo, scanResultFilterState))
        }
        return composablePreviews
    }
}