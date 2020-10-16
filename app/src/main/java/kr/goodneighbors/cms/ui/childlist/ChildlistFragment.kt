@file:Suppress("ConstantConditionIf", "DEPRECATION")

package kr.goodneighbors.cms.ui.childlist

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.app.SearchManager
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import com.google.gson.GsonBuilder
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.common.Constants
import kr.goodneighbors.cms.extensions.getText
import kr.goodneighbors.cms.extensions.getValue
import kr.goodneighbors.cms.extensions.isNetworkAvailable
import kr.goodneighbors.cms.extensions.observeOnce
import kr.goodneighbors.cms.extensions.setItem
import kr.goodneighbors.cms.extensions.setSelectKey
import kr.goodneighbors.cms.extensions.setSelectValue
import kr.goodneighbors.cms.service.entities.APP_SEARCH_HISTORY
import kr.goodneighbors.cms.service.model.ChildlistItem
import kr.goodneighbors.cms.service.model.ChildlistSearchItem
import kr.goodneighbors.cms.service.model.SpinnerOption
import kr.goodneighbors.cms.service.viewmodel.ChildlistViewModel
import kr.goodneighbors.cms.service.viewmodel.CommonViewModel
import kr.goodneighbors.cms.ui.BaseActivityFragment
import kr.goodneighbors.cms.ui.MapsVillageActivity
import kr.goodneighbors.cms.ui.QrScanActivity
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.appcompat.v7.coroutines.onQueryTextFocusChange
import org.jetbrains.anko.appcompat.v7.coroutines.onQueryTextListener
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.button
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onItemSelectedListener
import org.jetbrains.anko.space
import org.jetbrains.anko.spinner
import org.jetbrains.anko.support.v4.indeterminateProgressDialog
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.topPadding
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.view
import org.jetbrains.anko.wifiManager
import org.jetbrains.anko.wrapContent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule


