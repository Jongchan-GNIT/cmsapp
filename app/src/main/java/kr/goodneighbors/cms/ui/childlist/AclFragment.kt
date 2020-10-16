@file:Suppress("ConstantConditionIf")

package kr.goodneighbors.cms.ui.childlist


import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.graphics.Typeface
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
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.common.Constants
import kr.goodneighbors.cms.extensions.convertDateFormat
import kr.goodneighbors.cms.service.model.AclListItem
import kr.goodneighbors.cms.service.viewmodel.AclViewModel
import kr.goodneighbors.cms.ui.BaseActivityFragment
import kr.goodneighbors.cms.ui.DialogImageViewFragment
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.button
import org.jetbrains.anko.dimen
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.imageView
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.space
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent
import java.io.File

class AclFragment : BaseActivityFragment() {
    companion object {
        fun newInstance(chrcp_no: String): AclFragment {
            val fragment = AclFragment()
            val args = Bundle()
            args.putString("chrcp_no", chrcp_no)

            fragment.arguments = args
            return fragment
        }
    }

    private val ui = FragmentUI()

    private val viewModel: AclViewModel by lazy {
        //ViewModelProviders.of(this).get(ReportViewModel::class.java)
        AclViewModel()
    }

    private lateinit var chrcp_no: String

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getReports().observe(this, Observer {
            if (it == null || it.isEmpty()) {
                ui.messageTextView!!.visibility = View.GONE
            } else {
                ui.messageTextView!!.visibility = View.GONE
            }
            ui.recyclerView!!.adapter = AclListAdaptor(list = ArrayList(it),
                    onTitleClickListener = { _it -> onTitleClickListener(_it) },
                    onImageClickListener = { _it -> onImageClickListener(_it) })
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return ui.createView(AnkoContext.create(requireContext(), this))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = "ACL"

        chrcp_no = arguments!!.getString("chrcp_no", "")

        val recyclerView = ui.recyclerView!!
        recyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.setId(chrcp_no)
    }

    fun onTitleClickListener(item: AclListItem) {
        changeFragment.onChangeFragment(AclEditFragment.newInstance(chrcp_no = item.CHRCP_NO!!, rcp_no = item.RCP_NO, year = item.YEAR!!))
    }

    fun onImageClickListener(item: AclListItem) {
        val sdMain = Environment.getExternalStorageDirectory()
        val targetDir = File("$sdMain/${Constants.DIR_HOME}/${Constants.DIR_CONTENTS}")

        if (!item.GENERAL_FILE_PATH.isNullOrBlank()) {
            val thumnail = File(targetDir, "${item.GENERAL_FILE_PATH}")
            if (thumnail.exists()) {
                val ft = activity!!.supportFragmentManager.beginTransaction()
                val newFragment = DialogImageViewFragment.newInstance(thumnail.path)
                newFragment.show(ft, "acl_fragment_view")
            }
        }
        else if (item.RPT_STCD == "16") {
            changeFragment.onChangeFragment(AclEditFragment.newInstance(chrcp_no = item.CHRCP_NO!!, rcp_no = item.RCP_NO, year = item.YEAR!!))
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<AclFragment> {
        var recyclerView: RecyclerView? = null
        var messageTextView: TextView? = null

        override fun createView(ui: AnkoContext<AclFragment>) = with(ui) {
            verticalLayout {
                messageTextView = textView("") {
                    gravity = Gravity.CENTER
                    setTypeface(null, Typeface.BOLD)
                    visibility = View.GONE
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
                        setBackgroundResource(R.drawable.gnb_bgl_off)
                        textColorResource = R.color.colorWhite
                        allCaps = false
                        onClick {
                            owner.onChangeBottomNavigation(2)
                        }
                    }.lparams(width = 0, height = dimen(R.dimen.px112), weight = 1f)
                    button("ACL") {
                        setBackgroundResource(R.drawable.gnb_bgl_on)
                        textColorResource = R.color.colorWhite
                        allCaps = false
//                        onClickListener {
//                            owner.onChangeBottomNavigation(3)
//                        }
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

    class AclListAdaptor(var list: ArrayList<AclListItem> = arrayListOf(), val onTitleClickListener: (AclListItem) -> Unit, val onImageClickListener: (AclListItem) -> Unit) : RecyclerView.Adapter<AclListAdaptor.AclListAdaptorViewHolder>() {
        override fun getItemCount(): Int = list.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AclListAdaptorViewHolder {
            return AclListAdaptorViewHolder(ListViewHolderUI().createView(AnkoContext.create(parent.context, parent)))
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: AclListAdaptorViewHolder, position: Int) {
            val item = list[position]

            if (item.RPT_STCD == "16") {
                holder.containerView.setBackgroundResource(R.color.colorBgAccent)
                holder.titleTextView.textColorResource = R.color.colorWhite
                holder.aprvDateTextView.textColorResource = R.color.colorWhite
                holder.typeTextView.textColorResource = R.color.colorWhite
                holder.statusTextView.textColorResource = R.color.colorWhite

                holder.imageView.imageResource = R.drawable.add
            } else {
                holder.containerView.setBackgroundResource(R.color.colorLightGray)
                holder.titleTextView.setTextColor(holder.hiddenTextView.textColors)
                holder.aprvDateTextView.setTextColor(holder.hiddenTextView.textColors)
                holder.typeTextView.setTextColor(holder.hiddenTextView.textColors)
                holder.statusTextView.setTextColor(holder.hiddenTextView.textColors)

                val sdMain = Environment.getExternalStorageDirectory()
                val targetDir = File("$sdMain/${Constants.DIR_HOME}/${Constants.DIR_CONTENTS}")

                if (item.GENERAL_FILE_PATH.isNullOrBlank()) {
                    holder.imageView.imageResource = R.drawable.icon_3
                } else {
                    val thumnail = File(targetDir, "${item.GENERAL_FILE_PATH}")
                    if (thumnail.exists()) {
                        Glide.with(holder.itemView).load(thumnail).into(holder.imageView)
                    } else {
                        holder.imageView.imageResource = R.drawable.icon_3
                    }
                }
            }

            holder.titleContainer.setOnClickListener {
                onTitleClickListener(item)
            }

            holder.imageView.onClick {
                onImageClickListener(item)
            }

            holder.titleTextView.text = "${item.YEAR ?: "-"} ACL"
            holder.aprvDateTextView.text = "Approved Date: ${item.APRV_DT?.convertDateFormat() ?: "-"}"
            holder.typeTextView.text = "Type: ${item.RPT_TYPE_NM ?: "-"}, Substitued: ${item.SWRT_YN ?: "-"}"
            holder.statusTextView.text = "Status: ${item.RPT_STNM ?: "-"}"
        }

        inner class AclListAdaptorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val hiddenTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_COLOR)!!

            val containerView = itemView.findViewById<ViewGroup>(ListViewHolderUI.CONTAINER_ID)!!
            val titleTextView = itemView.findViewById<TextView>(ListViewHolderUI.TITLE_ID)!!
            val imageView = itemView.findViewById<ImageView>(ListViewHolderUI.IMAGE_ID)!!
            val aprvDateTextView = itemView.findViewById<TextView>(ListViewHolderUI.APRV_DT)!!
            val typeTextView = itemView.findViewById<TextView>(ListViewHolderUI.TYPE)!!
            val statusTextView = itemView.findViewById<TextView>(ListViewHolderUI.STATUS)!!

            val titleContainer = itemView.findViewById<ViewGroup>(ListViewHolderUI.ID_TITLE_CONTAINER)!!
        }

        class ListViewHolderUI : AnkoComponent<ViewGroup> {
            companion object {
                const val ID_COLOR = 0

                const val CONTAINER_ID = 1
                const val TITLE_ID = 2
                const val IMAGE_ID = 3
                const val APRV_DT = 4
                const val TYPE = 5
                const val STATUS = 6

                const val ID_TITLE_CONTAINER = 100
            }

            override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                linearLayout {
                    lparams(matchParent, wrapContent) {
                        topMargin = dimen(R.dimen.px20)
                    }
                    id = CONTAINER_ID
                    padding = dimen(R.dimen.px20)
                    gravity = Gravity.CENTER_VERTICAL
                    backgroundColorResource = R.color.colorBgLiteGray

                    textView {
                        id = ID_COLOR
                        visibility = View.GONE
                    }

                    verticalLayout {
                        gravity = Gravity.CENTER

                        imageView {
                            imageResource = R.drawable.add
                            id = IMAGE_ID
                        }.lparams(width = dimen(R.dimen.px88), height = dimen(R.dimen.px88))
                    }.lparams(width = dimen(R.dimen.px128), height = dimen(R.dimen.px171))
                    space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)
                    verticalLayout {
                        id = ID_TITLE_CONTAINER
                        textView("{{2018}} ACL") {
                            setTypeface(null, Typeface.BOLD)
                            id = TITLE_ID
                        }
                        textView("Approved Date: {{}}") { id = APRV_DT }
                        textView("Type: {{}}, Substitued: {{}}") { id = TYPE }
                        textView("Status: {{}}") { id = STATUS }
                    }.lparams(width = 0, height = wrapContent, weight = 1f)

                }
            }
        }
    }
}
