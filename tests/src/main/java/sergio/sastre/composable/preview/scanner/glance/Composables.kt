package sergio.sastre.composable.preview.scanner.glance

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.background
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.Text

@Composable
fun Example () {
    Row(modifier = GlanceModifier.fillMaxWidth()
        .padding(16.dp)
        .background(Color.Red)
    ) {
        val modifier = GlanceModifier.defaultWeight()
        Text("first", modifier)
        Text("second", modifier)
        Text("third", modifier)
    }
}

// TODO - Previews with none or only one of both
@OptIn(ExperimentalGlancePreviewApi::class)
@Composable
@Preview
@Preview(widthDp = 130) // looks similar to preview
@Preview(heightDp = 150)
@Preview(widthDp = 200)
@Preview(widthDp = 50, heightDp = 50)
@Preview(widthDp = 200, heightDp = 150)
@Preview(widthDp = 2000, heightDp = 1500)
fun examplePreview() {
    Example()
}