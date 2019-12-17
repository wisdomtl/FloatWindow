package taylor.com.floatwindow

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import taylor.com.floatwindow.FloatWindow.WindowClickListener

class MainActivity : AppCompatActivity() {

    private var progressRing: ProgressRing? = null
    private val FULL_TIME_MILLISECOND = 6 * 1000.toFloat()
    private var timer: Timer? = null
    private val d1 = 400
    private val d2 = 400
    val VALUE_ANIM_DURATION = 800
    val TAG_WINDOW_A = "A"
    val BOMB_ANIM_DURATION_IN_MILLISECOND = 6 * 100
    private var animationDrawable: AnimationDrawable? = null
    private var windowInfo: FloatWindow.WindowInfo? = null
    private var intimacyWindowInfo: FloatWindow.WindowInfo? = null
    private var intimacyTranslationX: Int? = null
    private var intimacyAnimator:ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        showDragableWindow()

//        showSideWindow()
    }

    private fun showDragableWindow() {
        if (windowInfo == null) {
            windowInfo = FloatWindow.WindowInfo(generateWindowView())
            windowInfo!!.width = DimensionUtil.dp2px(54.0)
            windowInfo!!.height = DimensionUtil.dp2px(54.0)
            FloatWindow.show(this, TAG_WINDOW_A, windowInfo, 0, 0, true)
        }
        FloatWindow.show(this, TAG_WINDOW_A, dragEnable = true)
        FloatWindow.setOutsideTouchable(true, {
            Log.v("ttaylor", "tag=touch outside, WindowActivity.onResume()  ")
        })
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
        FloatWindow.show(this, "intimacy", intimacyWindowInfo, x, y, true)
        view.post {
            val lin: LinearLayout = view.findViewById(R.id.llContainer)
                ?: return@post
            val location = IntArray(2)
            lin.getLocationOnScreen(location)
            intimacyTranslationX =
                DimensionUtil.dp2px(80.0) - (DimensionUtil.getScreenWidth(this@MainActivity) - location[0])
        }
        FloatWindow.setClickListener(R.id.vIntimacy, object : WindowClickListener {
            override fun onWindowClick(windowInfo: FloatWindow.WindowInfo?): Boolean {
                windowInfo?.let { onIntimacyClick(it,x) }
                return true
            }
        })
    }

    private fun onIntimacyClick(windowInfo: FloatWindow.WindowInfo, initX: Int): Boolean? {
        if (windowInfo.layoutParams == null) {
            return false
        }
        var end = 0
        end = if (windowInfo.layoutParams!!.x === initX) {
            windowInfo.layoutParams!!.x - (intimacyTranslationX ?: 0)
        } else {
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
            intimacyAnimator!!.setInterpolator(AccelerateDecelerateInterpolator())
            intimacyAnimator!!.setDuration(200)
            intimacyAnimator!!.addUpdateListener(AnimatorUpdateListener { animation: ValueAnimator ->
                val x1 = animation.animatedValue as Int
                if (windowInfo.layoutParams != null) {
                    windowInfo.layoutParams!!.x = x1
                }
                val windowManager =
                    getSystemService(Context.WINDOW_SERVICE) as WindowManager
                if (windowInfo.hasParent()) {
                    windowManager.updateViewLayout(
                        windowInfo.view,
                        windowInfo.layoutParams
                    )
                }
            })
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

    private fun generateWindowView(): View {
        progressRing = ProgressRing(this)
        animationDrawable = createAnimationDrawable(this)
        progressRing!!.setImageDrawable(animationDrawable)
        timer = Timer(Timer.TimerListener { pastMillisecond ->
            val mod = pastMillisecond % FULL_TIME_MILLISECOND
            val progress = getProgress(mod, FULL_TIME_MILLISECOND)
            progressRing!!.setProgress(progress)
            if (mod == 0f) {
                doFrameAnimation(animationDrawable)
                doValueAnimator(10f, 62f, progressRing!!, VALUE_ANIM_DURATION)
            }
        })
        timer!!.start(0, 100)
        return progressRing!!
    }

    fun decodeSampledBitmapFromResource(
        res: Resources?,
        resId: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(res, resId, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.RGB_565
        return BitmapFactory.decodeResource(res, resId, options)
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int { // 源图片的高度和宽度
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
// height and width larger than the requested height and width.
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun createAnimationDrawable(context: Context): AnimationDrawable {
        val drawable = AnimationDrawable()
        val frameDuration = BOMB_ANIM_DURATION_IN_MILLISECOND / 21
        drawable.addFrame(
            BitmapDrawable(
                decodeSampledBitmapFromResource(
                    context.resources,
                    R.drawable.watch_reward_1,
                    DimensionUtil.dp2px(54.0),
                    DimensionUtil.dp2px(54.0)
                )
            ), frameDuration
        )
        drawable.addFrame(
            BitmapDrawable(
                decodeSampledBitmapFromResource(
                    context.resources,
                    R.drawable.watch_reward_2,
                    DimensionUtil.dp2px(54.0),
                    DimensionUtil.dp2px(54.0)
                )
            ), frameDuration
        )
        drawable.addFrame(
            BitmapDrawable(
                decodeSampledBitmapFromResource(
                    context.resources,
                    R.drawable.watch_reward_3,
                    DimensionUtil.dp2px(54.0),
                    DimensionUtil.dp2px(54.0)
                )
            ), frameDuration
        )
        drawable.addFrame(
            BitmapDrawable(
                decodeSampledBitmapFromResource(
                    context.resources,
                    R.drawable.watch_reward_4,
                    DimensionUtil.dp2px(54.0),
                    DimensionUtil.dp2px(54.0)
                )
            ), frameDuration
        )
        drawable.addFrame(
            BitmapDrawable(
                decodeSampledBitmapFromResource(
                    context.resources,
                    R.drawable.watch_reward_5,
                    DimensionUtil.dp2px(54.0),
                    DimensionUtil.dp2px(54.0)
                )
            ), frameDuration
        )
        drawable.addFrame(
            BitmapDrawable(
                decodeSampledBitmapFromResource(
                    context.resources,
                    R.drawable.watch_reward_6,
                    DimensionUtil.dp2px(54.0),
                    DimensionUtil.dp2px(54.0)
                )
            ), frameDuration
        )
        drawable.addFrame(
            BitmapDrawable(
                decodeSampledBitmapFromResource(
                    context.resources,
                    R.drawable.watch_reward_7,
                    DimensionUtil.dp2px(54.0),
                    DimensionUtil.dp2px(54.0)
                )
            ), frameDuration
        )
        drawable.addFrame(
            BitmapDrawable(
                decodeSampledBitmapFromResource(
                    context.resources,
                    R.drawable.watch_reward_8,
                    DimensionUtil.dp2px(54.0),
                    DimensionUtil.dp2px(54.0)
                )
            ), frameDuration
        )
        drawable.addFrame(
            BitmapDrawable(
                decodeSampledBitmapFromResource(
                    context.resources,
                    R.drawable.watch_reward_9,
                    DimensionUtil.dp2px(54.0),
                    DimensionUtil.dp2px(54.0)
                )
            ), frameDuration
        )
        drawable.addFrame(
            BitmapDrawable(
                decodeSampledBitmapFromResource(
                    context.resources,
                    R.drawable.watch_reward_10,
                    DimensionUtil.dp2px(54.0),
                    DimensionUtil.dp2px(54.0)
                )
            ), frameDuration
        )
        drawable.addFrame(
            BitmapDrawable(
                decodeSampledBitmapFromResource(
                    context.resources,
                    R.drawable.watch_reward_11,
                    DimensionUtil.dp2px(54.0),
                    DimensionUtil.dp2px(54.0)
                )
            ), frameDuration
        )
        drawable.addFrame(
            BitmapDrawable(
                decodeSampledBitmapFromResource(
                    context.resources,
                    R.drawable.watch_reward_12,
                    DimensionUtil.dp2px(54.0),
                    DimensionUtil.dp2px(54.0)
                )
            ), frameDuration
        )
        drawable.addFrame(
            BitmapDrawable(
                decodeSampledBitmapFromResource(
                    context.resources,
                    R.drawable.watch_reward_13,
                    DimensionUtil.dp2px(54.0),
                    DimensionUtil.dp2px(54.0)
                )
            ), frameDuration
        )
        drawable.addFrame(
            BitmapDrawable(
                decodeSampledBitmapFromResource(
                    context.resources,
                    R.drawable.watch_reward_14,
                    DimensionUtil.dp2px(54.0),
                    DimensionUtil.dp2px(54.0)
                )
            ), frameDuration
        )
        drawable.addFrame(
            BitmapDrawable(
                decodeSampledBitmapFromResource(
                    context.resources,
                    R.drawable.watch_reward_15,
                    DimensionUtil.dp2px(54.0),
                    DimensionUtil.dp2px(54.0)
                )
            ), frameDuration
        )
        drawable.addFrame(
            BitmapDrawable(
                decodeSampledBitmapFromResource(
                    context.resources,
                    R.drawable.watch_reward_16,
                    DimensionUtil.dp2px(54.0),
                    DimensionUtil.dp2px(54.0)
                )
            ), frameDuration
        )
        drawable.addFrame(
            BitmapDrawable(
                decodeSampledBitmapFromResource(
                    context.resources,
                    R.drawable.watch_reward_17,
                    DimensionUtil.dp2px(54.0),
                    DimensionUtil.dp2px(54.0)
                )
            ), frameDuration
        )
        drawable.addFrame(
            BitmapDrawable(
                decodeSampledBitmapFromResource(
                    context.resources,
                    R.drawable.watch_reward_18,
                    DimensionUtil.dp2px(54.0),
                    DimensionUtil.dp2px(54.0)
                )
            ), frameDuration
        )
        drawable.addFrame(
            BitmapDrawable(
                decodeSampledBitmapFromResource(
                    context.resources,
                    R.drawable.watch_reward_19,
                    DimensionUtil.dp2px(54.0),
                    DimensionUtil.dp2px(54.0)
                )
            ), frameDuration
        )
        drawable.addFrame(
            BitmapDrawable(
                decodeSampledBitmapFromResource(
                    context.resources,
                    R.drawable.watch_reward_20,
                    DimensionUtil.dp2px(54.0),
                    DimensionUtil.dp2px(54.0)
                )
            ), frameDuration
        )
        drawable.addFrame(
            BitmapDrawable(
                decodeSampledBitmapFromResource(
                    context.resources,
                    R.drawable.watch_reward_21,
                    DimensionUtil.dp2px(54.0),
                    DimensionUtil.dp2px(54.0)
                )
            ), frameDuration
        )
        drawable.addFrame(
            BitmapDrawable(
                decodeSampledBitmapFromResource(
                    context.resources,
                    R.drawable.watch_reward_22,
                    DimensionUtil.dp2px(54.0),
                    DimensionUtil.dp2px(54.0)
                )
            ), frameDuration
        )
        drawable.isOneShot = true
        return drawable
    }

    private fun doFrameAnimation(animationDrawable: AnimationDrawable?) {
        progressRing!!.setImageDrawable(animationDrawable)
        if (animationDrawable!!.isRunning) {
            animationDrawable.stop()
        }
        animationDrawable.start()
    }

    private fun getProgress(mod: Float, totalTime: Float): Float {
        val i: Float = if (mod == 0f) 1f else mod
        return i / totalTime
    }

    private fun doValueAnimator(start: Float, end: Float, ring: ProgressRing, duration: Int) {
        ring.setTextAlpha(255)
        val animator = ValueAnimator.ofFloat(start, end)
        animator.interpolator = AccelerateInterpolator()
        animator.duration = duration.toLong()
        val animator1 = ValueAnimator.ofFloat(end, end)
        animator1.duration = d1.toLong()
        val animator2 = ValueAnimator.ofInt(255, 0)
        animator2.duration = d2.toLong()
        val set = AnimatorSet()
        set.playSequentially(animator, animator1, animator2)
        animator.addUpdateListener { animation ->
            val size = animation.animatedValue as Float
            ring.setTextSize(size)
        }
        animator1.addUpdateListener { animation ->
            val size = animation.animatedValue as Float
            ring.setTextSize(size)
        }
        animator2.addUpdateListener { animation ->
            val alpha = animation.animatedValue as Int
            ring.setTextAlpha(alpha)
            ring.setImageResource(R.drawable.watch_reward_1)
        }
        ring.setText("+8")
        set.start()
    }
}
