@file:Suppress("PrivatePropertyName")

package kr.goodneighbors.cms.ui.childlist

import android.app.Activity
import android.app.DatePickerDialog
import android.arch.lifecycle.Observer
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.extensions.convertDateFormat
import kr.goodneighbors.cms.extensions.observeOnce
import kr.goodneighbors.cms.service.entities.SRVC
import kr.goodneighbors.cms.service.model.ProvidedServiceEditItem
import kr.goodneighbors.cms.service.model.ProvidedServiceEditSearchItem
import kr.goodneighbors.cms.service.viewmodel.ProvidedServiceViewModel
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.noButton
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.space
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.topPadding
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent
import org.jetbrains.anko.yesButton
import java.util.*

@Suppress("PropertyName")
class ProvidedServiceEditDialog : DialogFragment() {
    companion object {
        fun newInstance(chrcp_no: String, svobj_dvcd: String, bsst_cd: String, spbd_cd: String, title: String, ch_stcd: String): ProvidedServiceEditDialog {
            val fragment = ProvidedServiceEditDialog()
            val args = Bundle()
            args.putString("chrcp_no", chrcp_no)
            args.putString("svobj_dvcd", svobj_dvcd)
            args.putString("bsst_cd", bsst_cd)
            args.putString("spbd_cd", spbd_cd)
            args.putString("title", title)
            args.putString("ch_stcd", ch_stcd)

            fragment.arguments = args
            return fragment
        }
    }

    private val ui = FragmentUI()

    private val viewModel: ProvidedServiceViewModel by lazy {
        ProvidedServiceViewModel()
    }

    private lateinit var chrcp_no: String
    private lateinit var svobj_dvcd: String
    private lateinit var bsst_cd: String
    private lateinit var spbd_cd: String

    private lateinit var title: String
    private lateinit var ch_stcd: String

    private var selectedItem: ProvidedServiceEditItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chrcp_no = arguments!!.getString("chrcp_no")
        svobj_dvcd = arguments!!.getString("svobj_dvcd")
        bsst_cd = arguments!!.getString("bsst_cd")
        spbd_cd = arguments!!.getString("spbd_cd")
        title = arguments!!.getString("title")
        ch_stcd = arguments!!.getString("ch_stcd")

        viewModel.getServiceEditList().observe(this, Observer {
            ui.recyclerView.adapter = ProvidedServiceEditDialogAdaptor(
                    list = ArrayList(it),
                    onClickListener = { _it -> onClickListener(_it) }
            )
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = ui.createView(AnkoContext.create(requireContext(), this))

        ui.titleTextView.text = title

        ui.cancelButtonTextView.onClick {
            dismiss()
        }

        ui.saveButtonTextView.onClick {
            val items = (ui.recyclerView.adapter as ProvidedServiceEditDialogAdaptor).getEditedItems()
            if (items != null) {
                viewModel.saveServiceEditItems(items).observeOnce(this@ProvidedServiceEditDialog, Observer {
                    targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, Intent())
                    dismiss()
                })
            } else {
                dismiss()
            }
        }

        ui.editImageView.onClick {
            if (selectedItem != null) {
                val c = Calendar.getInstance()
                val srvc = selectedItem!!.srvc
                val d = if (srvc.PRVD_DT?.length == 4) srvc.PRVD_DT + "0101" else srvc.PRVD_DT
                val mYear = (d?.convertDateFormat("yyyyMMdd", "yyyy")?.toInt()) ?: c.get(Calendar.YEAR)
                val mMonth = (d?.convertDateFormat("yyyyMMdd", "MM")?.toInt()?.minus(1)) ?: c.get(Calendar.MONTH)
                val mDay = (d?.convertDateFormat("yyyyMMdd", "dd")?.toInt()) ?: c.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(activity,
                        DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                            selectedItem!!.srvc.PRVD_DT = "$year${(monthOfYear + 1).toString().padStart(2, '0')}${dayOfMonth.toString().padStart(2, '0')}"
                            selectedItem!!.isEdited = true
                            selectedItem!!.isSelected = false
                            selectedItem = null
                            ui.recyclerView.adapter?.notifyDataSetChanged()
                        }, mYear, mMonth, mDay)
                datePickerDialog.datePicker.maxDate = Date().time

                datePickerDialog.show()
            }
        }

