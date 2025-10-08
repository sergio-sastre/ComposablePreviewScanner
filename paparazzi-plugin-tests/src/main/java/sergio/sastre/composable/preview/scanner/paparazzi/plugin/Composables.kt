package sergio.sastre.composable.preview.scanner.paparazzi.plugin

import android.os.Build
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices.PIXEL_C
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.PreviewScreenSizes

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
@Preview
@Composable
fun ScreenSizesExamplePreview() {
    Example("API ${Build.VERSION.SDK_INT}")
}

@Preview(name = "😍")
@Composable
fun ScreenSizesExample2Preview(
    @PreviewParameter(NamesProvider ::class) names: List<String>
) {
    Example("Names: ${names.joinToString(", ")}")
}