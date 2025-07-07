package sergio.sastre.composable.preview.scanner.glance

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.provideContent
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.Text

@Composable
fun hello () {
    Row(modifier = GlanceModifier.fillMaxWidth().padding(16.dp)) {
        val modifier = GlanceModifier.defaultWeight()
        Text("first", modifier)
        Text("second", modifier)
        Text("third", modifier)
    }
}

// TODO - Previews with none or only one of both
@OptIn(ExperimentalGlancePreviewApi::class)
@Composable
@Preview(widthDp = 200, heightDp = 150)
fun helloPreview() {
    hello()
}