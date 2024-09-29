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
    private val classLoader = this::class.java.classLoader

    fun findFor(
        classInfo: ClassInfo,
        scanResultFilterState: ScanResultFilterState<T>,
    ): List<ComposablePreview<T>> {
        val clazz = Class.forName(classInfo.name, false, classLoader)

        return classInfo.declaredMethodInfo.asSequence().flatMap { methodInfo ->
            methodInfo.getAnnotationInfo(annotationToScanClassName)?.let {
                if (methodInfo.hasExcludedAnnotation(scanResultFilterState) || scanResultFilterState.excludesMethod(methodInfo)) {
                    emptySequence()
                } else {
                    val methods = if (methodInfo.isPrivate) clazz.declaredMethods else clazz.methods

                    methods.asSequence()
                        .filter { it.name == methodInfo.name }
                        .onEach {
                            if (methodInfo.isPrivate) {
                                it.isAccessible = true
                            }
                        }
                        .flatMap { method ->
                            method.repeatMethodPerPreviewAnnotation(
                                methodInfo,
                                scanResultFilterState
                            )
                        }
                }
            } ?: emptySequence()
        }
            .toSet()
            .flatMap { mapper ->
                mapper.mapToComposablePreviews()
            }
    }

    private fun ScanResultFilterState<T>.excludesMethod(methodInfo: MethodInfo): Boolean =
        !includesPrivatePreviews && methodInfo.isPrivate

    private fun MethodInfo.hasExcludedAnnotation(scanResultFilterState: ScanResultFilterState<T>) =
        when (scanResultFilterState.excludedAnnotations.isNotEmpty()) {
            true -> scanResultFilterState.excludedAnnotations.any {
                this.getAnnotationInfo(it) != null
            }

            false -> false
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