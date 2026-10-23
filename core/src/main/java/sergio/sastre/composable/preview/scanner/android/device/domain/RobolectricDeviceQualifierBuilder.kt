package sergio.sastre.composable.preview.scanner.android.device.domain

import sergio.sastre.composable.preview.scanner.android.device.DevicePreviewInfoParser
import sergio.sastre.composable.preview.scanner.android.device.types.DEFAULT

object RobolectricDeviceQualifierBuilder {

    fun build(previewDevice: String, default: Device): String? {
        val device = DevicePreviewInfoParser.parse(previewDevice, default)
        return device?.let { nonNullDevice -> build(nonNullDevice) }
    }

    fun build(previewDevice: String): String? =
        build(previewDevice, DEFAULT)

    fun build(device: Device): String {
        val deviceInDp = device.inDp()

        val round = when(deviceInDp.shape){
            Shape.ROUND -> "round"
            Shape.NOTROUND -> "notround"
        }

        val type = when(deviceInDp.type){
            Type.PHONE -> null
            Type.TABLET -> null
            Type.DESKTOP -> null
            Type.FOLDABLE -> null
            Type.XR -> null
            Type.WEAR -> "watch"
            Type.CAR -> "car"
            Type.TV -> "television"
            null -> null
        }

        val orientation = when (deviceInDp.orientation){
            Orientation.PORTRAIT -> "port"
            Orientation.LANDSCAPE -> "land"
        }

        return listOfNotNull(
            "w${deviceInDp.dimensions.width.toInt()}dp",
            "h${deviceInDp.dimensions.height.toInt()}dp",
            deviceInDp.screenSize.name.lowercase(),
            deviceInDp.screenRatio.name.lowercase(),
            round,
            orientation,
            type,
            "${deviceInDp.densityDpi}dpi"
        ).joinToString("-")
    }
}