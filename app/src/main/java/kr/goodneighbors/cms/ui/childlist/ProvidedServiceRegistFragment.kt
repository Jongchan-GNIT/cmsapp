package kr.goodneighbors.cms.ui.childlist


import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.arch.lifecycle.Observer
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.preference.PreferenceManager
import android.telephony.TelephonyManager
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.extensions.getStringValue
import kr.goodneighbors.cms.extensions.observeOnce
import kr.goodneighbors.cms.extensions.viewsRecursive
import kr.goodneighbors.cms.service.entities.SRVC
import kr.goodneighbors.cms.service.model.ProvidedServiceRegistCheckedItem
import kr.goodneighbors.cms.service.model.ProvidedServiceRegistItem
import kr.goodneighbors.cms.service.model.ProvidedServiceRegistSearchItem
import kr.goodneighbors.cms.service.viewmodel.ProvidedServiceViewModel
import kr.goodneighbors.cms.ui.BaseActivityFragment
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.button
import org.jetbrains.anko.checkBox
import org.jetbrains.anko.dimen
import org.jetbrains.anko.editText
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.scrollView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent
import java.text.SimpleDateFormat
import java.util.*

class ProvidedServiceRegistFragment : BaseActivityFragment() {
    companion object {
        const val REQUEST_CODE = 99
        const val TAG_ITEM_ID = 0

        fun newInstance(chrcp_no: String): ProvidedServiceRegistFragment {
            val fragment = ProvidedServiceRegistFragment()
            val args = Bundle()
            args.putString("chrcp_no", chrcp_no)

            fragment.arguments = args
            return fragment
        }
    }

    private val ui = FragmentUI()

    private val viewModel: ProvidedServiceViewModel by lazy {
        ProvidedServiceViewModel()
    }

    private lateinit var chrcp_no: String
    private var date: String? = null

    private var checkedItem: ArrayList<ProvidedServiceRegistCheckedItem> ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        viewModel.getServiceRegistItem().observe(this, Observer {
            it?.apply {
                createItems(this)
            }
        })
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = ui.createView(AnkoContext.create(requireContext(), this))
        activity?.title = "Provided service"

        chrcp_no = arguments!!.getString("chrcp_no")


        viewModel.setServiceRegistItemSearch(ProvidedServiceRegistSearchItem(""))

        ui.searchImageView.onClick {
            checkedItem = ArrayList()
            ui.itemContainer.viewsRecursive.filter { it is CheckBox && it.isChecked }.forEach {
                if (it.tag != null && it.tag is ProvidedServiceRegistCheckedItem) {
                    checkedItem!!.add(it.tag as ProvidedServiceRegistCheckedItem)
                }
            }
            viewModel.setServiceRegistItemSearch(ProvidedServiceRegistSearchItem(search = ui.searchValueEditText.getStringValue(), checkedItems = checkedItem!!))

            try {
                context?.getSystemService(Context.INPUT_METHOD_SERVICE)?.let {
                    if (it is InputMethodManager) {
                        it.hideSoftInputFromWindow(view?.windowToken, 0)
                    }
                }
            }
            catch(e: Exception) {

            }
        }

        ui.selectDateButton.onClick {
            val c = Calendar.getInstance()
            val mYear = c.get(Calendar.YEAR)
            val mMonth = c.get(Calendar.MONTH)
            val mDay = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(activity,
                    DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                        date = "$year${(monthOfYear + 1).toString().padStart(2, '0')}${dayOfMonth.toString().padStart(2, '0')}"
                    }, mYear, mMonth, mDay)
            datePickerDialog.datePicker.maxDate = Date().time
            datePickerDialog.datePicker.minDate = SimpleDateFormat("yyyyMMdd").parse("${mYear - 1}0101").time

