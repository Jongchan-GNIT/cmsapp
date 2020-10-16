package kr.goodneighbors.cms.ui

import android.content.Context
import android.support.v4.app.Fragment

abstract class BaseActivityFragment: Fragment() {
    lateinit var changeFragment: ChangeFragment

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context != null && context is ChangeFragment) {
            changeFragment = context
        }
    }

    interface ChangeFragment {
        fun <T : Any?> onChangeActivity(clazz: Class<T>, useBackstack: Boolean = true)
        fun onChangeFragment(fragment: Fragment, useBackstack: Boolean = true)
    }
}