package kr.goodneighbors.cms.ui.childlist

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import kr.goodneighbors.cms.R
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.dimen
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.imageView
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.textSizeDimen
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout

class ChildlistDialogCaseinfoFragment : DialogFragment() {
    companion object {
        fun newInstance(): ChildlistDialogCaseinfoFragment {
            return ChildlistDialogCaseinfoFragment()
        }
    }

    private val ui = FragmentUI()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = ui.createView(AnkoContext.create(requireContext(), this))

        dialog.window?.setGravity(Gravity.BOTTOM)
        val p = dialog.window?.attributes

        p?.width = ViewGroup.LayoutParams.MATCH_PARENT
        p?.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        p?.x = 200
        p?.y = 50

        dialog.window?.attributes = p

        return v
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<ChildlistDialogCaseinfoFragment> {

        override fun createView(ui: AnkoContext<ChildlistDialogCaseinfoFragment>) = with(ui) {
            verticalLayout {
                padding = dimen(R.dimen.px40)

                lparams(width = matchParent, height = matchParent)

                linearLayout {
                    gravity = Gravity.CENTER_VERTICAL

                    imageView { imageResource = R.drawable.case_exposed }.lparams(height = dimen(R.dimen.px48))
                    textView(R.string.label_special_marriage) { textSizeDimen = R.dimen.px26 }.lparams { leftMargin = dimen(R.dimen.px20) }
                }

                linearLayout {
                    gravity = Gravity.CENTER_VERTICAL

                    imageView { imageResource = R.drawable.case_abuse }.lparams(height = dimen(R.dimen.px48))
                    textView(R.string.label_special_child_abuse) { textSizeDimen = R.dimen.px26 }.lparams { leftMargin = dimen(R.dimen.px20) }
                }.lparams {
                    topMargin = dimen(R.dimen.px10)
                }

                linearLayout {
                    gravity = Gravity.CENTER_VERTICAL

                    imageView { imageResource = R.drawable.case_labor }.lparams(height = dimen(R.dimen.px48))
                    textView(R.string.label_special_child_labor) { textSizeDimen = R.dimen.px26 }.lparams { leftMargin = dimen(R.dimen.px20) }
                }.lparams {
                    topMargin = dimen(R.dimen.px10)
                }

                linearLayout {
                    gravity = Gravity.CENTER_VERTICAL

                    imageView { imageResource = R.drawable.case_extreme }.lparams(height = dimen(R.dimen.px48))
                    textView(R.string.label_special_extream_proverty) { textSizeDimen = R.dimen.px26 }.lparams { leftMargin = dimen(R.dimen.px20) }
                }.lparams {
                    topMargin = dimen(R.dimen.px10)
                }

                linearLayout {
                    gravity = Gravity.CENTER_VERTICAL

                    imageView { imageResource = R.drawable.case_expected }.lparams(height = dimen(R.dimen.px48))
                    textView(R.string.label_special_expected_move) { textSizeDimen = R.dimen.px26 }.lparams { leftMargin = dimen(R.dimen.px20) }
                }.lparams {
                    topMargin = dimen(R.dimen.px10)
                }

                linearLayout {
                    gravity = Gravity.CENTER_VERTICAL

                    imageView { imageResource = R.drawable.case_guardian }.lparams(height = dimen(R.dimen.px48))
                    textView(R.string.label_special_guardian_absent) { textSizeDimen = R.dimen.px26 }.lparams { leftMargin = dimen(R.dimen.px20) }
                }.lparams {
                    topMargin = dimen(R.dimen.px10)
                }

                linearLayout {
                    gravity = Gravity.CENTER_VERTICAL

                    imageView { imageResource = R.drawable.case_media }.lparams(height = dimen(R.dimen.px48))
                    textView(R.string.label_special_media) { textSizeDimen = R.dimen.px26 }.lparams { leftMargin = dimen(R.dimen.px20) }
                }.lparams {
                    topMargin = dimen(R.dimen.px10)
                }

                linearLayout {
                    gravity = Gravity.CENTER_VERTICAL

                    imageView { imageResource = R.drawable.case_serious }.lparams(height = dimen(R.dimen.px48))
                    textView(R.string.label_special_ilness) { textSizeDimen = R.dimen.px26 }.lparams { leftMargin = dimen(R.dimen.px20) }
                }.lparams {
                    topMargin = dimen(R.dimen.px10)
                }
            }
        }
    }
}
