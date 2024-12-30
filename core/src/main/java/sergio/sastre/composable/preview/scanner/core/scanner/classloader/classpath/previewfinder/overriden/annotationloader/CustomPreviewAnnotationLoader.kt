package sergio.sastre.composable.preview.scanner.core.scanner.classloader.classpath.previewfinder.overriden.annotationloader

import io.github.classgraph.AnnotationInfoList

interface CustomPreviewAnnotationLoader {

    fun loadCustomPreviewAnnotation(): Map<String, AnnotationInfoList>
}