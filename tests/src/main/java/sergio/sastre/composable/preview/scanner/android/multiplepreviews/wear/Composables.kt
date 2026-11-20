package sergio.sastre.composable.preview.scanner.android.multiplepreviews.wear

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import androidx.wear.compose.ui.tooling.preview.WearPreviewLargeRound
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import androidx.wear.compose.ui.tooling.preview.WearPreviewSquare

@Composable
fun WearExample() {
    Text(
        text = "Wear Example",
        color = White
    )
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun WearMultiplePreviewsExample() {
    WearExample()
}

@WearPreviewSmallRound
@WearPreviewLargeRound
@WearPreviewSquare
@Composable
fun WearShapePreviewsExample() {
    WearExample()
}
