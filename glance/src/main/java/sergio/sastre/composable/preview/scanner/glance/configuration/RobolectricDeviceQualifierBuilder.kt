package sergio.sastre.composable.preview.scanner.glance.configuration

import sergio.sastre.composable.preview.scanner.glance.GlancePreviewInfo

object RobolectricDeviceQualifierBuilder {
    fun build(previewInfo: GlancePreviewInfo): String? {
        val width = when (previewInfo.widthDp == -1) {
            true -> null
            else -> "w${previewInfo.widthDp}dp"
        }
        val height = when (previewInfo.heightDp == -1) {
            true -> null
            else -> "h${previewInfo.heightDp}dp"
        }
        return listOfNotNull(
            width,
            height,
            "land",
        ).joinToString("-")
    }
}