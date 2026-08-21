package sergio.sastre.composable.preview.scanner.wear.configuration

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DeviceParametersBuilders.SCREEN_SHAPE_ROUND
import androidx.wear.protolayout.DeviceParametersBuilders.SCREEN_SHAPE_RECT
import androidx.wear.protolayout.DeviceParametersBuilders.DEVICE_PLATFORM_WEAR_OS
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.renderer.TileRenderer
import androidx.wear.tiles.tooling.preview.TilePreviewData
import java.util.concurrent.TimeUnit
import sergio.sastre.composable.preview.scanner.wear.preview.WearComposablePreview
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewInfo

import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.viewinterop.AndroidView

class WearTileSnapshotConfigurator(
    private val context: Context
) {
    private val rootView = FrameLayout(context)

    fun composableToView(
        composable: @Composable () -> TilePreviewData
    ): View {
        return ComposeView(context).apply {
            setContent {
                val tilePreviewData = composable()
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        tilePreviewToView(tilePreviewData)
                    }
                )
            }
        }
    }

    fun tilePreviewToView(
        data: TilePreviewData,
        device: String = "",
        widthDp: Int = 225,
        heightDp: Int = 225,
        parentView: ViewGroup? = null
    ): View {
        val density = context.resources.displayMetrics.density
        val widthPx = (widthDp * density).toInt()
        val heightPx = (heightDp * density).toInt()

        val container = parentView ?: rootView
        container.layoutParams = ViewGroup.LayoutParams(widthPx, heightPx)

        val deviceParams = buildDeviceParameters(widthDp, heightDp, device)
        val tileRequest =
            RequestBuilders.TileRequest.Builder().setDeviceConfiguration(deviceParams).build()
        val tile: TileBuilders.Tile = data.onTileRequest(tileRequest)

        val resourcesRequest = RequestBuilders.ResourcesRequest.Builder()
            .setVersion(tile.resourcesVersion.ifEmpty { "1" })
            .setDeviceConfiguration(deviceParams)
            .build()

        val resources = data.onTileResourceRequest(resourcesRequest)
        val layout = tile.tileTimeline?.timelineEntries?.firstOrNull()?.layout
            ?: error("TilePreview produced no layout")

        val parent = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            setBackgroundColor(Color.BLACK)
        }
        container.addView(parent)

        if (device.contains("round", ignoreCase = true) || device.isEmpty()) {
            container.clipToOutline = true
        }

        val renderer = TileRenderer(context, Runnable::run) { _ -> /* no-op loader */ }
        val view = renderer.inflateAsync(layout, resources, parent)
            .get(10, TimeUnit.SECONDS)
            ?: error("TileRenderer returned no view")

        (view.layoutParams as? FrameLayout.LayoutParams)?.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.MATCH_PARENT
            gravity = Gravity.CENTER
        }

        return container
    }

    private fun buildDeviceParameters(
        widthDp: Int,
        heightDp: Int,
        device: String
    ): DeviceParameters {
        val isRound = device.contains("round", ignoreCase = true) || device.isEmpty()
        return DeviceParameters.Builder()
            .setScreenWidthDp(widthDp)
            .setScreenHeightDp(heightDp)
            .setScreenDensity(2.0f)
            .setScreenShape(
                if (isRound) SCREEN_SHAPE_ROUND else SCREEN_SHAPE_RECT
            )
            .setDevicePlatform(DEVICE_PLATFORM_WEAR_OS)
            .build()
    }
}
