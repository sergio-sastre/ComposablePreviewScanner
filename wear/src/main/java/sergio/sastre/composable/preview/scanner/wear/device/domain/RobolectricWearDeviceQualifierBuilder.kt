package sergio.sastre.composable.preview.scanner.wear.device.domain

import sergio.sastre.composable.preview.scanner.android.device.domain.RobolectricDeviceQualifierBuilder
import sergio.sastre.composable.preview.scanner.android.device.types.Wear

object RobolectricWearDeviceQualifierBuilder {

    fun build(previewDevice: String): String? =
        RobolectricDeviceQualifierBuilder.build(
            previewDevice,
            Wear.WEAR_OS_SMALL_ROUND.device
        )
}