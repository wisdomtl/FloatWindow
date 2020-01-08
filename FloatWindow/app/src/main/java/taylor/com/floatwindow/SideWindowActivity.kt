package taylor.com.floatwindow

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class SideWindowActivity : AppCompatActivity() {

    private var intimacyWindowInfo: FloatWindow.WindowInfo? = null
    private var intimacyTranslationX: Int? = null
    private var intimacyAnimator: ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.second_activity)

        findViewById<Button>(R.id.btnGravity).setOnClickListener {
            Intent(this@SideWindowActivity, GravityActivity::class.java).let {
                startActivity(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showSideWindow()
    }

    private fun showSideWindow() {
        val view = LayoutInflater.from(this).inflate(R.layout.window_intimacy, null)
        if (intimacyWindowInfo == null) {
            intimacyWindowInfo = FloatWindow.WindowInfo(view)
            intimacyWindowInfo!!.width = DimensionUtil.dp2px(180.0)
            intimacyWindowInfo!!.height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        val x: Int = DimensionUtil.getScreenWidth(this) - DimensionUtil.dp2px(100.0)
        val y: Int = DimensionUtil.dp2px(75.0)
        FloatWindow.show(this, "intimacy", intimacyWindowInfo, x, y, true )
        view.post {
            val lin: LinearLayout = view.findViewById(R.id.llContainer)
                ?: return@post
            val location = IntArray(2)
            lin.getLocationOnScreen(location)
            intimacyTranslationX =
                DimensionUtil.dp2px(80.0) - (DimensionUtil.getScreenWidth(this@SideWindowActivity) - location[0])
        }
        FloatWindow.setClickListener(object : FloatWindow.WindowClickListener {
            override fun onWindowClick(view: View, windowInfo: FloatWindow.WindowInfo?): Boolean {
                when (view.id) {
                    R.id.vIntimacy -> onIntimacyClick(windowInfo, x)
                    else -> {
                    }
                }
                return true
            }
        })
    }

    private fun onIntimacyClick(windowInfo: FloatWindow.WindowInfo?, initX: Int): Boolean? {
        if (windowInfo?.layoutParams == null) {
            return false
        }
        var end = 0
        end = if (windowInfo.layoutParams!!.x === initX) {
            FloatWindow.setDimAmount(0.3f)
            windowInfo.layoutParams!!.x - (intimacyTranslationX ?: 0)
        } else {
            FloatWindow.setDimAmount(0.0f)
            windowInfo.layoutParams!!.x + (intimacyTranslationX ?: 0)
        }
        val start: Int = windowInfo.layoutParams!!.x
        animateIntimacy(windowInfo, start, end)
        return true
    }

    private fun animateIntimacy(windowInfo: FloatWindow.WindowInfo, start: Int, end: Int) {
        if (intimacyAnimator != null && intimacyAnimator!!.isRunning) {
            return
        }
        if (intimacyAnimator == null) {
            intimacyAnimator = ValueAnimator.ofInt(start, end)
            intimacyAnimator!!.addListener(intimacyAnimListener)
            intimacyAnimator!!.interpolator = AccelerateDecelerateInterpolator()
            intimacyAnimator!!.duration = 200
            intimacyAnimator!!.addUpdateListener { animation: ValueAnimator ->
                val x1 = animation.animatedValue as Int
                FloatWindow.updateWindowView(windowInfo = windowInfo, x = x1)
            }
        } else {
            intimacyAnimator!!.setIntValues(start, end)
        }
        intimacyAnimator!!.start()
    }

    private val intimacyAnimListener: Animator.AnimatorListener =
        object : Animator.AnimatorListener {
            private var lin: LinearLayout? = null
            private var startVisibility = View.INVISIBLE
            override fun onAnimationStart(animation: Animator) {
                showMilestone()
            }

            override fun onAnimationEnd(animation: Animator) {
                hideMilestone()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
            private fun showMilestone() {
                val windowInfo1: FloatWindow.WindowInfo = FloatWindow.windowInfo ?: return
                val intimacyView: View = windowInfo1.view ?: return
                lin = intimacyView.findViewById(R.id.llContainer)
                if (lin == null) {
                    return
                }
                startVisibility = lin!!.visibility
                if (startVisibility != View.VISIBLE) {
                    lin!!.visibility = View.VISIBLE
                }
            }

            private fun hideMilestone() {
                if (lin == null) {
                    return
                }
                if (startVisibility == View.VISIBLE) {
                    lin!!.visibility = View.GONE
                }
            }
        }
}