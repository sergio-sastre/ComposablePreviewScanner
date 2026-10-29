package sergio.sastre.composable.preview.scanner

import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity

class ScreenshotTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
    }
}
