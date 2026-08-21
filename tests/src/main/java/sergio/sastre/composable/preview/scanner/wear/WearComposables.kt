package sergio.sastre.composable.preview.scanner.wear

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Text
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import androidx.wear.tooling.preview.devices.WearDevices

import androidx.compose.ui.graphics.Color

@Composable
fun WearExample() {
    Text(
        text = "Wear Example",
        color = Color.Red,
    )
}

@Preview(device = WearDevices.SMALL_ROUND)
fun wearTilePreview(context: Context) = TilePreviewData {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
        LayoutElementBuilders.Box.Builder()
            .setWidth(DimensionBuilders.expand())
            .setHeight(DimensionBuilders.expand())
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setBackground(
                        ModifiersBuilders.Background.Builder()
                            .setColor(ColorBuilders.argb(android.graphics.Color.BLACK))
                            .build()
                    )
                    .build()
            )
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText("Tile Text")
                    .setFontStyle(
                        LayoutElementBuilders.FontStyle.Builder()
                            .setColor(ColorBuilders.argb(android.graphics.Color.RED))
                            .build()
                    )
                    .build()
            )
            .build()
    ).build()
}

@androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
@androidx.wear.compose.ui.tooling.preview.WearPreviewSquare
@Composable
fun WearComposePreview() {
    WearExample()
}
