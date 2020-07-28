package taylor.com.floatwindow

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.widget.Button
import android.widget.Toast
import taylor.com.BaseActivity
import taylor.com.HelloDialog
import taylor.com.util.Preference
import taylor.com.util.nightMode

class MainActivity : BaseActivity() {

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

    private val preference by lazy { Preference(getSharedPreferences("dark-mode", Context.MODE_PRIVATE)) }

    private var maskWindowInfo:FloatWindow.WindowInfo? = null

    private var darkModeEnable  = false
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        printCallStack()
        return super.dispatchTouchEvent(ev)
    }


    fun printCallStack(){
        val ex = Throwable()
        ex.stackTrace?.take(8)?.forEach {
            Log.v("ttaylor","tag=asdf, .printCallStack()  ${it.className}.${it.methodName} line number=${it.lineNumber}")
        }
        Log.e("ttaylor","tag=asdf, .printCallStack() --------------------------------------------")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        darkModeEnable = preference["dark-mode",false]

        findViewById<Button>(R.id.tvD).setOnClickListener { _->
            Intent(this@MainActivity,SideWindowActivity::class.java).let {
                startActivity(it)
            }
        }


        findViewById<Button>(R.id.btnMaskWindow).setOnClickListener {
            val view = LayoutInflater.from(applicationContext).inflate(R.layout.mask_window_view,null)
            maskWindowInfo = FloatWindow.WindowInfo(view).apply {
                width = DimensionUtil.getScreenWidth(this@MainActivity)
                height = DimensionUtil.getScreenHeight(this@MainActivity)
            }
            FloatWindow.show(applicationContext,"mask",maskWindowInfo,0,100,false,true,true)
        }

        findViewById<Button>(R.id.btnNormal).setOnClickListener {
            Toast.makeText(this@MainActivity,"globalMask",Toast.LENGTH_LONG).show()
        }


        findViewById<Button>(R.id.btnDarkMode).setOnClickListener {
            darkModeEnable = !darkModeEnable
            preference["dark-mode"] = darkModeEnable
            nightMode(darkModeEnable)
        }

        findViewById<Button>(R.id.btnShowDialog).setOnClickListener {
            HelloDialog.show(supportFragmentManager){}
        }

    }

    override fun onResume() {
        super.onResume()
        showDragableWindow()
    }

    private fun showDragableWindow() {
        FloatWindow.onWindowShow = {
            Log.v("ttaylor","tag=, MainActivity.showDragableWindow() onWindowShow ")
        }
        if (windowInfo == null) {
            windowInfo = FloatWindow.WindowInfo(generateWindowView())
            windowInfo!!.width = DimensionUtil.dp2px(54.0)
            windowInfo!!.height = DimensionUtil.dp2px(54.0)
            FloatWindow.show(this, TAG_WINDOW_A, windowInfo, 0, 0, true,false)
        }
        FloatWindow.show(this, TAG_WINDOW_A, dragEnable = true, overall =false)
        FloatWindow.setOutsideTouchable(true) {
            Log.v("ttaylor", "tag=touch outside, WindowActivity.onResume()  ")
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

    private fun decodeSampledBitmapFromResource(
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

    override fun onDestroy() {
        super.onDestroy()
        FloatWindow.dismiss("mask")
    }
}
