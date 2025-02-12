package sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.types.compiledclass

import io.github.classgraph.ClassInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreator
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.types.compiledclass.annotationloader.CustomPreviewAnnotationLoader
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.PreviewsFinder
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.types.buildtime.ComposablePreviewsAtBuildTimeFinder
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.classloaders.ClassLoader
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.ScanResultFilterState

internal class ComposablePreviewsInCompiledClassFinder<T>(
    override val annotationToScanClassName: String,
    previewInfoMapper: ComposablePreviewInfoMapper<T>,
    previewMapperCreator: ComposablePreviewMapperCreator<T>,
    classLoader: ClassLoader,
    crossModuleCustomPreviewAnnotationLoader: CustomPreviewAnnotationLoader
) : PreviewsFinder<T> {

    /**
     * Scanning in Compiled classes requires some extra considerations im comparison with Build time scanning
     * to find previews due to the way Kotlin classes are compiled
     * 1. Some custom Preview methods defined in the very same module suffixed with $Container
     * 2. Some custom Preview methods defined in external dependencies must be extra loaded to make heir info available
     */
    private val composableAnnotationFinders =
        listOf(
            ComposablePreviewsAtBuildTimeFinder(
                annotationToScanClassName,
                previewInfoMapper,
                previewMapperCreator,
                classLoader
            ),

            CrossModuleCustomPreviewsInCompiledClassFinder(
                annotationToScanClassName,
                previewInfoMapper,
                previewMapperCreator,
                classLoader,
                crossModuleCustomPreviewAnnotationLoader
            ),

            MultiplePreviewsInCompiledClassFinder(
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