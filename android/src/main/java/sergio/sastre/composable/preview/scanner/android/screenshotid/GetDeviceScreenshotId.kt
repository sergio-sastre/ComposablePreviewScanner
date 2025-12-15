package sergio.sastre.composable.preview.scanner.android.screenshotid

import sergio.sastre.composable.preview.scanner.android.device.DevicePreviewInfoParser
import sergio.sastre.composable.preview.scanner.android.device.domain.Device

private const val DEFAULT = ""
object GetDeviceScreenshotId {

    fun getDeviceScreenshotId(device: String): String? =
        when {
            device == DEFAULT -> null
            device.contains("parent") -> device.screenshotIdFromParent()
            // id:device_id or name:deviceName
            else -> {
                val parsedDevice = DevicePreviewInfoParser.parse(device)
                device.screenshotIdFromId(parsedDevice)?.trim('_') ?:
                device.screenshotIdFromName(parsedDevice)?.trim('_') ?:
                device.screenshotIdFromSpec().trim('_')
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
        device?.identifier?.id?.replaceSpecialChars()?.plus("_${this.removeDeviceKeyValues()}")

    private fun String.screenshotIdFromName(device: Device?): String? =
        device?.identifier?.name?.replaceSpecialChars()?.plus("_${this.removeDeviceKeyValues()}")

    private fun String.screenshotIdFromParent(): String {
        val parentDevice = DevicePreviewInfoParser.parse(this)?.identifier?.id
            ?: throw IllegalStateException("Device id is null for the given 'parent'")
        val pattern = Regex("""\s*parent\s*=\s*[^,]*""")
        val parentDeviceReplaced = this.replace(pattern, "PARENT_$parentDevice").trim()
        // might contain some spaces e.g. for "Nexus 7"
        return parentDeviceReplaced.screenshotIdFromSpec().replace(" ", "_")
    }

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
            .joinToString("_")
            .uppercase()
            // Replace float values like 1.55 with 1_55f
            .replace(Regex("(\\d+)\\.(\\d+)"), "$1_$2f")
}