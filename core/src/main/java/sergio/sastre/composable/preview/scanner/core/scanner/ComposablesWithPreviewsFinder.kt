package sergio.sastre.composable.preview.scanner.core.scanner

import io.github.classgraph.AnnotationInfo
import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreator
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.ScanResultFilterState
import java.lang.reflect.Method

/**
 * @param annotationToScanClassName The full className of the annotation the Composables we want to find are annotated with.
 *      This is usually the @Preview annotation, but could be any other one as far as it does not have AnnotationRetention.SOURCE
 * @param previewInfoMapper A Mapper that converts an AnnotationParameterValueList into the expected PreviewInfo class, e.g. containing apiLevel, Locale, UiMode, FontScale...
 * @param previewMapperCreator Returns a Mapper that convert a Composable annotated with one or more @Preview into a Sequence of ComposablePreview, one for each @Preview
 */
class ComposablesWithPreviewsFinder<T>(
    private val annotationToScanClassName: String,
    private val previewInfoMapper: ComposablePreviewInfoMapper<T>,
    private val previewMapperCreator: ComposablePreviewMapperCreator<T>,
) {
    fun findFor(
        classInfo: ClassInfo,
        scanResultFilterState: ScanResultFilterState<T>,
    ): List<ComposablePreview<T>> =
        classInfo.declaredMethodInfo.asSequence().flatMap { methodInfo ->
            // evaluate if method has preview
            when (methodInfo.getAnnotationInfo(annotationToScanClassName)) {
                null -> emptySequence()
                else -> {
                    when (methodInfo.hasExcludedAnnotation(scanResultFilterState)) {
                        false -> methodInfo.toSequenceOfMethods().flatMap { method ->
                            method.repeatMethodPerPreviewAnnotation(
                                methodInfo,
                                scanResultFilterState
                            )
                        }
                        true -> emptySequence()
                    }
                }
            }
        }
            .toSet()
            .flatMap { mapper ->
                mapper.mapToComposablePreviews()
            }

    private fun MethodInfo.hasExcludedAnnotation(scanResultFilterState: ScanResultFilterState<T>) =
        when (scanResultFilterState.excludedAnnotations.isNotEmpty()) {
            true -> scanResultFilterState.excludedAnnotations.any {
                this.getAnnotationInfo(it) != null
            }
            false -> false
        }

    private fun MethodInfo.toSequenceOfMethods(): Sequence<Method> =
        Class.forName(className)
            .methods
            .asSequence()
            .filter {
                it.name.substringAfterLast(".") == name
            }

    private fun Method.repeatMethodPerPreviewAnnotation(
        methodInfo: MethodInfo,
        scanResultFilterState: ScanResultFilterState<T>,
    ): Sequence<ComposablePreviewMapper<T>> {
        val previewMethods: MutableList<ComposablePreviewMapper<T>> = mutableListOf()

        val annotationInfos: List<AnnotationInfo> =
            methodInfo.getAnnotationInfoRepeatable(annotationToScanClassName)

        annotationInfos.forEach { annotationInfo ->
            val previewInfo = previewInfoMapper.mapToComposablePreviewInfo(annotationInfo.parameterValues)

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