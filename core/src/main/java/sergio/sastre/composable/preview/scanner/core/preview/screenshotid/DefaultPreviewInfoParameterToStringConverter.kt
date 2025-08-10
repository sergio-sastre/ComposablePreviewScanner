package sergio.sastre.composable.preview.scanner.core.preview.screenshotid

/**
 * Provides defaults conversion of preview info parameters values into [String]s
 * that can be used to generate unique screenshot ids based on such parameter combinations
 */
class DefaultPreviewInfoParameterToStringConverter {

    companion object {
        private val DEFAULT_NAME = ""
        private val DEFAULT_GROUP = ""
        private const val DEFAULT_API_LEVEL = -1
        private const val DEFAULT_WIDTH_DP = -1
        private const val DEFAULT_HEIGHT_DP = -1
        private const val DEFAULT_FONT_SCALE = 1F
        private const val DEFAULT_BACKGROUND_COLOR = 0L
    }

    fun name(name: String): String? =
        if (name == DEFAULT_NAME) {
            null
        } else {
            name.replace(" ", "_")
        }

    fun group(group: String): String? =
        if (group == DEFAULT_GROUP) {
            null
        } else {
            group.replace(" ", "_")
        }

    fun apiLevel(apiLevel: Int): String? =
        if (apiLevel == DEFAULT_API_LEVEL) {
            null
        } else {
            "API_LEVEL_$apiLevel"
        }

    fun widthDp(widthDp: Int): String? =
        if (widthDp == DEFAULT_WIDTH_DP) {
            null
        } else {
            "W${widthDp}dp"
        }

    fun heightDp(heightDp: Int): String? =
        if (heightDp == DEFAULT_HEIGHT_DP) {
            null
        } else {
            "H${heightDp}dp"
        }

    fun fontScale(fontScale: Float): String? =
        if (fontScale == DEFAULT_FONT_SCALE) {
            null
        } else {
            "FONT_${fontScale}f".replace(".", "_")
        }

    fun showSystemUi(showSystemUi: Boolean): String? =
        if (!showSystemUi) {
            null
        } else {
            "WITH_SYSTEM_UI"
        }

    fun showBackground(showBackground: Boolean): String? =
        if (!showBackground) {
            null
        } else {
            "WITH_BACKGROUND"
        }

    fun backgroundColor(backgroundColor: Long): String? =
        if (backgroundColor == DEFAULT_BACKGROUND_COLOR) {
            null
        } else {
            "BG_COLOR_$backgroundColor"
        }
}