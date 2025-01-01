package sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.previewfinder.overriden

import io.github.classgraph.AnnotationInfo
import io.github.classgraph.ClassInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreator
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.ClassLoader
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.previewfinder.PreviewsFinder
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.ScanResultFilterState

internal class SameModuleComposableWithCustomPreviewsFinder<T>(
    override val annotationToScanClassName: String,
    private val previewInfoMapper: ComposablePreviewInfoMapper<T>,
    private val previewMapperCreator: ComposablePreviewMapperCreator<T>,
    private val classLoader: ClassLoader,
): PreviewsFinder<T> {

    private fun hasPreviewsIn(classInfo: ClassInfo): Boolean =
        classInfo.hasDeclaredMethodAnnotation("$annotationToScanClassName\$Container")

    @Suppress("UNCHECKED_CAST")
    override fun findPreviewsFor(
        classInfo: ClassInfo,
        scanResultFilterState: ScanResultFilterState<T>,
    ): List<ComposablePreview<T>> {
        if (!hasPreviewsIn(classInfo)) return emptyList()

        return classInfo.declaredMethodInfo.asSequence().flatMap { methodInfo ->
            methodInfo.getAnnotationInfo("$annotationToScanClassName\$Container")?.let {
                if (scanResultFilterState.hasExcludedAnnotation(methodInfo) || scanResultFilterState.excludesMethod(methodInfo)) {
                    emptySequence()
                } else {
                    val clazz = classLoader.loadClass(classInfo)
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
}