        ui.deleteImageView.onClick {
            if (selectedItem != null) {
                alert(R.string.message_confirm_delete_service_provided_date) {
                    yesButton {
                        selectedItem!!.srvc.DEL_YN = "Y"
                        selectedItem!!.isSelected = false
                        selectedItem!!.isDeleted = true
                        selectedItem = null
                        ui.recyclerView.adapter?.notifyDataSetChanged()
                    }
                    noButton {}
                }.show()

            }
        }

        ui.recyclerView.layoutManager = LinearLayoutManager(context)
        viewModel.setServiceEditSearch(ProvidedServiceEditSearchItem(chrcp_no = chrcp_no, svobj_dvcd = svobj_dvcd, bsst_cd = bsst_cd, spbd_cd = spbd_cd))

        if (ch_stcd != "1") {
            ui.cancelButtonTextView.textResource = R.string.label_close
            ui.saveButtonTextView.visibility = View.GONE
            ui.editImageView.visibility = View.GONE
            ui.deleteImageView.visibility = View.GONE
        } else {
            ui.cancelButtonTextView.textResource = R.string.label_cancel
            ui.saveButtonTextView.visibility = View.VISIBLE
            ui.editImageView.visibility = View.VISIBLE
            ui.deleteImageView.visibility = View.VISIBLE
        }


