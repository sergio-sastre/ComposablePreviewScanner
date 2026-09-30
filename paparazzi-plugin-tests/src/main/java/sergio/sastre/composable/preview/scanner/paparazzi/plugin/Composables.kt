package sergio.sastre.composable.preview.scanner.paparazzi.plugin

import android.os.Build
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices.PIXEL_C
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import sergio.sastre.composable.preview.scanner.paparazzi.annotations.ExcludeInScreenshotTests

class NamesProvider : PreviewParameterProvider<List<String>> {
    override val values: Sequence<List<String>> = sequenceOf(
        listOf("Alice", "Bob", "Charlie"),
        listOf("Charlie", "Bob", "Alice")
    )
}

@Composable
fun Example(apiLevel: String) {
    Text(apiLevel)
}

@Preview(device = "id:pixel")
@Preview(
    device = PIXEL_C,
    showSystemUi = true,
    showBackground = true,
    backgroundColor = 0xFF000080
)
@PreviewScreenSizes
@Preview(apiLevel = 31)
@Preview(fontScale = 1.3f)
@Preview
@Composable
fun ScreenSizesExamplePreview() {
    Example("API ${Build.VERSION.SDK_INT}")
}

@Preview(name = "ScreenSize")
@Composable
fun ScreenSizesExample2Preview(
    @PreviewParameter(NamesProvider ::class) names: List<String>
) {
    Example("Names: ${names.joinToString(", ")}")
}

@ExcludeInScreenshotTests
@Preview(name = "ExcludedFromScreenshotTests")
@Composable
fun ExcludedFromScreenshotTestsPreview() {
    Example("This preview should be excluded from generated tests")
}