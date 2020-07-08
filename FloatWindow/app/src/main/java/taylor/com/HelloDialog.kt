package taylor.com

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager


class HelloDialog : DialogFragment() {

    private val rootView by lazy {
        ConstraintLayout {
            layout_width = 300
            layout_height = 200
            TextView {
                layout_width = wrap_content
                layout_height = wrap_content
                text = "Hello"
                textSize = 20f
                center_horizontal = true
                center_vertical = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        arguments?.getCharSequence("bonus")?.let {
        }
    }

    companion object {
        fun show(manager: FragmentManager, init: Fragment.() -> Unit) {
            val instance = HelloDialog()
            instance.apply(init)
            instance.show(manager, "hello")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootView.detachParent()
    }
}

fun View?.detachParent() = this?.parent?.let { it as? ViewGroup }?.also { it.removeView(this) }
