package kr.goodneighbors.cms.ui.init


import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.preference.PreferenceManager
import android.telephony.TelephonyManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.extensions.isNetworkAvailable
import kr.goodneighbors.cms.service.viewmodel.DeviceVerifyViewModel
import kr.goodneighbors.cms.ui.BaseActivityFragment
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.button
import org.jetbrains.anko.dimen
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.space
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textSizeDimen
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wifiManager
import org.jetbrains.anko.wrapContent
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class DeviceVerifyFragment : BaseActivityFragment() {
    companion object {
        fun newInstance(): DeviceVerifyFragment {
            return DeviceVerifyFragment()
        }
    }

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(DeviceVerifyFragment::class.java)
    }

    private val deviceVerifyViewModel: DeviceVerifyViewModel by lazy {
        ViewModelProviders.of(this).get(DeviceVerifyViewModel::class.java)
    }

    private val ui = FragmentUI()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        deviceVerifyViewModel.verifyInfo.observe(this, Observer { deviceVerifyResponse ->
            logger.debug("verifyInfo : $deviceVerifyResponse")

            if (deviceVerifyResponse != null) {
                if (!deviceVerifyResponse.data?.aes_key.isNullOrBlank()) {
                    changeFragment.onChangeFragment(InitSyncFragment.newInstance(), false)
                }
            }
        })

        deviceVerifyViewModel.loadStatus.observe(this, Observer { loading ->
            logger.debug("loadStatus : $loading")
        })

        deviceVerifyViewModel.errorStatus.observe(this, Observer { errorStatus ->
            logger.debug("errorStatus : $errorStatus")
            toast(errorStatus ?: "")
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        logger.debug("DeviceVerifyFragment.onCreateView")
        val v = ui.createView(AnkoContext.create(requireContext(), this))

        ui.registButton.onClick {
//            if (requireContext().wifiManager.isWifiEnabled) {
            if (requireContext().isNetworkAvailable()) {
                deviceVerifyViewModel.setDeviceImei(getDeviceIMEI()!!)
            } else {
                toast(R.string.message_wifi_disabled)
            }
        }

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)

        val aesKey = sharedPref.getString("aes_key", "")
        logger.debug("aes_key : $aesKey")

        if (!aesKey.isNullOrBlank()) {
            changeFragment.onChangeFragment(InitSyncFragment.newInstance(), false)
        }

        return v
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceIMEI(): String? {
        /*
         * <uses-permission android:no="android.permission.READ_PHONE_STATE"/>
         */
        val telephonyManager = activity?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        //return telephonyManager.imei
        return "358301090158015"
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<DeviceVerifyFragment> {

        lateinit var registButton: Button

        override fun createView(ui: AnkoContext<DeviceVerifyFragment>) = with(ui) {
            verticalLayout {
                lparams(width = matchParent, height = matchParent)
                gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER

                textView(R.string.message_device_not_registrated) {
                    gravity = Gravity.CENTER
                    textColorResource = R.color.colorAccent
                    textSizeDimen = R.dimen.px50
                    typeface = Typeface.DEFAULT_BOLD
                }

                space { }.lparams(height = dimen(R.dimen.px50))

                registButton = button(R.string.button_device_registration) {
                    gravity = Gravity.CENTER
                    backgroundColorResource = R.color.colorAccent
                    textColorResource = R.color.colorWhite
                    textSizeDimen = R.dimen.px26
                    typeface = Typeface.DEFAULT_BOLD
                    allCaps = false
                }.lparams(width = wrapContent, height = wrapContent)
            }
        }
    }
}
