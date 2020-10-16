package kr.goodneighbors.cms.ui.init


import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.extensions.convertDateFormat
import kr.goodneighbors.cms.extensions.getStringValue
import kr.goodneighbors.cms.service.viewmodel.UserInfoViewModel
import kr.goodneighbors.cms.ui.BaseActivityFragment
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.button
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.editText
import org.jetbrains.anko.hintTextColor
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.space
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.view
import org.jetbrains.anko.wrapContent
import java.util.*


class SigninFindFragment : BaseActivityFragment() {
    companion object {
        fun newInstance(): SigninFindFragment {
            return SigninFindFragment()
        }
    }

    private val userInfoViewModel: UserInfoViewModel by lazy {
        ViewModelProviders.of(this).get(UserInfoViewModel::class.java)
    }

    private val ui = FragmentUI()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userInfoViewModel.userName.observe(this, Observer { userName ->
            if (userName.isNullOrBlank()) {
                ui.idCheckMessageTextView.textResource = R.string.message_signin_no_match_user
                ui.idCheckMessageTextView.visibility = View.VISIBLE
            } else {
                ui.idCheckMessageTextView.visibility = View.GONE

                alert("User ID is $userName") {
                    positiveButton(R.string.button_check_password) { ui.pwCheckUserIdEditText.requestFocus() }
                    negativeButton(R.string.button_try_login) {
                        changeFragment.onChangeFragment(SigninFragment.newInstance(), false)
                    }
                }.show()
            }
        })

