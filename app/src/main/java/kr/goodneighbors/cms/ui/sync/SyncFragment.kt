package kr.goodneighbors.cms.ui.sync


import android.graphics.Typeface
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.extensions.isNetworkAvailable
import kr.goodneighbors.cms.ui.BaseActivityFragment
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.button
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.space
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textSizeDimen
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.view
import org.jetbrains.anko.wifiManager
import org.jetbrains.anko.wrapContent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SyncFragment : BaseActivityFragment() {
    companion object {
        fun newInstance(): SyncFragment {
            return SyncFragment()
        }
    }

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(SyncFragment::class.java)
    }

    private val ui = FragmentUI()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = ui.createView(AnkoContext.create(requireContext(), this))

        activity?.title = "Sync Data"

        ui.onlineButton.onClick {
//            if (requireContext().wifiManager.isWifiEnabled) {
            if (requireContext().isNetworkAvailable()) {
                changeFragment.onChangeFragment(SyncOnlineFragment.newInstance())
            } else {
                toast(R.string.message_wifi_disabled)
            }
        }

        ui.offlineButton.onClick {
            changeFragment.onChangeFragment(SyncOfflineFragment.newInstance())
        }

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val d = sharedPref.getString("GN_DATA_DATE", "-") ?: ""

        ui.versionTextView.text = d

        return v
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<SyncFragment> {

        lateinit var onlineButton: Button

        lateinit var offlineButton: Button

        lateinit var versionTextView: TextView

        override fun createView(ui: AnkoContext<SyncFragment>) = with(ui) {
            verticalLayout {
                padding = dimen(R.dimen.px100)

                textView(R.string.label_select_sync_method) {
                    gravity = Gravity.CENTER
                    textSizeDimen = R.dimen.sp40
                    typeface = Typeface.DEFAULT_BOLD
                }.lparams(width = matchParent, height = wrapContent)

                space { }.lparams(height = dimen(R.dimen.px112))

                onlineButton = button(R.string.label_online_sync) {
                    allCaps = false
                    backgroundColorResource = R.color.colorBrown
                    textColorResource = R.color.colorWhite
                }

                space { }.lparams(height = dimen(R.dimen.px26))

                offlineButton = button(R.string.label_offline_sync) {
                    allCaps = false
                    backgroundColorResource = R.color.colorBrown
                    textColorResource = R.color.colorWhite
                }

                space { }.lparams(height = dimen(R.dimen.px80))

                textView(R.string.message_info_online_sync)
                space { }.lparams(height = dimen(R.dimen.px26))
                textView(R.string.message_info_offline_sync)
                space { }.lparams(height = dimen(R.dimen.px112))
                view { backgroundColorResource = R.color.colorGray }.lparams(width = matchParent, height = dip(1))
                space { }.lparams(height = dimen(R.dimen.px26))

                linearLayout {
                    gravity = Gravity.CENTER

                    textView(owner.getString(R.string.label_initialized_file_info) + " :\n ")
                    versionTextView = textView {
                        textColorResource = R.color.colorAccent
                    }
                }
            }
        }
    }
}
