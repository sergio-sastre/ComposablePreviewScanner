package sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.types.compiledclass.annotationloader

import io.github.classgraph.AnnotationInfoList
import io.github.classgraph.ClassGraph

internal class PackageTreesCustomPreviewAnnotationLoader(
    private val packageTreesOfCustomPreviews: List<String> = emptyList(),
    private val annotationToScanClassName: String
) : CustomPreviewAnnotationLoader {

    private val scanResult
        get() = ClassGraph()
            .acceptPackages(*packageTreesOfCustomPreviews.toTypedArray())
            .enableAnnotationInfo()
            .scan()

    override fun loadCustomPreviewAnnotation(): Map<String, AnnotationInfoList> =
        when (packageTreesOfCustomPreviews.isEmpty()) {
            true -> emptyMap()
            false -> scanResult
                .use { scanResult ->
                    scanResult.allAnnotations
                        .associate { clazz ->
                            clazz.name to clazz.annotationInfo.filter { it.name == annotationToScanClassName }
                        }.filterValues { it.isNotEmpty() }
                }
        }

}