package sergio.sastre.composable.preview.scanner.glance.configuration

import android.content.Context
import android.graphics.Color
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import kotlinx.coroutines.runBlocking

/**
 * Helps configure a Glance Composable for screenshot testing.
 *
 * Glance has its own api, for instance, it uses GlanceModifiers instead of Modifiers, which
 * are not available in the standard Jetpack Compose API.
 * There snapshotting Glance Composables directly might lead to problems.
 *
 * Therefore, the best way to snapshotting Glance Composables is to convert them into Views.
 * This class aims at facilitating that.
 *
 * @param context: When possible (e.g. in Roborazzi or Instrumentation tests), an activity.
 * If not (e.g. Paparazzi), the View might not render exactly like in the Preview.
 *
 * WARNING: Make sure to define "targetSdk" in your gradle file. If missing, the View might not render
 * exactly like in the Preview.
 */
class GlanceSnapshotConfigurator(
    val context: Context
) {
    private var state: Any? = null
    private var size: DpSize = DpSize(Dp.Companion.Unspecified, Dp.Companion.Unspecified)
    private val rootView = FrameLayout(context)
    private val containerView = FrameLayout(rootView.context)

    private lateinit var glanceRemoteViews: RemoteViews

    fun setSizeDp(
        widthDp: Int,
        heightDp: Int
    ) = apply {
        val width = when (widthDp > 0) {
            true -> widthDp.dp
            false -> Dp.Companion.Unspecified
        }
        val height = when (heightDp > 0) {
            true -> heightDp.dp
            false -> Dp.Companion.Unspecified
        }
        this.size = DpSize(width = width, height = height)
    }

    fun <T> setState(state: T) = apply {
        this.state = state
    }

    @OptIn(ExperimentalGlanceRemoteViewsApi::class)
    fun composableToView(
        composable: @Composable () -> Unit
    ): View {
        return runBlocking {
            val remoteViews =
                GlanceRemoteViews().compose(
                    context = rootView.context,
                    size = size,
                    state = state,
                    content = composable
                ).remoteViews

            glanceRemoteViews = remoteViews

            containerView.setBackgroundColor(Color.WHITE)
            rootView.addView(containerView)

            val view = remoteViews.apply(rootView.context, containerView)
            containerView.addView(view)

            adjustContainerViewSize()
            rootView
        }
    }

    private fun adjustContainerViewSize(): View {
        val displayMetrics = rootView.context.resources.displayMetrics

        val width = when (size.width == Dp.Companion.Unspecified) {
            true -> FrameLayout.LayoutParams.WRAP_CONTENT
            false -> size.width.toPixels(displayMetrics)
        }
        val height = when (size.height == Dp.Companion.Unspecified) {
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