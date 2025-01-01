package sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.previewfinder.overriden

import io.github.classgraph.ClassInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreator
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.previewfinder.overriden.annotationloader.CustomPreviewAnnotationLoader
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.ClassLoader
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.previewfinder.PreviewsFinder
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.ScanResultFilterState

internal class CrossModuleComposableWithCustomPreviewsFinder<T>(
    override val annotationToScanClassName: String,
    private val previewInfoMapper: ComposablePreviewInfoMapper<T>,
    private val previewMapperCreator: ComposablePreviewMapperCreator<T>,
    private val classLoader: ClassLoader,
    private val customPreviewAnnotationLoader: CustomPreviewAnnotationLoader
) : PreviewsFinder<T> {

    private val customPreviewAnnotationInfoList by lazy {
        customPreviewAnnotationLoader.loadCustomPreviewAnnotation()
    }

    private fun hasPreviewsIn(
        classInfo: ClassInfo,
    ): Boolean =
        when (customPreviewAnnotationInfoList.isNotEmpty()) {
            true -> customPreviewAnnotationInfoList.any { classInfo.hasDeclaredMethodAnnotation(it.key) && it.value.isNotEmpty() }
            false -> false
        }

    override fun findPreviewsFor(
        classInfo: ClassInfo,
        scanResultFilterState: ScanResultFilterState<T>,
    ): List<ComposablePreview<T>> {
        if (!hasPreviewsIn(classInfo)) return emptyList()

        val composablePreviews: MutableList<ComposablePreview<T>> = mutableListOf()
        classInfo.declaredMethodInfo.asSequence().forEach { methodInfo ->
            val containedAnnotations =
                customPreviewAnnotationInfoList.filter { classInfo.hasDeclaredMethodAnnotation(it.key) && it.value.isNotEmpty() }

            containedAnnotations.onEach { previewAnnotation ->
                methodInfo.getAnnotationInfo(previewAnnotation.key)?.let {
                    if ((scanResultFilterState.hasExcludedAnnotation(methodInfo) || scanResultFilterState.excludesMethod(methodInfo)).not()) {
                        val clazz = classLoader.loadClass(classInfo)
                        val methods =
                            if (methodInfo.isPrivate) clazz.declaredMethods else clazz.methods
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
                                    val previewInfo =
                                        previewInfoMapper.mapToComposablePreviewInfo(preview.parameterValues)
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
}