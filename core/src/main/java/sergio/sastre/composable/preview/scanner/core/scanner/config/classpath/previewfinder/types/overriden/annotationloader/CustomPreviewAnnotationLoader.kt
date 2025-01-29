package sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.types.overriden.annotationloader

import io.github.classgraph.AnnotationInfoList

interface CustomPreviewAnnotationLoader {

    fun loadCustomPreviewAnnotation(): Map<String, AnnotationInfoList>
}