            datePickerDialog.show()
        }

        return v
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        activity?.menuInflater?.inflate(R.menu.toolbar_cif, menu)

        // 저장 버튼 클릭
        menu?.findItem(R.id.cif_toolbar_save)!!.setOnMenuItemClickListener {
            save()
            true
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun createItems(items: List<ProvidedServiceRegistItem>) {
        ui.itemContainer.removeAllViews()
        items.forEachIndexed { index, masterItem ->
            AnkoContext.createDelegate(ui.itemContainer).apply {
                verticalLayout {
                    lparams(width = matchParent, height = wrapContent)

                    linearLayout {
                        minimumHeight = dimen(R.dimen.px70)
                        leftPadding = dimen(R.dimen.px20)
                        rightPadding = dimen(R.dimen.px20)
                        gravity = Gravity.CENTER_VERTICAL

                        textView("${masterItem.master.CD_ENM}") {
                            gravity = Gravity.CENTER_VERTICAL
                            setTypeface(null, Typeface.BOLD)
                        }.lparams(width = 0, height = matchParent, weight = 1f)
                    }

                    verticalLayout {

                        masterItem.detailes.forEach { detail ->
                            linearLayout {
                                minimumHeight = dimen(R.dimen.px70)

                                val isChekcedItem = checkedItem?.filter {
                                    it.type == masterItem.master.REF_1!!
                                            && it.master_cd == masterItem.master.CD
                                            && it.detail_group_cd == detail.GRP_CD
                                            && it.detail_cd == detail.CD
                                }?.size ?: 0

                                checkBox {
                                    gravity = Gravity.CENTER
                                    tag = ProvidedServiceRegistCheckedItem(
                                            type = masterItem.master.REF_1!!,
                                            master_cd = masterItem.master.CD,
                                            detail_group_cd = detail.GRP_CD,
                                            detail_cd = detail.CD)

                                    if (isChekcedItem > 0) isChecked = true
                                }.lparams(width = dimen(R.dimen.px70), height = matchParent)

                                textView(detail.CD_ENM ?: "") {
                                    gravity = Gravity.CENTER_VERTICAL
                                }.lparams(height = matchParent)
                            }.lparams(width = matchParent, height = wrapContent)
                        }
                    }
                }
            }
        }
    }

    private fun save() {
        if (date.isNullOrBlank()) {
            toast(R.string.message_validate_select_provided_date)
            return
        }

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val userid = sharedPref.getString("userid", "")
        val timestamp = Date().time

        val srvcs = ArrayList<SRVC>()
        ui.itemContainer.viewsRecursive.filter { it is CheckBox && it.isChecked }.forEach {
            if (it.tag != null && it.tag is ProvidedServiceRegistCheckedItem) {
                val tag = it.tag as ProvidedServiceRegistCheckedItem
                srvcs.add(SRVC(
                        CHRCP_NO = chrcp_no,
                        SEQ_NO = -1,
                        SVOBJ_DVCD = tag.type,
                        BSST_CD = tag.master_cd,
                        SPBD_CD = tag.detail_cd,
                        PRVD_DT = date,

                        REGR_ID = userid,
                        REG_DT = timestamp,

                        CRT_TP = getDeviceIMEI()?:"",
                        DEL_YN = "N",
                        APP_MODIFY_DATE = timestamp))
            }

        }

        if (srvcs.isEmpty()) {
            toast(R.string.message_require_items_selected)
        }
        else {
            viewModel.saveServiceRegistItem(srvcs).observeOnce(this, Observer {
                activity!!.onBackPressed()
            })
        }
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

    @SuppressLint("MissingPermission")
    private fun getDeviceIMEI(): String? {
        val telephonyManager = activity?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        //return telephonyManager.imei
        return "358301090158015"
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<ProvidedServiceRegistFragment> {
        lateinit var itemContainer: LinearLayout

        lateinit var searchImageView: ImageView

        lateinit var searchValueEditText: EditText

        lateinit var selectDateButton: Button

        override fun createView(ui: AnkoContext<ProvidedServiceRegistFragment>) = with(ui) {

            verticalLayout {
                textView(R.string.label_service_record) {
                    leftPadding = dimen(R.dimen.px20)
                    gravity = Gravity.CENTER_VERTICAL
//                    textColorResource = R.color.colorBlack
                    setTypeface(null, Typeface.BOLD)

                }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                linearLayout {
                    padding = dimen(R.dimen.px20)

                    searchValueEditText = editText {inputType = InputType.TYPE_CLASS_TEXT}.lparams(width = 0, height = wrapContent, weight = 1f)

                    searchImageView = imageView {
                        imageResource = R.drawable.search
                    }.lparams(dimen(R.dimen.px76), dimen(R.dimen.px76))
                }

                scrollView {
                    itemContainer = verticalLayout {


                    }
                }.lparams(width = matchParent, height = 0, weight = 1f)

                selectDateButton = button(R.string.button_select_provided_date) {
                    allCaps = false
                }

                linearLayout {
                    gravity = Gravity.CENTER

                    button {
                        setBackgroundResource(R.drawable.gnb_child_off)
                        onClick {
                            owner.onChangeBottomNavigation(0)
                        }
                    }.lparams(width = 0, height = dimen(R.dimen.px112), weight = 1f)
                    button("Report") {
                        setBackgroundResource(R.drawable.gnb_bgl_off)
                        textColorResource = R.color.colorWhite
                        allCaps = false
                        onClick {
                            owner.onChangeBottomNavigation(1)
                        }
                    }.lparams(width = 0, height = dimen(R.dimen.px112), weight = 1f)
                    button("Provied Service") {
                        setBackgroundResource(R.drawable.gnb_bgl_on)
                        textColorResource = R.color.colorWhite
                        allCaps = false
//                            onClickListener {
//                                owner.onChangeBottomNavigation(2)
//                            }
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

    class ServiceItemUI(val index: Int) : AnkoComponent<ViewGroup> {
        override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
            verticalLayout {
                textView(index.toString())
            }
        }

    }
}
