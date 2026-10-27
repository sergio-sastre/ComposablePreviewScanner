package sergio.sastre.composable.preview.scanner.wear.configuration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
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

sealed interface TileRoundShapeClipping {
    /** Disables all shape & chin clipping (full rectangle) */
    data object Disabled : TileRoundShapeClipping

    /** Round shape clipping options */
    sealed interface Enabled : TileRoundShapeClipping {
        /**
         * Clips ALL round devices:
         * - If device has a chin -> Clips circle + flat chin cut.
         * - If device has no chin -> Clips full circle.
         */
        data object AllRoundDevices : Enabled

        /**
         * Clips ONLY devices that have a chin (circle + flat chin cut).
         * If the device has no chin (standard round or rectangular), clipping is skipped.
         *
         * This is useful for Paparazzi & Roborazzi tests, where using this in combination with setting
         * the Round shape in the Device Config if is Round Without Chin in Paparazzi
         * or using RecordOptions(applyDeviceCrop = isRoundWithoutChin) in Roborazzi results into a smaller screenshot image
         * in disk space
         */
        data object OnlyDevicesWithChin : Enabled

        /**
         * Clips round devices as full circles (ignores chin cut even if device has a chin).
         */
        data object IgnoreChin : Enabled
    }
}

class WearTileSnapshotConfigurator(
    private val context: Context
) {
    private var roundShapeClipping: TileRoundShapeClipping =
        TileRoundShapeClipping.Enabled.AllRoundDevices

    /**
     * Enables or disables round shape clipping.
     * If not set, the default is [TileRoundShapeClipping.Enabled.AllRoundDevices]
     * so applies clipping (circle + chin) to all Round devices
     */
    fun setTileRoundShapeClipping(mode: TileRoundShapeClipping) = apply {
        this.roundShapeClipping = mode
    }

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
        val default = Wear.WEAR_OS_SMALL_ROUND.device
        val deviceParser =
            DevicePreviewInfoParser.parse(device, default) ?: default
        val deviceInDp = deviceParser.inDp()
        val deviceInPx = deviceParser.inPx()

        val hasChin = deviceParser.shape == Shape.ROUND && deviceInPx.chinSize.value > 0f

        val isRound = when (roundShapeClipping) {
            TileRoundShapeClipping.Enabled.AllRoundDevices,
            TileRoundShapeClipping.Enabled.IgnoreChin -> deviceParser.shape == Shape.ROUND
            TileRoundShapeClipping.Enabled.OnlyDevicesWithChin -> hasChin
            TileRoundShapeClipping.Disabled -> false
        }

        val chinSizePx = when (roundShapeClipping) {
            TileRoundShapeClipping.Enabled.AllRoundDevices,
            TileRoundShapeClipping.Enabled.OnlyDevicesWithChin -> deviceInPx.chinSize.value
            TileRoundShapeClipping.Enabled.IgnoreChin,
            TileRoundShapeClipping.Disabled -> 0f
        }

        val widthDp = deviceInDp.dimensions.width
        val heightDp = deviceInDp.dimensions.height
        val widthPx = deviceInPx.dimensions.width.toInt()
        val heightPx = deviceInPx.dimensions.height.toInt()

        val container = ChinClippedFrameLayout(context, isRound, chinSizePx)
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
                .addPlatformDataProvider(provider, *platformDataValues.all.keys.toTypedArray())
                .build()
        } ?: TileRenderer(context, Runnable::run) { _ -> /* no-op loader */ }
}

private class ChinClippedFrameLayout(
    context: Context,
    private val isRound: Boolean,
    private val chinSizePx: Float,
) : FrameLayout(context) {

    init {
        setWillNotDraw(false)
    }

    private val clipPath = Path()
    private val outerPath = Path()
    private val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    private var lastWidth = -1
    private var lastHeight = -1

    private fun updateClipPath(w: Int, h: Int) {
        if (w <= 0 || h <= 0) return
        if (w == lastWidth && h == lastHeight) return
        lastWidth = w
        lastHeight = h

        clipPath.reset()
        if (isRound && chinSizePx > 0f) {
            val enlargedOval = Path().apply {
                addOval(
                    0f,
                    0f,
                    w.toFloat(),
                    h.toFloat() + chinSizePx,
                    Path.Direction.CW,
                )
            }
            val visibleScreenRect = Path().apply {
                addRect(
                    0f,
                    0f,
                    w.toFloat(),
                    h.toFloat(),
                    Path.Direction.CW,
                )
            }
            clipPath.op(enlargedOval, visibleScreenRect, Path.Op.INTERSECT)
        } else if (isRound) {
            clipPath.addCircle(w / 2f, h / 2f, w / 2f, Path.Direction.CW)
        } else {
            clipPath.addRect(0f, 0f, w.toFloat(), h.toFloat(), Path.Direction.CW)
        }

        outerPath.reset()
        if (isRound) {
            val fullRect = Path().apply {
                addRect(0f, 0f, w.toFloat(), h.toFloat(), Path.Direction.CW)
            }
            outerPath.op(fullRect, clipPath, Path.Op.DIFFERENCE)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateClipPath(w, h)
    }

    override fun dispatchDraw(canvas: Canvas) {
        updateClipPath(width, height)
        val saveCount = canvas.save()
        if (!clipPath.isEmpty) {
            canvas.clipPath(clipPath)
        }
        super.dispatchDraw(canvas)
        canvas.restoreToCount(saveCount)

        if (!outerPath.isEmpty) {
            canvas.drawPath(outerPath, clearPaint)
        }
    }
}
