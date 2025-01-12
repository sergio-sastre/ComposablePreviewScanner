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
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.previewfinder.overriden.OverridenClasspathComposablePreviewsFinder
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.ScanResultFilterState

/**
 * @param annotationToScanClassName The full className of the annotation the Composables we want to find are annotated with.
 *      This is usually the @Preview annotation, but could be any other one as far as it does not have AnnotationRetention.SOURCE
 * @param previewInfoMapper A Mapper that converts an AnnotationParameterValueList into the expected PreviewInfo class, e.g. containing apiLevel, Locale, UiMode, FontScale...
 * @param previewMapperCreator Returns a Mapper that converts a Composable annotated with one or more @Preview into a Sequence of ComposablePreviews, one for each @Preview
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
        // TODO -> reasons to fail (source set)
        // 1. Instrumentation test ->
        //      1.1 SourceSet ScreenshotTest -> include 'screenshotTest' sources
        //          Catch this error in ClassLoader
        //      2.1 Not using scanFromFile -> does not allow other versions...
        //          Catch this error in the ScanMethods
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