package sergio.sastre.composable.preview.scanner.android.device

import sergio.sastre.composable.preview.scanner.android.device.domain.Cutout
import sergio.sastre.composable.preview.scanner.android.device.domain.Cutout.NONE
import sergio.sastre.composable.preview.scanner.android.device.domain.Device
import sergio.sastre.composable.preview.scanner.android.device.domain.Dimensions
import sergio.sastre.composable.preview.scanner.android.device.domain.Navigation.GESTURE
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation.LANDSCAPE
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation.PORTRAIT
import sergio.sastre.composable.preview.scanner.android.device.domain.GetDeviceByIdentifier
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

object ParseDevice {
    fun from(device: String): Device? {
        if (device == "") return DEFAULT

        if (device.startsWith("spec:")) {
            return GetCustomDevice.from(device)
        }

        if (device.startsWith("id:")) {
            return GetPredefinedDeviceById.from(device)
        }

        if (device.startsWith("name:")) {
            return GetPredefinedDeviceByName.from(device)
        }

        return null
    }
}

private object GetPredefinedDeviceById {
    fun from(device: String): Device? {
        val id = device.removePrefix("id:")
        return GetDeviceByIdentifier.findByDeviceId<Phone>(id)?.device
            ?: GetDeviceByIdentifier.findByDeviceId<Tablet>(id)?.device
            ?: GetDeviceByIdentifier.findByDeviceId<Wear>(id)?.device
            ?: GetDeviceByIdentifier.findByDeviceId<Desktop>(id)?.device
            ?: GetDeviceByIdentifier.findByDeviceId<Automotive>(id)?.device
            ?: GetDeviceByIdentifier.findByDeviceId<Television>(id)?.device
            ?: GetDeviceByIdentifier.findByDeviceId<GenericDevices>(id)?.device
    }
}

private object GetPredefinedDeviceByName {
    fun from(device: String): Device? {
        val name = device.removePrefix("name:")
        return GetDeviceByIdentifier.findByDeviceName<Phone>(name)?.device
            ?: GetDeviceByIdentifier.findByDeviceName<Tablet>(name)?.device
            ?: GetDeviceByIdentifier.findByDeviceName<Wear>(name)?.device
            ?: GetDeviceByIdentifier.findByDeviceName<Desktop>(name)?.device
            ?: GetDeviceByIdentifier.findByDeviceName<Automotive>(name)?.device
            ?: GetDeviceByIdentifier.findByDeviceName<Television>(name)?.device
            ?: GetDeviceByIdentifier.findByDeviceName<GenericDevices>(name)?.device
    }
}

private object GetCustomDevice {
    fun from(device: String): Device {
        val spec = device.removePrefix("spec:")
            .splitToSequence(",")
            .map { it.trim() }
            .map { it.split("=", limit = 2) }
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
        val shapeValue = when (roundShape) {
            true -> Shape.ROUND
            false -> Shape.NORMAL
            null -> Shape.NORMAL
        }

        val dpiValue = spec["dpi"]?.toIntOrNull() ?: 420

        val orientation = spec["orientation"]
        val orientationValue =
            orientation?.let { Orientation.entries.find { it.value == orientation } }
                ?: if (dimensions.height >= dimensions.width) PORTRAIT else LANDSCAPE

        val chinSize = spec["chinSize"]?.removeSuffix("dp")?.toIntOrNull() ?: 0

        val navigation = spec["navigation"]
        val navigationValue =
            navigation?.let { Navigation.entries.find { it.value == navigation } } ?: GESTURE

        val cutout = spec["cutout"]
        val cutoutValue = cutout?.let { Cutout.entries.find { it.value == cutout } } ?: NONE

        return Device(
            dimensions = dimensions,
            shape = shapeValue,
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
        val parentDevice = requireNotNull(GetPredefinedDeviceById.from(parent))
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