@Suppress("DEPRECATION")
class ChildlistFragment : BaseActivityFragment() {
    companion object {
        private const val REQUEST_MORE_SEARCH = 100
        private const val REQUEST_QR_CODE = 101

        fun newInstance(): ChildlistFragment {
            return ChildlistFragment()
        }
    }

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(ChildlistFragment::class.java)
    }

    private val commonViewModel: CommonViewModel by lazy {
        //        ViewModelProviders.of(this).get(CommonViewModel::class.java)
        CommonViewModel()
    }

    private val childlistViewModel: ChildlistViewModel by lazy {
        //        ViewModelProviders.of(this).get(ChildlistViewModel::class.java)
        ChildlistViewModel()
    }

    private val ui = FragmentUI()

    lateinit var progress: ProgressDialog

    private var currentService: String? = null

    private var currentSearchItem = ChildlistSearchItem()

    private var currentYear: String? = null

    private var menu: Menu? = null

    private var isScrollEffect = false

    override fun onCreate(savedInstanceState: Bundle?) {
        logger.debug("ChildlistFragment.onCreate($savedInstanceState)")

        commonViewModel.getCommonCodeItems().observe(this, Observer {
            it?.supportCountry?.apply {
                ui.supportSpinner.setItem(items = this, adapterResource = R.layout.spinneritem)
            }

            it?.service?.apply {
                ui.serviceSpinner.setItem(items = this, hint = "Service", adapterResource = R.layout.spinneritem)
            }

            it?.status?.apply {
                ui.statusSpinner.setItem(items = this, hint = "Status", adapterResource = R.layout.spinneritem)
            }

            childlistViewModel.getSearchOptions()?.let { searchItem ->
                logger.debug("childlistViewModel.getSearchOptions() = $searchItem")
                currentSearchItem = searchItem

                searchItem.support?.let { _it -> ui.supportSpinner.setSelectKey(_it) }
                searchItem.service?.let { _it -> ui.serviceSpinner.setSelectKey(_it) }
                searchItem.status?.let { _it -> ui.statusSpinner.setSelectKey(_it) }

                search(searchItem)
                true
            } ?: run {
                search()
            }
        })

        // 검색 결과
        childlistViewModel.getChildList().observe(this, Observer {
            logger.debug("ChildlistFragment.onViewCreated - childlistViewModel.getChildList().observe : ${it?.size}")

            ui.totalTextView.text = NumberFormat.getNumberInstance(Locale.US).format(it?.size ?: 0)

            ui.itemsRecyclerView.adapter = ChildlistAdapter(
                    context = requireContext(),
                    items = it ?: arrayListOf(),
                    onClickListener = { item -> onClickChildListener(item) },
                    onStatusClickListener = { item -> onClickStatusListener(item = item) },
                    onGMLStatusClickListener = { item, gml -> onGMLStatusClickListener(item, gml) }
            )

            ui.searchContainer.visibility = View.VISIBLE
            progress.dismiss()
        })

        // 상단 검색
        childlistViewModel.findAllSuggestions().observe(this, Observer {
            it?.apply {
                ui.suggestionRecyclerView.adapter = SuggestionAdapter(list = ArrayList(this),
                        onClickListener = { history ->
                            logger.debug("onClickListener : $history")
                            search(ChildlistSearchItem(searchText = history.word, service = ui.serviceSpinner.getValue()
                                    ?: "",
                                    support = ui.supportSpinner.getValue(),
                                    year = ui.yearSpinner.getValue()))

                            val searchItem = menu?.findItem(R.id.childlist_toolbar_search)
                            searchItem?.apply {
                                collapseActionView()
                            }
                        },
                        onRemoveClickListener = { history ->
                            logger.debug("onRemoveClickListener : $history")
                            childlistViewModel.deleteSuggestion(history.word)
                        })

                ui.suggestionContainer.visibility = View.VISIBLE
            }
        })


        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        logger.debug("ChildlistFragment.onCreateView($savedInstanceState)")
        val v = ui.createView(AnkoContext.create(requireContext(), this))

        setHasOptionsMenu(true)

        ui.serviceSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, _ ->
                createYearSpinner((ui.serviceSpinner.getValue() ?: ""))

                when (ui.serviceSpinner.getValue() ?: "") {
                    Constants.SERVICE_SERVICE -> { // service
                        logger.debug("serivce selected")
                        ui.yearSpinner.isEnabled = false
                        ui.statusSpinner.isEnabled = false
                        ui.moreImageView.isClickable = true
                    }
                    else -> {
                        ui.yearSpinner.isEnabled = true
                        ui.statusSpinner.isEnabled = true
                        ui.moreImageView.isClickable = true
                    }
                }
            }
        }

        ui.refreshImageView.onClick {
            currentSearchItem = ChildlistSearchItem()

            ui.supportSpinner.setSelection(0)
            ui.serviceSpinner.setSelection(0)
            ui.yearSpinner.setSelection(0)
            ui.statusSpinner.setSelection(0)
        }

        ui.moreImageView.onClick {
            logger.debug("ui.moreImageView.onClickListener")
            val ft = activity!!.supportFragmentManager.beginTransaction()
            currentSearchItem.service = ui.serviceSpinner.getValue() ?: ""
            val newFragment = ChildlistDialogMoreFragment.newInstance(currentSearchItem)
            newFragment.setTargetFragment(this@ChildlistFragment, REQUEST_MORE_SEARCH)
            newFragment.show(ft, "moreoptions")
        }

        ui.caseInfoContainer.onClick {
            val ft = activity!!.supportFragmentManager.beginTransaction()
            val newFragment = ChildlistDialogCaseinfoFragment.newInstance()
            newFragment.show(ft, "caseinfo")
        }

        ui.newCifActionButton.onClick {
            changeFragment.onChangeFragment(CifFragment.newInstance(rcp_no = null))
        }

        ui.searchImageView.onClick {
            logger.debug("ui.searchImageView.onClickListener")
            currentSearchItem.searchCode = null
            currentSearchItem.searchText = null
            search()
        }

        ui.itemsRecyclerView.layoutManager = LinearLayoutManager(context)
        ui.suggestionRecyclerView.layoutManager = LinearLayoutManager(context)

        commonViewModel.setCommonCodeItems()

        ui.itemsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                logger.debug("onScrolled : $dx, $dy, ${recyclerView?.scrollBarSize}, ${ui.searchContainer.height}")

                if (recyclerView.adapter?.itemCount ?: 0 > 10) {
                    if (dy > 10 && !isScrollEffect && ui.searchContainer.visibility == View.VISIBLE) {
                        ui.searchContainer.visibility = View.GONE
                        isScrollEffect = true
                        Timer("SettingUp", false).schedule(500) {
                            isScrollEffect = false
                        }
                    } else if (dy < -10 && !isScrollEffect && ui.searchContainer.visibility == View.GONE) {
                        ui.searchContainer.visibility = View.VISIBLE
                        isScrollEffect = true
                        Timer("SettingUp", false).schedule(500) {
                            isScrollEffect = false
                        }
                    }
                } else {
                    ui.searchContainer.visibility = View.VISIBLE
                }
            }
        })

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = "Child List"
        progress = indeterminateProgressDialog("Fetching data...")
    }

    private fun createYearSpinner(service: String = "") {
        logger.debug("ChildlistFragment.createYearSpinner($service)")
        // 검색 조건 year
        val computedCurrentYear = Calendar.getInstance().get(Calendar.YEAR)
        val computedCurrentMonth = (Calendar.getInstance().get(Calendar.MONTH) + 1)

        when (service) {
            Constants.SERVICE_SERVICE -> { // SERVICE
                val years = LinkedList<SpinnerOption>()
                for (i in computedCurrentYear downTo 2015) years.add(SpinnerOption(i.toString(), i.toString()))

                ui.yearSpinner.setItem(years, hint = "Year", adapterResource = R.layout.spinneritem)
            }
            Constants.SERVICE_APR -> { // APR
                val lastYear = if (computedCurrentYear != 2020 ) {if (computedCurrentMonth >= 10) computedCurrentYear + 1 else computedCurrentYear}
                                        else {if (computedCurrentMonth >= 9) computedCurrentYear + 1 else computedCurrentYear} // 2021년 APR만 9월 1일 오픈
                val years = LinkedList<SpinnerOption>()
                for (i in lastYear downTo 2015) years.add(SpinnerOption(i.toString(), i.toString()))

                ui.yearSpinner.setItem(years, adapterResource = R.layout.spinneritem)
                logger.debug("ChildlistFragment.createYearSpinner : currentYear = $currentYear")
            }
            Constants.SERVICE_ACL -> { // ACL
                val lastYear = if (computedCurrentMonth >= 4) computedCurrentYear else computedCurrentYear - 1
                val years = LinkedList<SpinnerOption>()
                for (i in lastYear downTo 2015) years.add(SpinnerOption(i.toString(), i.toString()))

                ui.yearSpinner.setItem(years, adapterResource = R.layout.spinneritem)
                logger.debug("ChildlistFragment.createYearSpinner : currentYear = $currentYear")
            }
            else -> {
                val years = LinkedList<SpinnerOption>()
                for (i in computedCurrentYear downTo 2015) years.add(SpinnerOption(i.toString(), i.toString()))

                ui.yearSpinner.setItem(years, adapterResource = R.layout.spinneritem)
            }
        }
        currentSearchItem.year?.apply {
            ui.yearSpinner.setSelectKey(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        logger.debug("onCreateOptionsMenu")
        this.menu = menu

        activity?.menuInflater?.inflate(R.menu.toolbar_childlist, menu)


        val searchItem = menu?.findItem(R.id.childlist_toolbar_search)
        if (searchItem?.actionView is SearchView) {
            searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem?): Boolean = true

                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                    ui.suggestionContainer.visibility = View.GONE

                    return true
                }
            })

            val searchView = searchItem.actionView as SearchView
            val searchManager = activity!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager
            searchView.queryHint = "Code or Name"
            searchView.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))

            searchView.onQueryTextFocusChange { _, hasFocus ->
                if (hasFocus) childlistViewModel.setFindAllSuggestions()
            }
            searchView.onQueryTextListener {
                onQueryTextChange(listener = { it: String? ->
                    logger.debug("ChildlistFragment.onCreateOptionsMenu : onQueryTextChange($it)")
                    false
                })
                onQueryTextSubmit(listener = { it: String? ->
                    logger.debug("ChildlistFragment.onCreateOptionsMenu : onQueryTextSubmit($it)")
                    if (!it.isNullOrBlank()) {
                        search(ChildlistSearchItem(searchText = it, service = ui.serviceSpinner.getValue() ?: "",
                                support = ui.supportSpinner.getValue(),
                                year = ui.yearSpinner.getValue()))
                        searchItem.collapseActionView()
                    }
                    false
                })
            }
        }

        val qrcode = menu?.findItem(R.id.childlist_toolbar_qrcode)
        qrcode?.setOnMenuItemClickListener {
            val intent = Intent(activity!!, QrScanActivity::class.java)
            startActivityForResult(intent, REQUEST_QR_CODE)
            true
        }

        val map = menu?.findItem(R.id.action_place)
        map!!.setOnMenuItemClickListener {
//            if (requireContext().wifiManager.isWifiEnabled) {
            if (requireContext().isNetworkAvailable()) {
                childlistViewModel.findAllVillageLocation().observeOnce(this@ChildlistFragment, Observer { items ->
                    if (items != null) {
                        startActivity<MapsVillageActivity>(
                                "items" to items
                        )
                    }
                })
            } else {
                toast(R.string.message_wifi_disabled)
            }
            true
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_MORE_SEARCH -> {
                if (resultCode == Activity.RESULT_OK && data?.extras != null) {
                    val village = data.extras!!.getString("village")
                    val school = data.extras!!.getString("school")
                    val gender = data.extras!!.getString("gender")
                    val ageFrom = data.extras!!.getString("ageFrom")
                    val ageTo = data.extras!!.getString("ageTo")
                    val case1 = data.extras!!.getString("case1")
                    val case2 = data.extras!!.getString("case2")
                    val case3 = data.extras!!.getString("case3")
                    val dropoutExpected = data.extras!!.getBoolean("dropoutExpected", false)

                    currentSearchItem.village = village
                    currentSearchItem.school = school
                    currentSearchItem.gender = gender
                    currentSearchItem.ageFrom = ageFrom
                    currentSearchItem.ageTo = ageTo
                    currentSearchItem.case1 = case1
                    currentSearchItem.case2 = case2
                    currentSearchItem.case3 = case3
                    currentSearchItem.dropoutExpected = dropoutExpected

                    search()
                }
            }
            REQUEST_QR_CODE -> {
                if (resultCode == Activity.RESULT_OK && data?.extras != null) {
                    val s = data.extras.getString("qrcode")
                    search(ChildlistSearchItem(searchText = s, service = ui.serviceSpinner.getValue() ?: "",
                            support = ui.supportSpinner.getValue(),
                            year = ui.yearSpinner.getValue()))

                    val searchItem = menu?.findItem(R.id.childlist_toolbar_search)
                    searchItem?.apply {
                        collapseActionView()
                    }

                }
            }
        }
    }

    private var selectedItem: ChildlistItem? = null
    private fun onClickChildListener(item: ChildlistItem) {
        logger.debug("onClickChildListener : $item")

        if (item.SERVICE != Constants.SERVICE_SERVICE) {
            return
        }

        if (item.IS_SELECTED) {
            bottomNavibarHide()
            if (selectedItem != null) {
                item.IS_SELECTED = false
                ui.itemsRecyclerView.adapter?.notifyDataSetChanged()
                selectedItem = null
            }
        } else {
            if (ui.bottomMenuContainer.visibility == View.GONE) {
                val layoutParams = ui.newCifActionButton.layoutParams as CoordinatorLayout.LayoutParams
                layoutParams.bottomMargin = layoutParams.bottomMargin + 112
                ui.newCifActionButton.layoutParams = layoutParams
            }

            ui.bottomMenuContainer.visibility = View.VISIBLE
            if (selectedItem != null) selectedItem!!.IS_SELECTED = false
            item.IS_SELECTED = true
            ui.itemsRecyclerView.adapter?.notifyDataSetChanged()
            selectedItem = item
        }
    }

    private fun onClickStatusListener(item: ChildlistItem) {
        logger.debug(GsonBuilder().create().toJson(item))
        when (item.SERVICE) {
            Constants.SERVICE_CIF -> { // CIF
                changeFragment.onChangeFragment(CifFragment.newInstance(item.RCP_NO))
            }
            Constants.SERVICE_APR -> {
                changeFragment.onChangeFragment(AprFragment.newInstance(item.CHRCP_NO ?: "", item.RCP_NO, item.YEAR
                        ?: ""))
            }
            Constants.SERVICE_ACL -> {
                changeFragment.onChangeFragment(AclEditFragment.newInstance(item.CHRCP_NO
                        ?: "", item.RCP_NO, item.YEAR ?: ""))
            }
            Constants.SERVICE_DRO -> {
                if (item.CH_STCD == "1") {
                    changeFragment.onChangeFragment(DropoutFragment.newInstance(item.CHRCP_NO ?: "", item.RCP_NO))
                } else {
                    changeFragment.onChangeFragment(ProfileFragment.newInstance(item.CHRCP_NO ?: ""))
                }
            }
        }
    }

    private fun onGMLStatusClickListener(item: ChildlistItem, gml: String) {
        when (gml) {
            "G" -> {
                changeFragment.onChangeFragment(GmFragment.newInstance(chrcp_no = item.CHRCP_NO
                        ?: "", rcp_no = item.GMNY_RCP_NO, mng_no = item.GMNY_MNG_NO ?: ""))
            }
            "L" -> {
                changeFragment.onChangeFragment(GmLetterFragment.newInstance(chrcp_no = item.CHRCP_NO
                        ?: "", rcp_no = item.LETR_RCP_NO, mng_no = item.LETR_MNG_NO ?: ""))
            }
        }
    }

    private fun bottomNavibarHide() {
        if (ui.bottomMenuContainer.visibility == View.VISIBLE) {
            val layoutParams = ui.newCifActionButton.layoutParams as CoordinatorLayout.LayoutParams
            ui.bottomMenuContainer.visibility = View.GONE

            layoutParams.bottomMargin = layoutParams.bottomMargin - 112
            ui.newCifActionButton.layoutParams = layoutParams
        }
    }

    private fun search(searchItem: ChildlistSearchItem? = null) {
        // adapter 생성
        logger.debug("ChildlistFragmenu.search()")
        val support = searchItem?.support ?: ui.supportSpinner.getValue()
        val service = searchItem?.service ?: ui.serviceSpinner.getValue() ?: ""
        val year = searchItem?.year ?: ui.yearSpinner.getValue()
        val status = searchItem?.status ?: ui.statusSpinner.getValue()

        currentService = service

        if (service == "") {
            ui.newCifActionButton.visibility = View.VISIBLE
        } else {
            ui.newCifActionButton.visibility = View.GONE
            bottomNavibarHide()
        }

        when (service) {
            Constants.SERVICE_CIF -> { // CIF
                ui.cifTextView.visibility = View.VISIBLE
                ui.aprTextView.visibility = View.GONE
                ui.aclTextView.visibility = View.GONE
                ui.dropoutTextView.visibility = View.GONE
                ui.gmTextView.visibility = View.VISIBLE
                ui.letterTextView.visibility = View.VISIBLE
            }
            Constants.SERVICE_APR -> { // APR
                ui.cifTextView.visibility = View.GONE
                ui.aprTextView.visibility = View.VISIBLE
                ui.aclTextView.visibility = View.GONE
                ui.dropoutTextView.visibility = View.GONE
                ui.gmTextView.visibility = View.VISIBLE
                ui.letterTextView.visibility = View.VISIBLE
            }
            Constants.SERVICE_ACL -> { // ACL
                ui.cifTextView.visibility = View.GONE
                ui.aprTextView.visibility = View.GONE
                ui.aclTextView.visibility = View.VISIBLE
                ui.dropoutTextView.visibility = View.GONE
                ui.gmTextView.visibility = View.VISIBLE
                ui.letterTextView.visibility = View.VISIBLE
            }
            Constants.SERVICE_DRO -> { // DRO
                ui.cifTextView.visibility = View.GONE
                ui.aprTextView.visibility = View.GONE
                ui.aclTextView.visibility = View.GONE
                ui.dropoutTextView.visibility = View.VISIBLE
                ui.gmTextView.visibility = View.VISIBLE
                ui.letterTextView.visibility = View.VISIBLE
            }
            Constants.SERVICE_GML -> { // GML
                ui.cifTextView.visibility = View.GONE
                ui.aprTextView.visibility = View.GONE
                ui.aclTextView.visibility = View.GONE
                ui.dropoutTextView.visibility = View.GONE
                ui.gmTextView.visibility = View.VISIBLE
                ui.letterTextView.visibility = View.VISIBLE
            }
            else -> {
                ui.cifTextView.visibility = View.GONE
                ui.aprTextView.visibility = View.GONE
                ui.aclTextView.visibility = View.GONE
                ui.dropoutTextView.visibility = View.GONE
                ui.gmTextView.visibility = View.GONE
                ui.letterTextView.visibility = View.GONE
            }
        }

        currentSearchItem.support = support
        currentSearchItem.service = service
        currentSearchItem.year = year
        currentSearchItem.status = status

        progress.show()

        searchItem?.let {
            childlistViewModel.setSearchOptions(it)
        } ?: childlistViewModel.setSearchOptions(currentSearchItem)
    }

    fun onChangeBottomNavigation(position: Int) {
        selectedItem?.CHRCP_NO?.apply {

            when (position) {
                0 -> {
                    changeFragment.onChangeFragment(ProfileFragment.newInstance(this))
                }
                1 -> {
                    changeFragment.onChangeFragment(ReportFragment.newInstance(this))
                }
                2 -> {
                    changeFragment.onChangeFragment(ProvidedServiceFragment.newInstance(this))
                }
                3 -> {
                    changeFragment.onChangeFragment(AclFragment.newInstance(this))
                }
                4 -> {
                    changeFragment.onChangeFragment(GmlFragment.newInstance(this))
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<ChildlistFragment> {
        lateinit var supportSpinner: Spinner
        lateinit var serviceSpinner: Spinner
        lateinit var yearSpinner: Spinner
        lateinit var refreshImageView: ImageView
        lateinit var statusSpinner: Spinner

        lateinit var moreImageView: ImageView
        lateinit var searchImageView: ImageView

        lateinit var itemsRecyclerView: RecyclerView

        lateinit var totalTextView: TextView

        lateinit var caseInfoContainer: LinearLayout

        lateinit var bottomMenuContainer: LinearLayout

        lateinit var newCifActionButton: FloatingActionButton

        lateinit var cifTextView: TextView
        lateinit var aprTextView: TextView
        lateinit var aclTextView: TextView
        lateinit var dropoutTextView: TextView
        lateinit var gmTextView: TextView
        lateinit var letterTextView: TextView

        lateinit var suggestionRecyclerView: RecyclerView
        lateinit var suggestionContainer: LinearLayout

        lateinit var searchContainer: LinearLayout


        override fun createView(ui: AnkoContext<ChildlistFragment>) = with(ui) {
            coordinatorLayout {
                verticalLayout {
                    // 검색
                    searchContainer = verticalLayout {
                        backgroundColorResource = R.color.colorPrimary
                        leftPadding = dimen(R.dimen.px30)
                        rightPadding = dimen(R.dimen.px26)
                        topPadding = dimen(R.dimen.px20)
//                        bottomPadding = dimen(R.dimen.px16)

                        gravity = Gravity.CENTER_VERTICAL

                        linearLayout {
                            gravity = Gravity.CENTER_VERTICAL

                            supportSpinner = spinner {

                            }.lparams(width = 0, weight = 1f)
                            serviceSpinner = spinner {
                            }.lparams(width = 0, weight = 1f)
                            yearSpinner = spinner { }.lparams(width = 0, weight = 1f)
                            refreshImageView = imageView {
                                imageResource = R.drawable.search_re
                            }.lparams(width = dimen(R.dimen.px56), height = dimen(R.dimen.px56))
                        }

                        space { }.lparams(width = matchParent, height = dip(8))

                        linearLayout {
                            gravity = Gravity.CENTER_VERTICAL

                            statusSpinner = spinner {
                            }.lparams(width = 0, weight = 1f)

                            linearLayout {
                                gravity = Gravity.CENTER_VERTICAL

                                moreImageView = imageView {
                                    imageResource = R.drawable.search_more
                                }.lparams(width = dip(70))

                                space { }.lparams(width = 0, weight = 1f)

                                searchImageView = imageView {
                                    imageResource = R.drawable.search_search
                                }.lparams(width = dip(80))
                            }.lparams(width = 0, weight = 1f)
                        }
                    }

                    linearLayout {
                        backgroundColorResource = R.color.colorGridTitle
                        gravity = Gravity.CENTER_VERTICAL

                        textView("No") {
                            gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
//                            textSize = 15f
                        }.lparams(width = dimen(R.dimen.px100))
                        textView("Case") {
                            gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
//                            textSize = 15f
                        }.lparams(width = dimen(R.dimen.px70))
                        textView("Code") {
                            gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
//                            textSize = 15f
                        }.lparams(width = dimen(R.dimen.px160))
                        textView("Name") {
                            gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
//                            textSize = 15f
                        }.lparams(width = 0, weight = 1f)
                        cifTextView = textView("CIF") {
                            gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
//                            textSize = 15f
                            visibility = View.GONE
                        }.lparams(width = dimen(R.dimen.px80))
                        aprTextView = textView("APR") {
                            gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
//                            textSize = 15f
                            visibility = View.GONE
                        }.lparams(width = dimen(R.dimen.px80))
                        aclTextView = textView("ACL") {
                            gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
//                            textSize = 15f
                            visibility = View.GONE
                        }.lparams(width = dimen(R.dimen.px80))
                        dropoutTextView = textView("DRO") {
                            gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
//                            textSize = 15f
                            visibility = View.GONE
                        }.lparams(width = dimen(R.dimen.px80))
                        gmTextView = textView("GM") {
                            gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
//                            textSize = 15f
                            visibility = View.GONE
                        }.lparams(width = dimen(R.dimen.px80))
                        letterTextView = textView("L") {
                            gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
//                            textSize = 15f
                            visibility = View.GONE
                        }.lparams(width = dimen(R.dimen.px80))
                    }.lparams(width = matchParent, height = dimen(R.dimen.px80))

                    verticalLayout {
                        itemsRecyclerView = recyclerView {

                        }.lparams(width = matchParent, height = matchParent)
                    }.lparams(width = matchParent, height = 0, weight = 1f)

                    linearLayout {
                        gravity = Gravity.CENTER
                        backgroundColorResource = R.color.colorBgLiteGray
                        padding = dimen(R.dimen.px20)

                        textView(R.string.label_total)
                        space { }.lparams(width = dimen(R.dimen.px20))
                        totalTextView = textView("-") {
                            textColorResource = R.color.colorAccent
                        }.lparams(width = 0, weight = 1f)

                        caseInfoContainer = linearLayout {
                            gravity = Gravity.CENTER_VERTICAL
                            textView(R.string.label_caseinfo)
                            space { }.lparams(width = dimen(R.dimen.px10))
                            imageView {
                                imageResource = R.drawable.select_3
                            }.lparams(width = dimen(R.dimen.px18), height = dimen(R.dimen.px16)) {
                                gravity = Gravity.CENTER_VERTICAL
                            }
                        }
                    }

                    bottomMenuContainer = linearLayout {
                        gravity = Gravity.CENTER
                        visibility = View.GONE

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
                    }.lparams {
                        gravity = Gravity.BOTTOM
                    }
                }.lparams(width = matchParent, height = matchParent)

                suggestionContainer = verticalLayout {
                    leftPadding = dimen(R.dimen.px40)
                    rightPadding = dimen(R.dimen.px40)

                    visibility = View.GONE

                    suggestionRecyclerView = recyclerView {

                    }.lparams(width = matchParent, height = wrapContent)
                }.lparams(width = matchParent, height = wrapContent)

                newCifActionButton = floatingActionButton {
                    imageResource = R.drawable.ic_action_button_add
                }.lparams {
                    rightMargin = dip(16)
                    bottomMargin = dip(48)

                    gravity = Gravity.END or Gravity.BOTTOM

                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class SuggestionAdapter(val list: ArrayList<APP_SEARCH_HISTORY>,
                            val onClickListener: (APP_SEARCH_HISTORY) -> Unit,
                            val onRemoveClickListener: (APP_SEARCH_HISTORY) -> Unit)
        : RecyclerView.Adapter<SuggestionAdapter.ListAdaptorViewHolder>() {
        override fun getItemCount(): Int = list.size
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdaptorViewHolder {
            return ListAdaptorViewHolder(ListViewHolderUI().createView(AnkoContext.create(parent.context, parent)))
        }

        override fun onBindViewHolder(holder: ListAdaptorViewHolder, position: Int) {
            val item = list[position]

            holder.wordTextView.text = item.word
            holder.wordTextView.onClick {
                onClickListener(item)
            }
            holder.removeImageView.onClick {
                onRemoveClickListener(item)
                list.remove(item)
                notifyDataSetChanged()
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        inner class ListAdaptorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val wordTextView: TextView = itemView.findViewById(ListViewHolderUI.ID_WORD)
            val removeImageView: LinearLayout = itemView.findViewById(ListViewHolderUI.ID_REMOVE)
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        class ListViewHolderUI : AnkoComponent<ViewGroup> {
            companion object {
                const val ID_WORD = 1
                const val ID_REMOVE = 2
            }

            override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                linearLayout {
                    lparams(width = matchParent, height = wrapContent)
                    padding = dimen(R.dimen.px20)
                    backgroundColorResource = R.color.colorWhite

                    textView {
                        id = ID_WORD
                    }.lparams(width = 0, weight = 1f)
                    space { }.lparams(width = dimen(R.dimen.px10))
                    verticalLayout {
                        id = ID_REMOVE
                        gravity = Gravity.CENTER
                        imageView {
                            imageResource = R.drawable.close
                        }.lparams(width = dimen(R.dimen.px26), height = dimen(R.dimen.px26)) {
                            gravity = Gravity.CENTER
                        }
                    }.lparams(width = dimen(R.dimen.px40), height = dimen(R.dimen.px40))
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class ChildlistAdapter(val context: Context,
                           val items: List<ChildlistItem>,
                           val onClickListener: (ChildlistItem) -> Unit,
                           val onStatusClickListener: (ChildlistItem) -> Unit,
                           val onGMLStatusClickListener: (ChildlistItem, String) -> Unit)
        : RecyclerView.Adapter<ChildlistAdapter.ListAdapterViewHolder>() {
        override fun getItemCount(): Int = items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapterViewHolder {
            return ListAdapterViewHolder(ListViewHolderUI().createView(AnkoContext.create(parent.context, parent)))
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ListAdapterViewHolder, position: Int) {
            val item = items[position]

            when (item.SERVICE) {
                Constants.SERVICE_SERVICE -> {
                    holder.statusContainerView.visibility = View.GONE
                    holder.gmnyStatusContainerView.visibility = View.GONE
                    holder.letrStatusContainerView.visibility = View.GONE
                }
                Constants.SERVICE_GML -> { // GML
                    holder.statusContainerView.visibility = View.GONE
                    holder.gmnyStatusContainerView.visibility = View.VISIBLE
                    holder.letrStatusContainerView.visibility = View.VISIBLE
                }
                else -> {
                    holder.statusContainerView.visibility = View.VISIBLE
                    holder.gmnyStatusContainerView.visibility = View.VISIBLE
                    holder.letrStatusContainerView.visibility = View.VISIBLE
                }
            }

            when (item.SERVICE) {
                Constants.SERVICE_DRO -> { // DRO
                    if (item.STATUS_CODE == "1") {
                        holder.containerView.backgroundColor = Color.parseColor("#B53534")
                    } else {
                        holder.containerView.backgroundColor = Color.parseColor("#F16523")
                    }
                }
                else -> {
                    if (item.IS_SELECTED) {
                        holder.containerView.backgroundColor = Color.parseColor("#F4F4F4")
                    } else {
                        holder.containerView.backgroundColor = Color.parseColor("#FFFFFF")
                    }
                }
            }


            holder.containerView.onClick {
                onClickListener(item)
            }

            holder.noTextView.text = "${position + 1}"
            holder.codeTextView.text = item.CHILD_CODE
            holder.nameTextView.text = item.CHILD_NAME

            holder.statusTextView.text = item.STATUS_LABEL
            holder.statusTextView.backgroundColor = Color.parseColor(item.STATUS_COLOR ?: "#FFFFFF")
            holder.statusTextView.onClick {
                onStatusClickListener(item)
            }

            if (item.GMNY_MNG_NO.isNullOrBlank()) {
                holder.gmnyStatusTextView.visibility = View.GONE
            } else {
                holder.gmnyStatusTextView.visibility = View.VISIBLE
                holder.gmnyStatusTextView.text = "(${item.GMNY_MM})\n${item.GMNY_STATUS_LABEL}"
                holder.gmnyStatusTextView.backgroundColor = Color.parseColor(item.GMNY_STATUS_COLOR ?: "#FFFFFF")
            }
            holder.gmnyStatusTextView.onClick {
                onGMLStatusClickListener(item, "G")
            }

            if (item.LETR_MNG_NO.isNullOrBlank()) {
                holder.letrStatusTextView.visibility = View.GONE
            } else {
                holder.letrStatusTextView.visibility = View.VISIBLE
                holder.letrStatusTextView.text = "(${item.LETR_MM})\n${item.LETR_STATUS_LABEL}"
                holder.letrStatusTextView.backgroundColor = Color.parseColor(item.LETR_STATUS_COLOR ?: "#FFFFFF")
            }
            holder.letrStatusTextView.onClick {
                onGMLStatusClickListener(item, "L")
            }

            if (item.CASE1 == null) {
                holder.case1ImageView.visibility = View.GONE
            } else {
                getSpecialCaseResource(item.CASE1!!)?.apply {
                    holder.case1ImageView.imageResource = this
                    holder.case1ImageView.visibility = View.VISIBLE
                }
            }

            if (item.CASE2 == null) {
                holder.case2ImageView.visibility = View.GONE
            } else {
                getSpecialCaseResource(item.CASE2!!)?.apply {
                    holder.case2ImageView.imageResource = this
                    holder.case2ImageView.visibility = View.VISIBLE
                }
            }

            if (item.CASE3 == null) {
                holder.case3ImageView.visibility = View.GONE
            } else {
                getSpecialCaseResource(item.CASE3!!)?.apply {
                    holder.case3ImageView.imageResource = this
                    holder.case3ImageView.visibility = View.VISIBLE
                }
            }

            if (item.AGE >= 18) {
                holder.redDotImageView.visibility = View.VISIBLE
            } else {
                holder.redDotImageView.visibility = View.GONE
            }
        }

        private fun getSpecialCaseResource(case: String): Int? {
            when (case) {
                "1", "2" -> { // 1 : 심각한장애	Severe Disability, 2 : 심각한질병	Serious Illness
                    return R.drawable.case_serious
                }
                "3" -> { // 극심한빈곤	Extreme Poverty
                    return R.drawable.case_extreme
                }
                "4" -> { // 보호자부재	Absence of Parent/Guardian
                    return R.drawable.case_guardian
                }
                "5" -> { // 아동학대	Child Abuse
                    return R.drawable.case_abuse
                }
                "8" -> { // 사례아동(방송/비방송)	Media/fundraising-released child
                    return R.drawable.case_media
                }
                "9" -> { // 아동노동	Child labour
                    return R.drawable.case_labor
                }
                "10" -> { // 아동결혼	Child marriage
                    return R.drawable.case_exposed
                }
                "12" -> { // 이동예상	Expected Move
                    return R.drawable.case_expected
                }
            }

            return null
        }

        inner class ListAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val containerView = itemView.findViewById<ViewGroup>(ListViewHolderUI.ID_CONTAINER)!!
            //            val caseContainerView = itemView.findViewById<ViewGroup>(ListViewHolderUI.ID_CASE_CONTAINER)!!
            val statusContainerView = itemView.findViewById<ViewGroup>(ListViewHolderUI.ID_STATUS_CONTAINER)!!
            val gmnyStatusContainerView = itemView.findViewById<ViewGroup>(ListViewHolderUI.ID_GMNY_STATUS_CONTAINER)!!
            val letrStatusContainerView = itemView.findViewById<ViewGroup>(ListViewHolderUI.ID_LETR_STATUS_CONTAINER)!!

            val noTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_NO)!!
            val codeTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_CODE)!!
            val nameTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_NAME)!!
            val statusTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_STATUS)!!
            val gmnyStatusTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_GMNY_STATUS)!!
            val letrStatusTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_LETR_STATUS)!!

            val case1ImageView = itemView.findViewById<ImageView>(ListViewHolderUI.ID_CASE_1)!!
            val case2ImageView = itemView.findViewById<ImageView>(ListViewHolderUI.ID_CASE_2)!!
            val case3ImageView = itemView.findViewById<ImageView>(ListViewHolderUI.ID_CASE_3)!!
            val redDotImageView = itemView.findViewById<ImageView>(ListViewHolderUI.ID_RED_DOT)!!
        }

        class ListViewHolderUI : AnkoComponent<ViewGroup> {
            companion object {
                const val ID_NO = 1
                const val ID_CODE = 2
                const val ID_NAME = 3
                const val ID_STATUS = 4
                const val ID_GMNY_STATUS = 5
                const val ID_LETR_STATUS = 6

                const val ID_CONTAINER = 100
                const val ID_CASE_CONTAINER = 101
                const val ID_STATUS_CONTAINER = 102
                const val ID_GMNY_STATUS_CONTAINER = 103
                const val ID_LETR_STATUS_CONTAINER = 104

                const val ID_CASE_1 = 201
                const val ID_CASE_2 = 202
                const val ID_CASE_3 = 203
                const val ID_RED_DOT = 204
            }

            override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                frameLayout {
                    view { backgroundColorResource = R.color.colorGray }.lparams(width = matchParent, height = dip(1))
                    linearLayout {
                        id = ID_CONTAINER

                        textView {
                            id = ID_NO
                            gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
                        }.lparams(width = dimen(R.dimen.px100), height = matchParent)

                        linearLayout {
                            id = ID_CASE_CONTAINER

                            imageView {
                                id = ID_CASE_1
                                visibility = View.GONE
                            }.lparams(width = dimen(R.dimen.px20), height = dimen(R.dimen.px34)) {
                                gravity = Gravity.CENTER_VERTICAL
                            }
                            imageView {
                                id = ID_CASE_2
                                visibility = View.GONE
                            }.lparams(width = dimen(R.dimen.px20), height = dimen(R.dimen.px34)) {
                                gravity = Gravity.CENTER_VERTICAL
                            }
                            imageView {
                                id = ID_CASE_3
                                visibility = View.GONE
                            }.lparams(width = dimen(R.dimen.px20), height = dimen(R.dimen.px34)) {
                                gravity = Gravity.CENTER_VERTICAL
                            }
                        }.lparams(width = dimen(R.dimen.px70), height = matchParent)

                        frameLayout {
                            imageView {
                                id = ID_RED_DOT
                                visibility = View.GONE
                                imageResource = R.drawable.red_dot
                            }.lparams(width = dimen(R.dimen.px20), height = dimen(R.dimen.px20)) {
                                topMargin = dimen(R.dimen.px30)
                            }
                            textView {
                                id = ID_CODE
                                gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
                            }.lparams(width = matchParent, height = matchParent)
                        }.lparams(width = dimen(R.dimen.px160), height = matchParent)

                        textView {
                            id = ID_NAME
                            gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
                        }.lparams(width = 0, height = matchParent, weight = 1f)

                        verticalLayout {
                            id = ID_STATUS_CONTAINER
                            gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
                            visibility = View.GONE
                            textView {
                                id = ID_STATUS
                                gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
                            }.lparams(width = dimen(R.dimen.px74), height = dimen(R.dimen.px74))
                        }.lparams(width = dimen(R.dimen.px80), height = matchParent)
                        verticalLayout {
                            id = ID_GMNY_STATUS_CONTAINER
                            gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
                            visibility = View.GONE
                            textView {
                                id = ID_GMNY_STATUS
                                gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
                            }.lparams(width = dimen(R.dimen.px74), height = dimen(R.dimen.px74))
                        }.lparams(width = dimen(R.dimen.px80), height = matchParent)
                        verticalLayout {
                            id = ID_LETR_STATUS_CONTAINER
                            gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
                            visibility = View.GONE
                            textView {
                                id = ID_LETR_STATUS
                                gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
                            }.lparams(width = dimen(R.dimen.px74), height = dimen(R.dimen.px74))
                        }.lparams(width = dimen(R.dimen.px80), height = matchParent)

                    }.lparams(width = matchParent, height = dimen(R.dimen.px160))
                    view { backgroundColorResource = R.color.colorGray }.lparams(width = matchParent, height = dip(1)) {
                        gravity = Gravity.BOTTOM
                    }
                }
            }

        }
    }
}