        return v
    }

    override fun onStart() {
        super.onStart()
        dialog.window.setBackgroundDrawableResource(R.drawable.rounded_dialog)
        dialog.window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setCanceledOnTouchOutside(false)
    }


    private fun onClickListener(item: ProvidedServiceEditItem) {
        item.isSelected = !item.isSelected

        if (item.isSelected) {
            if (selectedItem == null) {
                selectedItem = item
            } else {
                selectedItem!!.isSelected = false
                selectedItem = item
            }
        } else {
            selectedItem = null
        }

        ui.recyclerView.adapter?.notifyDataSetChanged()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<ProvidedServiceEditDialog> {
        lateinit var cancelButtonTextView: TextView
        lateinit var saveButtonTextView: TextView

        lateinit var titleTextView: TextView
        lateinit var recyclerView: RecyclerView

        lateinit var editImageView: ImageView
        lateinit var deleteImageView: ImageView

        override fun createView(ui: AnkoContext<ProvidedServiceEditDialog>) = with(ui) {
            verticalLayout {
                verticalLayout {
                    topPadding = dimen(R.dimen.px40)
                    leftPadding = dimen(R.dimen.px40)
                    rightPadding = dimen(R.dimen.px40)
                    linearLayout {
                        titleTextView = textView("{{}}") {
                            setTypeface(null, Typeface.BOLD)
                        }.lparams(width = 0, height = matchParent, weight = 1f)

                        editImageView = imageView {
                            imageResource = R.drawable.modify
                        }.lparams(width = dimen(R.dimen.px46), height = dimen(R.dimen.px46))

                        deleteImageView = imageView {
                            imageResource = R.drawable.delete_3
                        }.lparams(width = dimen(R.dimen.px46), height = dimen(R.dimen.px46)) {
                            leftMargin = dimen(R.dimen.px20)
                        }
                    }

                    space { }.lparams(width = matchParent, height = dimen(R.dimen.px20))

                    linearLayout {
                        minimumHeight = dimen(R.dimen.px70)

                        textView("No") {
                            backgroundResource = R.drawable.layout_border
                            setTypeface(null, Typeface.BOLD)
//                            textColorResource = R.color.colorBlack
                            gravity = Gravity.CENTER
                        }.lparams(width = 0, height = matchParent, weight = 0.2f) {
                            leftMargin = dip(-1)
                            rightMargin = dip(-1)
                        }

                        textView(R.string.label_service_provided_date) {
                            backgroundResource = R.drawable.layout_border
                            setTypeface(null, Typeface.BOLD)
//                            textColorResource = R.color.colorBlack
                            gravity = Gravity.CENTER
                        }.lparams(width = 0, height = matchParent, weight = 0.8f) {
                            rightMargin = dip(-1)
                        }
                    }

                    recyclerView = recyclerView {

                    }
                }

                space { }.lparams(width = matchParent, height = dimen(R.dimen.px40))

                linearLayout {
                    minimumHeight = dimen(R.dimen.px100)

                    cancelButtonTextView = textView(R.string.label_cancel) {
                        backgroundResource = R.drawable.layout_border
                        setTypeface(null, Typeface.BOLD)
//                        textColorResource = R.color.colorBlack
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

    class ProvidedServiceEditDialogAdaptor(var list: ArrayList<ProvidedServiceEditItem> = arrayListOf(), val onClickListener: (ProvidedServiceEditItem) -> Unit) : RecyclerView.Adapter<ProvidedServiceEditDialogAdaptor.ListAdaptorViewHolder>() {
        fun getEditedItems(): ArrayList<SRVC>? {
            val selectedItems = ArrayList<SRVC>()
            list.filter { it.isEdited || it.isDeleted }.forEach {
                selectedItems.add(it.srvc)
            }

            if (selectedItems.isEmpty()) return null
            return selectedItems
        }

        override fun getItemCount(): Int = list.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdaptorViewHolder {
            return ListAdaptorViewHolder(ListViewHolderUI().createView(AnkoContext.create(parent.context, parent)))
        }

        override fun onBindViewHolder(holder: ListAdaptorViewHolder, position: Int) {
            val item = list[position]

            holder.rownumTextView.text = item.RNUM.toString()
            holder.dateTextView.text = (if (item.srvc.PRVD_DT?.length == 4) item.srvc.PRVD_DT + "0101" else item.srvc.PRVD_DT)?.convertDateFormat()
                    ?: "-"

            if (item.isSelected) {
                holder.dateTextView.backgroundColorResource = R.color.colorBgLiteGray
            } else {
                holder.dateTextView.backgroundResource = R.drawable.layout_border
            }

            if (item.isEdited) {
                holder.dateTextView.setTypeface(null, Typeface.BOLD_ITALIC)
            } else {
                holder.dateTextView.typeface = Typeface.DEFAULT
            }

            if (item.isDeleted) {
                holder.dateTextView.paintFlags = holder.dateTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                holder.dateTextView.typeface = Typeface.DEFAULT_BOLD
            } else {
                if (!item.isEdited) {
                    holder.dateTextView.paintFlags = 0
                    holder.dateTextView.typeface = Typeface.DEFAULT
                }
            }

            holder.dateTextView.onClick {
                if (!item.isDeleted) {
                    onClickListener(item)
                }
            }
        }

        inner class ListAdaptorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var rownumTextView: TextView = itemView.findViewById(ListViewHolderUI.ROW_ID)
            var dateTextView: TextView = itemView.findViewById(ListViewHolderUI.DATE_ID)
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        class ListViewHolderUI : AnkoComponent<ViewGroup> {
            companion object {
                const val ROW_ID = 1
                const val DATE_ID = 2
            }

            override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                verticalLayout {
                    lparams(width = matchParent, height = wrapContent)

                    linearLayout {
                        minimumHeight = dimen(R.dimen.px80)

                        textView("{{}}") {
                            backgroundResource = R.drawable.layout_border
//                            textColorResource = R.color.colorBlack
                            gravity = Gravity.CENTER

                            id = ROW_ID
                        }.lparams(width = 0, height = matchParent, weight = 0.2f) {
                            leftMargin = dip(-1)
                            rightMargin = dip(-1)
                            topMargin = dip(-1)
                        }

                        textView("{{}}") {
                            backgroundResource = R.drawable.layout_border
//                            textColorResource = R.color.colorBlack
                            gravity = Gravity.CENTER

                            id = DATE_ID
                        }.lparams(width = 0, height = matchParent, weight = 0.8f) {
                            rightMargin = dip(-1)
                            topMargin = dip(-1)
                        }
                    }.lparams(width = matchParent, height = wrapContent)
                }
            }
        }
    }
}
