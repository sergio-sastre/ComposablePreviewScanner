package sergio.sastre.composable.preview.scanner.wear.device

import sergio.sastre.composable.preview.scanner.android.device.DevicePreviewInfoParser
import sergio.sastre.composable.preview.scanner.android.device.domain.Device
import sergio.sastre.composable.preview.scanner.android.device.types.Wear

object WearDevicePreviewInfoParser {

    fun parse(device: String): Device? =
        DevicePreviewInfoParser.parse(device, Wear.WEAR_OS_SMALL_ROUND.device)
}