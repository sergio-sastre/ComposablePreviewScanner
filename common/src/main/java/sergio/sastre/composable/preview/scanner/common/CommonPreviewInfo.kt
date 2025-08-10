package sergio.sastre.composable.preview.scanner.common

data class CommonPreviewInfo(
    val name: String = "",
    val group: String = "",
    val widthDp: Int = -1,
    val heightDp: Int = -1,
    val locale: String = "",
    val showBackground: Boolean = false,
    val backgroundColor: Long = 0,
)