package sergio.sastre.composable.preview.scanner.core.scanner.classloader.classpath.previewfinder.overriden.annotationloader

import io.github.classgraph.AnnotationInfoList
import io.github.classgraph.ScanResult

class ScanResultCustomPreviewAnnotationLoader(
    private val scanResult: ScanResult,
    private val annotationToScanClassName: String
) : CustomPreviewAnnotationLoader {

    override fun loadCustomPreviewAnnotation(): Map<String, AnnotationInfoList> =
        scanResult.allAnnotations
            .associate { clazz ->
                clazz.name to clazz.annotationInfo.filter { it.name == annotationToScanClassName }
            }.filterValues { it.isNotEmpty() }
}