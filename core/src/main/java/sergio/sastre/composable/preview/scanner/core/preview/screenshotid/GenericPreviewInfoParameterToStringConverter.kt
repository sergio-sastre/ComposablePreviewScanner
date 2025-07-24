package sergio.sastre.composable.preview.scanner.core.preview.screenshotid

class GenericPreviewInfoParameterToStringConverter {

    fun name(name: String): String? =
        if (name == "") {
            null
        } else {
            name.replace(" ", "_")
        }

    fun group(group: String): String? =
        if (group == "") {
            null
        } else {
            group.replace(" ", "_")
        }

    fun apiLevel(apiLevel: Int): String? =
        if (apiLevel == -1) {
            null
        } else {
            "API_LEVEL_$apiLevel"
        }

    fun widthDp(widthDp: Int): String? = if (widthDp == -1) {
        null
    } else {
        "W${widthDp}dp"
    }

    fun heightDp(heightDp: Int): String? =
        if (heightDp == -1) {
            null
        } else {
            "H${heightDp}dp"
        }

    fun fontScale(fontScale: Float): String? =
        if (fontScale == 1F) {
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
        if (backgroundColor == 0L) {
            null
        } else {
            "BG_COLOR_$backgroundColor"
        }
}