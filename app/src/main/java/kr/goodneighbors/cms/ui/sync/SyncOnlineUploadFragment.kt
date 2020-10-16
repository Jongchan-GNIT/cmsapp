package kr.goodneighbors.cms.ui.sync


import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.common.DataSyncAdapter
import kr.goodneighbors.cms.extensions.isNetworkAvailable
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.button
import org.jetbrains.anko.dimen
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.space
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textSizeDimen
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wifiManager
import org.jetbrains.anko.wrapContent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SyncOnlineUploadFragment : Fragment() {
    companion object {
        fun newInstance(): SyncOnlineUploadFragment {
            return SyncOnlineUploadFragment()
        }
    }

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(SyncOnlineUploadFragment::class.java)
    }

    private val syncAdapter: DataSyncAdapter by lazy {
        DataSyncAdapter(fragment = this)
    }

    private var ui = FragmentUI()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = ui.createView(AnkoContext.create(requireContext(), this))

        activity?.title = "Sync Data"

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val gnid = sharedPref.getString("GN_ID", "")

        ui.executeButton.onClick {
            if (gnid.isNullOrBlank()) {
                toast("Device ID not found!")
            } else {
//                if (requireContext().wifiManager.isWifiEnabled) {
                if (requireContext().isNetworkAvailable()) {
                    ui.executeButton.isEnabled = false
                    ui.uploadProgressBar.progress = 0
                    doAsync {
                        syncAdapter.upload(imei = gnid,
                                onSuccessListener = { zippedFilePath ->
                                    runOnUiThread {
                                        logger.debug("onSuccessListener($zippedFilePath)")
                                        ui.messageTextView.text = "The upload of the ${zippedFilePath.substringAfterLast("/")} file is complete"
                                    }
                                },
                                onErrorListener = { m ->

                                    runOnUiThread {
                                        logger.debug("onErrorListener($m)")
                                        ui.errorMessageTextView.text = m

                                        ui.executeButton.text = "Upload Fail"
                                    }
                                },
                                onPublishProgress = { m ->
                                    runOnUiThread {
                                        logger.debug("onPublishProgress($m)")
                                        ui.messageTextView.text = m
                                    }
                                },
                                onUploadProgress = { p ->
                                    runOnUiThread {
                                        ui.uploadProgressBar.progress = p
                                    }
                                })
                    }
                } else {
                    toast(R.string.message_wifi_disabled)
                }
            }
        }

        return v
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<SyncOnlineUploadFragment> {

        lateinit var executeButton: Button
        lateinit var messageTextView: TextView
        lateinit var errorMessageTextView: TextView

        lateinit var uploadProgressBar: ProgressBar

        override fun createView(ui: AnkoContext<SyncOnlineUploadFragment>) = with(ui) {
            verticalLayout {
                padding = dimen(R.dimen.px100)

                linearLayout {
                    gravity = Gravity.CENTER

                    textView("Sync Method : ") {
                        gravity = Gravity.CENTER
                        textSizeDimen = R.dimen.sp40
                        typeface = Typeface.DEFAULT_BOLD
                    }

                    textView(R.string.label_online_sync) {
                        gravity = Gravity.CENTER
                        textSizeDimen = R.dimen.sp40
                        textColorResource = R.color.colorAccent
                        typeface = Typeface.DEFAULT_BOLD
                    }
                }.lparams(width = matchParent, height = wrapContent)

                space { }.lparams(height = dimen(R.dimen.px112))

                executeButton = button(R.string.button_upload) {
                    allCaps = false
                    backgroundColorResource = R.color.colorBrown
                    textColorResource = R.color.colorWhite
                }

                space { }.lparams(height = dimen(R.dimen.px20))
                uploadProgressBar = horizontalProgressBar {
                }

                space { }.lparams(height = dimen(R.dimen.px26))
                messageTextView = textView {
                    gravity = Gravity.CENTER
                }
                space { }.lparams(height = dimen(R.dimen.px20))
                errorMessageTextView = textView {
                    gravity = Gravity.CENTER
                    textColorResource = R.color.colorAccent
                }
            }
        }
    }
}
