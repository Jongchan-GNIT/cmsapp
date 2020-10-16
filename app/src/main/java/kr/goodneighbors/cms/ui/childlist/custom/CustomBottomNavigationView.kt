package kr.goodneighbors.cms.ui.childlist.custom

import android.content.Context
import android.support.v4.app.Fragment
import android.util.AttributeSet
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.ui.BaseActivityFragment
import kr.goodneighbors.cms.ui.childlist.AclFragment
import kr.goodneighbors.cms.ui.childlist.GmlFragment
import kr.goodneighbors.cms.ui.childlist.ProfileFragment
import kr.goodneighbors.cms.ui.childlist.ProvidedServiceFragment
import kr.goodneighbors.cms.ui.childlist.ReportFragment
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.button
import org.jetbrains.anko.dimen
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.textColorResource

class CustomBottomNavigationView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0
) : LinearLayout(context) {

    var index: Int = -1
        set(_index) {
            when (_index) {
                0 -> {
                    button1.setBackgroundResource(R.drawable.gnb_child_on)
                }
                1 -> {
                    button2.setBackgroundResource(R.drawable.gnb_bgl_on)
                }
                2 -> {
                    button3.setBackgroundResource(R.drawable.gnb_bgl_on)
                }
                3 -> {
                    button4.setBackgroundResource(R.drawable.gnb_bgl_on)
                }
                4 -> {
                    button5.setBackgroundResource(R.drawable.gnb_bgl_on)
                }
            }
        }
    var container: Fragment? = null
        set(v) {
            if (v is BaseActivityFragment) {
                fragment = v
            }
        }
    
    private var fragment: BaseActivityFragment? = null


    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var button3: Button
    private lateinit var button4: Button
    private lateinit var button5: Button

    init {
        AnkoContext.createDelegate(this).apply {
            linearLayout {
                gravity = Gravity.CENTER

                button1 = button {
                    setBackgroundResource(R.drawable.gnb_child_off)
                    onClick {
                        fragment?.changeFragment?.onChangeFragment(ProfileFragment.newInstance(""))
                    }
                }.lparams(width = 0, height = dimen(R.dimen.px112), weight = 1f)
                button2 = button("Report") {
                    setBackgroundResource(R.drawable.gnb_bgl_off)
                    textColorResource = R.color.colorWhite
                    allCaps = false
                    onClick {
                        fragment?.changeFragment?.onChangeFragment(ReportFragment.newInstance(""))
                    }
                }.lparams(width = 0, height = dimen(R.dimen.px112), weight = 1f)
                button3 = button("Provied Service") {
                    setBackgroundResource(R.drawable.gnb_bgl_off)
                    textColorResource = R.color.colorWhite
                    allCaps = false
                    onClick {
                        fragment?.changeFragment?.onChangeFragment(ProvidedServiceFragment.newInstance(""))
                    }
                }.lparams(width = 0, height = dimen(R.dimen.px112), weight = 1f)
                button4 = button("ACL") {
                    setBackgroundResource(R.drawable.gnb_bgl_off)
                    textColorResource = R.color.colorWhite
                    allCaps = false
                    onClick {
                        fragment?.changeFragment?.onChangeFragment(AclFragment.newInstance(""))
                    }
                }.lparams(width = 0, height = dimen(R.dimen.px112), weight = 1f)
                button5 = button("GML") {
                    setBackgroundResource(R.drawable.gnb_bgr_off)
                    textColorResource = R.color.colorWhite
                    allCaps = false
                    onClick {
                        fragment?.changeFragment?.onChangeFragment(GmlFragment.newInstance(""))
                    }
                }.lparams(width = 0, height = dimen(R.dimen.px112), weight = 1f)
            }
        }
    }
}