package kr.goodneighbors.cms.ui.sync

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.common.DataSyncAdapter
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.button
import org.jetbrains.anko.dimen
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.space
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textResource
import org.jetbrains.anko.textSizeDimen
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class SyncOfflineGenerateFragment : Fragment() {
    companion object {
        fun newInstance(): SyncOfflineGenerateFragment {
            return SyncOfflineGenerateFragment()
        }
    }

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(SyncOfflineGenerateFragment::class.java)
    }

    private val syncAdapter: DataSyncAdapter by lazy {
        DataSyncAdapter(fragment = this)
    }

    private var ui = FragmentUI()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = ui.createView(AnkoContext.create(requireContext(), this))

        activity?.title = "Sync Data"

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val gnid = sharedPref.getString("GN_ID", "") ?: ""

        ui.execButton.onClick {
            ui.execButton.isEnabled = false

            doAsync {
                syncAdapter.export(imei = gnid, isOffline = true,
                        onSuccessListener = { zippedFilePath ->
                            onSuccessListener(zippedFilePath)
                        },
                        onErrorListener = { m ->
                            onErrorListener(m)

                        },
                        onPublishProgress = { m ->
                            onPublishProgress(m)
                        })
            }
        }

        return v
    }

    private fun onPublishProgress(message: String) {
        runOnUiThread {
            ui.messageTextView.text = message
        }
    }

    private fun onErrorListener(message: String) {
        runOnUiThread {
            ui.execButton.isEnabled = true
            ui.messageTextView.text = message
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onSuccessListener(fileName: String) {
        runOnUiThread {
            ui.execButton.isEnabled = false
            ui.execButton.textResource = R.string.label_complete
            ui.messageTextView.text = getString(R.string.label_export_complete) + "!\n${fileName.substringAfterLast("/")}"

            if (fileName.isNotBlank()) {
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                val contentUri = Uri.fromFile(File(fileName))
                mediaScanIntent.data = contentUri
                activity!!.sendBroadcast(mediaScanIntent)
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<SyncOfflineGenerateFragment> {

        lateinit var execButton: Button

        lateinit var messageTextView: TextView

        override fun createView(ui: AnkoContext<SyncOfflineGenerateFragment>) = with(ui) {
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

                execButton = button(R.string.button_generation) {
                    allCaps = false
                    backgroundColorResource = R.color.colorBrown
                    textColorResource = R.color.colorWhite
                }

                space { }.lparams(height = dimen(R.dimen.px26))
                messageTextView = textView {
                    gravity = Gravity.CENTER
                }

                space { }.lparams(height = dimen(R.dimen.px80))

                textView(R.string.message_info_how_to_upload_data) {
                    typeface = Typeface.DEFAULT_BOLD
                }
                space { }.lparams(height = dimen(R.dimen.px26))
                textView(R.string.message_sync_offline_upload) {
                    textSize = 12f
                }
            }
        }
    }
}
