package taylor.com.floatwindow

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator

class GravityActivity : AppCompatActivity() {

    private var windowInfo: FloatWindow.WindowInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gravity_activity)

        findViewById<Button>(R.id.btnGravity).setOnClickListener {
            showGravityWindow()
        }
    }

    private fun showGravityWindow() {
        val view = LayoutInflater.from(this).inflate(R.layout.gravity_window, null)
        if (windowInfo == null) {
            windowInfo = FloatWindow.WindowInfo(view)
            windowInfo!!.width = DimensionUtil.getScreenWidth(this)
            windowInfo!!.height = DimensionUtil.dp2px(80.0)
        }

        FloatWindow.show(this, "gravity", windowInfo, FloatWindow.GRAVITY_TOP) { windowInfo ->
            windowInfo?.layoutParams?.y?.let {
                ValueAnimator.ofInt(it, 0).apply {
                    interpolator = LinearOutSlowInInterpolator()
                    duration = 250L
                    addUpdateListener { animation ->
                        FloatWindow.updateWindowView(y = animation.animatedValue as Int)
                    }
                    start()
                }
            }
        }
    }

}