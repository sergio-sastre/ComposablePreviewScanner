package io.github.sergio.sastre.composable.preview.scanner.paparazzi.plugin

sealed class AnnotationFilter : java.io.Serializable {

    open class Include(val annotations: List<String>) : AnnotationFilter() {
        companion object Default : Include(listOf(DEFAULT_INCLUDE_ANNOTATION)) {
            operator fun invoke(vararg annotations: String = arrayOf(DEFAULT_INCLUDE_ANNOTATION)): Include =
                Include(annotations.toList())
        }
    }

    open class Exclude(val annotations: List<String>) : AnnotationFilter() {
        companion object Default : Exclude(listOf(DEFAULT_EXCLUDE_ANNOTATION)) {
            operator fun invoke(vararg annotations: String = arrayOf(DEFAULT_EXCLUDE_ANNOTATION)): Exclude =
                Exclude(annotations.toList())
        }
    }

    companion object {
        const val DEFAULT_INCLUDE_ANNOTATION =
            "sergio.sastre.composable.preview.scanner.paparazzi.annotations.IncludeInScreenshotTests"
        const val DEFAULT_EXCLUDE_ANNOTATION =
            "sergio.sastre.composable.preview.scanner.paparazzi.annotations.ExcludeInScreenshotTests"
    }
}
