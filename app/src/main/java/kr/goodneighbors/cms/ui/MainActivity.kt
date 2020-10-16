@file:Suppress("ConstantConditionIf")

package kr.goodneighbors.cms.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.TextView
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.common.Constants
import kr.goodneighbors.cms.extensions.hideKeyboard
import kr.goodneighbors.cms.ui.childlist.AclEditFragment
import kr.goodneighbors.cms.ui.childlist.AprFragment
import kr.goodneighbors.cms.ui.childlist.ChildlistFragment
import kr.goodneighbors.cms.ui.childlist.CifFragment
import kr.goodneighbors.cms.ui.childlist.GmFragment
import kr.goodneighbors.cms.ui.childlist.GmLetterFragment
import kr.goodneighbors.cms.ui.home.HomeFragment
import kr.goodneighbors.cms.ui.settings.SettingFragment
import kr.goodneighbors.cms.ui.statistics.StatisticsFragment
import kr.goodneighbors.cms.ui.sync.SyncFragment
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alert
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.design.themedAppBarLayout
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.noButton
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.support.v4.drawerLayout
import org.jetbrains.anko.support.v4.space
import org.jetbrains.anko.textSizeDimen
import org.jetbrains.anko.textView
import org.jetbrains.anko.topPadding
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.view
import org.jetbrains.anko.yesButton
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class MainActivity : BaseActivity(), BaseActivityFragment.ChangeFragment {
    companion object {
        const val ID_MAIN_CONTAINER = 1

        const val ID_MENU_HOME = 2
        const val ID_MENU_CLOSE = 3
        const val ID_MENU_STATISTICS = 4
        const val ID_MENU_CHILDLIST = 5
        const val ID_MENU_SETTING = 6
        const val ID_MENU_LOGOUT = 7
        const val ID_MENU_SYNC = 8

        const val DRAWER_CONTAINER = 100
    }

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(MainActivity::class.java)
    }

    private val ui = ActivityUI()

    private lateinit var drawerLayout: DrawerLayout

    override fun <T> onChangeActivity(clazz: Class<T>, useBackstack: Boolean) {
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

        try {
            fragment.context?.getSystemService(Context.INPUT_METHOD_SERVICE)?.let {
                if (it is InputMethodManager) {
                    it.hideSoftInputFromWindow(fragment.view?.windowToken, 0)
                }
            }
        } catch (e: Exception) {
            logger.error("onChangeFragment : ", e)
        }

        val manager = supportFragmentManager.beginTransaction()
                .replace(ui.container.id, fragment)

        if (useBackstack) {
            manager.addToBackStack(null)
        }

        manager.commit()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ui.setContentView(this)

        setSupportActionBar(ui.mainToolbar)

        drawerLayout = findViewById(DRAWER_CONTAINER)
        drawerLayout.setDrawerShadow(android.R.color.transparent, GravityCompat.START)
        val toggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(this, drawerLayout, ui.mainToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                drawerView.hideKeyboard()
            }
        }
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            onChangeFragment(HomeFragment.newInstance(), false)
        }

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        ui.useridTextView.text = sharedPref.getString("userid", "")
        ui.usernameTextView.text = sharedPref.getString("username", "")

        val info = packageManager.getPackageInfo(packageName, 0)
        ui.versionTextView.text = "CMS ${info.versionName}${if (Constants.BUILD == "dev") "\n- development edition -" else ""}"
    }

    override fun onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            val currentFragment = supportFragmentManager.findFragmentById(ui.container.id)
