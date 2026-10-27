package sergio.sastre.composable.preview.scanner.tests.paparazzi.utils

import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import app.cash.paparazzi.DeviceConfig
import com.android.resources.Density
import com.android.resources.NightMode
import com.android.resources.ScreenOrientation
import com.android.resources.ScreenRatio
import com.android.resources.ScreenRound
import com.android.resources.ScreenSize
import sergio.sastre.composable.preview.scanner.android.device.DevicePreviewInfoParser
import sergio.sastre.composable.preview.scanner.android.device.types.Wear
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewInfo
import sergio.sastre.composable.preview.scanner.wear.device.WearDevicePreviewInfoParser

object DeviceConfigBuilder {
    fun build(previewDevice: String): DeviceConfig {
        val device = DevicePreviewInfoParser.parse(previewDevice) ?: return DeviceConfig()
        return DeviceConfig(
            screenHeight = device.dimensions.height.toInt(),
            screenWidth = device.dimensions.width.toInt(),
            xdpi = device.densityDpi, // not 100% precise
            ydpi = device.densityDpi, // not 100% precise
            ratio = ScreenRatio.valueOf(device.screenRatio.name),
            size = ScreenSize.valueOf(device.screenSize.name),
            density = Density(device.densityDpi),
            screenRound = ScreenRound.valueOf(device.shape.name)
        )
    }

    fun buildWear(previewDevice: String): DeviceConfig {
        val parsedDevice =
            WearDevicePreviewInfoParser.parse(previewDevice)?.inPx() ?: return DeviceConfig()

        // if chinSize, it has to be NOTROUND & set externally
        val shape = when (parsedDevice.chinSize.value > 0f) {
            true -> ScreenRound.NOTROUND
            false -> ScreenRound.valueOf(parsedDevice.shape.name)
        }

        return DeviceConfig(
            screenHeight = parsedDevice.dimensions.height.toInt(),
            screenWidth = parsedDevice.dimensions.width.toInt(),
            density = Density(parsedDevice.densityDpi),
            xdpi = parsedDevice.densityDpi, // not 100% precise
            ydpi = parsedDevice.densityDpi, // not 100% precise
            size = ScreenSize.valueOf(parsedDevice.screenSize.name),
            ratio = ScreenRatio.valueOf(parsedDevice.screenRatio.name),
            screenRound = shape,
            orientation = ScreenOrientation.valueOf(parsedDevice.orientation.name)
        )
    }
}

fun DeviceConfig.applyUiMode(uiMode: Int): DeviceConfig {
    return this.copy(
        nightMode =
            when (uiMode and UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES) {
                true -> NightMode.NIGHT
                false -> NightMode.NOTNIGHT
            }
    )
}