package kr.goodneighbors.cms.ui.childlist


import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.common.Constants
import kr.goodneighbors.cms.extensions.getStringValue
import kr.goodneighbors.cms.extensions.getValue
import kr.goodneighbors.cms.extensions.observeOnce
import kr.goodneighbors.cms.extensions.setItem
import kr.goodneighbors.cms.extensions.setSelectKey
import kr.goodneighbors.cms.service.model.ChildlistSearchItem
import kr.goodneighbors.cms.service.model.SpinnerOption
import kr.goodneighbors.cms.service.viewmodel.CommonViewModel
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.appcompat.v7.buttonBarLayout
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.checkBox
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.editText
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.radioButton
import org.jetbrains.anko.radioGroup
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.space
import org.jetbrains.anko.spinner
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.topPadding
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent

class ChildlistDialogMoreFragment : DialogFragment() {
    companion object {
        fun newInstance(searchItem: ChildlistSearchItem): ChildlistDialogMoreFragment {
            val fragment = ChildlistDialogMoreFragment()
            val args = Bundle()

            args.putString("service", searchItem.service ?: "")
            args.putString("village", searchItem.village ?: "")
            args.putString("school", searchItem.school ?: "")
            args.putString("gender", searchItem.gender ?: "")
            args.putString("ageFrom", searchItem.ageFrom ?: "")
            args.putString("ageTo", searchItem.ageTo ?: "")
            args.putString("case1", searchItem.case1 ?: "")
            args.putString("case2", searchItem.case2 ?: "")
            args.putString("case3", searchItem.case3 ?: "")

            fragment.arguments = args
            return fragment
        }
    }

    private val commonViewModel: CommonViewModel by lazy {
//        ViewModelProviders.of(this).get(CommonViewModel::class.java)
        CommonViewModel()
    }

    private val ui = FragmentUI()

    private lateinit var service: String
    private lateinit var village: String
    private lateinit var school: String
    private lateinit var gender: String
    private lateinit var ageFrom: String
    private lateinit var ageTo: String
    private lateinit var case1: String
    private lateinit var case2: String
    private lateinit var case3: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = ui.createView(AnkoContext.create(requireContext(), this))
        service = arguments!!.getString("service")
        village = arguments!!.getString("village")
        school = arguments!!.getString("school")
        gender = arguments!!.getString("gender")
        ageFrom = arguments!!.getString("ageFrom")
        ageTo = arguments!!.getString("ageTo")
        case1 = arguments!!.getString("case1")
        case2 = arguments!!.getString("case2")
        case3 = arguments!!.getString("case3")

