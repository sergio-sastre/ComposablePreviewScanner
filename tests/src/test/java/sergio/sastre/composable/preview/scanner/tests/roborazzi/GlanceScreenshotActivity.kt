package sergio.sastre.composable.preview.scanner.tests.roborazzi

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import kotlinx.coroutines.runBlocking

// TODO check example here
// https://github.com/google/glance-experimental-tools/blob/main/sample/src/test/java/com/google/android/glance/tools/testing/SampleGlanceScreenshotTest.kt
@RequiresApi(Build.VERSION_CODES.O)
class GlanceScreenshotTestActivity : Activity() {
    private var state: Any? = null
    private var size: DpSize = DpSize(Dp.Unspecified, Dp.Unspecified)
    private var wrapContentSize: Boolean = false
    private lateinit var containerView: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FrameLayout(this), FrameLayout.LayoutParams(100, 100))
    }

    /**
     * Sets the appwidget state that can be accessed via LocalState composition local.
     */
    public fun <T> setState(state: T) {
        this.state = state
    }

    /**
     * Sets the size of rendering area to wrap size of the composable under test instead of using
     * the same size as one provided in [setAppWidgetSize]. This is useful when you are testing a
     * small part of the appwidget independently.
     *
     * Note: Calling [wrapContentSize] doesn't impact "LocalSize" compositionLocal. Use
     * [setAppWidgetSize] to set the value that should be used for the compositionLocal.
     */
    public fun wrapContentSize() {
        this.wrapContentSize = true
    }

    /**
     * Renders the given glance composable in the activity.
     *
     * Provide appwidget size before calling this.
     */
    @OptIn(ExperimentalGlanceRemoteViewsApi::class)
    fun renderComposable(
        size: DpSize,
        composable: @Composable () -> Unit
    ): View {
        this.size = size
        return runBlocking {
            val remoteViews = GlanceRemoteViews().compose(
                context = applicationContext,
                size = size,
                state = state,
                content = composable
            ).remoteViews

            val activityFrame = findViewById<FrameLayout>(android.R.id.content)
            containerView = FrameLayout(applicationContext)
            containerView.setBackgroundColor(Color.TRANSPARENT)
            activityFrame.addView(containerView)

            val view = remoteViews.apply(applicationContext, containerView)
            containerView.addView(view)

            adjustContainerViewSize()
        }
    }

    private fun adjustContainerViewSize(): View {
        val displayMetrics = resources.displayMetrics

        if (wrapContentSize) {
            containerView.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        } else {
            val width = size.width.toPixels(displayMetrics)
            val height = size.height.toPixels(displayMetrics)

            containerView.layoutParams = FrameLayout.LayoutParams(width, height, Gravity.CENTER)
        }

        containerView.requestLayout()
        return containerView
    }

    private fun Dp.toPixels(displayMetrics: DisplayMetrics) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, displayMetrics).toInt()
}