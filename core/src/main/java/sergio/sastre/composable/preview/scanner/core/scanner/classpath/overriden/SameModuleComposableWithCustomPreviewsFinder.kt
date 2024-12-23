package sergio.sastre.composable.preview.scanner.core.scanner.classpath.overriden

import io.github.classgraph.AnnotationInfo
import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreator
import sergio.sastre.composable.preview.scanner.core.scanner.previewfinder.PreviewsFinder
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.ScanResultFilterState

class SameModuleComposableWithCustomPreviewsFinder<T>(
    private val annotationToScanClassName: String,
    private val previewInfoMapper: ComposablePreviewInfoMapper<T>,
    private val previewMapperCreator: ComposablePreviewMapperCreator<T>
): PreviewsFinder<T> {

    override fun hasPreviewsIn(classInfo: ClassInfo): Boolean =
        classInfo.hasDeclaredMethodAnnotation("$annotationToScanClassName\$Container")

    @Suppress("UNCHECKED_CAST")
    override fun findPreviewsFor(
        clazz: Class<*>,
        classInfo: ClassInfo,
        scanResultFilterState: ScanResultFilterState<T>
    ): List<ComposablePreview<T>> {
        return classInfo.declaredMethodInfo.asSequence().flatMap { methodInfo ->
            methodInfo.getAnnotationInfo("$annotationToScanClassName\$Container")?.let {
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
                            // Rewrite this
                            val previewMethods: MutableList<ComposablePreviewMapper<T>> =
                                mutableListOf()
                            val previews: Array<Any> =
                                it.parameterValues.getValue("value") as Array<Any>
                            previews
                                .map { (it as AnnotationInfo) }
                                .forEach { annotationInfo ->
                                    val previewInfo =
                                        previewInfoMapper.mapToComposablePreviewInfo(annotationInfo.parameterValues)

                                    if (scanResultFilterState.meetsPreviewCriteria(previewInfo)) {
                                        val annotationsInfo =
                                            methodInfo.annotationInfo.filter { annotation ->
                                                scanResultFilterState.namesOfIncludeAnnotationsInfo.contains(
                                                    annotation.name
                                                )
                                            }
                                        previewMethods.add(
                                            previewMapperCreator.createComposablePreviewMapper(
                                                previewMethod = method,
                                                previewInfo = previewInfo,
                                                annotationsInfo = annotationsInfo
                                            )
                                        )
                                    }
                                }
                            previewMethods
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

}