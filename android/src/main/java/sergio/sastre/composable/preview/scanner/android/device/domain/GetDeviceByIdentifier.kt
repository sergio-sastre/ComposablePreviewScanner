package sergio.sastre.composable.preview.scanner.android.device.domain

interface GetDeviceByIdentifier<T : Enum<T>> {
    val device: Device

    companion object {
        inline fun <reified T> findByDeviceId(deviceId: String): T? where T : Enum<T>, T : GetDeviceByIdentifier<T> {
            return enumValues<T>().find { it.device.id?.id == deviceId }
        }

        inline fun <reified T> findByDeviceName(deviceName: String): T? where T : Enum<T>, T : GetDeviceByIdentifier<T> {
            return enumValues<T>().find { it.device.id?.name == deviceName }
        }
    }
}