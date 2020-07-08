package taylor.com

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import taylor.com.floatwindow.DimensionUtil
import taylor.com.floatwindow.FloatWindow

/**
 * base activity for the sake of dark mode
 */
open class BaseActivity : AppCompatActivity() {

    private val preference by lazy { Preference(getSharedPreferences("dark-mode", Context.MODE_PRIVATE)) }

    companion object {
        val maskHandler by lazy { Handler(Looper.getMainLooper()) }
    }

    /**
     * show or remove dark mask
     */
    internal fun toggleDarkMode(show: Boolean) {
        val id = "darkMask"
        if (show) {
            maskHandler.postAtFrontOfQueue {
                val maskView = View {
                    layout_id = id
                    layout_width = match_parent
                    layout_height = match_parent
                    background_color = "#c8000000"
                }
                decorView?.apply {
                    val view = findViewById<View>(id.toLayoutId())
                    if (view == null) {
                        addView(maskView)
                    }
                }
            }
        } else {
            decorView?.apply {
                find<View>(id)?.let { removeView(it) }
            }
        }
    }

    // this way is not so good to implement dark mode
    private fun showMaskWindow() {
        val view = View {
            layout_width = match_parent
            layout_height = match_parent
            background_color = "#c8000000"
        }
        val windowInfo = FloatWindow.WindowInfo(view).apply {
            width = DimensionUtil.getScreenWidth(this@BaseActivity)
            height = DimensionUtil.getScreenHeight(this@BaseActivity)
        }
        FloatWindow.show(this, "mask", windowInfo, 0, 100, false, false, true)
    }

    override fun onStart() {
        toggleDarkMode(preference["dark-mode", false])
        super.onStart()
    }
}

fun Activity.contentView(): FrameLayout? =
    (takeIf { !isFinishing && !isDestroyed }?.window?.decorView) as FrameLayout

val Activity.decorView: FrameLayout?
    get() = (takeIf { !isFinishing && !isDestroyed }?.window?.decorView) as? FrameLayout