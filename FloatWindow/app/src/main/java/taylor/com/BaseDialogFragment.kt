package taylor.com

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment

 open class BaseDialogFragment : DialogFragment() {

    private fun toggleDarkMode(show: Boolean) {
        val id = "darkMask"
        if (show) {
            BaseActivity.maskHandler.postAtFrontOfQueue {
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

    override fun onStart() {
        toggleDarkMode(true)
        super.onStart()
    }
}

val DialogFragment.decorView:ViewGroup?
    get() {
        return view?.parent as? ViewGroup
    }