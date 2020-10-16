package kr.goodneighbors.cms.ui.childlist

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.common.Constants
import kr.goodneighbors.cms.service.entities.RPT_BSC
import kr.goodneighbors.cms.service.viewmodel.ReportViewModel
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.space
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.topPadding
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent
import java.io.File

class SiblingSponsorshipFindDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(word: String, selectedCount: Int): SiblingSponsorshipFindDialogFragment {
            val fragment = SiblingSponsorshipFindDialogFragment()
            val args = Bundle()
            args.putString("word", word)
            args.putInt("count", selectedCount)

            fragment.arguments = args
            return fragment
        }
    }

    private val ui = FragmentUI()

    private val viewModel: ReportViewModel by lazy {
        ReportViewModel()
    }

    private lateinit var adapter:SiblingSponsorshipAdaptor

    private lateinit var word: String
    private var count: Int = 0

    private lateinit var siblings:ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        word = arguments!!.getString("word")
        count = arguments!!.getInt("count")

        viewModel.findAllSiblingSponsorship().observe(this, Observer {
            it?.apply {
                adapter = SiblingSponsorshipAdaptor(list = ArrayList(it), onClickListener = { _it -> onClickItem(_it) })
                ui.recyclerView.adapter = adapter
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val v = ui.createView(AnkoContext.create(requireContext(), this))

        siblings = ArrayList()

        ui.cancelButtonTextView.onClick {
            dismiss()
        }

        ui.saveButtonTextView.onClick {
            val data = Intent()
            data.putExtra("siblings", siblings)
            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, data)
            dismiss()
        }

        ui.recyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.setFindAllSiblingSponsorship(word)
        return v
    }

    override fun onStart() {
        super.onStart()
        dialog.window.setBackgroundDrawableResource(R.drawable.rounded_dialog)
        dialog.window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setCanceledOnTouchOutside(false)
    }

    private fun onClickItem(item: RPT_BSC) {
        val chrcp_no = item.CH_MST!!.CHRCP_NO
        val child_code = "${item.CH_MST?.CTR_CD?:""}-${item.CH_MST?.BRC_CD?:""}${item.CH_MST?.PRJ_CD?:""}-${item.CH_MST?.CH_CD?:""}"
        if (siblings.contains(child_code)) {
            siblings.remove(child_code)
            adapter.removeSelectItem(item.CH_MST!!.CHRCP_NO)
        }
        else {
            if (siblings.size < 2 - count) {
                siblings.add(child_code)
                adapter.addSelectItem(item.CH_MST!!.CHRCP_NO)
            }
            else {
                toast(R.string.message_validate_exceeded_number)
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<SiblingSponsorshipFindDialogFragment> {
        lateinit var cancelButtonTextView: TextView
        lateinit var saveButtonTextView: TextView

        lateinit var recyclerView: RecyclerView

        override fun createView(ui: AnkoContext<SiblingSponsorshipFindDialogFragment>) = with(ui) {
            verticalLayout {
                verticalLayout {
                    topPadding = dimen(R.dimen.px40)
                    leftPadding = dimen(R.dimen.px40)
                    rightPadding = dimen(R.dimen.px40)

                    linearLayout {
                        minimumHeight = dimen(R.dimen.px70)

                        textView(R.string.label_name) {
                            //                            backgroundResource = R.drawable.layout_border_bottom
                            setTypeface(null, Typeface.BOLD)
                            textColorResource = R.color.colorPrimary
                            gravity = Gravity.CENTER_VERTICAL
                        }.lparams(width = 0, height = matchParent, weight = 0.5f) {
                        }

                        textView(R.string.label_code) {
                            //                            backgroundResource = R.drawable.layout_border_bottom
                            setTypeface(null, Typeface.BOLD)
                            textColorResource = R.color.colorPrimary
                            gravity = Gravity.CENTER_VERTICAL
                        }.lparams(width = 0, height = matchParent, weight = 0.5f) {
                        }
                    }

                    recyclerView = recyclerView {

                    }.lparams(width = matchParent, height = dimen(R.dimen.px500))
                }
                space { }.lparams(width = matchParent, height = dimen(R.dimen.px40))

                linearLayout {
                    minimumHeight = dimen(R.dimen.px100)

                    cancelButtonTextView = textView(R.string.label_cancel) {
                        backgroundResource = R.drawable.layout_border
                        setTypeface(null, Typeface.BOLD)
                        textColorResource = R.color.colorBlack
                        gravity = Gravity.CENTER
                    }.lparams(width = 0, height = matchParent, weight = 1f) {
                        bottomMargin = dip(-1)
                        leftMargin = dip(-1)
                        rightMargin = dip(-1)
                    }

                    saveButtonTextView = textView(R.string.label_save) {
                        backgroundResource = R.drawable.layout_border
                        setTypeface(null, Typeface.BOLD)
                        textColorResource = R.color.colorAccent
                        gravity = Gravity.CENTER
                    }.lparams(width = 0, height = matchParent, weight = 1f) {
                        bottomMargin = dip(-1)
                        rightMargin = dip(-1)
                    }
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class SiblingSponsorshipAdaptor(var list: ArrayList<RPT_BSC>, val onClickListener: (RPT_BSC) -> Unit) : RecyclerView.Adapter<SiblingSponsorshipAdaptor.ListAdaptorViewHolder>() {
        private val sdMain = Environment.getExternalStorageDirectory()
        private val contentsRootDir = File("$sdMain/${Constants.DIR_HOME}/${Constants.DIR_CONTENTS}")
        private val selectedItems = ArrayList<String>()

        override fun getItemCount(): Int = list.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdaptorViewHolder {
            return ListAdaptorViewHolder(ListViewHolderUI().createView(AnkoContext.create(parent.context, parent)))
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ListAdaptorViewHolder, position: Int) {
            val item = list[position]

            val chrcp_no = item.CHRCP_NO!!

            if (selectedItems.contains(chrcp_no)) {
                holder.containerView.backgroundColorResource = R.color.colorLightGray
            }
            else {
                holder.containerView.backgroundColorResource = R.color.colorWhite
            }

            holder.containerView.onClick {
                onClickListener(item)
            }
            holder.nameTextView.text = "${item.CH_MST?.CH_EFNM?:""} ${item.CH_MST?.CH_EMNM?:""} ${item.CH_MST?.CH_ELNM?:""}"
            holder.codeTextView.text = "${item.CH_MST?.CTR_CD?:""}-${item.CH_MST?.BRC_CD?:""}${item.CH_MST?.PRJ_CD?:""}-${item.CH_MST?.CH_CD?:""}"
        }

        fun addSelectItem(chrcp_no: String) {
            selectedItems.add(chrcp_no)
            notifyDataSetChanged()
        }

        fun removeSelectItem(chrcp_no: String) {
            selectedItems.remove(chrcp_no)
            notifyDataSetChanged()
        }

        inner class ListAdaptorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var containerView: ViewGroup = itemView.findViewById(ListViewHolderUI.CONTAINER_ID)
//            var profileImageView: ImageView = itemView.findViewById(ListViewHolderUI.IMAGE_ID)
            var nameTextView: TextView = itemView.findViewById(ListViewHolderUI.NAME_ID)
            var codeTextView: TextView = itemView.findViewById(ListViewHolderUI.CODE_ID)
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        class ListViewHolderUI : AnkoComponent<ViewGroup> {
            companion object {
                const val CONTAINER_ID = 0
//                const val IMAGE_ID = 1
                const val NAME_ID = 2
                const val CODE_ID = 3
            }

            override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                verticalLayout {
                    lparams(width = matchParent, height = wrapContent)

                    linearLayout {
                        id = CONTAINER_ID
                        padding = dimen(R.dimen.px10)

                        gravity = Gravity.CENTER_VERTICAL
                        minimumHeight = dimen(R.dimen.px70)

                        linearLayout {
//                            imageView {
//                                id = IMAGE_ID
//                            }.lparams(width = dimen(R.dimen.px76), height = dimen(R.dimen.px76))

                            textView("{{}}") {
                                gravity = Gravity.CENTER_VERTICAL
                                id = NAME_ID
                            }.lparams(width = 0, height = wrapContent, weight = 1f)
                        }.lparams(width = 0, height = matchParent, weight = 0.5f)

                        textView("{{}}") {
                            gravity = Gravity.CENTER_VERTICAL

                            id = CODE_ID
                        }.lparams(width = 0, height = matchParent, weight = 0.5f)
                    }
                }
            }
        }
    }
}