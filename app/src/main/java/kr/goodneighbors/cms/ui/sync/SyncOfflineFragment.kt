package kr.goodneighbors.cms.ui.sync


import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.ui.BaseActivityFragment
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.button
import org.jetbrains.anko.dimen
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.space
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textSizeDimen
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SyncOfflineFragment : BaseActivityFragment() {
    companion object {
        fun newInstance(): SyncOfflineFragment {
            return SyncOfflineFragment()
        }
    }

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(SyncOfflineFragment::class.java)
    }

    private val ui = FragmentUI()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = ui.createView(AnkoContext.create(requireContext(), this))

        activity?.title = "Sync Data"

        // 동기화 파일 생성
        ui.makeSyncButton.onClick {
            changeFragment.onChangeFragment(SyncOfflineGenerateFragment.newInstance())
        }

        // 동기화
        ui.syncButton.onClick {
            changeFragment.onChangeFragment(SyncOfflineSyncFragment.newInstance())
        }

        return v
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<SyncOfflineFragment> {

        lateinit var makeSyncButton: Button

        lateinit var syncButton: Button

        override fun createView(ui: AnkoContext<SyncOfflineFragment>) = with(ui) {
            verticalLayout {
                padding = dimen(R.dimen.px100)

                linearLayout {
                    gravity = Gravity.CENTER

                    textView("Sync Method : ") {
                        gravity = Gravity.CENTER
                        textSizeDimen = R.dimen.sp40
                        typeface = Typeface.DEFAULT_BOLD
                    }

                    textView(R.string.label_offline_sync) {
                        gravity = Gravity.CENTER
                        textSizeDimen = R.dimen.sp40
                        textColorResource = R.color.colorAccent
                        typeface = Typeface.DEFAULT_BOLD
                    }
                }.lparams(width = matchParent, height = wrapContent)

                space { }.lparams(height = dimen(R.dimen.px112))

                makeSyncButton = button(R.string.button_make_sync_data) {
                    allCaps = false
                    backgroundColorResource = R.color.colorBrown
                    textColorResource = R.color.colorWhite
                }

                space { }.lparams(height = dimen(R.dimen.px26))

                syncButton = button(R.string.button_sync_data) {
                    allCaps = false
                    backgroundColorResource = R.color.colorBrown
                    textColorResource = R.color.colorWhite
                }

                space { }.lparams(height = dimen(R.dimen.px80))

                textView(R.string.message_info_make_sync_data)
                space { }.lparams(height = dimen(R.dimen.px26))
                textView(R.string.message_info_gen_sync_data)
            }
        }
    }
}
