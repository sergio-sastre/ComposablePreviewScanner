package sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.types.compiledclass.annotationloader

import io.github.classgraph.AnnotationInfoList

interface CustomPreviewAnnotationLoader {

    fun loadCustomPreviewAnnotation(): Map<String, AnnotationInfoList>
}