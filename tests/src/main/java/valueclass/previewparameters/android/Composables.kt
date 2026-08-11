package valueclass.previewparameters.android

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Repro fixture (in its own top-level package so the broad invoke suites don't pick it up): a
 * @PreviewParameter whose provider yields a Kotlin value class (Dp). The preview method's signature
 * therefore contains a value-class parameter, which trips kotlin-reflect's ValueClassAwareCaller when
 * the scanner resolves `Method.kotlinFunction`.
 */
class DpValueClassProvider : PreviewParameterProvider<Dp> {
    override val values: Sequence<Dp> = sequenceOf(32.dp, 64.dp)
}

@Preview
@Composable
fun ValueClassPreviewParameterPreview(
    @PreviewParameter(provider = DpValueClassProvider::class) size: Dp,
) {
    Text(size.toString())
}
