package sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder

import io.github.classgraph.ClassInfo
import io.github.classgraph.ScanResult
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreator
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.types.compiledclass.annotationloader.PackageTreesCustomPreviewAnnotationLoader
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.types.compiledclass.annotationloader.ScanResultCustomPreviewAnnotationLoader
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.types.buildtime.ComposablePreviewsAtBuildTimeFinder
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.classloaders.ReflectionClassLoader
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.classloaders.SourceSetClassLoader
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.types.compiledclass.ComposablePreviewsInCompiledClassFinder
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.ScanResultFilterState

/**
 * @param annotationToScanClassName The full className of the annotation the Composables we want to find are annotated with.
 *      This is usually the @Preview annotation, but could be any other one as far as it does not have AnnotationRetention.SOURCE
 * @param previewInfoMapper A Mapper that converts an AnnotationParameterValueList into the expected PreviewInfo class, e.g. containing apiLevel, Locale, UiMode, FontScale...
 * @param previewMapperCreator Returns a Mapper that converts a Composable @Preview containing some kind of @PreviewParameter as argument into a Sequence of ComposablePreviews,
 * one for each value provided in that argument
 */
class ClasspathPreviewsFinder<T>(
    override val annotationToScanClassName: String,
    private val previewInfoMapper: ComposablePreviewInfoMapper<T>,
    private val previewMapperCreator: ComposablePreviewMapperCreator<T>,
) : PreviewsFinder<T> {

    private var overridenClassPath: Classpath? = null
    private val crossModuleCustomPreviewsPackageTrees = mutableListOf<String>()

    private var scanResult: ScanResult? = null

    private val crossModuleCustomPreviewAnnotationLoader by lazy {
        scanResult?.let {
            ScanResultCustomPreviewAnnotationLoader(it, annotationToScanClassName)
        } ?: PackageTreesCustomPreviewAnnotationLoader(
            crossModuleCustomPreviewsPackageTrees,
            annotationToScanClassName
        )
    }

    private val previewsFinder: PreviewsFinder<T>
        get() =
            overridenClassPath
                ?.let {
                    ComposablePreviewsInCompiledClassFinder(
                        annotationToScanClassName = annotationToScanClassName,
                        previewInfoMapper = previewInfoMapper,
                        previewMapperCreator = previewMapperCreator,
                        classLoader = SourceSetClassLoader(it),
                        crossModuleCustomPreviewAnnotationLoader = crossModuleCustomPreviewAnnotationLoader
                    )
                }
                ?: ComposablePreviewsAtBuildTimeFinder(
                    annotationToScanClassName = annotationToScanClassName,
                    previewInfoMapper = previewInfoMapper,
                    previewMapperCreator = previewMapperCreator,
                    classLoader = ReflectionClassLoader(),
                )

    override fun findPreviewsFor(
        classInfo: ClassInfo,
        scanResultFilterState: ScanResultFilterState<T>,
    ): List<ComposablePreview<T>> =
        previewsFinder.findPreviewsFor(classInfo, scanResultFilterState)

    fun applyOverridenClasspath(classPath: Classpath) = apply {
        overridenClassPath = classPath
    }

    fun applyCustomPreviewsScanResult(customPreviewsScanResult: ScanResult) = apply {
        this.scanResult = customPreviewsScanResult
    }

    fun applyCrossModuleCustomPreviewPackageTrees(packageTrees: List<String>) = apply {
        crossModuleCustomPreviewsPackageTrees.addAll(packageTrees)
    }
}