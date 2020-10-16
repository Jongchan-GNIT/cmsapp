package kr.goodneighbors.cms.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.telephony.TelephonyManager
import android.view.WindowManager
import android.widget.FrameLayout
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.common.Constants
import kr.goodneighbors.cms.ui.init.DeviceVerifyFragment
import kr.goodneighbors.cms.ui.init.InitSyncFragment
import kr.goodneighbors.cms.ui.init.SigninFragment
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.setContentView
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class InitActivity : BaseActivity(), BaseActivityFragment.ChangeFragment {
    companion object {
        const val ID_MAIN_CONTAINER = 1

        const val REQUEST_USED_PERMISSION = 200
    }

    private val needPermissions = arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(InitActivity::class.java)
    }

    private val ui = ActivityUI()

    override fun <T : Any?> onChangeActivity(clazz: Class<T>, useBackstack: Boolean) {
        logger.debug("onChangeActivity : $clazz, $useBackstack")

        val intent = Intent(this, clazz)

        if (!useBackstack) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        startActivity(intent)

        if (!useBackstack) {
            finish()
        }
    }

    override fun onChangeFragment(fragment: Fragment, useBackstack: Boolean) {
        logger.debug("onChangeFragment : $fragment, $useBackstack")

        val manager = supportFragmentManager
                .beginTransaction()
//                .replace(R.id.initFrame, fragment)
                .replace(ui.container.id, fragment)

        if (useBackstack) {
            manager.addToBackStack(null)
        }

        manager.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ui.setContentView(this)

        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val aesKey = sharedPref.getString("aes_key", "")
        val gnid = sharedPref.getString("GN_ID", "")
        val isInit = sharedPref.getBoolean("init", false)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            createDirectory()
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            if (gnid.isNullOrBlank()) {
                loadDeviceIMEI()
            }
        }

        checkPermission()

        if (savedInstanceState == null) {
            if (aesKey.isNullOrBlank()) {
                onChangeFragment(DeviceVerifyFragment.newInstance(), false)
            }
            else if (!isInit) {
                onChangeFragment(InitSyncFragment.newInstance(), false)
            }
            else {
                onChangeFragment(SigninFragment.newInstance(), false)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            REQUEST_USED_PERMISSION-> {
                val writePermissionIndex = permissions.indexOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                val writePermissionGrant = grantResults[writePermissionIndex]

                if (writePermissionGrant == PackageManager.PERMISSION_GRANTED) {
                    createDirectory()
                }

                val telephonePermissionIndex = permissions.indexOf(Manifest.permission.READ_PHONE_STATE)
                val telephonePermissionGrant = grantResults[telephonePermissionIndex]

                if (telephonePermissionGrant == PackageManager.PERMISSION_GRANTED) {
                    logger.debug("Manifest.permission.READ_PHONE_STATE : PackageManager.PERMISSION_GRANTED")
                    loadDeviceIMEI()
                }
            }
        }

        if (grantResults.indexOf(-1) > -1) {
            finish()
        }
    }

    private fun checkPermission() {
        var hasNotGranedPermission = false
        for (permission in needPermissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                hasNotGranedPermission = true
                ActivityCompat.requestPermissions(this, needPermissions, REQUEST_USED_PERMISSION)
                break
            }
        }

        if (!hasNotGranedPermission) {
//            registrationDevice()
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
        } else {
        }

    }

    private fun createDirectory() {
        logger.debug("InitActivity.createDirectory()")
        val sdMain = Environment.getExternalStorageDirectory()

        val dirs = arrayOf(Constants.DIR_INIT, Constants.DIR_CONTENTS, Constants.DIR_TEMP, Constants.DIR_DOWNLOAD, Constants.DIR_IMPORT, Constants.DIR_EXPORT, Constants.DIR_EXPORT_OFFLINE, Constants.DIR_FAIL, Constants.DIR_LOG)
        dirs.forEach {
            val dir = File("$sdMain/${Constants.DIR_HOME}/$it")
            if (!dir.exists()) {
                dir.mkdirs()

                logger.debug("디렉토리 생성 : ${dir.path}")

                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                val contentUri = Uri.fromFile(dir)
                mediaScanIntent.data = contentUri
                sendBroadcast(mediaScanIntent)
            }

            if (it == Constants.DIR_CONTENTS || it == Constants.DIR_TEMP || it == Constants.DIR_INIT) {
                val nomedia = File(dir, ".nomedia")
                if (!nomedia.exists()) {
                    val isNewFileCreated: Boolean = nomedia.createNewFile()

                    logger.debug("nomedia file create : $isNewFileCreated")
                    broadcast(nomedia.path)
                }
            }
            else {
                val nomedia = File(dir, ".nomedia")
                if (nomedia.exists()) {
                    nomedia.delete()
                }
            }
        }
    }

    private fun broadcast(path: String) {
        logger.debug("----------broadcast: $path")
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = Uri.fromFile(File(path))
        sendBroadcast(mediaScanIntent)
    }

    @SuppressLint("MissingPermission")
    private fun loadDeviceIMEI() {
        /*
         * <uses-permission android:no="android.permission.READ_PHONE_STATE"/>
         */
        try {
            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
            //sharedPref.edit().putString("GN_ID", telephonyManager.imei ?: "").apply()
            sharedPref.edit().putString("GN_ID", "358301090158015").apply()
        }
        catch(e: Exception) {
            e.printStackTrace()
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class ActivityUI: AnkoComponent<InitActivity> {
        lateinit var container: FrameLayout

        override fun createView(ui: AnkoContext<InitActivity>) = with(ui) {
            constraintLayout {
                fitsSystemWindows = true
                backgroundResource = R.drawable.login_bgimg

                lparams(width = matchParent, height = matchParent)

                container = frameLayout {
                    id = ID_MAIN_CONTAINER
                }.lparams(width = matchParent, height = matchParent)
            }
        }
    }
}