        commonViewModel.getMoreDialogCommonCode().observeOnce(this, Observer { _commonCode ->
            _commonCode?.villages?.apply { ui.villageSpinner.setItem(items = this, hasEmptyOption = true) }
            _commonCode?.schoolName?.apply { ui.schoolSpinner.setItem(items = this, hasEmptyOption = true) }
            _commonCode?.specialCase?.apply {
                run {
                    // special case 1
                    val options = arrayListOf(SpinnerOption("", ""))
                    options.addAll(this)

                    val spinnerAdapter = object : ArrayAdapter<SpinnerOption>(context, R.layout.spinneritem_dark, options) {
                        override fun isEnabled(position: Int): Boolean {
                            return !(position > 0 && (ui.specialCase2Spinner.selectedItemPosition == position || ui.specialCase3Spinner.selectedItemPosition == position))
                        }

                        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
                            val view = super.getDropDownView(position, convertView, parent)
                            val tv = view as TextView
                            if (position > 0 && (ui.specialCase2Spinner.selectedItemPosition == position || ui.specialCase3Spinner.selectedItemPosition == position)) {
                                // Set the disable item text color
                                tv.setTextColor(Color.GRAY)
                            } else {
                                tv.setTextColor(Color.BLACK)
                            }
                            return view
                        }
                    }

                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                    ui.specialCase1Spinner.adapter = spinnerAdapter
                    ui.specialCase1Spinner.tag = options
                }

                run {
                    // special case 2
                    val options = arrayListOf(SpinnerOption("", ""))
                    options.addAll(this)

                    val spinnerAdapter = object : ArrayAdapter<SpinnerOption>(context, R.layout.spinneritem_dark, options) {
                        override fun isEnabled(position: Int): Boolean {
                            return !(position > 0 && (ui.specialCase1Spinner.selectedItemPosition == position || ui.specialCase3Spinner.selectedItemPosition == position))
                        }

                        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
                            val view = super.getDropDownView(position, convertView, parent)
                            val tv = view as TextView
                            if (position > 0 && (ui.specialCase1Spinner.selectedItemPosition == position || ui.specialCase3Spinner.selectedItemPosition == position)) {
                                // Set the disable item text color
                                tv.setTextColor(Color.GRAY)
                            } else {
                                tv.setTextColor(Color.BLACK)
                            }
                            return view
                        }
                    }

                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                    ui.specialCase2Spinner.adapter = spinnerAdapter
                    ui.specialCase2Spinner.tag = options
                }

                run {
                    // special case 3
                    val options = arrayListOf(SpinnerOption("", ""))
                    options.addAll(this)

                    val spinnerAdapter = object : ArrayAdapter<SpinnerOption>(context, R.layout.spinneritem_dark, options) {
                        override fun isEnabled(position: Int): Boolean {
                            return !(position > 0 && (ui.specialCase1Spinner.selectedItemPosition == position || ui.specialCase2Spinner.selectedItemPosition == position))
                        }

                        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
                            val view = super.getDropDownView(position, convertView, parent)
                            val tv = view as TextView
                            if (position > 0 && (ui.specialCase1Spinner.selectedItemPosition == position || ui.specialCase2Spinner.selectedItemPosition == position)) {
                                // Set the disable item text color
                                tv.setTextColor(Color.GRAY)
                            } else {
                                tv.setTextColor(Color.BLACK)
                            }
                            return view
                        }
                    }

                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                    ui.specialCase3Spinner.adapter = spinnerAdapter
                    ui.specialCase3Spinner.tag = options
                }
            }

            ui.villageSpinner.setSelectKey(village)
            ui.schoolSpinner.setSelectKey(school)
            ui.specialCase1Spinner.setSelectKey(case1)
            ui.specialCase2Spinner.setSelectKey(case2)
            ui.specialCase3Spinner.setSelectKey(case3)

            ui.ageFromEditText.setText(ageFrom)
            ui.ageToEditText.setText(ageTo)

            when(gender) {
                "F"->ui.genderRadioGroup.check(ui.genderFemaleRadioButton.id)
                "M"->ui.genderRadioGroup.check(ui.genderMaleRadioButton.id)
            }
        })

        ui.dropoutExpectedCheckBox.visibility = if (service == Constants.SERVICE_ACL) View.VISIBLE else View.GONE

        ui.cancelButtonTextView.onClick {
            dismiss()
        }

        ui.saveButtonTextView.onClick {
            if (ui.ageFromEditText.getStringValue() == "1" || ui.ageToEditText.getStringValue() == "1") {
                toast(R.string.message_validate_minimum_age)
                return@onClick
            }

            val case1SelectedValule = ui.specialCase1Spinner.getValue()
            val case2SelectedValule = ui.specialCase2Spinner.getValue()
            val case3SelectedValule = ui.specialCase3Spinner.getValue()

            val selectedGender = when (ui.genderRadioGroup.checkedRadioButtonId) {
                ui.genderFemaleRadioButton.id -> "F"
                ui.genderMaleRadioButton.id -> "M"
                else-> null
            }

            val data = Intent()
            data.putExtra("village", ui.villageSpinner.getValue() ?: "")
            data.putExtra("school", ui.schoolSpinner.getValue() ?: "")
            data.putExtra("gender", selectedGender)
            data.putExtra("ageFrom", ui.ageFromEditText.getStringValue())
            data.putExtra("ageTo", ui.ageToEditText.getStringValue())
            data.putExtra("case1", case1SelectedValule)
            data.putExtra("case2", case2SelectedValule)
            data.putExtra("case3", case3SelectedValule)
            data.putExtra("dropoutExpected", ui.dropoutExpectedCheckBox.isChecked)

            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, data)
            dismiss()
        }

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }

    override fun onStart() {
        super.onStart()
        dialog.window.setBackgroundDrawableResource(R.drawable.rounded_dialog)
        dialog.window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setCanceledOnTouchOutside(false)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<ChildlistDialogMoreFragment> {
        lateinit var cancelButtonTextView: TextView
        lateinit var saveButtonTextView: TextView

        lateinit var villageSpinner: Spinner
        lateinit var schoolSpinner: Spinner

        lateinit var genderRadioGroup: RadioGroup
        lateinit var genderFemaleRadioButton: RadioButton
        lateinit var genderMaleRadioButton: RadioButton

        lateinit var specialCase1Spinner: Spinner
        lateinit var specialCase2Spinner: Spinner
        lateinit var specialCase3Spinner: Spinner

        lateinit var ageFromEditText: EditText
        lateinit var ageToEditText: EditText

        lateinit var dropoutExpectedCheckBox: CheckBox

        @SuppressLint("SetTextI18n")
        override fun createView(ui: AnkoContext<ChildlistDialogMoreFragment>) = with(ui) {
            verticalLayout {
                verticalLayout {
                    isFocusableInTouchMode = true

                    topPadding = dimen(R.dimen.px40)
                    leftPadding = dimen(R.dimen.px40)
                    rightPadding = dimen(R.dimen.px40)

                    linearLayout {
                        minimumHeight = dimen(R.dimen.px70)

                        textView(R.string.label_more_condition) {
                            setTypeface(null, Typeface.BOLD)
                            textColorResource = R.color.colorPrimary
                            gravity = Gravity.CENTER_VERTICAL
                        }
                    }
                    space { }.lparams(width = matchParent, height = dimen(R.dimen.px20))

                    linearLayout {
                        textView(R.string.label_village) {}.lparams(width = 0, height = wrapContent, weight = 0.3f)
                        villageSpinner = spinner {}.lparams(width = 0, height = wrapContent, weight = 0.7f)
                    }
                    linearLayout {
                        textView(R.string.label_school) {}.lparams(width = 0, height = wrapContent, weight = 0.3f)
                        schoolSpinner = spinner {}.lparams(width = 0, height = wrapContent, weight = 0.7f)
                    }
                    linearLayout {
                        textView(R.string.label_gender) {}.lparams(width = 0, height = wrapContent, weight = 0.3f)
                        genderRadioGroup = radioGroup {
                            orientation = RadioGroup.HORIZONTAL

                            genderFemaleRadioButton = radioButton { textResource = R.string.label_female }
                            genderMaleRadioButton = radioButton { textResource = R.string.label_male }
                        }.lparams(width = 0, height = wrapContent, weight = 0.7f)
                    }
                    linearLayout {
                        textView(R.string.label_age) {}.lparams(width = 0, height = wrapContent, weight = 0.3f)
                        linearLayout {
                            ageFromEditText = editText {
                                inputType = InputType.TYPE_CLASS_NUMBER
                                filters = arrayOf(InputFilter.LengthFilter(2))
                            }.lparams(width = 0, height = wrapContent, weight = 0.1f)
                            textView("~")
                            ageToEditText = editText {
                                inputType = InputType.TYPE_CLASS_NUMBER
                                filters = arrayOf(InputFilter.LengthFilter(2))
                            }.lparams(width = 0, height = wrapContent, weight = 0.1f)
                        }.lparams(width = 0, height = wrapContent, weight = 0.7f)
                    }
                    linearLayout {
                        textView(R.string.label_case) {}.lparams(width = 0, height = wrapContent, weight = 0.3f)
                        specialCase1Spinner = spinner {}.lparams(width = 0, height = wrapContent, weight = 0.7f)
                    }
                    linearLayout {
                        space {}.lparams(width = 0, height = wrapContent, weight = 0.3f)
                        specialCase2Spinner = spinner {}.lparams(width = 0, height = wrapContent, weight = 0.7f)
                    }
                    linearLayout {
                        space {}.lparams(width = 0, height = wrapContent, weight = 0.3f)
                        specialCase3Spinner = spinner {}.lparams(width = 0, height = wrapContent, weight = 0.7f)
                    }

                    dropoutExpectedCheckBox = checkBox {
                        visibility = View.GONE
                        textResource = R.string.message_dropout_age
                    }


                }.lparams(width = matchParent, height = wrapContent)
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

                    saveButtonTextView = textView(R.string.label_search) {
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
