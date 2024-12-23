package sergio.sastre.composable.preview.scanner.core.scanner.classpath.overriden

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreator
import sergio.sastre.composable.preview.scanner.core.scanner.previewfinder.PreviewsFinder
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.ScanResultFilterState

class CrossModuleComposableWithCustomPreviewsFinder<T>(
    private val annotationToScanClassName: String,
    private val previewInfoMapper: ComposablePreviewInfoMapper<T>,
    private val previewMapperCreator: ComposablePreviewMapperCreator<T>,
    private val customPreviewsPackageTrees: List<String>
): PreviewsFinder<T> {

    private val previewRepeatableAnnotations by lazy {
        ClassGraph()
            .enableAnnotationInfo()
            .acceptPackages(*customPreviewsPackageTrees.toTypedArray())
            .scan()
            .use { scanResult ->
                scanResult
                    .allClasses
                    .associate { clazz ->
                        clazz.name to clazz.annotationInfo.filter { it.name == annotationToScanClassName }
                    }.filterValues { it.isNotEmpty() }
            }
    }

    override fun hasPreviewsIn(
        classInfo: ClassInfo,
    ): Boolean =
        previewRepeatableAnnotations.any { classInfo.hasDeclaredMethodAnnotation(it.key) && it.value.isNotEmpty() }

    override fun findPreviewsFor(
        clazz: Class<*>,
        classInfo: ClassInfo,
        scanResultFilterState: ScanResultFilterState<T>,
    ): List<ComposablePreview<T>> {

        val composablePreviews: MutableList<ComposablePreview<T>> = mutableListOf()
        classInfo.declaredMethodInfo.asSequence().forEach { methodInfo ->
            val containedAnnotations =
                previewRepeatableAnnotations.filter { classInfo.hasDeclaredMethodAnnotation(it.key) && it.value.isNotEmpty() }

            containedAnnotations.onEach { previewAnnotation ->
                methodInfo.getAnnotationInfo(previewAnnotation.key)?.let {
                    if ((methodInfo.hasExcludedAnnotation(scanResultFilterState) || scanResultFilterState.excludesMethod(methodInfo)).not()) {
                        val methods = if (methodInfo.isPrivate) clazz.declaredMethods else clazz.methods
                        val allPreviewMethods = methods.asSequence()
                            .filter { it.name == methodInfo.name }
                            .onEach {
                                if (methodInfo.isPrivate) {
                                    it.isAccessible = true
                                }
                            }
                            .flatMap { method ->
                                val previewMethods: MutableList<ComposablePreviewMapper<T>> =
                                    mutableListOf()
                                previewAnnotation.value.forEach { preview ->
                                    val previewInfo = previewInfoMapper.mapToComposablePreviewInfo(preview.parameterValues)
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
                                previewMethods.asSequence()
                            }
                            .toSet()
                            .flatMap { mapper ->
                                mapper.mapToComposablePreviews()
                            }

                        composablePreviews.addAll(allPreviewMethods)
                    }
                }
            }
        }
        return composablePreviews
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