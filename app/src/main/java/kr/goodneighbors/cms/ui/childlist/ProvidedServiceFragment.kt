package kr.goodneighbors.cms.ui.childlist


import android.arch.lifecycle.Observer
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.extensions.convertDateFormat
import kr.goodneighbors.cms.extensions.observeOnce
import kr.goodneighbors.cms.service.entities.CH_MST
import kr.goodneighbors.cms.service.model.ProvidedServiceListItem
import kr.goodneighbors.cms.service.viewmodel.ProvidedServiceViewModel
import kr.goodneighbors.cms.ui.BaseActivityFragment
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.button
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.view
import org.jetbrains.anko.wrapContent

@Suppress("PrivatePropertyName")
class ProvidedServiceFragment : BaseActivityFragment() {
    companion object {
        const val REQUEST_CODE = 99

        fun newInstance(chrcp_no: String): ProvidedServiceFragment {
            val fragment = ProvidedServiceFragment()
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
    private var ch_mst:CH_MST ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getReports().observe(this, Observer {
            ui.recyclerView.adapter = ProvidedServiceListAdaptor(list = ArrayList(it), onClickEditButton = { _it -> onClickEditItem(_it) })
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = ui.createView(AnkoContext.create(requireContext(), this))
        activity?.title = "Provided service"

        chrcp_no = arguments!!.getString("chrcp_no")

        val recyclerView = ui.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.setId(chrcp_no)
        viewModel.getChild(chrcp_no).observeOnce(this, Observer {
            ch_mst = it
            it?.apply {
                if (CH_STCD == "1") setHasOptionsMenu(true)
            }
        })

        return v
    }

    private fun onClickEditItem(item: ProvidedServiceListItem) {
        val ft = activity!!.supportFragmentManager.beginTransaction()
        val newFragment = ProvidedServiceEditDialog.newInstance(
                chrcp_no = item.CHRCP_NO,
                svobj_dvcd = item.SVOBJ_DVCD,
                bsst_cd = item.BSST_CD,
                spbd_cd = item.SPBD_CD,
                title = item.TITLE,
                ch_stcd = ch_mst?.CH_STCD ?: "")
        newFragment.setTargetFragment(this, REQUEST_CODE)
        newFragment.show(ft, "")
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        activity?.menuInflater?.inflate(R.menu.toolbar_provide_regist, menu)

        // 저장 버튼 클릭
        menu?.findItem(R.id.toolbar_add_icon)!!.setOnMenuItemClickListener {
            add()
            true
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE -> {
                viewModel.setId(chrcp_no)
            }
        }
    }

    fun add() {
        changeFragment.onChangeFragment(ProvidedServiceRegistFragment.newInstance(chrcp_no))
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
    class FragmentUI : AnkoComponent<ProvidedServiceFragment> {
        lateinit var recyclerView: RecyclerView

        override fun createView(ui: AnkoContext<ProvidedServiceFragment>) = with(ui) {

            verticalLayout {
                textView(R.string.label_service_record) {
                    leftPadding = dimen(R.dimen.px20)
                    gravity = Gravity.CENTER_VERTICAL
//                    textColorResource = R.color.colorBlack
                    setTypeface(null, Typeface.BOLD)

                }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                recyclerView = recyclerView {

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

    class ProvidedServiceListAdaptor(var list: ArrayList<ProvidedServiceListItem> = arrayListOf(), val onClickEditButton: (ProvidedServiceListItem) -> Unit) : RecyclerView.Adapter<ProvidedServiceListAdaptor.ListAdaptorViewHolder>() {
        override fun getItemCount(): Int = list.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdaptorViewHolder {
            return ListAdaptorViewHolder(ListViewHolderUI().createView(AnkoContext.create(parent.context, parent)))
        }

        override fun onBindViewHolder(holder: ListAdaptorViewHolder, position: Int) {
            val item = list[position]
            holder.titleTextView.text = item.TITLE
            holder.countTextView.text = item.COUNT.toString()
            holder.dateTextView.text = (if (item.PRVD_DT?.length == 4) item.PRVD_DT + "0101" else item.PRVD_DT)?.convertDateFormat() ?: "-"
            holder.detailImageView.onClick {
                onClickEditButton(item)
            }
        }

        inner class ListAdaptorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var titleTextView: TextView = itemView.findViewById(ListViewHolderUI.TITLE_ID)
            var countTextView: TextView = itemView.findViewById(ListViewHolderUI.COUNT_ID)
            var dateTextView: TextView = itemView.findViewById(ListViewHolderUI.DATE_ID)
            var detailImageView: ImageView = itemView.findViewById(ListViewHolderUI.DETAIL_ID)
        }

        class ListViewHolderUI: AnkoComponent<ViewGroup> {
            companion object {
                const val TITLE_ID = 1
                const val COUNT_ID = 2
                const val DATE_ID = 3
                const val DETAIL_ID = 4
            }

            override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                verticalLayout {
                    lparams {
                        bottomMargin = dimen(R.dimen.px20)
                    }
                    view { backgroundColorResource = R.color.colorPrimary }.lparams(width = matchParent, height = dip(1))
                    linearLayout {
                        leftPadding = dimen(R.dimen.px20)
                        rightPadding = dimen(R.dimen.px20)
                        gravity = Gravity.CENTER_VERTICAL
                        backgroundColorResource = R.color.colorLightGray

                        textView("{{}}") {
                            setTypeface(null, Typeface.BOLD)
                            id = TITLE_ID
                        }.lparams(width = 0, height = wrapContent, weight = 1f)
                        imageView {
                            imageResource = R.drawable.more_dot

                            id = DETAIL_ID
                        }.lparams(width = dimen(R.dimen.px35), height = dimen(R.dimen.px35))
                    }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                    linearLayout {
                        minimumHeight = dimen(R.dimen.px70)

                        textView(R.string.label_total) {
                            gravity = Gravity.CENTER
                            backgroundResource = R.drawable.layout_border
                            setTypeface(null, Typeface.BOLD)
//                            textColorResource = R.color.colorBlack
                        }.lparams(width = 0, height = matchParent, weight = 1f) {
                            rightMargin = dip(-1)
                        }
                        textView(R.string.label_recently_provided_date) {
                            gravity = Gravity.CENTER
                            backgroundResource = R.drawable.layout_border
                            setTypeface(null, Typeface.BOLD)
//                            textColorResource = R.color.colorBlack
                        }.lparams(width = 0, height = matchParent, weight = 1f)
                    }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                    linearLayout {
                        minimumHeight = dimen(R.dimen.px70)

                        textView("{{}}") {
                            gravity = Gravity.CENTER
                            backgroundResource = R.drawable.layout_border
//                            textColorResource = R.color.colorBlack

                            id = COUNT_ID
                        }.lparams(width = 0, height = matchParent, weight = 1f) {
                            rightMargin = dip(-1)
                        }
                        textView("{{}}") {
                            gravity = Gravity.CENTER
                            backgroundResource = R.drawable.layout_border
//                            textColorResource = R.color.colorBlack

                            id = DATE_ID
                        }.lparams(width = 0, height = matchParent, weight = 1f)
                    }.lparams(width = matchParent, height = dimen(R.dimen.px70)) {
                        topMargin = dip(-1)
                    }
                }
            }
        }
    }
}
