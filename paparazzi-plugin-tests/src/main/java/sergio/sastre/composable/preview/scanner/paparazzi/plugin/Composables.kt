package sergio.sastre.composable.preview.scanner.paparazzi.plugin

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices.PIXEL_C
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes

@Composable
fun Example() {
    Text("Example")
}

@Preview(device = "id:pixel")
@Preview(
    device = PIXEL_C,
    showSystemUi = true,
    showBackground = true,
    backgroundColor = 0xFF000080
)
@PreviewScreenSizes
@Preview
@Composable
fun ScreenSizesExamplePreview() {
    Example()
}