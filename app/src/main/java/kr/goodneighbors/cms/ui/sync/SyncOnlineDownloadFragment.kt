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
import android.widget.ProgressBar
import android.widget.TextView
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.common.Constants
import kr.goodneighbors.cms.common.DataSyncAdapter
import kr.goodneighbors.cms.extensions.observeOnce
import kr.goodneighbors.cms.service.model.ApiDownloadListRsponse
import kr.goodneighbors.cms.service.viewmodel.SyncViewModel
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.button
import org.jetbrains.anko.dimen
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.space
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textSizeDimen
import org.jetbrains.anko.textView
import org.jetbrains.anko.topPadding
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.view
import org.jetbrains.anko.wrapContent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class SyncOnlineDownloadFragment : Fragment() {
    companion object {
        fun newInstance(): SyncOnlineDownloadFragment {
            return SyncOnlineDownloadFragment()
        }
    }

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(SyncOnlineDownloadFragment::class.java)
    }

    private val viewModel: SyncViewModel by lazy {
        ViewModelProviders.of(this).get(SyncViewModel::class.java)
    }

    private val syncAdapter: DataSyncAdapter by lazy {
        DataSyncAdapter(fragment = this)
    }

    private var ui = FragmentUI()

    private val fileQueue = LinkedList<ApiDownloadListRsponse.FileList>()
    private var currentItem: ApiDownloadListRsponse.FileList? = null

    private lateinit var adapter: SyncOnlineDownloadAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return ui.createView(AnkoContext.create(requireContext(), this))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.findAllAppDataImportHistory().observeOnce(this, Observer {
            val history = ArrayList<String>()
            if (it != null) {
                history.addAll(it)
            }

            viewModel.findAllDownloadList().observeOnce(this, Observer { response ->
                logger.debug("findAllDownloadList : $response")

                if (response != null) {
                    if (response.code == "S0000") {
                        val data = response.data
//                        val pageInfo = data?.page_info
                        val fileList = data?.file_list

                        logger.debug("filelist : $fileList")

                        fileList?.apply {
                            forEach { file ->
                                file.status = if (history.contains(file.fileId)) {
                                    ApiDownloadListRsponse.FileStatus.SyncComplete
                                } else {
                                    ApiDownloadListRsponse.FileStatus.Standby
                                }
                            }

                            val lm = LinearLayoutManager(requireContext())
                            lm.orientation = LinearLayoutManager.VERTICAL

                            adapter = SyncOnlineDownloadAdapter(items = this, onClickListener = { item -> onClickListener(item) })
                            ui.listRecyclerView.layoutManager = lm
                            ui.listRecyclerView.adapter = adapter
                        }

                    } else {
                        toast(response.message)
                    }
                } else {
                    toast(R.string.message_error_response_not_found)
                }
            })
        })

        viewModel.errorStatus.observeOnce(this, Observer { errorStatus ->
            toast(errorStatus ?: "")
        })
    }


    private fun onClickListener(item: ApiDownloadListRsponse.FileList) {
        fileQueue.addLast(item)

        item.status = ApiDownloadListRsponse.FileStatus.ReadyToDownload
        adapter.notifyDataSetChanged()

        download()
    }

    private fun download() {
        if (fileQueue.isNotEmpty() && currentItem == null) {
            currentItem = fileQueue.poll()
            currentItem?.status = ApiDownloadListRsponse.FileStatus.Downloading
            adapter.notifyDataSetChanged()

            val filePath = "${Constants.CF}/${currentItem?.file_path}/${currentItem?.file_nm}"
            logger.debug("download file : $filePath")

            doAsync {
                var input: InputStream? = null
                var output: OutputStream? = null
                var connection: HttpURLConnection? = null

                try {
                    val sdMain = Environment.getExternalStorageDirectory()

                    val targetDir = "$sdMain/${Constants.DIR_HOME}/${Constants.DIR_DOWNLOAD}"
                    val targetFile = File(targetDir, filePath.substringAfterLast("/"))

                    val url = URL(filePath)
                    connection = url.openConnection() as HttpURLConnection
                    connection.connect()

                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        publishProgress(connection.responseMessage)
                        return@doAsync
                    }

                    // getting file length
                    val lengthOfFile = connection.contentLength

                    input = connection.inputStream
                    onDownloadProgress(0)
                    input.let {
                        output = FileOutputStream(targetFile, false)
                        var loadedFileSize: Double = 0.0

                        val data = ByteArray(1024)
                        var count: Int
                        var prevProgress = 0L

                        do {
                            count = input.read(data)
                            if (count != -1) {
                                output!!.write(data, 0, count)
                                loadedFileSize += count

                                val p = Math.round(loadedFileSize / lengthOfFile * 100)
                                if (p != prevProgress) {
                                    prevProgress = p
                                    onDownloadProgress(p)
                                }
                            } else {
                                break
                            }

                        } while (count != -1)
                    }

                    onDownloadComplete(targetFile.path)
                } catch (e: Exception) {
                    e.printStackTrace()
                    logger.error("Error: ", e)
                } finally {
                    try {
                        output?.close()
                        input?.close()
                        connection?.disconnect()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun onUpdateComplete(status: ApiDownloadListRsponse.FileStatus) {
        if (currentItem != null) {
            currentItem?.status = status
            adapter.notifyDataSetChanged()

            currentItem = null
        }
        download()
    }

    private fun onDownloadComplete(filePath: String) {
        runOnUiThread {
            logger.debug("onDownloadComplete($filePath)")
            currentItem?.status = ApiDownloadListRsponse.FileStatus.Syncing
            adapter.notifyDataSetChanged()
        }

        doAsync {
            syncAdapter.import(filePath = filePath,
                    onSuccessListener = { fileId ->
                        logger.debug("onSuccessListener($fileId)")
                        runOnUiThread {
                            ui.messageTextView.text = "Update complete!"
                            onUpdateComplete(ApiDownloadListRsponse.FileStatus.SyncComplete)
                        }
                    },
                    onErrorListener = { m ->
                        runOnUiThread {
                            logger.debug("onErrorListener($m)")
                            ui.messageTextView.text = m
                            onUpdateComplete(ApiDownloadListRsponse.FileStatus.Standby)
                        }
                    },
                    onPublishProgress = { m ->
                        runOnUiThread {
                            ui.messageTextView.text = m
                        }
                    })
        }
    }

    private fun onDownloadProgress(p: Long) {
        runOnUiThread {
            when (p) {
                0L -> {
                    publishProgress("Download start!")
                    ui.downloadProgress.progress = 0
                    ui.downloadProgress.visibility = View.VISIBLE
                }
                100L -> {
                    publishProgress("Download complete!")
                    ui.downloadProgress.progress = 100
                    ui.downloadProgress.visibility = View.INVISIBLE
                }
                else -> {
                    publishProgress("Downloading...")
                    ui.downloadProgress.progress = p.toInt()
                }
            }
        }
    }

    private fun publishProgress(message: String) {
        runOnUiThread {
            ui.messageTextView.text = message
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<SyncOnlineDownloadFragment> {

        lateinit var messageTextView: TextView

        lateinit var listRecyclerView: RecyclerView

        lateinit var downloadProgress: ProgressBar

        override fun createView(ui: AnkoContext<SyncOnlineDownloadFragment>) = with(ui) {
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

                    textView(R.string.label_online_sync) {
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
                space { }.lparams(height = dimen(R.dimen.px10))
                downloadProgress = horizontalProgressBar {
                    visibility = View.INVISIBLE
                }
                space { }.lparams(height = dimen(R.dimen.px60))

                view { setBackgroundResource(R.color.colorMenuSplitLine) }.lparams(width = matchParent, height = 1)

                listRecyclerView = recyclerView {

                }.lparams(width = matchParent, height = 0, weight = 1f)

                space { }.lparams(height = dimen(R.dimen.px40))
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class SyncOnlineDownloadAdapter(
            val items: List<ApiDownloadListRsponse.FileList>,
            val onClickListener: (ApiDownloadListRsponse.FileList) -> Unit
    ) : RecyclerView.Adapter<SyncOnlineDownloadAdapter.ListAdaptorViewHolder>() {
        override fun getItemCount(): Int = items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdaptorViewHolder {
            return ListAdaptorViewHolder(ListViewHolderUI().createView(AnkoContext.create(parent.context, parent)))
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: SyncOnlineDownloadAdapter.ListAdaptorViewHolder, position: Int) {
            val currentItem = items[position]

            holder.rownumTextView.text = "${position + 1}"
            holder.filenameTextView.text = currentItem.fileId

            holder.executeButton.onClick {
                onClickListener(currentItem)
            }

            when (currentItem.status) {
                ApiDownloadListRsponse.FileStatus.Standby -> {
                    holder.executeButton.isEnabled = true
                    holder.executeButton.text = "Download"
                    holder.filenameTextView.typeface = Typeface.DEFAULT
                }
                ApiDownloadListRsponse.FileStatus.ReadyToDownload -> {
                    holder.executeButton.isEnabled = false
                    holder.executeButton.text = "Download pending..."
                    holder.filenameTextView.typeface = Typeface.DEFAULT
                }
                ApiDownloadListRsponse.FileStatus.Downloading -> {
                    holder.executeButton.isEnabled = false
                    holder.executeButton.text = "Downloading..."
                    holder.filenameTextView.typeface = Typeface.DEFAULT_BOLD
                }
                ApiDownloadListRsponse.FileStatus.DownloadComplete -> {
                    holder.executeButton.isEnabled = false
                    holder.executeButton.text = "Download complete"
                    holder.filenameTextView.typeface = Typeface.DEFAULT_BOLD
                }
                ApiDownloadListRsponse.FileStatus.Syncing -> {
                    holder.executeButton.isEnabled = false
                    holder.executeButton.text = "Updating..."
                    holder.filenameTextView.typeface = Typeface.DEFAULT_BOLD
                }
                ApiDownloadListRsponse.FileStatus.SyncComplete -> {
                    holder.executeButton.isEnabled = false
                    holder.executeButton.text = "Complete"
                    holder.filenameTextView.typeface = Typeface.DEFAULT
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
