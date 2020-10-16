package kr.goodneighbors.cms.ui.statistics

import android.arch.lifecycle.Observer
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.extensions.getValue
import kr.goodneighbors.cms.extensions.observeOnce
import kr.goodneighbors.cms.extensions.setItem
import kr.goodneighbors.cms.service.model.SpinnerOption
import kr.goodneighbors.cms.service.viewmodel.StatisticsViewModel
import kr.goodneighbors.cms.ui.BaseActivityFragment
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.imageView
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.scrollView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onItemSelectedListener
import org.jetbrains.anko.spinner
import org.jetbrains.anko.support.v4.space
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.view
import org.jetbrains.anko.wrapContent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class StatisticsFragment : BaseActivityFragment() {
    companion object {
        fun newInstance(): StatisticsFragment {
            return StatisticsFragment()
        }
    }

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(StatisticsFragment::class.java)
    }

    private val ui = FragmentUI()

    private val viewModel: StatisticsViewModel by lazy {
        StatisticsViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        logger.debug("onCreateView")
        val v = ui.createView(AnkoContext.create(requireContext(), this))
        activity?.title = "Statistics"

        viewModel.getPageItem().observeOnce(this, Observer {
            it?.apply {
                ui.cifSupportSpinner.setItem(items = codeSupportCountry)
                ui.aprSupportSpinner.setItem(items = codeSupportCountry)
                ui.aclSupportSpinner.setItem(items = codeSupportCountry)
                ui.dropoutSupportSpinner.setItem(items = codeSupportCountry)

                val computedCurrentYear = Calendar.getInstance().get(Calendar.YEAR)
                val computedCurrentMonth = (Calendar.getInstance().get(Calendar.MONTH) + 1)
                val years = LinkedList<SpinnerOption>()
                for (i in computedCurrentYear downTo 2015) years.add(SpinnerOption(i.toString(), i.toString()))

                ui.cifYearSpinner.setItem(items = years)
                ui.aprYearSpinner.setItem(items = years)
                ui.aclYearSpinner.setItem(items = years)
                ui.dropoutYearSpinner.setItem(items = years)
                ui.gmlYearSpinner.setItem(items = years)


                val months = LinkedList<SpinnerOption>()
                for (i in 1..12) months.add(SpinnerOption(i.toString().padStart(2, '0'), i.toString().padStart(2, '0')))

                ui.gmlFromMonthSpinner.setItem(items = months)
                ui.gmlFromMonthSpinner.setSelection(computedCurrentMonth - 1)
                ui.gmlToMonthSpinner.setItem(items = months)
                ui.gmlToMonthSpinner.setSelection(computedCurrentMonth - 1)

                initUIEvent()
            }
        })

        return v
    }

    private fun initUIEvent() {
        ui.cifToggleImageView.onClick {
            if (ui.cifDetailContainer.visibility == View.VISIBLE) {
                ui.cifDetailContainer.visibility = View.GONE
                ui.cifToggleImageView.imageResource = R.drawable.select_4
            }
            else {
                ui.cifDetailContainer.visibility = View.VISIBLE
                ui.cifToggleImageView.imageResource = R.drawable.select_5
            }
        }

        ui.cifSupportSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, _ ->
                loadCifData()
            }
        }

        ui.cifYearSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, _ ->
                loadCifData()
            }
        }

        ui.aprToggleImageView.onClick {
            if (ui.aprDetailContainer.visibility == View.VISIBLE) {
                ui.aprDetailContainer.visibility = View.GONE
                ui.aprToggleImageView.imageResource = R.drawable.select_4
            }
            else {
                ui.aprDetailContainer.visibility = View.VISIBLE
                ui.aprToggleImageView.imageResource = R.drawable.select_5
            }
        }

        ui.aprSupportSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, _ ->
                loadAprData()
            }
        }

        ui.aprYearSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, _ ->
                loadAprData()
            }
        }

        ui.aclToggleImageView.onClick {
            if (ui.aclDetailContainer.visibility == View.VISIBLE) {
                ui.aclDetailContainer.visibility = View.GONE
                ui.aclToggleImageView.imageResource = R.drawable.select_4
            }
            else {
                ui.aclDetailContainer.visibility = View.VISIBLE
                ui.aclToggleImageView.imageResource = R.drawable.select_5
            }
        }

        ui.aclSupportSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, _ ->
                loadAclData()
            }
        }

        ui.aclYearSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, _ ->
                loadAclData()
            }
        }

        ui.dropoutToggleImageView.onClick {
            if (ui.dropoutDetailContainer.visibility == View.VISIBLE) {
                ui.dropoutDetailContainer.visibility = View.GONE
                ui.dropoutToggleImageView.imageResource = R.drawable.select_4
            }
            else {
                ui.dropoutDetailContainer.visibility = View.VISIBLE
                ui.dropoutToggleImageView.imageResource = R.drawable.select_5
            }
        }

        ui.dropoutSupportSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, _ ->
                loadDropoutData()
            }
        }

        ui.dropoutYearSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, _ ->
                loadDropoutData()
            }
        }

        ui.gmlToggleImageView.onClick {
            if (ui.gmlDetailContainer.visibility == View.VISIBLE) {
                ui.gmlDetailContainer.visibility = View.GONE
                ui.gmlToggleImageView.imageResource = R.drawable.select_4
            }
            else {
                ui.gmlDetailContainer.visibility = View.VISIBLE
                ui.gmlToggleImageView.imageResource = R.drawable.select_5
            }
        }

        ui.gmlTypeSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, _ ->
                loadGmlData()
            }
        }

        ui.gmlYearSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, _ ->
                loadGmlData()
            }
        }

        ui.gmlFromMonthSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, _ ->
                loadGmlData()
            }
        }

        ui.gmlToMonthSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, _ ->
                loadGmlData()
            }
        }
    }

    private fun loadCifData() {
        logger.debug("loadCifData : ${ui.cifSupportSpinner.getValue()?:""}, ${ui.cifYearSpinner.getValue()?:""}")
        viewModel.getCifData(ui.cifSupportSpinner.getValue()?:"", ui.cifYearSpinner.getValue()?:"").observeOnce(this, Observer {items ->
            ui.cifDataContainer.removeAllViews()
            items?.apply {
                if (isNotEmpty()) {
                    AnkoContext.createDelegate(ui.cifDataContainer).apply {
                        forEach { item ->
                            linearLayout {
                                lparams(width = matchParent, height = wrapContent) {
                                    topMargin = dip(-1)
                                }
                                minimumHeight = dimen(R.dimen.px70)

                                textView(item.SPLY_MON) {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                textView(item.SUB) {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                textView("${item.NR}\n(${item.NR_PCT}%)") {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                textView("${item.WA}\n(${item.WA_PCT}%)") {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                textView("${item.RE}\n(${item.RE_PCT}%)") {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                textView("${item.AP}\n(${item.AP_PCT}%)") {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                }
                            }
                        }
                    }
                }
                else {
                    AnkoContext.createDelegate(ui.cifDataContainer).apply {
                        linearLayout {
                            lparams(width = matchParent, height = wrapContent) {
                                topMargin = dip(-1)
                            }
                            minimumHeight = dimen(R.dimen.px70)

                            textView("-") {
                                gravity = Gravity.CENTER
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                            }
                        }
                    }
                }
            }
        })
    }

    private fun loadAprData() {
        logger.debug("loadAprData : ${ui.aprSupportSpinner.getValue()?:""}, ${ui.aprYearSpinner.getValue()?:""}")
        viewModel.getAprData(ui.aprSupportSpinner.getValue()?:"", ui.aprYearSpinner.getValue()?:"").observeOnce(this, Observer {items ->
            ui.aprDataContainer.removeAllViews()
            items?.apply {
                if (isNotEmpty()) {
                    AnkoContext.createDelegate(ui.aprDataContainer).apply {
                        forEach { item ->
                            linearLayout {
                                lparams(width = matchParent, height = wrapContent) {
                                    topMargin = dip(-1)
                                }
                                minimumHeight = dimen(R.dimen.px70)

                                textView(item.SUB ?: "0") {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                textView("${item.NR ?: "0"}\n(${item.NR_PCT ?: "0"}%)") {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                textView("${item.WA ?: "0"}\n(${item.WA_PCT ?: "0"}%)") {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                textView("${item.RE ?: "0"}\n(${item.RE_PCT ?: "0"}%)") {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                textView("${item.AP ?: "0"}\n(${item.AP_PCT ?: "0"}%)") {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                }
                            }
                        }
                    }
                }
                else {
                    AnkoContext.createDelegate(ui.aprDataContainer).apply {
                        linearLayout {
                            lparams(width = matchParent, height = wrapContent) {
                                topMargin = dip(-1)
                            }
                            minimumHeight = dimen(R.dimen.px70)

                            textView("-") {
                                gravity = Gravity.CENTER
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                            }
                        }
                    }
                }
            }
        })
    }

    private fun loadAclData() {
        logger.debug("loadAclData : ${ui.aclSupportSpinner.getValue()?:""}, ${ui.aclYearSpinner.getValue()?:""}")
        viewModel.getAclData(ui.aclSupportSpinner.getValue()?:"", ui.aclYearSpinner.getValue()?:"").observeOnce(this, Observer {items ->
            ui.aclDataContainer.removeAllViews()
            items?.apply {
                if (isNotEmpty()) {
                    AnkoContext.createDelegate(ui.aclDataContainer).apply {
                        forEach { item ->
                            linearLayout {
                                lparams(width = matchParent, height = wrapContent) {
                                    topMargin = dip(-1)
                                }
                                minimumHeight = dimen(R.dimen.px70)

                                textView(item.SUB ?: "0") {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                textView("${item.NR ?: "0"}\n(${item.NR_PCT ?: "0"}%)") {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                textView("${item.WA ?: "0"}\n(${item.WA_PCT ?: "0"}%)") {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                textView("${item.RE ?: "0"}\n(${item.RE_PCT ?: "0"}%)") {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                textView("${item.AP ?: "0"}\n(${item.AP_PCT ?: "0"}%)") {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                }
                            }
                        }
                    }
                }
                else {
                    AnkoContext.createDelegate(ui.aclDataContainer).apply {
                        linearLayout {
                            lparams(width = matchParent, height = wrapContent) {
                                topMargin = dip(-1)
                            }
                            minimumHeight = dimen(R.dimen.px70)

                            textView("-") {
                                gravity = Gravity.CENTER
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                            }
                        }
                    }
                }
            }
        })
    }

    private fun loadDropoutData() {
        logger.debug("loaddropoutData : ${ui.dropoutSupportSpinner.getValue()?:""}, ${ui.dropoutYearSpinner.getValue()?:""}")
        viewModel.getDropoutData(ui.dropoutSupportSpinner.getValue()?:"", ui.dropoutYearSpinner.getValue()?:"").observeOnce(this, Observer {items ->
            ui.dropoutDataContainer.removeAllViews()
            items?.apply {
                if (isNotEmpty()) {
                    AnkoContext.createDelegate(ui.dropoutDataContainer).apply {
                        forEach { item ->
                            linearLayout {
                                lparams(width = matchParent, height = wrapContent) {
                                    topMargin = dip(-1)
                                }
                                minimumHeight = dimen(R.dimen.px70)

                                textView(item.REG_CNT ?: "0") {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                textView("${item.AP ?: "0"}\n(${item.AP_PCT ?: "0"}%)") {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                textView("${item.RE ?: "0"}\n(${item.RE_PCT ?: "0"}%)") {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                textView("${item.WA ?: "0"}\n(${item.WA_PCT ?: "0"}%)") {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                }
                            }
                        }
                    }
                }
                else {
                    AnkoContext.createDelegate(ui.dropoutDataContainer).apply {
                        linearLayout {
                            lparams(width = matchParent, height = wrapContent) {
                                topMargin = dip(-1)
                            }
                            minimumHeight = dimen(R.dimen.px70)

                            textView("-") {
                                gravity = Gravity.CENTER
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                            }
                        }
                    }
                }
            }
        })
    }

    private fun loadGmlData() {
        logger.debug("loadGmlData")
        viewModel.getGmlData(
                ui.gmlTypeSpinner.getValue()?:"",
                ui.gmlYearSpinner.getValue()?:"",
                ui.gmlFromMonthSpinner.getValue()?:"",
                ui.gmlToMonthSpinner.getValue()?:""
        ).observeOnce(this, Observer {items ->
            ui.gmlDataContainer.removeAllViews()
            items?.apply {
                if (isNotEmpty()) {
                    AnkoContext.createDelegate(ui.gmlDataContainer).apply {
                        forEach { item ->
                            linearLayout {
                                lparams(width = matchParent, height = wrapContent) {
                                    topMargin = dip(-1)
                                }
                                minimumHeight = dimen(R.dimen.px70)

                                textView(item.MONTH) {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                textView(item.CNT) {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                textView(item.AMT) {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                textView("${item.NR?:"0"}\n(${item.NR_PCT?:"0"}%)") {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                textView("${item.RE?:"0"}\n(${item.RE_PCT?:"0"}%)") {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                textView("${item.WA?:"0"}\n(${item.WA_PCT?:"0"}%)") {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                textView("${item.AP?:"0"}\n(${item.AP_PCT?:"0"}%)") {
                                    gravity = Gravity.CENTER
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                }
                            }
                        }
                    }
                }
                else {
                    AnkoContext.createDelegate(ui.gmlDataContainer).apply {
                        linearLayout {
                            lparams(width = matchParent, height = wrapContent) {
                                topMargin = dip(-1)
                            }
                            minimumHeight = dimen(R.dimen.px70)

                            textView("-") {
                                gravity = Gravity.CENTER
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                            }
                        }
                    }
                }
            }
        })
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<StatisticsFragment> {
        lateinit var cifSupportSpinner: Spinner
        lateinit var aprSupportSpinner: Spinner
        lateinit var aclSupportSpinner: Spinner
        lateinit var dropoutSupportSpinner: Spinner

        lateinit var cifYearSpinner: Spinner
        lateinit var aprYearSpinner: Spinner
        lateinit var aclYearSpinner: Spinner
        lateinit var dropoutYearSpinner: Spinner

        lateinit var gmlTypeSpinner: Spinner
        lateinit var gmlYearSpinner: Spinner
        lateinit var gmlFromMonthSpinner: Spinner
        lateinit var gmlToMonthSpinner: Spinner

        lateinit var cifDetailContainer: LinearLayout
        lateinit var cifDataContainer: LinearLayout

        lateinit var aprDetailContainer: LinearLayout
        lateinit var aprDataContainer: LinearLayout

        lateinit var aclDetailContainer: LinearLayout
        lateinit var aclDataContainer: LinearLayout

        lateinit var dropoutDetailContainer: LinearLayout
        lateinit var dropoutDataContainer: LinearLayout

        lateinit var gmlDetailContainer: LinearLayout
        lateinit var gmlDataContainer: LinearLayout

        lateinit var cifToggleImageView: ImageView
        lateinit var aprToggleImageView: ImageView
        lateinit var aclToggleImageView: ImageView
        lateinit var gmlToggleImageView: ImageView
        lateinit var dropoutToggleImageView: ImageView

        override fun createView(ui: AnkoContext<StatisticsFragment>) = with(ui) {
            scrollView {
                verticalLayout {
                    linearLayout {
                        padding = dip(10)
                        gravity = Gravity.CENTER_VERTICAL

                        textView("CIF") {
                            typeface = Typeface.DEFAULT_BOLD
                        }

                        space {  }.lparams(width = dip(10), height = matchParent)
                        cifSupportSpinner = spinner { }
                        cifYearSpinner = spinner { }

                        space {  }.lparams(width = 0, height = matchParent, weight = 1f)

                        cifToggleImageView = imageView {
                            imageResource = R.drawable.select_4
                        }.lparams(width = dimen(R.dimen.px35), height = dimen(R.dimen.px35))
                    }
                    cifDetailContainer = verticalLayout {
                        visibility = View.GONE

                        linearLayout {
                            minimumHeight = dimen(R.dimen.px70)
                            textView("Mth") {
                                gravity = Gravity.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_title_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView("Sub.G") {
                                gravity = Gravity.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_title_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView("NR") {
                                gravity = Gravity.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_title_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView("WA") {
                                gravity = Gravity.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_title_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView("RE") {
                                gravity = Gravity.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_title_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView("AP") {
                                gravity = Gravity.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_title_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                            }
                        }.lparams(width = matchParent, height = wrapContent)

                        cifDataContainer = verticalLayout {

                        }
                    }

                    space {}.lparams(width = matchParent, height = dip(10))
                    view { backgroundColorResource = R.color.colorSplitLine }.lparams(width = matchParent, height = dip(1))
                    linearLayout {
                        padding = dip(10)
                        gravity = Gravity.CENTER_VERTICAL

                        textView("APR") {
                            typeface = Typeface.DEFAULT_BOLD
                        }

                        space {  }.lparams(width = dip(10), height = matchParent)
                        aprSupportSpinner = spinner { }
                        aprYearSpinner = spinner { }

                        space {  }.lparams(width = 0, height = matchParent, weight = 1f)

                        aprToggleImageView = imageView {
                            imageResource = R.drawable.select_4
                        }.lparams(width = dimen(R.dimen.px35), height = dimen(R.dimen.px35))
                    }
                    aprDetailContainer = verticalLayout {
                        visibility = View.GONE
                        linearLayout {
                            minimumHeight = dimen(R.dimen.px70)
                            textView("Sub.G") {
                                gravity = Gravity.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_title_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView("NR") {
                                gravity = Gravity.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_title_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView("WA") {
                                gravity = Gravity.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_title_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView("RE") {
                                gravity = Gravity.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_title_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView("AP") {
                                gravity = Gravity.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_title_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                            }
                        }.lparams(width = matchParent, height = wrapContent)

                        aprDataContainer = verticalLayout {

                        }
                    }

                    space {}.lparams(width = matchParent, height = dip(10))
                    view { backgroundColorResource = R.color.colorSplitLine }.lparams(width = matchParent, height = dip(1))
                    linearLayout {
                        padding = dip(10)
                        gravity = Gravity.CENTER_VERTICAL

                        textView("ACL") {
                            typeface = Typeface.DEFAULT_BOLD
                        }

                        space {  }.lparams(width = dip(10), height = matchParent)
                        aclSupportSpinner = spinner { }
                        aclYearSpinner = spinner { }

                        space {  }.lparams(width = 0, height = matchParent, weight = 1f)

                        aclToggleImageView = imageView {
                            imageResource = R.drawable.select_4
                        }.lparams(width = dimen(R.dimen.px35), height = dimen(R.dimen.px35))
                    }
                    aclDetailContainer = verticalLayout {
                        visibility = View.GONE
                        linearLayout {
                            minimumHeight = dimen(R.dimen.px70)
                            textView("Sub.G") {
                                gravity = Gravity.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_title_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView("NR") {
                                gravity = Gravity.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_title_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView("WA") {
                                gravity = Gravity.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_title_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView("RE") {
                                gravity = Gravity.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_title_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView("AP") {
                                gravity = Gravity.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_title_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                            }
                        }.lparams(width = matchParent, height = wrapContent)

                        aclDataContainer = verticalLayout {

                        }
                    }

                    space {}.lparams(width = matchParent, height = dip(10))
                    view { backgroundColorResource = R.color.colorSplitLine }.lparams(width = matchParent, height = dip(1))
                    linearLayout {
                        padding = dip(10)
                        gravity = Gravity.CENTER_VERTICAL

                        textView("GML") {
                            typeface = Typeface.DEFAULT_BOLD
                        }

                        space {  }.lparams(width = 0, height = matchParent, weight = 1f)

                        gmlToggleImageView = imageView {
                            imageResource = R.drawable.select_4
                        }.lparams(width = dimen(R.dimen.px35), height = dimen(R.dimen.px35))
                    }
                    gmlDetailContainer = verticalLayout {
                        visibility = View.GONE
                        linearLayout {
                            padding = dip(10)
                            gravity = Gravity.CENTER_VERTICAL

                            val items = listOf("GM", "Letter")
                            val gmlAdapter = ArrayAdapter(owner.requireContext(), R.layout.spinneritem_dark, items)
                            gmlAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            gmlTypeSpinner = spinner {
                                adapter = gmlAdapter
                            }
                            space {  }.lparams(width = dip(10), height = matchParent)
                            gmlYearSpinner = spinner { }
                            space {  }.lparams(width = dip(10), height = matchParent)
                            gmlFromMonthSpinner = spinner { }
                            textView(" ~  ")
                            gmlToMonthSpinner = spinner { }
                        }

                        linearLayout {
                            minimumHeight = dimen(R.dimen.px70)
                            textView("Mth") {
                                gravity = Gravity.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_title_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }

                            verticalLayout {
                                textView("Sub.G") {
                                    gravity = Gravity.CENTER
                                    typeface = Typeface.DEFAULT_BOLD
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_title_border
                                }

                                linearLayout {
                                    textView("count") {
                                        gravity = Gravity.CENTER
                                        typeface = Typeface.DEFAULT_BOLD
                                        padding = dimen(R.dimen.px10)
                                        backgroundResource = R.drawable.layout_title_border
                                    }.lparams(width = 0, height = matchParent, weight = 1f) {
                                        rightMargin = dip(-1)
                                        topMargin = dip(-1)
                                    }
                                    textView("$") {
                                        gravity = Gravity.CENTER
                                        typeface = Typeface.DEFAULT_BOLD
                                        padding = dimen(R.dimen.px10)
                                        backgroundResource = R.drawable.layout_title_border
                                    }.lparams(width = 0, height = matchParent, weight = 1f) {
                                        rightMargin = dip(-1)
                                        topMargin = dip(-1)
                                    }
                                }
                            }.lparams(width = 0, height = matchParent, weight = 2f) {
                                rightMargin = dip(-1)
                            }

                            verticalLayout {
                                textView("Status") {
                                    gravity = Gravity.CENTER
                                    typeface = Typeface.DEFAULT_BOLD
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_title_border
                                }
                                linearLayout {
                                    textView("NR") {
                                        gravity = Gravity.CENTER
                                        typeface = Typeface.DEFAULT_BOLD
                                        padding = dimen(R.dimen.px10)
                                        backgroundResource = R.drawable.layout_title_border
                                    }.lparams(width = 0, height = matchParent, weight = 1f) {
                                        rightMargin = dip(-1)
                                        topMargin = dip(-1)
                                    }
                                    textView("WA") {
                                        gravity = Gravity.CENTER
                                        typeface = Typeface.DEFAULT_BOLD
                                        padding = dimen(R.dimen.px10)
                                        backgroundResource = R.drawable.layout_title_border
                                    }.lparams(width = 0, height = matchParent, weight = 1f) {
                                        rightMargin = dip(-1)
                                        topMargin = dip(-1)
                                    }
                                    textView("RE") {
                                        gravity = Gravity.CENTER
                                        typeface = Typeface.DEFAULT_BOLD
                                        padding = dimen(R.dimen.px10)
                                        backgroundResource = R.drawable.layout_title_border
                                    }.lparams(width = 0, height = matchParent, weight = 1f) {
                                        rightMargin = dip(-1)
                                        topMargin = dip(-1)
                                    }
                                    textView("AP") {
                                        gravity = Gravity.CENTER
                                        typeface = Typeface.DEFAULT_BOLD
                                        padding = dimen(R.dimen.px10)
                                        backgroundResource = R.drawable.layout_title_border
                                    }.lparams(width = 0, height = matchParent, weight = 1f) {
                                        topMargin = dip(-1)
                                    }
                                }
                            }.lparams(width = 0, height = matchParent, weight = 4f) {
                                rightMargin = dip(-1)
                            }

                        }.lparams(width = matchParent, height = wrapContent)

                        gmlDataContainer = verticalLayout {

                        }
                    }

                    space {}.lparams(width = matchParent, height = dip(10))
                    view { backgroundColorResource = R.color.colorSplitLine }.lparams(width = matchParent, height = dip(1))
                    linearLayout {
                        padding = dip(10)
                        gravity = Gravity.CENTER_VERTICAL
                        textView("Drop-out") {
                            typeface = Typeface.DEFAULT_BOLD
                        }

                        space {  }.lparams(width = dip(10), height = matchParent)
                        dropoutSupportSpinner = spinner { }
                        dropoutYearSpinner = spinner { }

                        space {  }.lparams(width = 0, height = matchParent, weight = 1f)

                        dropoutToggleImageView = imageView {
                            imageResource = R.drawable.select_4
                        }.lparams(width = dimen(R.dimen.px35), height = dimen(R.dimen.px35))
                    }
                    dropoutDetailContainer = verticalLayout {
                        visibility = View.GONE
                        linearLayout {
                            minimumHeight = dimen(R.dimen.px70)
                            textView("Registered Children") {
                                gravity = Gravity.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_title_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView("Drop-out") {
                                gravity = Gravity.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_title_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView("Return") {
                                gravity = Gravity.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_title_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                            textView("Waiting") {
                                gravity = Gravity.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                                padding = dimen(R.dimen.px10)
                                backgroundResource = R.drawable.layout_title_border
                            }.lparams(width = 0, height = matchParent, weight = 1f) {
                                rightMargin = dip(-1)
                            }
                        }.lparams(width = matchParent, height = wrapContent)

                        dropoutDataContainer = verticalLayout {

                        }
                    }
                    space {}.lparams(width = matchParent, height = dip(10))
                }

            }
        }
    }
}
