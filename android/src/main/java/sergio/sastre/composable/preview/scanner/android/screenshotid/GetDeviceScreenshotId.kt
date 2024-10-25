package sergio.sastre.composable.preview.scanner.android.screenshotid

import androidx.compose.ui.tooling.preview.Devices
import sergio.sastre.composable.preview.scanner.android.device.ParseDevice
import sergio.sastre.composable.preview.scanner.android.device.domain.Device

object GetDeviceScreenshotId {

    fun getDeviceScreenshotId(device: String): String? =
        when (device) {
            Devices.DEFAULT -> null
            else -> {
                val parsedDevice = ParseDevice.from(device)
                device.screenshotIdFromId(parsedDevice) ?:
                device.screenshotIdFromName(parsedDevice) ?:
                device.screenshotIdFromSpec()
            }
        }

    private fun String.replaceSpecialChars(): String {
        return this
            .replace(Regex("[ ()=.]+"), "_")
            .replace(",", "")
            .uppercase()
    }

    /**
     * remove key-values for "id:value", "name:value" "parent=value" and remaining "spec:value"
     */
    private fun String.removeDeviceKeyValues(): String {
        // Define a regex pattern to match unwanted prefixes and key-value pairs
        val regex = Regex("(id:[^,]*|name:[^,]*|spec:|parent=[^,]*)(,)?")
        return this
            .replace(regex, "")
            .trim()
            .replaceSpecialChars()
    }

    private fun String.screenshotIdFromId(device: Device?): String? =
        device?.id?.id?.replaceSpecialChars()?.plus("_${this.removeDeviceKeyValues()}")

    private fun String.screenshotIdFromName(device: Device?): String? =
        device?.id?.name?.replaceSpecialChars()?.plus("_${this.removeDeviceKeyValues()}")

    private fun String.screenshotIdFromSpec(): String =
        this.removePrefix("spec:")
            .splitToSequence(",")
            .map {
                // Split into key and values, trim and reformat
                it.split("=", limit = 2).joinToString("=") { value -> value.trim() }
            }
            .map {
                // Remove unwanted prefixes and characters, replace '=' and ',' with '_'
                it.replace(Regex("id=reference_|name:|parent=|id:|:id|:"), "")
                    .replace("=", "_")
                    .trim()
            }
            .filter { it.isNotEmpty() }
            .joinToString("_")
            .uppercase()
}