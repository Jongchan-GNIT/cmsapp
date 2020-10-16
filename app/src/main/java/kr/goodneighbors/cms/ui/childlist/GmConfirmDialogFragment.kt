package kr.goodneighbors.cms.ui.childlist

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.service.model.GiftConfirmData
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.appcompat.v7.buttonBarLayout
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.space
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.topPadding
import org.jetbrains.anko.verticalLayout

class GmConfirmDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(items: List<GiftConfirmData>): GmConfirmDialogFragment {
            val f = GmConfirmDialogFragment()
            val args = Bundle()

            args.putParcelableArrayList("items", ArrayList(items))

            f.arguments = args

            return f
        }
    }

    private val ui = FragmentUI()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = ui.createView(AnkoContext.create(requireContext(), this))

        val items = arguments!!.getParcelableArrayList<GiftConfirmData>("items")

        items?.apply {
            AnkoContext.createDelegate(ui.gridContainer).apply {
                items.forEachIndexed { index, item ->
                    linearLayout {
                        textView("${index + 1}"){ gravity = Gravity.CENTER }.lparams(width = 0, weight = 1f)
                        textView(item.TITLE){ gravity = Gravity.CENTER }.lparams(width = 0, weight = 2f)
                        textView(item.COUNT){ gravity = Gravity.CENTER }.lparams(width = 0, weight = 1f)
                        textView(item.PRICE){ gravity = Gravity.CENTER }.lparams(width = 0, weight = 3f)
                        textView("$${item.TOTAL}"){ gravity = Gravity.CENTER }.lparams(width = 0, weight = 3f)
                    }
                }
            }
        }

        ui.cancelButtonTextView.onClick {
            dismiss()
        }

        ui.saveButtonTextView.onClick {
            val data = Intent()
            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, data)
            dismiss()
        }

        return v
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setCanceledOnTouchOutside(false)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<GmConfirmDialogFragment> {
        lateinit var gridContainer: LinearLayout
        lateinit var cancelButtonTextView: TextView
        lateinit var saveButtonTextView: TextView

        override fun createView(ui: AnkoContext<GmConfirmDialogFragment>) = with(ui) {
            verticalLayout {
                verticalLayout {
                    isFocusableInTouchMode = true

                    topPadding = dimen(R.dimen.px40)
                    leftPadding = dimen(R.dimen.px40)
                    rightPadding = dimen(R.dimen.px40)

                    linearLayout {
                        minimumHeight = dimen(R.dimen.px70)

                        textView("GM List") {
                            setTypeface(null, Typeface.BOLD)
                            textColorResource = R.color.colorPrimary
                            gravity = Gravity.CENTER_VERTICAL
                        }
                    }
                    space { }.lparams(width = matchParent, height = dimen(R.dimen.px10))

                    linearLayout {
                        textView("No") {
                            gravity = Gravity.CENTER
                            typeface = Typeface.DEFAULT_BOLD
                        }.lparams(width = 0, weight = 1f)
                        textView("Item") {
                            gravity = Gravity.CENTER
                            typeface = Typeface.DEFAULT_BOLD
                        }.lparams(width = 0, weight = 2f)
                        textView("No") {
                            gravity = Gravity.CENTER
                            typeface = Typeface.DEFAULT_BOLD
                        }.lparams(width = 0, weight = 1f)
                        textView("Unit price") {
                            gravity = Gravity.CENTER
                            typeface = Typeface.DEFAULT_BOLD
                        }.lparams(width = 0, weight = 3f)
                        textView("Total price") {
                            gravity = Gravity.CENTER
                            typeface = Typeface.DEFAULT_BOLD
                        }.lparams(width = 0, weight = 3f)
                    }
                    gridContainer = verticalLayout {

                    }
                }

                space { }.lparams(width = matchParent, height = dimen(R.dimen.px20))

                buttonBarLayout {
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

                    saveButtonTextView = textView(R.string.label_confirm) {
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
}