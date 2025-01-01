package sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.previewfinder

import io.github.classgraph.ClassInfo
import io.github.classgraph.ScanResult
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreator
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.previewfinder.overriden.annotationloader.PackageTreesCustomPreviewAnnotationLoader
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.previewfinder.overriden.annotationloader.ScanResultCustomPreviewAnnotationLoader
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.previewfinder.buildtime.ComposablePreviewsAtBuildTimeFinder
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.ReflectionClassLoader
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.SourceSetClassLoader
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.previewfinder.overriden.OverridenClasspathComposablePreviewsFinder
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.ScanResultFilterState

/**
 * @param annotationToScanClassName The full className of the annotation the Composables we want to find are annotated with.
 *      This is usually the @Preview annotation, but could be any other one as far as it does not have AnnotationRetention.SOURCE
 * @param previewInfoMapper A Mapper that converts an AnnotationParameterValueList into the expected PreviewInfo class, e.g. containing apiLevel, Locale, UiMode, FontScale...
 * @param previewMapperCreator Returns a Mapper that convert a Composable annotated with one or more @Preview into a Sequence of ComposablePreview, one for each @Preview
 */
class ClasspathPreviewsFinder<T>(
    override val annotationToScanClassName: String,
    private val previewInfoMapper: ComposablePreviewInfoMapper<T>,
    private val previewMapperCreator: ComposablePreviewMapperCreator<T>,
) : PreviewsFinder<T> {

    private var overridenClassPath: String? = null
    private val crossModuleCustomPreviewsPackageTrees = mutableListOf<String>()

    private var scanResult: ScanResult? = null

    private val crossmoduleCustomPreviewAnnotationLoader by lazy {
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
                    OverridenClasspathComposablePreviewsFinder(
                        annotationToScanClassName = annotationToScanClassName,
                        previewInfoMapper = previewInfoMapper,
                        previewMapperCreator = previewMapperCreator,
                        classLoader = SourceSetClassLoader(it),
                        crossModuleCustomPreviewAnnotationLoader = crossmoduleCustomPreviewAnnotationLoader
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

    fun applyOverridenClasspath(classPath: String) = apply {
        overridenClassPath = classPath
    }

    fun applyCustomPreviewsScanResult(customPreviewsScanResult: ScanResult) = apply {
        this.scanResult = customPreviewsScanResult
    }

    fun applyCrossModuleCustomPreviewPackageTrees(packageTrees: List<String>) = apply {
        crossModuleCustomPreviewsPackageTrees.addAll(packageTrees)
    }
}