        userInfoViewModel.password.observe(this, Observer { userInfo ->
            if (userInfo == null) {
                ui.pwCheckMessageTextView.textResource = R.string.message_signin_no_match_user
                ui.pwCheckMessageTextView.visibility = View.VISIBLE
            } else {
                ui.pwCheckMessageTextView.visibility = View.GONE


                val pw = userInfo.PW_CONF
                alert("User Password is $pw") {
                    positiveButton(R.string.button_try_login) {
                        changeFragment.onChangeFragment(SigninFragment.newInstance(), false)
                    }
                }.show()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = ui.createView(AnkoContext.create(requireContext(), this))

        ui.idCheckBirthDateEditText.onClick {
            val c = Calendar.getInstance()
            var mYear = c.get(Calendar.YEAR)
            var mMonth = c.get(Calendar.MONTH)
            var mDay = c.get(Calendar.DAY_OF_MONTH)

            if (!ui.idCheckBirthDateEditText.getStringValue().isBlank()) {
                val b = ui.idCheckBirthDateEditText.getStringValue()
                mYear = b.convertDateFormat(before = "MM-dd-yyyy", after = "yyyy").toInt()
                mMonth = b.convertDateFormat(before = "MM-dd-yyyy", after = "MM").toInt() - 1
                mDay = b.convertDateFormat(before = "MM-dd-yyyy", after = "dd").toInt()
            }

            val datePickerDialog = DatePickerDialog(activity, android.R.style.Theme_Holo_Dialog,
                    DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                        ui.idCheckBirthDateEditText.setText("""${(monthOfYear + 1).toString().padStart(2, '0')}-${dayOfMonth.toString().padStart(2, '0')}-$year""")
                    }, mYear, mMonth, mDay)
            datePickerDialog.datePicker.maxDate = Date().time
            datePickerDialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            datePickerDialog.show()
        }

        ui.idCheckSubmitButton.onClick {
            var isValid = true

            val userName = ui.idCheckUserNameEditText.getStringValue()
            val birth = ui.idCheckBirthDateEditText.getStringValue()
            val email = ui.idCheckEmailEditText.getStringValue()

            if (isValid) {
                userInfoViewModel.findUserId(userName, birth.convertDateFormat(before = "MM-dd-yyyy", after = "yyyyMMdd"), email)
            }
        }

        ui.pwCheckBirthDateEditText.onClick {
            val c = Calendar.getInstance()
            var mYear = c.get(Calendar.YEAR)
            var mMonth = c.get(Calendar.MONTH)
            var mDay = c.get(Calendar.DAY_OF_MONTH)

            if (!ui.pwCheckBirthDateEditText.getStringValue().isBlank()) {
                val b = ui.pwCheckBirthDateEditText.getStringValue()
                mYear = b.convertDateFormat(before = "MM-dd-yyyy", after = "yyyy").toInt()
                mMonth = b.convertDateFormat(before = "MM-dd-yyyy", after = "MM").toInt() - 1
                mDay = b.convertDateFormat(before = "MM-dd-yyyy", after = "dd").toInt()
            }

            val datePickerDialog = DatePickerDialog(activity, android.R.style.Theme_Holo_Dialog,
                    DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                        ui.pwCheckBirthDateEditText.setText("""${(monthOfYear + 1).toString().padStart(2, '0')}-${dayOfMonth.toString().padStart(2, '0')}-$year""")
                    }, mYear, mMonth, mDay)
            datePickerDialog.datePicker.maxDate = Date().time
            datePickerDialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            datePickerDialog.show()
        }

        ui.pwCheckSubmitButton.onClick {
            val userId = ui.pwCheckUserIdEditText.getStringValue()
            val birth = ui.pwCheckBirthDateEditText.getStringValue()
            val email = ui.pwCheckEmailEditText.getStringValue()

            userInfoViewModel.findPassword(userId, birth.convertDateFormat(before = "MM-dd-yyyy", after = "yyyyMMdd"), email)
        }

        return v
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<SigninFindFragment> {
        lateinit var idCheckUserNameEditText: EditText
        lateinit var idCheckBirthDateEditText: EditText
        lateinit var idCheckEmailEditText: EditText
        lateinit var idCheckMessageTextView: TextView
        lateinit var idCheckSubmitButton: Button


        lateinit var pwCheckUserIdEditText: EditText
        lateinit var pwCheckBirthDateEditText: EditText
        lateinit var pwCheckEmailEditText: EditText
        lateinit var pwCheckMessageTextView: TextView
        lateinit var pwCheckSubmitButton: Button

        override fun createView(ui: AnkoContext<SigninFindFragment>) = with(ui) {
            verticalLayout {
                lparams(width = matchParent, height = matchParent)

                isFocusableInTouchMode = true

                leftPadding = dimen(R.dimen.px160)
                rightPadding = dimen(R.dimen.px160)

                gravity = Gravity.CENTER

                verticalLayout {
                    textView("ID Check") {
                        textColorResource = R.color.colorWhite
                    }

                    linearLayout {
                        imageView {
                            imageResource = R.drawable.login_user
                        }.lparams(width = dimen(R.dimen.px22)) {
                            gravity = Gravity.CENTER_VERTICAL
                        }

                        space { }.lparams(width = dip(10), height = matchParent)
                        view {
                            backgroundColorResource = R.color.colorLightGray
                        }.lparams(width = dip(1), height = dimen(R.dimen.px22)) {
                            gravity = Gravity.CENTER_VERTICAL
                        }
                        space { }.lparams(width = dip(10), height = matchParent)

                        idCheckUserNameEditText = editText {
                            hint = "* User Name"
                            hintTextColor = Color.parseColor("#AAAAAA")

                            inputType = InputType.TYPE_CLASS_TEXT
                            textColorResource = R.color.colorWhite
                            backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                        }.lparams(width = 0, height = wrapContent, weight = 1f)
                    }

                    linearLayout {
                        imageView {
                            imageResource = R.drawable.login_date
                        }.lparams(width = dimen(R.dimen.px22)) {
                            gravity = Gravity.CENTER_VERTICAL
                        }

                        space { }.lparams(width = dip(10), height = matchParent)
                        view {
                            backgroundColorResource = R.color.colorLightGray
                        }.lparams(width = dip(1), height = dimen(R.dimen.px22)) {
                            gravity = Gravity.CENTER_VERTICAL
                        }
                        space { }.lparams(width = dip(10), height = matchParent)

                        idCheckBirthDateEditText = editText {
                            hint = "* Birth date"
                            hintTextColor = Color.parseColor("#AAAAAA")

                            isFocusable = false

                            textColorResource = R.color.colorWhite
                            backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                        }.lparams(width = 0, height = wrapContent, weight = 1f)
                    }

                    linearLayout {
                        imageView {
                            imageResource = R.drawable.login_email
                        }.lparams(width = dimen(R.dimen.px22)) {
                            gravity = Gravity.CENTER_VERTICAL
                        }

                        space { }.lparams(width = dip(10), height = matchParent)
                        view {
                            backgroundColorResource = R.color.colorLightGray
                        }.lparams(width = dip(1), height = dimen(R.dimen.px22)) {
                            gravity = Gravity.CENTER_VERTICAL
                        }
                        space { }.lparams(width = dip(10), height = matchParent)

                        idCheckEmailEditText = editText {
                            hint = "* Email Address"
                            hintTextColor = Color.parseColor("#AAAAAA")

                            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                            textColorResource = R.color.colorWhite
                            backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                        }.lparams(width = 0, height = wrapContent, weight = 1f)
                    }

                    idCheckMessageTextView = textView {
                        textColorResource = R.color.colorAccent
                        gravity = Gravity.CENTER
                        visibility = View.GONE
                    }

                    idCheckSubmitButton = button("Submit")
                }

                space { }.lparams(width = matchParent, height = dimen(R.dimen.px40))

                verticalLayout {
                    textView("Password Check") {
                        textColorResource = R.color.colorWhite
                    }

                    linearLayout {
                        imageView {
                            imageResource = R.drawable.login_user
                        }.lparams(width = dimen(R.dimen.px22)) {
                            gravity = Gravity.CENTER_VERTICAL
                        }

                        space { }.lparams(width = dip(10), height = matchParent)
                        view {
                            backgroundColorResource = R.color.colorLightGray
                        }.lparams(width = dip(1), height = dimen(R.dimen.px22)) {
                            gravity = Gravity.CENTER_VERTICAL
                        }
                        space { }.lparams(width = dip(10), height = matchParent)

                        pwCheckUserIdEditText = editText {
                            hint = "* User ID"
                            hintTextColor = Color.parseColor("#AAAAAA")

                            inputType = InputType.TYPE_CLASS_TEXT
                            textColorResource = R.color.colorWhite
                            backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                        }.lparams(width = 0, height = wrapContent, weight = 1f)
                    }

                    linearLayout {
                        imageView {
                            imageResource = R.drawable.login_date
                        }.lparams(width = dimen(R.dimen.px22)) {
                            gravity = Gravity.CENTER_VERTICAL
                        }

                        space { }.lparams(width = dip(10), height = matchParent)
                        view {
                            backgroundColorResource = R.color.colorLightGray
                        }.lparams(width = dip(1), height = dimen(R.dimen.px22)) {
                            gravity = Gravity.CENTER_VERTICAL
                        }
                        space { }.lparams(width = dip(10), height = matchParent)

                        pwCheckBirthDateEditText = editText {
                            hint = "* Birth date"
                            hintTextColor = Color.parseColor("#AAAAAA")

                            isFocusable = false

                            textColorResource = R.color.colorWhite
                            backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                        }.lparams(width = 0, height = wrapContent, weight = 1f)
                    }

                    linearLayout {
                        imageView {
                            imageResource = R.drawable.login_email
                        }.lparams(width = dimen(R.dimen.px22)) {
                            gravity = Gravity.CENTER_VERTICAL
                        }

                        space { }.lparams(width = dip(10), height = matchParent)
                        view {
                            backgroundColorResource = R.color.colorLightGray
                        }.lparams(width = dip(1), height = dimen(R.dimen.px22)) {
                            gravity = Gravity.CENTER_VERTICAL
                        }
                        space { }.lparams(width = dip(10), height = matchParent)

                        pwCheckEmailEditText = editText {
                            hint = "* Email Address"
                            hintTextColor = Color.parseColor("#AAAAAA")

                            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                            textColorResource = R.color.colorWhite
                            backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                        }.lparams(width = 0, height = wrapContent, weight = 1f)
                    }

                    pwCheckMessageTextView = textView {
                        textColorResource = R.color.colorAccent
                        gravity = Gravity.CENTER
                        visibility = View.GONE
                    }

                    pwCheckSubmitButton = button(R.string.button_submit)
                }
            }
        }
    }
}
