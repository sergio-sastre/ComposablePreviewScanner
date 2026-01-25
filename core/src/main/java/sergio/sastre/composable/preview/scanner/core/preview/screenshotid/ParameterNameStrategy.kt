package sergio.sastre.composable.preview.scanner.core.preview.screenshotid

import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

interface ParameterNameStrategy {
    fun resolve(preview: ComposablePreview<*>): String?
}

object UseIndexStrategy : ParameterNameStrategy {
    override fun resolve(preview: ComposablePreview<*>): String? =
        preview.previewIndex?.toString()
}

object UseDisplayNameStrategy : ParameterNameStrategy {
    override fun resolve(preview: ComposablePreview<*>): String? =
        preview.previewIndexDisplayName?.replace(" ", "_")
            ?: preview.previewIndex?.toString()
}