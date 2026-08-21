package sergio.sastre.composable.preview.scanner.wear.configuration

import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DeviceParametersBuilders.SCREEN_SHAPE_ROUND
import androidx.wear.protolayout.DeviceParametersBuilders.SCREEN_SHAPE_RECT
import androidx.wear.protolayout.DeviceParametersBuilders.DEVICE_PLATFORM_WEAR_OS
import androidx.wear.protolayout.expression.pipeline.PlatformDataProvider
import androidx.wear.protolayout.expression.pipeline.PlatformDataReceiver
import java.util.concurrent.Executor
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.renderer.TileRenderer
import androidx.wear.tiles.tooling.preview.TilePreviewData
import java.util.concurrent.TimeUnit
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.viewinterop.AndroidView
import sergio.sastre.composable.preview.scanner.android.device.DevicePreviewInfoParser
import sergio.sastre.composable.preview.scanner.android.device.domain.Shape
import sergio.sastre.composable.preview.scanner.android.device.types.Wear
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreviewWithResult
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewInfo

class WearTileSnapshotConfigurator(
    private val context: Context
) {
    private val rootView = FrameLayout(context)

    fun previewToView(
        preview: ComposablePreviewWithResult<WearTilePreviewInfo, TilePreviewData>,
    ): View {
        return ComposeView(context).apply {
            setContent {
                val tilePreviewData = preview()
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        tilePreviewToView(preview.previewInfo.device, tilePreviewData)
                    }
                )
            }
        }
    }

    private fun tilePreviewToView(
        device: String,
        data: TilePreviewData
    ): View {

        val deviceParser = DevicePreviewInfoParser.parse(device) ?: Wear.WEAR_OS_SMALL_ROUND.device
        val isRound = deviceParser.shape == Shape.ROUND
        val widthDp = deviceParser.dimensions.width
        val heightDp = deviceParser.dimensions.width
        val density = context.resources.displayMetrics.density
        val widthPx = (widthDp * density).toInt()
        val heightPx = (heightDp * density).toInt()

        val container = rootView
        container.layoutParams = ViewGroup.LayoutParams(widthPx, heightPx)

        val deviceParams = buildDeviceParameters(widthDp.toInt(), heightDp.toInt(), isRound)
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

        if (isRound) {
            container.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setOval(0, 0, view.width, view.height)
                }
            }
            container.clipToOutline = true
        }

        val renderer = createRenderer(data)

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
        isRound: Boolean
    ): DeviceParameters {
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

    private fun createRenderer(data: TilePreviewData): TileRenderer =
        data.platformDataValues?.let { platformDataValues ->
            val provider = object : PlatformDataProvider {
                override fun setReceiver(executor: Executor, receiver: PlatformDataReceiver) {
                    executor.execute { receiver.onData(platformDataValues) }
                }

                override fun clearReceiver() {
                    // no-op
                }
            }
            TileRenderer.Builder(context, Runnable::run) { _ -> }
                .addPlatformDataProvider(provider, *platformDataValues.getAll().keys.toTypedArray())
                .build()
        } ?: TileRenderer(context, Runnable::run) { _ -> /* no-op loader */ }
}
