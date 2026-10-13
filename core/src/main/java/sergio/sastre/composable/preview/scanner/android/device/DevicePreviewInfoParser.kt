package sergio.sastre.composable.preview.scanner.android.device

import sergio.sastre.composable.preview.scanner.android.device.domain.ChinSize
import sergio.sastre.composable.preview.scanner.android.device.domain.Cutout
import sergio.sastre.composable.preview.scanner.android.device.domain.Cutout.NONE
import sergio.sastre.composable.preview.scanner.android.device.domain.Device
import sergio.sastre.composable.preview.scanner.android.device.domain.Dimensions
import sergio.sastre.composable.preview.scanner.android.device.domain.Navigation.GESTURE
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation.LANDSCAPE
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation.PORTRAIT
import sergio.sastre.composable.preview.scanner.android.device.domain.GetDeviceByIdentifier.Companion.findByDeviceId
import sergio.sastre.composable.preview.scanner.android.device.domain.GetDeviceByIdentifier.Companion.findByDeviceName
import sergio.sastre.composable.preview.scanner.android.device.domain.Navigation
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation
import sergio.sastre.composable.preview.scanner.android.device.domain.Shape
import sergio.sastre.composable.preview.scanner.android.device.domain.Type
import sergio.sastre.composable.preview.scanner.android.device.domain.Unit
import sergio.sastre.composable.preview.scanner.android.device.types.DEFAULT
import sergio.sastre.composable.preview.scanner.android.device.types.Automotive
import sergio.sastre.composable.preview.scanner.android.device.types.Desktop
import sergio.sastre.composable.preview.scanner.android.device.types.GenericDevices
import sergio.sastre.composable.preview.scanner.android.device.types.Phone
import sergio.sastre.composable.preview.scanner.android.device.types.Tablet
import sergio.sastre.composable.preview.scanner.android.device.types.Television
import sergio.sastre.composable.preview.scanner.android.device.types.Wear
import sergio.sastre.composable.preview.scanner.android.device.types.XR

object DevicePreviewInfoParser {
    fun parse(device: String): Device? {
        if (device == "") return DEFAULT

        if (device.startsWith("spec:")) {
            return GetCustomDevice.from(device)
        }

        if (device.startsWith("id:")) {
            return GetDeviceById.from(device)
        }

        if (device.startsWith("name:")) {
            return GetDeviceByName.from(device)
        }

        return null
    }
}

private object GetDeviceById {
    fun from(device: String): Device? {
        val id = device.removePrefix("id:")
        return findByDeviceId<Phone>(id)?.device
            ?: findByDeviceId<Tablet>(id)?.device
            ?: findByDeviceId<Wear>(id)?.device
            ?: findByDeviceId<Desktop>(id)?.device
            ?: findByDeviceId<Automotive>(id)?.device
            ?: findByDeviceId<Television>(id)?.device
            ?: findByDeviceId<GenericDevices>(id)?.device
            ?: findByDeviceId<XR>(id)?.device
    }
}

private object GetDeviceByName {
    fun from(device: String): Device? {
        val name = device.removePrefix("name:")
        return findByDeviceName<Phone>(name)?.device
            ?: findByDeviceName<Tablet>(name)?.device
            ?: findByDeviceName<Wear>(name)?.device
            ?: findByDeviceName<Desktop>(name)?.device
            ?: findByDeviceName<Automotive>(name)?.device
            ?: findByDeviceName<Television>(name)?.device
            ?: findByDeviceName<GenericDevices>(name)?.device
            ?: findByDeviceName<XR>(name)?.device
    }
}

private object GetCustomDevice {
    fun from(device: String): Device {
        val spec = device.removePrefix("spec:")
            .splitToSequence(",")
            .map { it.split("=", limit = 2).map { value -> value.trim() } }
            .associateBy({ it[0] }, { it[1] })

        val parent = spec["parent"]
        if (parent != null) {
            return buildDeviceFromParent(
                parent = parent,
                orientation = spec["orientation"],
                navigation = spec["navigation"]
            )
        }

        // This is deprecated... but needed for old configs
        val type = spec["id"]
        val typeValue = when (type) {
            "reference_desktop" -> Type.DESKTOP
            "reference_tablet" -> Type.TABLET
            "reference_phone" -> Type.PHONE
            "reference_foldable" -> Type.FOLDABLE
            else -> null
        }

        val dimensions = dimensions(spec)

        val roundShape = (spec["isRound"]?.toBoolean() ?: spec["shape"]?.equals("Round"))
        val roundShapeValue = when (roundShape) {
            true -> Shape.ROUND
            false -> Shape.NOTROUND
            null -> Shape.NOTROUND
        }

        val dpiValue = spec["dpi"]?.toIntOrNull() ?: 420

        val orientation = spec["orientation"]
        val orientationValue =
            orientation?.let { Orientation.entries.find { it.value == orientation } }
                ?: if (dimensions.height >= dimensions.width) PORTRAIT else LANDSCAPE

        val chinString = spec["chinSize"]
        val chinSize: ChinSize = when {
            chinString == null -> ChinSize(0f, dimensions.unit)
            chinString.endsWith(Unit.PX.value) -> {
                val value = chinString.removeSuffix(Unit.PX.value).toFloatOrNull() ?: 0f
                ChinSize(value, Unit.PX)
            }
            chinString.endsWith(Unit.DP.value) -> {
                val value = chinString.removeSuffix(Unit.DP.value).toFloatOrNull() ?: 0f
                ChinSize(value, Unit.DP)
            }
            // the dimensions in ChinSize must be the same as in the device
            else -> ChinSize(0f, dimensions.unit)
        }

        val navigation = spec["navigation"]
        val navigationValue =
            navigation?.let { Navigation.entries.find { it.value == navigation } } ?: GESTURE

        val cutout = spec["cutout"]
        val cutoutValue = cutout?.let { Cutout.entries.find { it.value == cutout } } ?: NONE

        return Device(
            identifier = null,
            dimensions = dimensions,
            shape = roundShapeValue,
            densityDpi = dpiValue,
            type = typeValue,
            orientation = orientationValue,
            chinSize = chinSize,
            navigation = navigationValue,
            cutout = cutoutValue,
        )
    }

    private fun buildDeviceFromParent(
        parent: String,
        orientation: String?,
        navigation: String?,
    ): Device {
        val parentDevice = requireNotNull(GetDeviceById.from(parent))
        val orientationValue = orientation?.let {
            Orientation.entries.find { it.value == orientation }
        } ?: parentDevice.orientation

        val navigationValue = navigation?.let {
            Navigation.entries.find { it.value == navigation }
        } ?: parentDevice.navigation

        return parentDevice.copy(orientation = orientationValue, navigation = navigationValue)
    }

    private fun dimensions(spec: Map<String, String>): Dimensions {
        val screenHeight = spec["height"]
        val heightValue: Float =
            screenHeight?.removeSuffix("dp")?.removeSuffix("px")?.toFloatOrNull()
                ?: throw IllegalArgumentException("height can never be null")

        val screenWidth = spec["width"]
        val widthValue: Float =
            screenWidth?.removeSuffix("dp")?.removeSuffix("px")?.toFloatOrNull()
                ?: throw IllegalArgumentException("width can never be null")

        val unit = spec["unit"] ?: spec["height"]?.takeLast(2) ?: spec["width"]?.takeLast(2)
        val unitValue: Unit =
            unit?.let { Unit.entries.find { it.value == unit } }
                ?: throw IllegalArgumentException("unit can never be null")

        return Dimensions(
            height = heightValue,
            width = widthValue,
            unit = unitValue
        )
    }
}