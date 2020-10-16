@file:Suppress("PrivatePropertyName")

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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.gson.GsonBuilder
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.common.Constants
import kr.goodneighbors.cms.extensions.convertDateFormat
import kr.goodneighbors.cms.service.model.GmlListItem
import kr.goodneighbors.cms.service.viewmodel.GmlViewModel
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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class GmlFragment : BaseActivityFragment() {
    companion object {
        fun newInstance(chrcp_no: String): GmlFragment {
            val fragment = GmlFragment()
            val args = Bundle()
            args.putString("chrcp_no", chrcp_no)

            fragment.arguments = args
            return fragment
        }
    }

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(GmlFragment::class.java)
    }

    private val viewModel: GmlViewModel by lazy {
        GmlViewModel()
    }

    private val ui = FragmentUI()

    private lateinit var chrcp_no: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chrcp_no = arguments!!.getString("chrcp_no")!!


        viewModel.getReports().observe(this, Observer {
            logger.debug("onCreate : viewModel.getReports() - $it")
            it?.apply {
                ui.recyclerView.adapter = GmlListAdaptor(
                        list = ArrayList(this),
                        onImageClickListener = {path->
                            val ft = requireActivity().supportFragmentManager.beginTransaction()
                            val newFragment = DialogImageViewFragment.newInstance(path)
                            newFragment.show(ft, "view")
                        },
                        onClickListener = {item-> onClickListener(item) }
                )
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = ui.createView(AnkoContext.create(requireContext(), this))
        activity?.title = "GML"

        ui.recyclerView.layoutManager = LinearLayoutManager(context)
        viewModel.setGetReportsTrigger(chrcp_no)

        return v
    }

    fun onClickListener(item: GmlListItem) {
        logger.debug("onClickListener : ${GsonBuilder().create().toJson(item)}")
        when(item.TYPE) {
            "G"-> changeFragment.onChangeFragment(GmFragment.newInstance(chrcp_no = item.CHRCP_NO!!, rcp_no = item.RCP_NO, mng_no = item.MNG_NO!!))
            "L"-> changeFragment.onChangeFragment(GmLetterFragment.newInstance(chrcp_no = item.CHRCP_NO!!, rcp_no = item.RCP_NO, mng_no = item.MNG_NO!!))
        }
    }

    fun onChangeBottomNavigation(position: Int) {
        when(position) {
            0-> {
                changeFragment.onChangeFragment(ProfileFragment.newInstance(chrcp_no))
            }
            1-> {
                changeFragment.onChangeFragment(ReportFragment.newInstance(chrcp_no))
            }
            2-> {
                changeFragment.onChangeFragment(ProvidedServiceFragment.newInstance(chrcp_no))
            }
            3-> {
                changeFragment.onChangeFragment(AclFragment.newInstance(chrcp_no))
            }
            4-> {
                changeFragment.onChangeFragment(GmlFragment.newInstance(chrcp_no))
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<GmlFragment> {
        lateinit var recyclerView: RecyclerView

        override fun createView(ui: AnkoContext<GmlFragment>) = with(ui) {
            verticalLayout {
//                button("GM 등록") {
//                    onTitleClickListener {
//                        owner.changeFragment.onChangeFragment(GmFragment.newInstance())
//                    }
//                }
//                button("Letter 등록") {
//                    onTitleClickListener {
//                        owner.changeFragment.onChangeFragment(GmLetterFragment.newInstance())
//                    }
//                }

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
                        setBackgroundResource(R.drawable.gnb_bgl_off)
                        textColorResource = R.color.colorWhite
                        allCaps = false
                        onClick {
                            owner.onChangeBottomNavigation(3)
                        }
                    }.lparams(width = 0, height = dimen(R.dimen.px112), weight = 1f)
                    button("GML") {
                        setBackgroundResource(R.drawable.gnb_bgr_on)
                        textColorResource = R.color.colorWhite
                        allCaps = false
//                        onClickListener {
//                            owner.onChangeBottomNavigation(4)
//                        }
                    }.lparams(width = 0, height = dimen(R.dimen.px112), weight = 1f)
                }
            }
        }
    }

    class GmlListAdaptor(
            val list: ArrayList<GmlListItem> = arrayListOf(),
            val onImageClickListener:(String) -> Unit,
            val onClickListener: (GmlListItem) -> Unit
    ) : RecyclerView.Adapter<GmlListAdaptor.ListAdaptorViewHolder>()
    {
        private val sdMain = Environment.getExternalStorageDirectory()
        private val targetDir = File("$sdMain/${Constants.DIR_HOME}/${Constants.DIR_CONTENTS}")

        override fun getItemCount(): Int = list.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdaptorViewHolder {
            return ListAdaptorViewHolder(ListViewHolderUI().createView(AnkoContext.create(parent.context, parent)))
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ListAdaptorViewHolder, position: Int) {
            val item = list[position]
            var thumb: File?= null

            if (item.RPT_STCD == "16") {
                holder.containerView.backgroundColorResource = R.color.colorBgAccent
                holder.titleTextView.textColorResource = R.color.colorWhite
                holder.approvedDateTextView.textColorResource = R.color.colorWhite
                holder.substituedTextView.textColorResource = R.color.colorWhite
                holder.statusTextView.textColorResource = R.color.colorWhite

                holder.profileImageView.imageResource = R.drawable.add
            }
            else {
                holder.containerView.backgroundColorResource = R.color.colorLightGray
                holder.titleTextView.setTextColor(holder.hiddenTextView.textColors)
                holder.approvedDateTextView.setTextColor(holder.hiddenTextView.textColors)
                holder.substituedTextView.setTextColor(holder.hiddenTextView.textColors)
                holder.statusTextView.setTextColor(holder.hiddenTextView.textColors)

                if (item.FILE_PATH != null) {
                    thumb = File(targetDir, item.FILE_PATH)
                    if (!thumb.exists()) thumb = null
                }

                if (thumb != null) {
                    Glide.with(holder.containerView).load(thumb)
                            .apply(RequestOptions.skipMemoryCacheOf(true))
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                            .into(holder.profileImageView)

                }
                else {
                    holder.profileImageView.imageResource = R.drawable.icon_3
                }
            }
            holder.profileImageView.onClick {
                if (thumb != null) {
                    onImageClickListener(thumb.path)
                }
                else {
                    onClickListener(item)
                }
            }

            holder.titleContainerView.onClick {
                onClickListener(item)
            }

            holder.titleTextView.text = "${item.RCP_DT?.convertDateFormat(before = "yyyyMMdd", after = "yyyyMM")} ${item.TYPE}"
            holder.approvedDateTextView.text = "Approved Date: ${(item.APRV_DT?.convertDateFormat())?:"-"}"
            holder.substituedTextView.text = "Substitued: ${item.SWRT_YN?:"-"}"
            holder.statusTextView.text = "Status: ${item.RPT_STNM?:"-"}"
        }

        inner class ListAdaptorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val hiddenTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_COLOR)!!

            val containerView = itemView.findViewById<ViewGroup>(ListViewHolderUI.ID_CONTAINER)!!
            val titleContainerView = itemView.findViewById<ViewGroup>(ListViewHolderUI.ID_TITLE_CONTAINER)!!

            val titleTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_TITLE)!!
            val approvedDateTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_APPROVED_DATE)!!
            val substituedTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_SUBSTITUED)!!
            val statusTextView = itemView.findViewById<TextView>(ListViewHolderUI.ID_STATUS)!!

            val profileImageView = itemView.findViewById<ImageView>(ListViewHolderUI.ID_PROFILE_IMAGE)!!
        }

        class ListViewHolderUI: AnkoComponent<ViewGroup> {
            companion object {
                const val ID_COLOR = 0
                const val ID_TITLE = 1
                const val ID_APPROVED_DATE = 2
                const val ID_SUBSTITUED = 3
                const val ID_STATUS = 4
                const val ID_PROFILE_IMAGE = 5

                const val ID_CONTAINER = 100
                const val ID_TITLE_CONTAINER = 101
            }

            override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                linearLayout {
                    id = ID_CONTAINER
                    lparams(matchParent, wrapContent) {
                        topMargin = dimen(R.dimen.px20)
                    }

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
                            id = ID_PROFILE_IMAGE
                        }.lparams(width = dimen(R.dimen.px88), height = dimen(R.dimen.px88))
                    }.lparams(width = dimen(R.dimen.px128), height = dimen(R.dimen.px171))
                    space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)
                    verticalLayout {
                        id = ID_TITLE_CONTAINER
                        textView {
                            id = ID_TITLE
                            setTypeface(null, Typeface.BOLD)
                        }
                        textView("Approved Date: {{}}" ) { id = ID_APPROVED_DATE}
                        textView("Substitued: {{}}" ) { id = ID_SUBSTITUED }
                        textView("Status: {{}}" ) { id = ID_STATUS }
                    }.lparams(width = 0, height = wrapContent, weight = 1f)
                }
            }
        }
    }
}
