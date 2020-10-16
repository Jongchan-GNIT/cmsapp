@file:Suppress("ConstantConditionIf")

package kr.goodneighbors.cms.ui.init


import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.common.Constants
import kr.goodneighbors.cms.extensions.getStringValue
import kr.goodneighbors.cms.extensions.toSHA
import kr.goodneighbors.cms.service.viewmodel.UserInfoViewModel
import kr.goodneighbors.cms.ui.BaseActivityFragment
import kr.goodneighbors.cms.ui.MainActivity
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.button
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.editText
import org.jetbrains.anko.hintResource
import org.jetbrains.anko.hintTextColor
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.space
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.view
import org.jetbrains.anko.wrapContent
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class SigninFragment : BaseActivityFragment() {
    companion object {
        fun newInstance(): SigninFragment {
            return SigninFragment()
        }
    }

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(SigninFragment::class.java)
    }

    private val userInfoViewModel: UserInfoViewModel by lazy {
        ViewModelProviders.of(this).get(UserInfoViewModel::class.java)
    }

    private val ui = FragmentUI()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userInfoViewModel.userinfo.observe(this, Observer { userInfo ->
            logger.debug("USERINFO : $userInfo")
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)

            if (userInfo != null) {
                val password = ui.passwordEditText.getStringValue()
                val isEqualPassword = userInfo.PW == password.toSHA()

                if (isEqualPassword) {
                    sharedPref.edit()
                            .putString("userid", userInfo.ID)
                            .putString("username", userInfo.NM)
                            .putString("user_ctr_cd", userInfo.CTR_CD)
                            .putString("user_brc_cd", userInfo.BRC_CD)
                            .putString("user_prj_cd", userInfo.PRJ_CD)
                            .putString("user_auth_cd", userInfo.AUTH_CD)
                            .apply()

                    userInfoViewModel.findBucketInfo(userInfo.ID).observe(this, Observer {
                        if (it != null) {
                            sharedPref.edit()
                                    .putString("ref_1", it.REF_1 ?: "")
                                    .putString("ref_2", it.REF_2 ?: "")
                                    .apply()
                        }

                        changeFragment.onChangeActivity(MainActivity::class.java, false)
                    })
                } else {
                    ui.passwordMessageTextView.textResource = R.string.message_signin_retype_password
                    ui.passwordMessageTextView.visibility = View.VISIBLE
                }
            } else {
                ui.userIdMessageTextView.textResource = R.string.message_signin_retype_userid
                ui.userIdMessageTextView.visibility = View.VISIBLE
            }

        })
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = ui.createView(AnkoContext.create(requireContext(), this))

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        ui.userIdEditText.setText(sharedPref.getString("userid", ""))

        val info = activity!!.packageManager.getPackageInfo(activity!!.packageName, 0)
        ui.versionNameTextView.text = "Ver ${info.versionName}${if (Constants.BUILD == "dev") "\n- development edition -" else ""}"

        ui.loginButton.onClick {
            val userId = ui.userIdEditText.getStringValue()
            val password = ui.passwordEditText.getStringValue()

            logger.debug("userId : $userId")

            var isValid = true

            if (userId.isBlank()) {
                ui.userIdMessageTextView.textResource = R.string.message_signin_require_userid
                ui.userIdMessageTextView.visibility = View.VISIBLE
                isValid = false
            } else {

                ui.userIdMessageTextView.visibility = View.GONE
            }

            if (password.isBlank()) {
                ui.passwordMessageTextView.textResource = R.string.message_signin_require_password
                ui.passwordMessageTextView.visibility = View.VISIBLE
                isValid = false
            } else {
                ui.passwordMessageTextView.visibility = View.GONE
            }

            if (isValid) {
                userInfoViewModel.login(userId, password)
            }
        }

        ui.forgotAccountTextView.onClick {
            logger.debug("SigninFragment.findUserInfoById.setOnClickListener")

            changeFragment.onChangeFragment(SigninFindFragment.newInstance())
        }

        return v
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<SigninFragment> {
        lateinit var versionNameTextView: TextView

        lateinit var loginButton: Button

        lateinit var userIdEditText: EditText
        lateinit var userIdMessageTextView: TextView

        lateinit var passwordEditText: EditText
        lateinit var passwordMessageTextView: TextView

        lateinit var forgotAccountTextView: TextView

        override fun createView(ui: AnkoContext<SigninFragment>) = with(ui) {
            verticalLayout {
                isFocusableInTouchMode = true

                leftPadding = dimen(R.dimen.px160)
                rightPadding = dimen(R.dimen.px160)

                space { }.lparams(width = matchParent, height = dimen(R.dimen.px80))

                verticalLayout {
                    gravity = Gravity.CENTER

                    imageView {
                        imageResource = R.drawable.login_logo
                    }.lparams(width = wrapContent, height = wrapContent) {
                        gravity = Gravity.CENTER
                    }

                    space {}.lparams(height = dimen(R.dimen.px150))

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

                        userIdEditText = editText {
                            hintResource = R.string.label_userid
                            hintTextColor = Color.parseColor("#AAAAAA")

                            inputType = InputType.TYPE_CLASS_TEXT
                            textColorResource = R.color.colorWhite
                            backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                        }.lparams(width = 0, height = wrapContent, weight = 1f)
                    }
                    userIdMessageTextView = textView {
                        textColorResource = R.color.colorAccent
                        gravity = Gravity.CENTER_VERTICAL
                        visibility = View.GONE
                    }

                    space { }.lparams(width = matchParent, height = dimen(R.dimen.px20))

                    linearLayout {
                        imageView {
                            imageResource = R.drawable.login_pass
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

                        passwordEditText = editText {
                            hintResource = R.string.label_password
                            hintTextColor = Color.parseColor("#AAAAAA")

                            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            textColorResource = R.color.colorWhite
                            backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                        }.lparams(width = 0, height = wrapContent, weight = 1f)
                    }
                    passwordMessageTextView = textView {
                        textColorResource = R.color.colorAccent
                        gravity = Gravity.CENTER_VERTICAL
                        visibility = View.GONE
                    }.lparams(width = matchParent)

                    space { }.lparams(width = matchParent, height = dimen(R.dimen.px100))

                    loginButton = button(R.string.button_login)

                    space { }.lparams(width = matchParent, height = dimen(R.dimen.px50))

                    forgotAccountTextView = textView(R.string.message_signin_forgot_account) {
                        gravity = Gravity.CENTER
                        textColorResource = R.color.colorWhite
                    }
                }.lparams(width = matchParent, height = 0, weight = 1f)

                versionNameTextView = textView {
                    gravity = Gravity.CENTER
                    textColorResource = R.color.colorWhite
                }.lparams(width = matchParent, height = wrapContent) {
                    bottomMargin = dimen(R.dimen.px128)
                }
            }
        }
    }
}
