package sergio.sastre.composable.preview.scanner.multiplepreviews

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes

@Composable
fun Example(){
    Text("Example")
}

@PreviewFontScale
@Composable
fun FontScaleExamplePreview(){
    Example()
}

@Preview(device = Devices.DEFAULT)
@Preview(device = "name:Nexus 10")
@Preview(device = "id:pixel")
@PreviewScreenSizes
@Composable
fun ScreenSizesExamplePreview(){
    Example()
}