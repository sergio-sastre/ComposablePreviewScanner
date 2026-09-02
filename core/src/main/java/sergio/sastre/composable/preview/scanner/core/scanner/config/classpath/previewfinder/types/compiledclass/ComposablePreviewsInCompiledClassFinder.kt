package sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.types.compiledclass

import io.github.classgraph.ClassInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreviewWithResult
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreatorWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.types.compiledclass.annotationloader.CustomPreviewAnnotationLoader
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.PreviewsFinderWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.types.buildtime.ComposablePreviewsAtBuildTimeFinderWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.classloaders.ClassLoader
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.ScanResultFilterState

internal class ComposablePreviewsInCompiledClassFinderWithResult<T, R>(
    override val annotationToScanClassName: String,
    previewInfoMapper: ComposablePreviewInfoMapper<T>,
    previewMapperCreator: ComposablePreviewMapperCreatorWithResult<T, R>,
    classLoader: ClassLoader,
    crossModuleCustomPreviewAnnotationLoader: CustomPreviewAnnotationLoader
) : PreviewsFinderWithResult<T, R> {

    /**
     * Scanning in Compiled classes requires some extra considerations im comparison with Build time scanning
     * to find previews due to the way Kotlin classes are compiled
     * 1. Some custom Preview methods defined in the very same module suffixed with $Container
     * 2. Some custom Preview methods defined in external dependencies must be extra loaded to make heir info available
     */
    private val composableAnnotationFinders =
        listOf(
            ComposablePreviewsAtBuildTimeFinderWithResult(
                annotationToScanClassName,
                previewInfoMapper,
                previewMapperCreator,
                classLoader
            ),

            CrossModuleCustomPreviewsInCompiledClassFinderWithResult(
                annotationToScanClassName,
                previewInfoMapper,
                previewMapperCreator,
                classLoader,
                crossModuleCustomPreviewAnnotationLoader
            ),

            MultiplePreviewsInCompiledClassFinderWithResult(
                annotationToScanClassName,
                previewInfoMapper,
                previewMapperCreator,
                classLoader
            )
        )

    override fun findPreviewsFor(
        classInfo: ClassInfo,
        scanResultFilterState: ScanResultFilterState<T>,
    ): List<ComposablePreviewWithResult<T, R>> {
        val composablePreviews: MutableList<ComposablePreviewWithResult<T, R>> = mutableListOf()
        composableAnnotationFinders.forEach {
            val previews = it.findPreviewsFor(classInfo, scanResultFilterState)
            composablePreviews.addAll(previews)
        }
        return composablePreviews
    }
}

internal typealias ComposablePreviewsInCompiledClassFinder<T> = ComposablePreviewsInCompiledClassFinderWithResult<T, Unit>