//            val methods = currentFragment.javaClass.methods.filter { it.name == "onBackPressed" }
            val methods = currentFragment!!::class.java.methods.filter { it.name == "onBackPressed" }

            logger.debug("currentFragment : $currentFragment")

            if (methods.isNotEmpty()) {
                val isOnBackpressed: Boolean = methods[0].invoke(currentFragment) as Boolean
                if (isOnBackpressed) {
                    excuteBackPressed()
                }
            } else {
                if ((currentFragment is CifFragment && currentFragment.isEditable())
                        || (currentFragment is AprFragment && currentFragment.isEditable())
                        || (currentFragment is AclEditFragment && currentFragment.isEditable())
                        || (currentFragment is GmFragment && currentFragment.isEditable())
                        || (currentFragment is GmLetterFragment && currentFragment.isEditable())) {

                    alert(R.string.message_confirm_leave_page) {
                        yesButton {
                            excuteBackPressed()
                        }
                        cancelButton {

                        }
                    }.show()
                } else excuteBackPressed()
            }
        }

    }

    private fun excuteBackPressed() {
        super.onBackPressed()
    }

    fun onClickedMenuItem(menuId: Int) {
        logger.debug("onClickedMenuItem - menu : ${menuId}")

        val currentFragment = supportFragmentManager.findFragmentById(ui.container.id)
        if ((currentFragment is CifFragment && currentFragment.isEditable())
                || (currentFragment is AprFragment && currentFragment.isEditable())
                || (currentFragment is AclEditFragment && currentFragment.isEditable())
                || (currentFragment is GmFragment && currentFragment.isEditable())
                || (currentFragment is GmLetterFragment && currentFragment.isEditable())) {

            alert(R.string.message_confirm_leave_page) {
                yesButton {
                    moveTo(menuId)
                }
                cancelButton {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
            }.show()
        } else moveTo(menuId)
    }

    private fun moveTo(menuId: Int) {
        when (menuId) {
            ID_MENU_HOME -> onChangeFragment(HomeFragment.newInstance())
            ID_MENU_STATISTICS -> onChangeFragment(StatisticsFragment.newInstance())
            ID_MENU_CHILDLIST -> onChangeFragment(ChildlistFragment.newInstance())
            ID_MENU_SETTING -> onChangeFragment(SettingFragment.newInstance())
            ID_MENU_SYNC -> onChangeFragment(SyncFragment.newInstance())
            ID_MENU_LOGOUT -> {
                alert(R.string.message_confirm_logout) {
                    yesButton { onChangeActivity(InitActivity::class.java, false) }
                    noButton {}
                }.show()
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class ActivityUI : AnkoComponent<MainActivity> {
        lateinit var container: FrameLayout

        lateinit var mainToolbar: Toolbar

        lateinit var useridTextView: TextView
        lateinit var usernameTextView: TextView
        lateinit var versionTextView: TextView

        override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
            drawerLayout {
                id = DRAWER_CONTAINER
                fitsSystemWindows = true

                lparams(width = matchParent, height = matchParent)

                verticalLayout {
                    themedAppBarLayout(theme = R.style.AppTheme_AppBarOverlay) {
                        backgroundColorResource = R.color.colorPrimaryDark

                        mainToolbar = toolbar {
                            popupTheme = R.style.AppTheme_PopupOverlay
                            titleMarginStart = 0

                        }.lparams(width = matchParent, height = matchParent) {
                            rightMargin = dimen(R.dimen.px34)
                        }
                    }.lparams(width = matchParent, height = dimen(R.dimen.px112))

                    container = frameLayout {
                        id = ID_MAIN_CONTAINER
                    }.lparams(width = matchParent, height = matchParent) {
                        //                        topMargin = dimen(R.dimen.px112) //android.R.attr.actionBarSize
                    }
                }.lparams(width = matchParent, height = matchParent)

                // LEFT MENU
                verticalLayout {
                    leftPadding = dimen(R.dimen.px40)
                    rightPadding = dimen(R.dimen.px40)

                    topPadding = dimen(R.dimen.px60)
                    backgroundColorResource = R.color.colorGridTitle

                    linearLayout {
                        imageView {
                            imageResource = R.drawable.m_home
                            onClick {
                                owner.onClickedMenuItem(ID_MENU_HOME)
                            }
                        }.lparams(width = dimen(R.dimen.px50), height = dimen(R.dimen.px50))

                        space { }.lparams(width = 0, weight = 1f)

                        imageView {
                            imageResource = R.drawable.m_close
                            onClick {
                                owner.onClickedMenuItem(ID_MENU_CLOSE)
                            }
                        }.lparams(width = dimen(R.dimen.px50), height = dimen(R.dimen.px50))
                    }

                    view { backgroundColorResource = R.color.colorPrimaryDark }.lparams(width = matchParent, height = dip(1)) {
                        topMargin = dimen(R.dimen.px40)
                        bottomMargin = dimen(R.dimen.px40)
                    }

                    useridTextView = textView { textSizeDimen = R.dimen.px36 }
                    usernameTextView = textView {}

                    view { backgroundColorResource = R.color.colorMenuSplitLine }.lparams(width = matchParent, height = dip(1)) {
                        topMargin = dimen(R.dimen.px46)
                        bottomMargin = dimen(R.dimen.px46)
                    }

                    linearLayout {
                        gravity = Gravity.CENTER_VERTICAL
                        onClick {
                            owner.onClickedMenuItem(ID_MENU_STATISTICS)
                        }

                        imageView { imageResource = R.drawable.m_statistics }.lparams(width = dimen(R.dimen.px50), height = dimen(R.dimen.px50))
                        textView("Statistics") { gravity = Gravity.CENTER_VERTICAL }.lparams(width = 0, weight = 1f) { leftMargin = dimen(R.dimen.px40) }
                        imageView { imageResource = R.drawable.m_arrow }.lparams(width = dimen(R.dimen.px50), height = dimen(R.dimen.px50))
                    }

                    view { backgroundColorResource = R.color.colorMenuSplitLine }.lparams(width = matchParent, height = dip(1)) {
                        topMargin = dimen(R.dimen.px46)
                        bottomMargin = dimen(R.dimen.px46)
                    }

                    linearLayout {
                        gravity = Gravity.CENTER_VERTICAL
                        onClick {
                            owner.onClickedMenuItem(ID_MENU_CHILDLIST)
                        }
                        imageView { imageResource = R.drawable.m_childlist }.lparams(width = dimen(R.dimen.px50), height = dimen(R.dimen.px50))
                        textView("Child List") { gravity = Gravity.CENTER_VERTICAL }.lparams(width = 0, weight = 1f) { leftMargin = dimen(R.dimen.px40) }
                        imageView { imageResource = R.drawable.m_arrow }.lparams(width = dimen(R.dimen.px50), height = dimen(R.dimen.px50))
                    }

                    view { backgroundColorResource = R.color.colorMenuSplitLine }.lparams(width = matchParent, height = dip(1)) {
                        topMargin = dimen(R.dimen.px46)
                        bottomMargin = dimen(R.dimen.px46)
                    }

                    linearLayout {
                        gravity = Gravity.CENTER_VERTICAL
                        onClick {
                            owner.onClickedMenuItem(ID_MENU_SETTING)
                        }
                        imageView { imageResource = R.drawable.m_setting }.lparams(width = dimen(R.dimen.px50), height = dimen(R.dimen.px50))
                        textView("Setting") { gravity = Gravity.CENTER_VERTICAL }.lparams(width = 0, weight = 1f) { leftMargin = dimen(R.dimen.px40) }
                        imageView { imageResource = R.drawable.m_arrow }.lparams(width = dimen(R.dimen.px50), height = dimen(R.dimen.px50))
                    }

                    view { backgroundColorResource = R.color.colorMenuSplitLine }.lparams(width = matchParent, height = dip(1)) {
                        topMargin = dimen(R.dimen.px46)
                        bottomMargin = dimen(R.dimen.px46)
                    }

                    linearLayout {
                        gravity = Gravity.CENTER_VERTICAL
                        onClick {
                            owner.onClickedMenuItem(ID_MENU_LOGOUT)
                        }
                        imageView { imageResource = R.drawable.m_logout }.lparams(width = dimen(R.dimen.px50), height = dimen(R.dimen.px50))
                        textView("Log-out") { gravity = Gravity.CENTER_VERTICAL }.lparams(width = 0, weight = 1f) { leftMargin = dimen(R.dimen.px40) }
                        imageView { imageResource = R.drawable.m_arrow }.lparams(width = dimen(R.dimen.px50), height = dimen(R.dimen.px50))
                    }

                    view { backgroundColorResource = R.color.colorMenuSplitLine }.lparams(width = matchParent, height = dip(1)) {
                        topMargin = dimen(R.dimen.px46)
                        bottomMargin = dimen(R.dimen.px46)
                    }

                    linearLayout {
                        gravity = Gravity.CENTER_VERTICAL
                        onClick {
                            owner.onClickedMenuItem(ID_MENU_SYNC)
                        }
                        imageView { imageResource = R.drawable.m_sync }.lparams(width = dimen(R.dimen.px50), height = dimen(R.dimen.px50))
                        textView("Sync Data") { gravity = Gravity.CENTER_VERTICAL }.lparams(width = 0, weight = 1f) { leftMargin = dimen(R.dimen.px40) }
                        imageView { imageResource = R.drawable.m_arrow }.lparams(width = dimen(R.dimen.px50), height = dimen(R.dimen.px50))
                    }

                    view { backgroundColorResource = R.color.colorMenuSplitLine }.lparams(width = matchParent, height = dip(1)) {
                        topMargin = dimen(R.dimen.px46)
                        bottomMargin = dimen(R.dimen.px46)
                    }

                    linearLayout {
                        gravity = Gravity.CENTER_VERTICAL
                        imageView { imageResource = R.drawable.m_cms }.lparams(width = dimen(R.dimen.px50), height = dimen(R.dimen.px50))
                        versionTextView = textView { gravity = Gravity.CENTER_VERTICAL }.lparams(width = 0, weight = 1f) { leftMargin = dimen(R.dimen.px40) }
                    }

                    view { backgroundColorResource = R.color.colorMenuBottomSplitLine }.lparams(width = matchParent, height = dip(1)) {
                        topMargin = dimen(R.dimen.px46)
                        bottomMargin = dimen(R.dimen.px46)
                    }

                    textView("© Good Neighbors") {
                        gravity = Gravity.CENTER
                    }
                }.lparams(width = matchParent, height = matchParent) {
                    gravity = Gravity.START
                }
            }
        }
    }
}
