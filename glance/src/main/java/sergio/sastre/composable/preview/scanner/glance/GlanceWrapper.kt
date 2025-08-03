package sergio.sastre.composable.preview.scanner.glance

import android.graphics.Color
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import kotlinx.coroutines.runBlocking

class GlanceWrapper(
    val rootView: ViewGroup
) {
    private var state: Any? = null
    private var size: DpSize = DpSize(Dp.Unspecified, Dp.Unspecified)
    private lateinit var containerView: FrameLayout

    fun setSizeDp(
        widthDp: Int,
        heightDp: Int
    ) = apply {
        val width = when (widthDp > 0) {
            true -> widthDp.dp
            false -> Dp.Unspecified
        }
        val height = when (heightDp > 0) {
            true -> heightDp.dp
            false -> Dp.Unspecified
        }
        this.size = DpSize(width = width, height = height)
    }

    fun <T> setState(state: T) = apply {
        this.state = state
    }

    @OptIn(ExperimentalGlanceRemoteViewsApi::class)
    fun renderComposable(
        composable: @Composable () -> Unit
    ): View {
        return runBlocking {
            val remoteViews = GlanceRemoteViews().compose(
                context = rootView.context,
                size = size,
                state = state,
                content = composable
            ).remoteViews

            containerView = FrameLayout(rootView.context)
            containerView.setBackgroundColor(Color.WHITE)
            rootView.addView(containerView)

            val view = remoteViews.apply(rootView.context, containerView)
            containerView.addView(view)

            adjustContainerViewSize()
        }
    }

    private fun adjustContainerViewSize(): View {
        val displayMetrics = rootView.context.resources.displayMetrics

        val width = when (size.width == Dp.Unspecified) {
            true -> FrameLayout.LayoutParams.WRAP_CONTENT
            false -> size.width.toPixels(displayMetrics)
        }
        val height = when (size.height == Dp.Unspecified) {
            true -> FrameLayout.LayoutParams.WRAP_CONTENT
            false -> size.height.toPixels(displayMetrics)
        }

        containerView.layoutParams = FrameLayout.LayoutParams(width, height, Gravity.CENTER)
        containerView.requestLayout()

        return containerView
    }

    private fun Dp.toPixels(displayMetrics: DisplayMetrics) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, displayMetrics).toInt()
}