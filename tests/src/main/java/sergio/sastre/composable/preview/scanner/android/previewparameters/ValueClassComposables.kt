package sergio.sastre.composable.preview.scanner.android.previewparameters

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * @PreviewParameter whose provider yields a Kotlin value class (Dp). The preview method's signature
 * therefore contains a value-class parameter, which trips kotlin-reflect's ValueClassAwareCaller when
 * the scanner resolves `Method.kotlinFunction`.
 */
class DpValueClassProvider : PreviewParameterProvider<Dp> {
    override val values: Sequence<Dp> = sequenceOf(32.dp, 64.dp)
}

@Preview(name = "valueClassDp")
@Composable
fun ValueClassPreviewParameterPreview(
    @PreviewParameter(provider = DpValueClassProvider::class) size: Dp,
) {
    Text(size.toString())
}

class NullableDpValueClassProvider : PreviewParameterProvider<Dp?> {
    override val values: Sequence<Dp?> = sequenceOf(null)
}

@Preview(name = "valueClassDp")
@Composable
fun NullableValueClassPreviewParameterPreview(
    @PreviewParameter(provider = NullableDpValueClassProvider::class) size: Dp?,
) {
    Text(size.toString())
}

@JvmInline
value class DpExtended(val value: Float)

class DpExtendedValueClassProvider : PreviewParameterProvider<DpExtended> {
    override val values: Sequence<DpExtended> =
        sequenceOf(DpExtended(32F), DpExtended(64F))
}

@Preview(name = "valueClassDpExtended")
@Composable
fun DpExtendedValueClassPreviewParameterPreview(
    @PreviewParameter(provider = DpExtendedValueClassProvider::class) size: DpExtended,
) {
    Text(size.toString())
}
