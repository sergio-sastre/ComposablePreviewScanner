package sergio.sastre.composable.preview.scanner.core.scanner.classloader.classpath.previewfinder.overriden

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreator
import sergio.sastre.composable.preview.scanner.core.scanner.classloader.ClassLoader
import sergio.sastre.composable.preview.scanner.core.scanner.classloader.classpath.previewfinder.PreviewsFinder
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.ScanResultFilterState

internal class CrossModuleComposableWithCustomPreviewsFinder<T>(
    private val annotationToScanClassName: String,
    private val previewInfoMapper: ComposablePreviewInfoMapper<T>,
    private val previewMapperCreator: ComposablePreviewMapperCreator<T>,
    private val customPreviewsPackageTrees: List<String>,
    private val classLoader: ClassLoader,
) : PreviewsFinder<T> {

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

    private fun hasPreviewsIn(
        classInfo: ClassInfo,
    ): Boolean =
        when (customPreviewsPackageTrees.isNotEmpty()) {
            true -> previewRepeatableAnnotations.any { classInfo.hasDeclaredMethodAnnotation(it.key) && it.value.isNotEmpty() }
            false -> false // otherwise it scans ALL
        }

    override fun findPreviewsFor(
        classInfo: ClassInfo,
        scanResultFilterState: ScanResultFilterState<T>,
    ): List<ComposablePreview<T>> {
        if (!hasPreviewsIn(classInfo)) return emptyList()

        val composablePreviews: MutableList<ComposablePreview<T>> = mutableListOf()
        classInfo.declaredMethodInfo.asSequence().forEach { methodInfo ->
            val containedAnnotations =
                previewRepeatableAnnotations.filter { classInfo.hasDeclaredMethodAnnotation(it.key) && it.value.isNotEmpty() }

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