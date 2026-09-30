package io.github.sergio.sastre.composable.preview.scanner.paparazzi.plugin

sealed class AnnotationFilter : java.io.Serializable {
    data class Include(val annotations: List<String>) : AnnotationFilter()
    data class Exclude(val annotations: List<String>) : AnnotationFilter()
}
