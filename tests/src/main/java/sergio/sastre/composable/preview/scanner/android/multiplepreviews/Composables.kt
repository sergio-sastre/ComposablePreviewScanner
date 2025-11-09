package sergio.sastre.composable.preview.scanner.android.multiplepreviews

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
@Preview(device = Devices.AUTOMOTIVE_1024p)
@Preview(device = "name:Nexus 10")
@Preview(device = "id:pixel")
@Preview(device = "spec:width=114.3dp,height=114.3dp,isRound=true,chinSize=9.7dp")
@PreviewScreenSizes
@Composable
fun ScreenSizesExamplePreview(){
    Example()
}