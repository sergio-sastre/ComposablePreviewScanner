package sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.types.compiledclass

import io.github.classgraph.AnnotationInfo
import io.github.classgraph.ClassInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreviewWithResult
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperWithResult
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreatorWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.MethodFinder
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.PreviewsFinderWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.classloaders.ClassLoader
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.ScanResultFilterState

/**
 * This is necessary when using a custom source set that scans Previews from compiled classes.
 *
 * That is because in compiled classes, when a Repeatable annotation like @Preview is repeated,
 * their name is suffixed with $Container and the values of each annotation are grouped under
 * the parameterValues of the previously mentioned suffixed annotation.
 */
internal class MultiplePreviewsInCompiledClassFinderWithResult<T, R>(
    override val annotationToScanClassName: String,
    private val previewInfoMapper: ComposablePreviewInfoMapper<T>,
    private val previewMapperCreator: ComposablePreviewMapperCreatorWithResult<T, R>,
    private val classLoader: ClassLoader,
) : PreviewsFinderWithResult<T, R> {

    private fun hasPreviewsIn(classInfo: ClassInfo): Boolean =
        classInfo.hasDeclaredMethodAnnotation("$annotationToScanClassName\$Container")

    @Suppress("UNCHECKED_CAST")
    override fun findPreviewsFor(
        classInfo: ClassInfo,
        scanResultFilterState: ScanResultFilterState<T>,
    ): List<ComposablePreviewWithResult<T, R>> {
        if (!hasPreviewsIn(classInfo)) return emptyList()

        return classInfo.declaredMethodInfo.asSequence().flatMap { methodInfo ->
            methodInfo.getAnnotationInfo("$annotationToScanClassName\$Container")?.let {
                if (scanResultFilterState.shouldIncludeMethod(methodInfo)) {
                    val method = MethodFinder(classInfo, classLoader).find(methodInfo)
                    val previewMethods: MutableList<ComposablePreviewMapperWithResult<T, R>> = mutableListOf()
                    val previews: Array<Any> = it.parameterValues.getValue("value") as Array<Any>
                    previews
                        .map { annotation -> (annotation as AnnotationInfo) }
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
                    previewMethods.asSequence()
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
}

internal typealias MultiplePreviewsInCompiledClassFinder<T> = MultiplePreviewsInCompiledClassFinderWithResult<T, Unit>
