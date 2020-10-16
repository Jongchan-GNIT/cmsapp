package kr.goodneighbors.cms.ui.sync


import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.common.Constants
import kr.goodneighbors.cms.common.DataSyncAdapter
import kr.goodneighbors.cms.common.ProcessState
import kr.goodneighbors.cms.extensions.fileId
import kr.goodneighbors.cms.extensions.observeOnce
import kr.goodneighbors.cms.service.model.SyncListItem
import kr.goodneighbors.cms.service.viewmodel.SyncViewModel
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.button
import org.jetbrains.anko.dimen
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.space
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textResource
import org.jetbrains.anko.textSizeDimen
import org.jetbrains.anko.textView
import org.jetbrains.anko.topPadding
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.view
import org.jetbrains.anko.wrapContent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileFilter
import java.util.*
import kotlin.collections.ArrayList

class SyncOfflineSyncFragment : Fragment() {
    companion object {
        fun newInstance(): SyncOfflineSyncFragment {
            return SyncOfflineSyncFragment()
        }
    }

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(SyncOfflineSyncFragment::class.java)
    }

    val viewModel: SyncViewModel by lazy {
        ViewModelProviders.of(this).get(SyncViewModel::class.java)
    }

    private val syncAdapter: DataSyncAdapter by lazy {
        DataSyncAdapter(fragment = this)
    }

    private var ui = FragmentUI()

    private val fileQueue = LinkedList<SyncListItem>()
    private var currentItem: SyncListItem? = null

    private lateinit var adapter: SyncOfflineSyncAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return ui.createView(AnkoContext.create(requireContext(), this))
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = "Sync Data"

        viewModel.findAllAppDataImportHistory().observeOnce(this, Observer {
            val importedFileList = ArrayList<String>()

            if (it != null) {
                importedFileList.addAll(it)
            }

            val sdMain = Environment.getExternalStorageDirectory()
            val importFiles = File("$sdMain/${Constants.DIR_HOME}/${Constants.DIR_IMPORT}").listFiles(FileFilter { it.isFile })

            if (importFiles.isEmpty()) {
                ui.messageTextView.textResource = R.string.message_error_file_not_found
            } else {
                ui.messageTextView.text = ""
                val listItems = arrayListOf<SyncListItem>()
                importFiles.forEachIndexed { index, file ->
                    logger.debug("onViewCreated ::: init file : $file")
                    listItems.add(index,
                            SyncListItem(index = index + 1,
                                    path = file.path,
                                    state = if (importedFileList.contains(file.fileId())) {
                                        ProcessState.DONE
                                    } else {
                                        ProcessState.STANDBY
                                    })
                    )
                }

                val lm = LinearLayoutManager(requireContext())
                lm.orientation = LinearLayoutManager.VERTICAL
                adapter = SyncOfflineSyncAdapter(items = listItems, onClickListener = { item -> onClickListener(item) })
                ui.listRecyclerView.layoutManager = lm
                ui.listRecyclerView.adapter = adapter
            }
        })
    }

    private fun onClickListener(item: SyncListItem) {
        fileQueue.addLast(item)
        item.state = ProcessState.PROGRESSING
        adapter.notifyDataSetChanged()
        excute()
    }

    private fun excute() {
        if (fileQueue.isNotEmpty() && currentItem == null) {
            currentItem = fileQueue.poll()

            currentItem?.apply {
                doAsync {
                    syncAdapter.import(
                            filePath = path,
                            onSuccessListener = { fileId ->
                                logger.debug("onSuccessListener($fileId)")
                                runOnUiThread {
                                    ui.messageTextView.textResource = R.string.message_update_complete
                                    onUpdateComplete(ProcessState.DONE)
                                }
                            },
                            onErrorListener = { m ->
                                runOnUiThread {
                                    logger.debug("onErrorListener($m)")
                                    ui.messageTextView.text = m
                                    onUpdateComplete(ProcessState.STANDBY)
                                }
                            },
                            onPublishProgress = { m ->
                                runOnUiThread {
                                    ui.messageTextView.text = m
                                }
                            })
                }
            }
        }
    }

    private fun onUpdateComplete(status: ProcessState) {
        if (currentItem != null) {
            currentItem?.state = status
            adapter.notifyDataSetChanged()

            currentItem = null
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<SyncOfflineSyncFragment> {

        lateinit var messageTextView: TextView

        lateinit var listRecyclerView: RecyclerView

        override fun createView(ui: AnkoContext<SyncOfflineSyncFragment>) = with(ui) {
            verticalLayout {
                topPadding = dimen(R.dimen.px100)
                leftPadding = dimen(R.dimen.px40)
                rightPadding = dimen(R.dimen.px40)
                bottomPadding = dimen(R.dimen.px40)

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

                space { }.lparams(height = dimen(R.dimen.px40))
                messageTextView = textView {
                    gravity = Gravity.CENTER
                }

                space { }.lparams(height = dimen(R.dimen.px60))

                view { setBackgroundResource(R.color.colorMenuSplitLine) }.lparams(width = matchParent, height = 1)

                listRecyclerView = recyclerView {

                }.lparams(width = matchParent, height = 0, weight = 1f)

                view { setBackgroundResource(R.color.colorMenuSplitLine) }.lparams(width = matchParent, height = 1)

                space { }.lparams(height = dimen(R.dimen.px20))

                textView(R.string.message_info_how_to_offline_sync) {
                    typeface = Typeface.DEFAULT_BOLD
                }
                space { }.lparams(height = dimen(R.dimen.px26))
                textView(R.string.message_sync_offline_sync) {
                    textSize = 12f
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class SyncOfflineSyncAdapter(
            val items: List<SyncListItem>,
            val onClickListener: (SyncListItem) -> Unit
    ) : RecyclerView.Adapter<SyncOfflineSyncAdapter.ListAdaptorViewHolder>() {
        override fun getItemCount(): Int = items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdaptorViewHolder {
            return ListAdaptorViewHolder(ListViewHolderUI().createView(AnkoContext.create(parent.context, parent)))
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: SyncOfflineSyncAdapter.ListAdaptorViewHolder, position: Int) {
            val currentItem = items[position]

            holder.rownumTextView.text = "${position + 1}"
            holder.filenameTextView.text = currentItem.fileId

            holder.executeButton.onClick {
                onClickListener(currentItem)
            }

            when (currentItem.state) {
                ProcessState.STANDBY -> {
                    holder.executeButton.isEnabled = true
                    holder.executeButton.text = "Ready"
                }
                ProcessState.PROGRESSING -> {
                    holder.executeButton.isEnabled = false
                    holder.executeButton.text = "Updating..."
                }
                ProcessState.DONE -> {
                    holder.executeButton.isEnabled = false
                    holder.executeButton.text = "Complete"
                }
                else -> {
                }
            }
        }

        class ListAdaptorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val rownumTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_ROWNUM)!!
            val filenameTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_FILENAME)!!
            val executeButton = itemView.findViewById<Button>(ListViewHolderUI.ID_BUTTON)!!
        }

        class ListViewHolderUI : AnkoComponent<ViewGroup> {
            companion object {
                const val ID_ROWNUM = 1
                const val ID_FILENAME = 2
                const val ID_BUTTON = 3
            }

            override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                verticalLayout {
                    lparams(width = matchParent, height = wrapContent)

                    gravity = Gravity.CENTER_VERTICAL
                    topPadding = dimen(R.dimen.px4)
                    bottomPadding = dimen(R.dimen.px4)

                    linearLayout {
                        textView {
                            id = ID_ROWNUM
                        }
                        textView {
                            id = ID_FILENAME
                            textSizeDimen = R.dimen.sp24
                        }.lparams(width = 0, weight = 1f) {
                            leftMargin = dimen(R.dimen.px22)
                        }

                        button {
                            id = ID_BUTTON
                            textSizeDimen = R.dimen.sp24
                            allCaps = false
                        }
                    }
                }
            }
        }
    }
}
