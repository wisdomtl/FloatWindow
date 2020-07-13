package taylor.com

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import taylor.com.floatwindow.DimensionUtil
import taylor.com.floatwindow.FloatWindow
import taylor.com.util.*

/**
 * base activity for the sake of dark mode
 */
open class BaseActivity : AppCompatActivity() {

    private val preference by lazy { Preference(getSharedPreferences("dark-mode", Context.MODE_PRIVATE)) }

    companion object {
        val maskHandler by lazy { Handler(Looper.getMainLooper()) }
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
        nightMode(preference["dark-mode", false])
        super.onStart()
    }
}


