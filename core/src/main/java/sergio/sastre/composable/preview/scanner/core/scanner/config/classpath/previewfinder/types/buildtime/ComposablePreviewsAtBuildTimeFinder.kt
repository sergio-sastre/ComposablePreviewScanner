package sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.types.buildtime

import io.github.classgraph.AnnotationInfo
import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreator
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.MethodFinder
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.PreviewsFinder
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.classloaders.ClassLoader
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.ScanResultFilterState
import java.lang.reflect.Method

/**
 * @param annotationToScanClassName The full className of the annotation the Composables we want to find are annotated with.
 *      This is usually the @Preview annotation, but could be any other one as far as it does not have AnnotationRetention.SOURCE
 * @param previewInfoMapper A Mapper that converts an AnnotationParameterValueList into the expected PreviewInfo class, e.g. containing apiLevel, Locale, UiMode, FontScale...
 * @param previewMapperCreator Returns a Mapper that convert a Composable annotated with one or more @Preview into a Sequence of ComposablePreview, one for each @Preview
 */
internal class ComposablePreviewsAtBuildTimeFinder<T>(
    override val annotationToScanClassName: String,
    private val previewInfoMapper: ComposablePreviewInfoMapper<T>,
    private val previewMapperCreator: ComposablePreviewMapperCreator<T>,
    private val classLoader: ClassLoader,
) : PreviewsFinder<T> {

    private fun hasPreviewsIn(classInfo: ClassInfo): Boolean =
        classInfo.hasDeclaredMethodAnnotation(annotationToScanClassName)

    override fun findPreviewsFor(
        classInfo: ClassInfo,
        scanResultFilterState: ScanResultFilterState<T>,
    ): List<ComposablePreview<T>> {
        if (!hasPreviewsIn(classInfo)) return emptyList()

        return classInfo.declaredMethodInfo.asSequence().flatMap { methodInfo ->
            methodInfo.getAnnotationInfo(annotationToScanClassName)?.let {
                if (scanResultFilterState.shouldIncludeMethod(methodInfo)) {
                    val method = MethodFinder(classInfo, classLoader).find(methodInfo)
                    method.repeatMethodPerPreviewAnnotation(
                        methodInfo,
                        scanResultFilterState
                    )
                } else {
                    emptySequence()
                }
            } ?: emptySequence()
        }
            .toSet()
            .flatMap { mapper ->
                mapper.mapToComposablePreviews()
            }
    }

    private fun Method.repeatMethodPerPreviewAnnotation(
        methodInfo: MethodInfo,
        scanResultFilterState: ScanResultFilterState<T>,
    ): Sequence<ComposablePreviewMapper<T>> {
        val previewMethods: MutableList<ComposablePreviewMapper<T>> = mutableListOf()

        val annotationInfos: List<AnnotationInfo> =
            methodInfo.getAnnotationInfoRepeatable(annotationToScanClassName)

        annotationInfos.forEach { annotationInfo ->
            val previewInfo =
                previewInfoMapper.mapToComposablePreviewInfo(annotationInfo.parameterValues)

            if (scanResultFilterState.meetsPreviewCriteria(previewInfo)) {
                val annotationsInfo = methodInfo.annotationInfo.filter { annotation ->
                    scanResultFilterState.namesOfIncludeAnnotationsInfo.contains(annotation.name)
                }
                previewMethods.add(
                    previewMapperCreator.createComposablePreviewMapper(
                        previewMethod = this,
                        previewInfo = previewInfo,
                        annotationsInfo = annotationsInfo
                    )
                )
            }
        }
        return previewMethods.asSequence()
    }
}