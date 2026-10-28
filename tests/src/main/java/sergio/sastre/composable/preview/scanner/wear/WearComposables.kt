package sergio.sastre.composable.preview.scanner.wear

import android.content.Context
import android.graphics.Color.BLACK
import android.graphics.Color.RED
import androidx.compose.ui.tooling.preview.Devices
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ResourceBuilders.AndroidImageResourceByResId
import androidx.wear.protolayout.ResourceBuilders.ImageResource
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.protolayout.TypeBuilders
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicFloat
import androidx.wear.protolayout.expression.DynamicDataBuilders.DynamicDataValue
import androidx.wear.protolayout.expression.PlatformDataValues
import androidx.wear.protolayout.expression.PlatformHealthSources
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import androidx.wear.tooling.preview.devices.WearDevices
import androidx.wear.protolayout.ModifiersBuilders.Background
import androidx.wear.protolayout.ModifiersBuilders.Modifiers
import sergio.sastre.composable.preview.scanner.R

annotation class PaparazziConfig(
    val maxPercentDifference: Double,
    val onlyApplyToDevice: String,
)

@Preview(device = WearDevices.LARGE_ROUND)
@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.RECT)
@Preview(device = WearDevices.SQUARE)
@Preview(device = "id:pixel_5")
@Preview(device = Devices.DEFAULT)
fun wearTilePreview() = TilePreviewData {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
        LayoutElementBuilders.Box.Builder()
            .setWidth(DimensionBuilders.expand())
            .setHeight(DimensionBuilders.expand())
            .setModifiers(
                Modifiers.Builder()
                    .setBackground(
                        Background.Builder()
                            .setColor(ColorBuilders.argb(BLACK))
                            .build()
                    )
                    .build()
            )
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText("Tile Text")
                    .setFontStyle(
                        LayoutElementBuilders.FontStyle.Builder()
                            .setColor(ColorBuilders.argb(RED))
                            .build()
                    )
                    .build()
            )
            .build()
    ).build()
}

private var tilePreviewInvocations = 0

@Preview(name = "First")
@Preview(name = "Second")
fun statefulTilePreview(context: Context) = TilePreviewData {
    tilePreviewInvocations++
    TilePreviewHelper.singleTimelineEntryTileBuilder(
        LayoutElementBuilders.Box.Builder()
            .setWidth(DimensionBuilders.expand())
            .setHeight(DimensionBuilders.expand())
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText("Invocations: $tilePreviewInvocations")
                    .build()
            )
            .build()
    ).build()
}

private const val chinSizeDevice = "spec:width=250px,height=250px,isRound=true, chinSize=15.5px"
@PaparazziConfig(
    maxPercentDifference = 0.00196,
    onlyApplyToDevice = chinSizeDevice // avoid error in Pipeline
)
@Preview(device = chinSizeDevice)
@Preview(device = "spec:width=250dp,height=250dp,isRound=false")
@Preview(locale = "es", fontScale = 2f)
fun localeTilePreview(context: Context) = TilePreviewData {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
        LayoutElementBuilders.Box.Builder()
            .setWidth(DimensionBuilders.expand())
            .setHeight(DimensionBuilders.expand())
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText(context.getString(R.string.tile_hello))
                    .build()
            )
            .build()
    ).build()
}

// for the string test: same method name, different signature (context as type)
@Preview
fun tilePreviewWithResourcesAndPlatformData(context: Context) =
    tilePreviewWithResourcesAndPlatformData()

@Preview
fun tilePreviewWithResourcesAndPlatformData() = TilePreviewData(
    onTileResourceRequest = { request ->
        Resources.Builder()
            .setVersion(request.version)
            .addIdToImageMapping(
                "image_id",
                ImageResource.Builder()
                    .setAndroidResourceByResId(
                        AndroidImageResourceByResId.Builder()
                            .setResourceId(R.drawable.ic_launcher_background)
                            .build()
                    )
                    .build()
            )
            .build()
    },
    platformDataValues = PlatformDataValues.Builder()
        .put(PlatformHealthSources.Keys.HEART_RATE_BPM, DynamicDataValue.fromFloat(120f))
        .build()
) { request ->
    TilePreviewHelper.singleTimelineEntryTileBuilder(
        LayoutElementBuilders.Box.Builder()
            .addContent(
                LayoutElementBuilders.Column.Builder()
                    .addContent(
                        LayoutElementBuilders.Image.Builder()
                            .setResourceId("image_id")
                            .setWidth(DimensionBuilders.dp(24f))
                            .setHeight(DimensionBuilders.dp(24f))
                            .build()
                    )
                    .addContent(
                        LayoutElementBuilders.Text.Builder()
                            .setText(
                                TypeBuilders.StringProp.Builder("--")
                                    .setDynamicValue(
                                        DynamicFloat.from(PlatformHealthSources.Keys.HEART_RATE_BPM)
                                            .format()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    ).build()
}
