package sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder

import io.github.classgraph.ClassInfo
import io.github.classgraph.ScanResult
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreviewWithResult
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreator
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreatorWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.types.compiledclass.annotationloader.PackageTreesCustomPreviewAnnotationLoader
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.types.compiledclass.annotationloader.ScanResultCustomPreviewAnnotationLoader
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.types.buildtime.ComposablePreviewsAtBuildTimeFinderWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.classloaders.ReflectionClassLoader
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.classloaders.SourceSetClassLoader
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.types.compiledclass.ComposablePreviewsInCompiledClassFinderWithResult
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.ScanResultFilterState

/**
 * @param annotationToScanClassName The full className of the annotation the Composables we want to find are annotated with.
 *      This is usually the @Preview annotation, but could be any other one as far as it does not have AnnotationRetention.SOURCE
 * @param previewInfoMapper A Mapper that converts an AnnotationParameterValueList into the expected PreviewInfo class, e.g. containing apiLevel, Locale, UiMode, FontScale...
 * @param previewMapperCreator Returns a Mapper that converts a Composable @Preview containing some kind of @PreviewParameter as argument into a Sequence of ComposablePreviews,
 * one for each value provided in that argument
 */
open class ClasspathPreviewsFinderWithResult<T, R>(
    override val annotationToScanClassName: String,
    private val previewInfoMapper: ComposablePreviewInfoMapper<T>,
    private val previewMapperCreator: ComposablePreviewMapperCreatorWithResult<T, R>,
) : PreviewsFinderWithResult<T, R> {

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

    private val previewsFinder: PreviewsFinderWithResult<T, R>
        get() =
            overridenClassPath
                ?.let {
                    ComposablePreviewsInCompiledClassFinderWithResult(
                        annotationToScanClassName = annotationToScanClassName,
                        previewInfoMapper = previewInfoMapper,
                        previewMapperCreator = previewMapperCreator,
                        classLoader = SourceSetClassLoader(it),
                        crossModuleCustomPreviewAnnotationLoader = crossModuleCustomPreviewAnnotationLoader
                    )
                }
                ?: ComposablePreviewsAtBuildTimeFinderWithResult(
                    annotationToScanClassName = annotationToScanClassName,
                    previewInfoMapper = previewInfoMapper,
                    previewMapperCreator = previewMapperCreator,
                    classLoader = ReflectionClassLoader(),
                )

    override fun findPreviewsFor(
        classInfo: ClassInfo,
        scanResultFilterState: ScanResultFilterState<T>,
    ): List<ComposablePreviewWithResult<T, R>> =
        previewsFinder.findPreviewsFor(classInfo, scanResultFilterState)

    open fun applyOverridenClasspath(classPath: Classpath): ClasspathPreviewsFinderWithResult<T, R> = apply {
        overridenClassPath = classPath
    }

    open fun applyCustomPreviewsScanResult(customPreviewsScanResult: ScanResult): ClasspathPreviewsFinderWithResult<T, R> = apply {
        this.scanResult = customPreviewsScanResult
    }

    open fun applyCrossModuleCustomPreviewPackageTrees(packageTrees: List<String>): ClasspathPreviewsFinderWithResult<T, R> = apply {
        crossModuleCustomPreviewsPackageTrees.addAll(packageTrees)
    }
}

class ClasspathPreviewsFinder<T>(
    annotationToScanClassName: String,
    previewInfoMapper: ComposablePreviewInfoMapper<T>,
    previewMapperCreator: ComposablePreviewMapperCreator<T>,
) : ClasspathPreviewsFinderWithResult<T, Unit>(
    annotationToScanClassName = annotationToScanClassName,
    previewInfoMapper = previewInfoMapper,
    previewMapperCreator = previewMapperCreator
), PreviewsFinder<T> {

    @Suppress("UNCHECKED_CAST")
    override fun findPreviewsFor(
        classInfo: ClassInfo,
        scanResultFilterState: ScanResultFilterState<T>
    ): List<ComposablePreview<T>> =
        super.findPreviewsFor(classInfo, scanResultFilterState) as List<ComposablePreview<T>>

    override fun applyOverridenClasspath(classPath: Classpath): ClasspathPreviewsFinder<T> =
        super.applyOverridenClasspath(classPath) as ClasspathPreviewsFinder<T>

    override fun applyCustomPreviewsScanResult(customPreviewsScanResult: ScanResult): ClasspathPreviewsFinder<T> =
        super.applyCustomPreviewsScanResult(customPreviewsScanResult) as ClasspathPreviewsFinder<T>

    override fun applyCrossModuleCustomPreviewPackageTrees(packageTrees: List<String>): ClasspathPreviewsFinder<T> =
        super.applyCrossModuleCustomPreviewPackageTrees(packageTrees) as ClasspathPreviewsFinder<T>
}
