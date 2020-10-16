@file:Suppress("DEPRECATION")

package kr.goodneighbors.cms.ui.settings

import android.app.ProgressDialog
import android.arch.lifecycle.Observer
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.common.Constants
import kr.goodneighbors.cms.common.GNLocaleManager
import kr.goodneighbors.cms.extensions.observeOnce
import kr.goodneighbors.cms.service.viewmodel.SettingViewModel
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alert
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.button
import org.jetbrains.anko.dip
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.noButton
import org.jetbrains.anko.padding
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onItemSelectedListener
import org.jetbrains.anko.spinner
import org.jetbrains.anko.support.v4.indeterminateProgressDialog
import org.jetbrains.anko.support.v4.space
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent
import org.jetbrains.anko.yesButton
import java.io.File
import java.util.*

class SettingFragment : Fragment() {
    companion object {
        fun newInstance(): SettingFragment {
            return SettingFragment()
        }
    }

    private val viewModel: SettingViewModel by lazy {
        SettingViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return FragmentUI().createView(AnkoContext.create(requireContext(), this))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Settings"
    }

    fun onClickDeleteContentsListener() {
        val progress: ProgressDialog = indeterminateProgressDialog(R.string.message_processing)
        viewModel.deletePastContentFile().observeOnce(this, Observer {
            progress.dismiss()
            it?.apply {
                if (this) {
                    toast(R.string.message_the_file_deletion_is_complete)
                }
                else {
                    toast(R.string.message_there_was_an_error_deleting_the_file)
                }
            }

        })
    }

    fun onClickGenerateLogfile() {
        val sdMain = Environment.getExternalStorageDirectory()
        val targetFile = File("$sdMain/${Constants.DIR_HOME}/${Constants.DIR_LOG}", "debug-${Date().time}.log")
        val logFile = File(context?.filesDir, "debug.log")
        logFile.copyTo(targetFile)

        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri = Uri.fromFile(targetFile)
        mediaScanIntent.data = contentUri
        activity!!.sendBroadcast(mediaScanIntent)
    }

    fun onLocaleChangeListener(localeString: String) {
        if (GNLocaleManager.getCurrentLanguage(requireContext()) != localeString) {
            GNLocaleManager.setNewLocale(requireContext(), localeString)
            GNLocaleManager.persistLanguagePreference(requireContext(), localeString)

            requireActivity().recreate()
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<SettingFragment> {
        override fun createView(ui: AnkoContext<SettingFragment>) = with(ui) {
            verticalLayout {
                padding = dip(20)

                linearLayout {
                    textView(R.string.label_language) {}.lparams(width = 0, height = wrapContent, weight = 1f) {

                    }
                    val items = listOf("English", "French", "Spanish")
                    val spinnerAdapter = ArrayAdapter(owner.requireContext(), R.layout.spinneritem_dark, items)
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner {
                        adapter = spinnerAdapter

                        when(GNLocaleManager.getCurrentLanguage(owner.requireContext())) {
                            "en"-> setSelection(0)
                            "fr"-> setSelection(1)
                            "es"-> setSelection(2)
                        }

                        onItemSelectedListener {
                            onItemSelected { adapterView, view, i, l ->
                                val ls = items[i]
                                when(ls) {
                                    "English"-> {
                                        owner.onLocaleChangeListener("en")
                                    }
                                    "French"-> {
                                        owner.onLocaleChangeListener("fr")
                                    }
                                    "Spanish"-> {
                                        owner.onLocaleChangeListener("es")
                                    }
                                }

                            }
                        }
                    }
                }

                space { }.lparams(width = matchParent, height = dip(20))

                verticalLayout {
                    textView(R.string.message_info_contents_delete_info) {
                        textColorResource = R.color.colorAccent
                    }

                    button(R.string.button_delete_contents) {
                        allCaps = false
                        backgroundColorResource = R.color.colorBrown
                        textColorResource = R.color.colorWhite
                        onClick {
                            alert(R.string.message_confirm_delete_contents) {
                                yesButton {
                                    owner.onClickDeleteContentsListener()
                                }
                                noButton {}
                            }.show()
                        }
                    }
                }

                space { }.lparams(width = matchParent, height = 0, weight = 1f)

                button(R.string.button_create_log_file) {
                    allCaps = false
                    onClick {
                        owner.onClickGenerateLogfile()
                    }
                }
            }
        }
    }
}
