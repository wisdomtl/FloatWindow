package taylor.com

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import taylor.com.util.Preference
import taylor.com.util.nightMode

open class BaseDialogFragment : DialogFragment() {
     private val preference: Preference by lazy {
         Preference(
             context!!.getSharedPreferences(
                 "dark-mode",
                 Context.MODE_PRIVATE
             )
         )
     }


     override fun onActivityCreated(savedInstanceState: Bundle?) {
         super.onActivityCreated(savedInstanceState)
         nightMode(preference["dark-mode",false])
     }
}
