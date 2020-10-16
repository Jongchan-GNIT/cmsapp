package kr.goodneighbors.cms.ui.childlist


import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import com.google.gson.GsonBuilder
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.extensions.getStringValue
import kr.goodneighbors.cms.extensions.getValue
import kr.goodneighbors.cms.extensions.observeOnce
import kr.goodneighbors.cms.extensions.setItem
import kr.goodneighbors.cms.extensions.setSelectKey
import kr.goodneighbors.cms.extensions.toDateFormat
import kr.goodneighbors.cms.extensions.viewsRecursive
import kr.goodneighbors.cms.service.entities.DROPOUT
import kr.goodneighbors.cms.service.entities.INTV
import kr.goodneighbors.cms.service.entities.REMRK
import kr.goodneighbors.cms.service.entities.RPT_BSC
import kr.goodneighbors.cms.service.entities.RPT_DIARY
import kr.goodneighbors.cms.service.model.DropoutEditItem
import kr.goodneighbors.cms.service.model.DropoutEditSearchItem
import kr.goodneighbors.cms.service.viewmodel.ProfileViewModel
import kr.goodneighbors.cms.ui.BaseActivityFragment
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.editText
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.scrollView
import org.jetbrains.anko.spinner
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.view
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

@Suppress("PrivatePropertyName")
class DropoutFragment : BaseActivityFragment() {
    companion object {
        fun newInstance(chrcp_no: String, rcp_no: String?= null): DropoutFragment {
            val fragment = DropoutFragment()
            val args = Bundle()
            args.putString("chrcp_no", chrcp_no)
            args.putString("rcp_no", rcp_no ?: "")

            fragment.arguments = args
            return fragment
        }
    }

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(DropoutFragment::class.java)
    }

    private val ui = FragmentUI()

    private val viewModel: ProfileViewModel by lazy {
        ProfileViewModel()
    }

    private lateinit var chrcp_no: String
    private lateinit var rcp_no: String

    private var editItem: DropoutEditItem ?= null
    private var isEditable = true
    private var returnCode: String ?= null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chrcp_no = arguments!!.getString("chrcp_no", "")
        rcp_no = arguments!!.getString("rcp_no", "")

        viewModel.getDropoutEditViewItem().observe(this, Observer { dropoutEditItem ->
            editItem = dropoutEditItem

            dropoutEditItem?.apply {
                ui.dropoutReasonSpinner.setItem(dropoutReasonList, hint = "Select Reason")
                ui.deathReasonSpinner.setItem(deathReasonList, true)
                ui.illnessSpinner.setItem(illnessList, true)
                ui.disabilitySpinner.setItem(disabilityList, true)
                ui.jobSpinner.setItem(jobList, true)

                if (rpt_bsc?.RPT_STCD ?: "" == "2" || rpt_bsc?.RPT_STCD ?: "" == "15") {
                    returnCode = rpt_bsc!!.RPT_STCD

                    ui.returnContainer.visibility = View.VISIBLE
                    when(rpt_bsc?.RPT_STCD) {
                        "2"-> {
                            ui.returnRemarkTitleTextView.textResource = R.string.message_return_remark_ihq
                        }
                        "15"-> {
                            ui.returnRemarkTitleTextView.textResource = R.string.message_return_remark_ho
                        }
                    }

                    returns?.let {
                        AnkoContext.createDelegate(ui.returnItemsContainer).apply {
                            returns?.forEachIndexed { index, returnItem ->
                                textView("${index + 1}. ${returnItem.RTRN_BCD_LABEL}")
                                val returns = ArrayList<String>()
                                if (!returnItem.RTRN_SCD_LABEL.isNullOrBlank()) returns.add(returnItem.RTRN_SCD_LABEL)
                                if (!returnItem.RTRN_DETL.isNullOrBlank()) returns.add(returnItem.RTRN_DETL)

                                textView(returns.joinToString(" -> "))

                                when(returnItem.RTRN_BCD) {
                                    "1" -> {
                                        ui.dropoutReasonTitleTextView.textColorResource = R.color.colorAccent
                                    }
                                    "2"-> {
                                        ui.distanceOfMovementTitleTextView.textColorResource = R.color.colorAccent
                                        ui.statePlaceTitleTextView.textColorResource = R.color.colorAccent
                                    }
                                    "3"-> {
                                        ui.causeOfDathTitleTextView.textColorResource = R.color.colorAccent
                                        ui.employmentTitleTextView.textColorResource = R.color.colorAccent
                                    }
                                }
                            }
                        }
                    }
                }
                else {
                    ui.returnContainer.visibility = View.GONE
                }

                rpt_bsc?.RPT_STCD?.apply {
                    isEditable = this == "15"
                }

                rpt_bsc?.DROPOUT?.apply {
                    ui.dropoutReasonSpinner.setSelectKey(DROP_RNCD)
                    Handler().postDelayed({
                        ui.deathReasonSpinner.setSelectKey(DTH_RNCD)
                        Handler().postDelayed({
                            when (DTH_RNCD) {
                                "338002" -> {// 338002	질병
                                    ui.illnessSpinner.setSelectKey(DTH_DTL_RNCD)
                                }
                                "338003" -> {// 338003	장애
                                    ui.disabilitySpinner.setSelectKey(DTH_DTL_RNCD)
                                }
                            }

                            ui.jobSpinner.setSelectKey(WORK_CD)
                            ui.distanceEditText.setText(MVLC_DIST ?: "")
                            ui.placeNameEditText.setText(MVLC_NM ?: "")
                        }, 100)
                    }, 100)

                }

                rpt_bsc?.REMRK?.apply {
                    ui.remarkEditText.setText(REMRK_ENG?:"")
                }

                if (isEditable && ch_mst?.CH_STCD != "1") isEditable = false

                if (!isEditable) {
                    ui.container.viewsRecursive.filter { it is EditText || it is Spinner || it is Switch || it is Button }.forEach {
                        it.isEnabled = false
                    }
                }
                else {
                    setHasOptionsMenu(true)
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = ui.createView(AnkoContext.create(requireContext(), this))

        activity?.title = "Drop-out"

        ui.dropoutReasonSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            @SuppressLint("SetTextI18n")
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                logger.debug("onCreateView : continueReasonSpinner.onItemSelected : ${ui.dropoutReasonSpinner.getValue()}")

                when (ui.dropoutReasonSpinner.getValue()) {
                    // 9	이사
                    "1", "9" -> {
                        ui.moveLayout.visibility = View.VISIBLE
                        ui.deathReasonLayout.visibility = View.GONE
                        ui.employmentLayout.visibility = View.GONE

                        ui.remarkTitleTextView.text = "*4. " + getString(R.string.label_remark)
                    }

                    // 8	사망
                    "8" -> {
                        ui.deathReasonSpinner.setSelection(0)
                        ui.illnessSpinner.setSelection(0)
                        ui.disabilitySpinner.setSelection(0)

                        ui.moveLayout.visibility = View.GONE
                        ui.deathReasonLayout.visibility = View.VISIBLE
                        ui.illnessLayout.visibility = View.GONE
                        ui.disabilityLayout.visibility = View.GONE
                        ui.employmentLayout.visibility = View.GONE

                        ui.remarkTitleTextView.text = "*3. " + getString(R.string.label_remark)
                    }

                    // 취직 2	취직
                    "2" -> {
                        ui.jobSpinner.setSelection(0)

                        ui.moveLayout.visibility = View.GONE
                        ui.deathReasonLayout.visibility = View.GONE
                        ui.employmentLayout.visibility = View.VISIBLE

                        ui.remarkTitleTextView.text = "*3. " + getString(R.string.label_remark)
                    }

                    else -> {
                        ui.moveLayout.visibility = View.GONE
                        ui.deathReasonLayout.visibility = View.GONE
                        ui.employmentLayout.visibility = View.GONE

                        ui.remarkTitleTextView.text = "*2. " + getString(R.string.label_remark)
                    }
                }
            }
        }

        ui.deathReasonSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            @SuppressLint("SetTextI18n")
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                logger.debug("onCreateView : deathReasonSpinner.onItemSelected : ${ui.deathReasonSpinner.getValue()}")

                ui.illnessSpinner.setSelection(0)
                ui.disabilitySpinner.setSelection(0)

                when (ui.deathReasonSpinner.getValue()) {
                    // 338002	질병
                    "338002" -> {
                        ui.illnessLayout.visibility = View.VISIBLE
                        ui.disabilityLayout.visibility = View.GONE
                    }

                    // 338003	장애
                    "338003" -> {
                        ui.illnessLayout.visibility = View.GONE
                        ui.disabilityLayout.visibility = View.VISIBLE
                    }

                    else -> {
                        ui.illnessLayout.visibility = View.GONE
                        ui.disabilityLayout.visibility = View.GONE
                    }
                }
            }
        }

        viewModel.setDropoutEditViewItemSearch(DropoutEditSearchItem(chrcp_no = chrcp_no, rcp_no = rcp_no))

        return v
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        logger.debug("onCreateOptionsMenu")
        activity?.menuInflater?.inflate(R.menu.toolbar_cif, menu)

        // 저장 버튼 클릭
        menu?.findItem(R.id.cif_toolbar_save)!!.setOnMenuItemClickListener {
            save()
            true
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun save() {
        val dropoutReason: String? = ui.dropoutReasonSpinner.getValue()?.let { if (it.isBlank()) null else it }
        val deathReason: String? = ui.deathReasonSpinner.getValue()?.let { if (it.isBlank()) null else it }
        val illness: String? = ui.illnessSpinner.getValue()?.let { if (it.isBlank()) null else it }
        val disability: String? = ui.disabilitySpinner.getValue()?.let { if (it.isBlank()) null else it }
        val job: String? = ui.jobSpinner.getValue()?.let { if (it.isBlank()) null else it }
        val distance: String? = ui.distanceEditText.getStringValue().let { if (it.isBlank()) null else it }
        val placeName: String? = ui.placeNameEditText.getStringValue().let { if (it.isBlank()) null else it }
        val remark: String? = ui.remarkEditText.getStringValue().let { if (it.isBlank()) null else it }


        var isValid = true
        if (dropoutReason.isNullOrBlank()) {
            isValid = false
        }

        // 사망
        if (ui.deathReasonLayout.visibility == View.VISIBLE) {
            if (deathReason.isNullOrBlank()) {
                isValid = false
            }

            if (ui.illnessLayout.visibility == View.VISIBLE) {
                if (illness.isNullOrBlank()) {
                    isValid = false
                }
            }
            if (ui.disabilityLayout.visibility == View.VISIBLE) {
                if (disability.isNullOrBlank()) {
                    isValid = false
                }
            }
        }

        // 취업
        if (ui.employmentLayout.visibility == View.VISIBLE && job.isNullOrBlank()) {
            isValid = false
        }

        // 이사
        if (ui.moveLayout.visibility == View.VISIBLE) {
            if (distance.isNullOrBlank()) {
                isValid = false
            }

            if (placeName.isNullOrBlank()) {
                isValid = false
            }
        }

        if (remark.isNullOrBlank() || remark.length < 200) {
            isValid = false
        }

        if (!isValid) {
            toast(R.string.message_require_fields)
            return
        }

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val userid = sharedPref.getString("userid", "")
        val timestamp = Date().time

        val report = editItem!!.rpt_bsc ?: RPT_BSC(CHRCP_NO = chrcp_no, RCP_NO = "${chrcp_no}3", RPT_DVCD = "3")

        if (report.REG_DT == null) {
            report.YEAR = timestamp.toDateFormat("YYYY")
            report.REG_DT = timestamp
            report.REGR_ID = userid
            report.DEL_YN = "N"
            report.DEGR = 1
            report.EXPT_YN = "N"
            report.PSCRN_YN = "N"
            report.XSCRN_YN = "N"
            report.FRCP_NO = report.RCP_NO
            report.DCMT_YN = "N"
            report.FIDG_YN = "Y"

            report.RPT_DIARY = RPT_DIARY(RCP_NO = report.RCP_NO)
        } else {
            report.UPD_DT = timestamp
            report.UPDR_ID = userid
        }
        report.RPT_STCD = "13" //if (returnCode == null) "13" else returnCode
        report.LAST_UPD_DT = timestamp
        report.APP_MODIFY_DATE = timestamp

        report.DROPOUT = DROPOUT(RCP_NO = report.RCP_NO, DROP_RNCD = dropoutReason, MVLC_DIST = distance, MVLC_NM = placeName,
                DROP_DT = report.REG_DT!!.toDateFormat("YYYYMMdd"), DTH_RNCD = deathReason, DTH_DTL_RNCD = illness
                ?: disability, WORK_CD = job)

        report.REMRK = REMRK(RCP_NO = report.RCP_NO, REMRK_ENG = remark)
        report.INTV = INTV(RCP_NO = report.RCP_NO, INTVR_NM = userid, INTV_DT = report.REG_DT!!.toDateFormat("YYYYMMdd"))

        logger.debug(GsonBuilder().create().toJson(report))

        viewModel.saveDropout(report).observeOnce(this, Observer {
            if (it != null && it == true) {
                toast(R.string.message_data_has_been_saved)
                activity!!.onBackPressed()
            }
        })
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<DropoutFragment> {
        lateinit var container: LinearLayout

        lateinit var moveLayout: ViewGroup
        lateinit var deathReasonLayout: ViewGroup
        lateinit var illnessLayout: ViewGroup
        lateinit var disabilityLayout: ViewGroup
        lateinit var employmentLayout: ViewGroup

        lateinit var remarkTitleTextView: TextView

        lateinit var dropoutReasonSpinner: Spinner


        lateinit var deathReasonSpinner: Spinner
        lateinit var illnessSpinner: Spinner
        lateinit var disabilitySpinner: Spinner
        lateinit var jobSpinner: Spinner

        lateinit var distanceEditText: EditText
        lateinit var placeNameEditText: EditText

        lateinit var remarkEditText: EditText

        lateinit var returnContainer: LinearLayout
        lateinit var returnRemarkTitleTextView: TextView
        lateinit var returnItemsContainer: LinearLayout

        lateinit var dropoutReasonTitleTextView: TextView
        lateinit var distanceOfMovementTitleTextView: TextView
        lateinit var statePlaceTitleTextView: TextView
        lateinit var causeOfDathTitleTextView: TextView
        lateinit var employmentTitleTextView: TextView

        override fun createView(ui: AnkoContext<DropoutFragment>) = with(ui) {
            verticalLayout {
                returnContainer = verticalLayout {
                    padding = dip(15)
                    backgroundColorResource = R.color.colorReturnBackground
                    visibility = View.GONE

                    returnRemarkTitleTextView = textView {
                        typeface = Typeface.DEFAULT_BOLD
                    }
                    view {
                        backgroundColorResource = R.color.colorSplitLine
                    }.lparams(width = matchParent, height = dip(1)) {
                        topMargin = dip(10)
                        bottomMargin = dip(10)
                    }
                    returnItemsContainer = verticalLayout {

                    }
                }

                scrollView {
                    container = verticalLayout {
                        padding = dimen(R.dimen.px20)

                        dropoutReasonTitleTextView = textView("*1. " + owner.getString(R.string.label_drop_out_reason)) {
                            gravity = Gravity.CENTER_VERTICAL
                            setTypeface(null, Typeface.BOLD)
                        }.lparams(width = matchParent, height = dimen(R.dimen.px70))
                        dropoutReasonSpinner = spinner { }

                        // 이사
                        moveLayout = verticalLayout {
                            visibility = View.GONE

                            distanceOfMovementTitleTextView = textView("*2. " + owner.getString(R.string.label_how_far_place)) {
                                gravity = Gravity.CENTER_VERTICAL
                                setTypeface(null, Typeface.BOLD)
                            }.lparams(width = matchParent, height = dimen(R.dimen.px70))
                            distanceEditText = editText {
                                inputType = InputType.TYPE_CLASS_NUMBER
                            }

                            statePlaceTitleTextView = textView("*3. " + owner.getString(R.string.label_name_of_place)) {
                                gravity = Gravity.CENTER_VERTICAL
                                setTypeface(null, Typeface.BOLD)
                            }.lparams(width = matchParent, height = dimen(R.dimen.px70))
                            placeNameEditText = editText { }
                        }

                        // 사망
                        deathReasonLayout = verticalLayout {
                            visibility = View.GONE
                            causeOfDathTitleTextView = textView("*2. " + owner.getString(R.string.label_cause_of_death)) {
                                gravity = Gravity.CENTER_VERTICAL
                                setTypeface(null, Typeface.BOLD)
                            }.lparams(width = matchParent, height = dimen(R.dimen.px70))
                            deathReasonSpinner = spinner { }

                            illnessLayout = verticalLayout {
                                leftPadding = dimen(R.dimen.px20)
                                visibility = View.GONE
                                textView("*2-1. " + owner.getString(R.string.label_kind_of_illness)) {
                                    gravity = Gravity.CENTER_VERTICAL
                                    setTypeface(null, Typeface.BOLD)
                                }.lparams(width = matchParent, height = dimen(R.dimen.px70))
                                illnessSpinner = spinner { }
                            }
                            disabilityLayout = verticalLayout {
                                leftPadding = dimen(R.dimen.px20)
                                visibility = View.GONE
                                textView("*2-1. " + owner.getString(R.string.label_kind_of_disability)) {
                                    gravity = Gravity.CENTER_VERTICAL
                                    setTypeface(null, Typeface.BOLD)
                                }.lparams(width = matchParent, height = dimen(R.dimen.px70))
                                disabilitySpinner = spinner { }
                            }
                        }

                        // 취업
                        employmentLayout = verticalLayout {
                            visibility = View.GONE
                            employmentTitleTextView = textView("*2. " + owner.getString(R.string.label_child_working_as)) {
                                gravity = Gravity.CENTER_VERTICAL
                                setTypeface(null, Typeface.BOLD)
                            }.lparams(width = matchParent, height = dimen(R.dimen.px70))
                            jobSpinner = spinner { }
                        }

                        remarkTitleTextView = textView("*2. " + owner.getString(R.string.label_remark)) {
                            gravity = Gravity.CENTER_VERTICAL
                            setTypeface(null, Typeface.BOLD)
                        }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                        remarkEditText = editText {
                            backgroundResource = R.drawable.layout_border
                            gravity = Gravity.TOP
                            minLines = 8
                            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE

                            filters = arrayOf(InputFilter.LengthFilter(2000))
                        }
                    }
                }.lparams(width = matchParent, height = 0, weight = 1f)
            }
        }
    }
}
