@file:Suppress("DEPRECATION")

package kr.goodneighbors.cms.ui.childlist


import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Environment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.common.Constants
import kr.goodneighbors.cms.extensions.circleImageView
import kr.goodneighbors.cms.extensions.convertDateFormat
import kr.goodneighbors.cms.extensions.isNetworkAvailable
import kr.goodneighbors.cms.extensions.isNumber
import kr.goodneighbors.cms.extensions.observeOnce
import kr.goodneighbors.cms.service.model.ReportListItem
import kr.goodneighbors.cms.service.viewmodel.ReportViewModel
import kr.goodneighbors.cms.ui.BaseActivityFragment
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.button
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.imageView
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.space
import org.jetbrains.anko.support.v4.indeterminateProgressDialog
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wifiManager
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

@Suppress("PrivatePropertyName")
class ReportFragment : BaseActivityFragment() {
    companion object {
        fun newInstance(chrcp_no: String): ReportFragment {
            val fragment = ReportFragment()
            val args = Bundle()
            args.putString("chrcp_no", chrcp_no)

            fragment.arguments = args
            return fragment
        }
    }

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(ReportFragment::class.java)
    }

    private val ui = FragmentUI()

    private val viewModel: ReportViewModel by lazy {
        ViewModelProviders.of(this).get(ReportViewModel::class.java)
    }

    private lateinit var chrcp_no: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chrcp_no = arguments!!.getString("chrcp_no")

        viewModel.findAllReportByChild().observe(this, Observer {

            logger.debug("items : $it")

            val adapter = ReportAdaptor(
                    items = it ?: arrayListOf(),
                    onClickTitleListener = { selectedItem -> onClickTitleListener(selectedItem) },
                    onClickRefreshListener = { selectedItem -> onClickRefreshListener(selectedItem) },
                    onClickAddListener = { selectedItem -> onClickAddListener(selectedItem) }
            )

            ui.itemsRecyclerView.adapter = adapter
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = ui.createView(AnkoContext.create(requireContext(), this))

        activity?.title = "Report"

        ui.itemsRecyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.setFindAllReportByChild(chrcp_no)
        return v
    }

    private fun onClickTitleListener(item: ReportListItem) {
        when (item.RPT_DVCD) {
            "1" -> changeFragment.onChangeFragment(CifFragment.newInstance(item.RCP_NO))
            "2" -> changeFragment.onChangeFragment(AprFragment.newInstance(item.CHRCP_NO, item.RCP_NO, item.YEAR))
            "3" -> changeFragment.onChangeFragment(DropoutFragment.newInstance(item.CHRCP_NO, item.RCP_NO))
        }
    }

    // 삭제 또는 이미지 새로 고침
    private fun onClickRefreshListener(item: ReportListItem) {
        if (item.RPT_STCD == "12") {
            viewModel.deleteReportById(item.RCP_NO ?: "").observeOnce(this, Observer {
                viewModel.setFindAllReportByChild(chrcp_no)
            })
        }
        else {
//            if (requireContext().wifiManager.isWifiEnabled) {
            if (requireContext().isNetworkAvailable()) {
                val progress: ProgressDialog = indeterminateProgressDialog(R.string.message_downloading)
                val sdMain = Environment.getExternalStorageDirectory()
                val contentsRootDir = File("$sdMain/${Constants.DIR_HOME}/${Constants.DIR_CONTENTS}")

                viewModel.findAllFiles(item.RCP_NO ?: "").observeOnce(this, Observer { files ->
                    doAsync {
                        files?.filter { !it.FILE_PATH.isNullOrBlank() && !it.FILE_NM.isNullOrBlank() }?.forEach { file ->
                            var input: InputStream? = null
                            var output: OutputStream? = null
                            var connection: HttpURLConnection? = null

                            try {
                                val targetFile = File(contentsRootDir, "${file.FILE_PATH}/${file.FILE_NM}")
                                val targetDir = targetFile.parentFile
                                if (!targetDir.exists()) targetDir.mkdirs()

                                logger.debug("targetFile : $targetFile")

                                val url = URL("${Constants.CF}/${file.FILE_PATH}/${file.FILE_NM}")
                                connection = url.openConnection() as HttpURLConnection
                                connection.connect()

                                logger.debug("connection success : ${url}")

                                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                                    toast(connection.responseMessage)
                                }

                                // getting file length
                                val lengthOfFile = connection.contentLength

                                input = connection.inputStream
                                input.let {
                                    output = FileOutputStream(targetFile, false)
                                    var loadedFileSize:Double = 0.0

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
                                            }
                                        } else {
                                            break
                                        }

                                    } while (count != -1)
                                }

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
                                    logger.error("Error: ", e)
                                }
                            }
                        }

                        progress.dismiss()
                        viewModel.setFindAllReportByChild(chrcp_no)
                    }
                })
            } else {
                toast(R.string.message_wifi_disabled)
            }
        }
    }

    // 추가 또는 이미지 클릭(pass)
    private fun onClickAddListener(item: ReportListItem) {
        // 추가
        changeFragment.onChangeFragment(AprFragment.newInstance(item.CHRCP_NO, item.RCP_NO, item.YEAR))
    }

    fun onChangeBottomNavigation(position: Int) {
        when (position) {
            0 -> {
                changeFragment.onChangeFragment(ProfileFragment.newInstance(chrcp_no))
            }
            1 -> {
                changeFragment.onChangeFragment(ReportFragment.newInstance(chrcp_no))
            }
            2 -> {
                changeFragment.onChangeFragment(ProvidedServiceFragment.newInstance(chrcp_no))
            }
            3 -> {
                changeFragment.onChangeFragment(AclFragment.newInstance(chrcp_no))
            }
            4 -> {
                changeFragment.onChangeFragment(GmlFragment.newInstance(chrcp_no))
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<ReportFragment> {
        lateinit var itemsRecyclerView: RecyclerView

        override fun createView(ui: AnkoContext<ReportFragment>) = with(ui) {
            verticalLayout {
                verticalLayout {
                    itemsRecyclerView = recyclerView {
                    }.lparams(width = matchParent, height = matchParent)
                }.lparams(width = matchParent, height = 0, weight = 1f)

                linearLayout {
                    gravity = Gravity.CENTER

                    button {
                        setBackgroundResource(R.drawable.gnb_child_off)
                        onClick {
                            owner.onChangeBottomNavigation(0)
                        }
                    }.lparams(width = 0, height = dimen(R.dimen.px112), weight = 1f)
                    button("Report") {
                        setBackgroundResource(R.drawable.gnb_bgl_on)
                        textColorResource = R.color.colorWhite
                        allCaps = false
//                        onClickListener {
//                            owner.onChangeBottomNavigation(1)
//                        }
                    }.lparams(width = 0, height = dimen(R.dimen.px112), weight = 1f)
                    button("Provied Service") {
                        setBackgroundResource(R.drawable.gnb_bgl_off)
                        textColorResource = R.color.colorWhite
                        allCaps = false
                        onClick {
                            owner.onChangeBottomNavigation(2)
                        }
                    }.lparams(width = 0, height = dimen(R.dimen.px112), weight = 1f)
                    button("ACL") {
                        setBackgroundResource(R.drawable.gnb_bgl_off)
                        textColorResource = R.color.colorWhite
                        allCaps = false
                        onClick {
                            owner.onChangeBottomNavigation(3)
                        }
                    }.lparams(width = 0, height = dimen(R.dimen.px112), weight = 1f)
                    button("GML") {
                        setBackgroundResource(R.drawable.gnb_bgr_off)
                        textColorResource = R.color.colorWhite
                        allCaps = false
                        onClick {
                            owner.onChangeBottomNavigation(4)
                        }
                    }.lparams(width = 0, height = dimen(R.dimen.px112), weight = 1f)
                }
            }
        }
    }

    class ReportAdaptor(
            val items: List<ReportListItem>,
            val onClickTitleListener: (ReportListItem) -> Unit,
            val onClickRefreshListener: (ReportListItem) -> Unit,
            val onClickAddListener: (ReportListItem) -> Unit) : RecyclerView.Adapter<ReportAdaptor.ListAdaptorViewHolder>() {

        private val logger: Logger by lazy {
            LoggerFactory.getLogger(ReportAdaptor::class.java)
        }

        private val sdMain = Environment.getExternalStorageDirectory()
        private val targetDir = File("$sdMain/${Constants.DIR_HOME}/${Constants.DIR_CONTENTS}")

        override fun getItemCount(): Int = items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdaptorViewHolder {
            return ListAdaptorViewHolder(ListViewHolderUI().createView(AnkoContext.create(parent.context, parent)))
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ListAdaptorViewHolder, position: Int) {
            logger.debug("----------ReportAdaptor.onBindViewHolder : position = $position")
            val currentItem = items[position]
//        holder.item = currentItem

            // 새로고침 또는 삭제
            holder.refreshImageView.onClick {
                onClickRefreshListener(currentItem)
            }

            // 제목 영역 클릭
            holder.titleContainerView.onClick {
                onClickTitleListener(currentItem)
            }

            // 등록 또는 이미지뷰
            holder.thumbnailImageView.onClick {
                onClickAddListener(currentItem)
            }

            holder.detailToggleImageView.onClick {
                currentItem.IS_SELECTED = currentItem.IS_SELECTED != true
                notifyDataSetChanged()
            }

            val reportType = when (currentItem.RPT_DVCD) {
                "1" -> "CIF"
                "2" -> "APR"
                "3" -> "DROP-OUT"
                else -> currentItem.RPT_DVCD
            }

            holder.titleTextView.text = "${currentItem.YEAR} $reportType"
            holder.approvedDateTextView.text = "Approved Date : ${currentItem.APRV_DT?.convertDateFormat() ?: "-"}"
            holder.statusTextView.text = "Status : ${currentItem.RPT_STNM}"

            if (currentItem.RPT_STCD == "16") { // NR
                holder.containerView.backgroundColorResource = R.color.colorBgAccent
                holder.titleTextView.textColorResource = R.color.colorWhite
                holder.approvedDateTextView.textColorResource = R.color.colorWhite
                holder.statusTextView.textColorResource = R.color.colorWhite

                holder.refreshImageView.imageResource = R.drawable.delete_2
                holder.thumbnailImageView.imageResource = R.drawable.add
                holder.detailToggleImageView.visibility = View.GONE
            } else {
                holder.containerView.backgroundColorResource = R.color.colorLightGray
                holder.titleTextView.setTextColor(holder.hiddenTextView.textColors)
                holder.approvedDateTextView.setTextColor(holder.hiddenTextView.textColors)
                holder.statusTextView.setTextColor(holder.hiddenTextView.textColors)

                if (currentItem.RPT_STCD == "12") {
                    holder.refreshImageView.imageResource = R.drawable.delete_2
                } else {
                    holder.refreshImageView.imageResource = R.drawable.re_1
                }

                if (currentItem.RPT_DVCD == "3") {
                    holder.detailToggleImageView.visibility = View.GONE
                }
                else {
                    holder.detailToggleImageView.visibility = View.VISIBLE
                }

                var thumbnail: File? = null
                currentItem.THUMB_FILE_PATH?.apply {
                    thumbnail = File(targetDir, this)
                    if (!thumbnail!!.exists()) thumbnail = null
                }
                if (thumbnail == null) {
                    holder.thumbnailImageView.imageResource = R.drawable.m_childlist
                } else {
                    Glide.with(holder.containerView).load(thumbnail)
                            .apply(RequestOptions.skipMemoryCacheOf(true))
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                            .into(holder.thumbnailImageView)
                }

                var profileImage: File? = null
                currentItem.GENERAL_FILE_PATH?.apply {
                    profileImage = File(targetDir, this)
                    if (!profileImage!!.exists()) profileImage = null
                }
                if (profileImage == null) {
                    holder.profileImageView.imageResource = R.drawable.icon_2
                } else {
                    Glide.with(holder.containerView).load(profileImage)
                            .apply(RequestOptions.skipMemoryCacheOf(true))
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                            .into(holder.profileImageView)
                }

                holder.childNameTextView.text = currentItem.CHILD_NAME
                holder.birthDateTextView.text = currentItem.BDAY?.convertDateFormat() ?: "-"
                holder.genderTextView.text = currentItem.GNDR ?: "-"
                holder.addressTextView.text = "${currentItem.HS_ADDR ?: ""} ${currentItem.HS_ADDR_DTL ?: ""}"
                holder.disabilityTextView.text = "${currentItem.DISB_NM ?: ""} ${currentItem.ILNS_NM ?: ""}"
                holder.schoolTextView.text = "${currentItem.SCTP_NM ?: ""} ${currentItem.SCHL_NM ?: ""} ${currentItem.GRAD ?: ""}"

                val family = ArrayList<String>()
                if (currentItem.FA_LTYN == "Y") {
                    family.add("Father")
                }
                if (currentItem.MO_LTYN == "Y") {
                    family.add("Mother")
                }

                //val brother = currentItem.EBRO_LTNUM
                //if (!brother.isNullOrBlank() && brother.isNumber() && brother.toInt() > 0) {
                //    family.add("Brother($brother)")
                //}
                val eBrother = currentItem.EBRO_LTNUM?.toIntOrNull()?:0
                val yBrother = currentItem.YBRO_LTNUM?.toIntOrNull()?:0
                val brother = eBrother + yBrother
                if (brother > 0) family.add("Brother($brother)")

                //val sister = currentItem.ESIS_LTNUM
                //if (!sister.isNullOrBlank() && sister.isNumber() && sister.toInt() > 0) {
                //    family.add("Sister($sister)")
                //}
                val eSister = currentItem.ESIS_LTNUM?.toIntOrNull()?:0
                val ySister = currentItem.YSIS_LTNUM?.toIntOrNull()?:0
                val sister = eSister + ySister
                if (sister > 0) family.add("Sister($sister)")

                holder.familyTextView.text = family.joinToString(",")

                holder.villageTextView.text = currentItem.VLG_NM ?: ""

                holder.remarkTextView.text = currentItem.REMRK_ENG ?: ""

                val cases = ArrayList<String>()
                currentItem.CASE1_NM?.apply { cases.add(this) }
                currentItem.CASE2_NM?.apply { cases.add(this) }
                currentItem.CASE3_NM?.apply { cases.add(this) }
                if (cases.isNotEmpty()) holder.specialCaseTextView.text = cases.joinToString(", ")
                else holder.specialCaseTextView.text = ""

                val siblings = ArrayList<String>()
                currentItem.SIBLING1?.apply { siblings.add(this) }
                currentItem.SIBLING2?.apply { siblings.add(this) }
                if (siblings.isNotEmpty()) holder.siblingTextView.text = siblings.joinToString(", ")
                else holder.siblingTextView.text = ""

                holder.bmiTextView.text = currentItem.BMI_NM

                if (currentItem.AGE?.compareTo(18) ?: -1 > -1 || currentItem.PLAN_YN != null) {
                    when(currentItem.PLAN_YN) {
                        "Y"-> {
                            holder.futurePlanTitleTextView.textResource = R.string.label_future_plan
                            holder.detailPlanTitleTextView.textResource = R.string.label_detail_plan
                            holder.futurePlanTextView.text = currentItem.FTPLN_NM
                            holder.detailPlanTextView.text = currentItem.FTPLN_DTL
                        }
                        "N"-> {
                            holder.futurePlanTitleTextView.textResource = R.string.label_continue_spon_reason
                            holder.detailPlanTitleTextView.textResource = R.string.label_detail_reason
                            holder.futurePlanTextView.text = currentItem.CTNSPN_RNNM
                            holder.detailPlanTextView.text = currentItem.CTNSPN_DTL
                        }
                        else -> {
                            holder.futurePlanTitleTextView.textResource = R.string.label_future_plan
                            holder.detailPlanTitleTextView.textResource = R.string.label_detail_plan
                            holder.futurePlanTextView.text = ""
                            holder.detailPlanTextView.text = ""
                        }
                    }

                    holder.futurePlanContainer.visibility = View.VISIBLE
                    holder.detailPlanContainer.visibility = View.VISIBLE
                } else {
                    holder.futurePlanContainer.visibility = View.GONE
                    holder.detailPlanContainer.visibility = View.GONE
                }
            }

            if (currentItem.IS_SELECTED == true) {
                holder.detailsContainerView.visibility = View.VISIBLE
            } else {
                holder.detailsContainerView.visibility = View.GONE
            }

        }

        inner class ListAdaptorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val hiddenTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_COLOR)!!

            val containerView = itemView.findViewById<ViewGroup>(ListViewHolderUI.ID_CONTAINER)!!
            val titleContainerView = itemView.findViewById<ViewGroup>(ListViewHolderUI.ID_TITLE_CONTAINER)!!
            val detailsContainerView = itemView.findViewById<ViewGroup>(ListViewHolderUI.ID_DETAILS_CONTAINER)!!

            val titleTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_TITLE)!!
            val approvedDateTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_APPROVED_DATE)!!
            val statusTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_STATUS)!!

            val profileImageView = itemView.findViewById<ImageView>(ListViewHolderUI.ID_PROFILE_IMAGE)!!
            val specialCaseTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_SPECIAL_CASE)!!
            val childNameTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_CHILD_NAME)!!
            val birthDateTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_BIRTH_DATE)!!
            val genderTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_GENDER)!!
            val bmiTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_BMI)!!
            val villageTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_VILLAGE)!!
            val addressTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_ADDRESS)!!
            val disabilityTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_DISABILITY)!!
            val schoolTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_SCHOOL)!!
            val familyTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_FAMILY)!!
            val siblingTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_SIBLING)!!
            val remarkTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_REMARK)!!

            val futurePlanContainer = itemView.findViewById<ViewGroup>(ListViewHolderUI.ID_FUTURE_PLAN_CONTAINER)!!
            val futurePlanTitleTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_FUTURE_PLAN_TITLE)!!
            val futurePlanTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_FUTURE_PLAN)!!

            val detailPlanContainer = itemView.findViewById<ViewGroup>(ListViewHolderUI.ID_DETAIL_PLAN_CONTAINER)!!
            val detailPlanTitleTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_DETAIL_PLAN_TITLE)!!
            val detailPlanTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_DETAIL_PLAN)!!

            val refreshImageView = itemView.findViewById<ImageView>(ListViewHolderUI.ID_REFRESH)!!
            val thumbnailImageView = itemView.findViewById<ImageView>(ListViewHolderUI.ID_THUMBNAIL)!!
            val detailToggleImageView = itemView.findViewById<ImageView>(ListViewHolderUI.ID_DETAIL_TOGGLE)!!
        }

        class ListViewHolderUI : AnkoComponent<ViewGroup> {
            companion object {
                const val ID_COLOR = 0

                const val ID_CONTAINER = 1
                const val ID_TITLE = 2
                const val ID_APPROVED_DATE = 3
                const val ID_STATUS = 4

                const val ID_SPECIAL_CASE = 5
                const val ID_CHILD_NAME = 6
                const val ID_BIRTH_DATE = 7
                const val ID_GENDER = 8
                const val ID_BMI = 9
                const val ID_VILLAGE = 10
                const val ID_ADDRESS = 11
                const val ID_DISABILITY = 12
                const val ID_SCHOOL = 13
                const val ID_FAMILY = 14
                const val ID_SIBLING = 15
                const val ID_REMARK = 16

                const val ID_FUTURE_PLAN_CONTAINER = 17
                const val ID_FUTURE_PLAN_TITLE = 18
                const val ID_FUTURE_PLAN = 19
                const val ID_DETAIL_PLAN_CONTAINER = 20
                const val ID_DETAIL_PLAN_TITLE = 21
                const val ID_DETAIL_PLAN = 22

                const val ID_TITLE_CONTAINER = 23
                const val ID_DETAILS_CONTAINER = 24
                const val ID_PROFILE_IMAGE = 25

                const val ID_REFRESH = 100
                const val ID_THUMBNAIL = 101
                const val ID_DETAIL_TOGGLE = 102
            }

            override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                verticalLayout {
                    lparams(matchParent, wrapContent) {
                        topMargin = dimen(R.dimen.px20)
                    }
                    id = ID_CONTAINER
                    padding = dimen(R.dimen.px20)
                    gravity = Gravity.CENTER_VERTICAL

                    textView {
                        id = ID_COLOR
                        visibility = View.GONE
                    }

                    linearLayout {
                        imageView {
                            id = ID_REFRESH
                        }.lparams(width = dimen(R.dimen.px70), height = dimen(R.dimen.px70)) {
                            gravity = Gravity.CENTER_VERTICAL
                        }
                        space { }.lparams(width = dimen(R.dimen.px20))
                        circleImageView {
                            id = ID_THUMBNAIL
                        }.lparams(width = dimen(R.dimen.px88), height = dimen(R.dimen.px88)) {
                            gravity = Gravity.CENTER_VERTICAL
                        }
                        space { }.lparams(width = dimen(R.dimen.px20))
                        verticalLayout {
                            id = ID_TITLE_CONTAINER
                            textView { id = ID_TITLE }
                            textView { id = ID_APPROVED_DATE }
                            textView { id = ID_STATUS }
                        }.lparams(width = 0, weight = 1f)
                        imageView {
                            id = ID_DETAIL_TOGGLE
                            imageResource = R.drawable.select_4
                        }.lparams(width = dimen(R.dimen.px35)) {
                            gravity = Gravity.CENTER_VERTICAL
                        }
                    }

                    verticalLayout {
                        id = ID_DETAILS_CONTAINER
                        visibility = View.GONE

                        imageView {
                            id = ID_PROFILE_IMAGE
                            adjustViewBounds = true
                            padding = dimen(R.dimen.px40)
                        }.lparams {
                            gravity = Gravity.CENTER
                        }

                        linearLayout {
                            minimumHeight = dimen(R.dimen.px70)

                            textView(R.string.label_special_case) {
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView {
                                id = ID_SPECIAL_CASE
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f)
                        }.lparams(width = matchParent, height = wrapContent)

                        linearLayout {
                            minimumHeight = dimen(R.dimen.px70)

                            textView("* " + resources.getString(R.string.label_child_name)) {
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView {
                                id = ID_CHILD_NAME
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f)
                        }.lparams(width = matchParent, height = wrapContent) {
                            topMargin = dip(-1)
                        }

                        linearLayout {
                            minimumHeight = dimen(R.dimen.px70)

                            textView("* " + resources.getString(R.string.label_birthdate)) {
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView {
                                id = ID_BIRTH_DATE
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f)
                        }.lparams(width = matchParent, height = wrapContent) {
                            topMargin = dip(-1)
                        }

                        linearLayout {
                            minimumHeight = dimen(R.dimen.px70)

                            textView("* " + resources.getString(R.string.label_gender)) {
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView {
                                id = ID_GENDER
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f)
                        }.lparams(width = matchParent, height = wrapContent) {
                            topMargin = dip(-1)
                        }

                        linearLayout {
                            minimumHeight = dimen(R.dimen.px70)

                            textView(R.string.label_level_of_health) {
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView {
                                id = ID_BMI
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f)
                        }.lparams(width = matchParent, height = wrapContent) {
                            topMargin = dip(-1)
                        }

                        linearLayout {
                            minimumHeight = dimen(R.dimen.px70)

                            textView("* " + resources.getString(R.string.label_village)) {
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView {
                                id = ID_VILLAGE
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f)
                        }.lparams(width = matchParent, height = wrapContent) {
                            topMargin = dip(-1)
                        }

                        linearLayout {
                            minimumHeight = dimen(R.dimen.px70)

                            textView("* " + resources.getString(R.string.label_address)) {
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView {
                                id = ID_ADDRESS
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f)
                        }.lparams(width = matchParent, height = wrapContent) {
                            topMargin = dip(-1)
                        }

                        linearLayout {
                            minimumHeight = dimen(R.dimen.px70)

                            textView("* " + resources.getString(R.string.label_disabillity_illness)) {
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView {
                                id = ID_DISABILITY
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f)
                        }.lparams(width = matchParent, height = wrapContent) {
                            topMargin = dip(-1)
                        }

                        linearLayout {
                            minimumHeight = dimen(R.dimen.px70)

                            textView("* " + resources.getString(R.string.label_school_information)) {
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView {
                                id = ID_SCHOOL
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f)
                        }.lparams(width = matchParent, height = wrapContent) {
                            topMargin = dip(-1)
                        }

                        linearLayout {
                            minimumHeight = dimen(R.dimen.px70)

                            textView("* " + resources.getString(R.string.label_family_information)) {
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView {
                                id = ID_FAMILY
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f)
                        }.lparams(width = matchParent, height = wrapContent) {
                            topMargin = dip(-1)
                        }

                        linearLayout {
                            minimumHeight = dimen(R.dimen.px70)

                            textView(R.string.label_sibling_sponsorship) {
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView {
                                id = ID_SIBLING
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f)
                        }.lparams(width = matchParent, height = wrapContent) {
                            topMargin = dip(-1)
                        }

                        linearLayout {
                            id = ID_FUTURE_PLAN_CONTAINER
                            minimumHeight = dimen(R.dimen.px70)

                            textView(R.string.label_future_plan) {
                                id = ID_FUTURE_PLAN_TITLE
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                                textColorResource = R.color.colorAccent
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView {
                                id = ID_FUTURE_PLAN
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f)
                        }.lparams(width = matchParent, height = wrapContent) {
                            topMargin = dip(-1)
                        }

                        linearLayout {
                            id = ID_DETAIL_PLAN_CONTAINER
                            minimumHeight = dimen(R.dimen.px70)

                            textView(R.string.label_detail_plan) {
                                id = ID_DETAIL_PLAN_TITLE
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                                textColorResource = R.color.colorAccent
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView {
                                id = ID_DETAIL_PLAN
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f)
                        }.lparams(width = matchParent, height = wrapContent) {
                            topMargin = dip(-1)
                        }

                        linearLayout {
                            minimumHeight = dimen(R.dimen.px70)

                            textView(R.string.label_remark) {
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView {
                                id = ID_REMARK
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f)
                        }.lparams(width = matchParent, height = wrapContent) {
                            topMargin = dip(-1)
                        }
                    }
                }
            }
        }
    }
}
