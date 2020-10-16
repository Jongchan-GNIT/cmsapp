package kr.goodneighbors.cms.ui

import android.content.Context
import android.support.v7.app.AppCompatActivity
import kr.goodneighbors.cms.common.GNLocaleManager


open class BaseActivity: AppCompatActivity() {
//    override fun onBackPressed() {
//        val mngr = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//
//        val taskList = mngr.getRunningTasks(10)
//
//        if (taskList[0].numActivities == 1 && taskList[0].topActivity.className == this.javaClass.NM) {
//            Log.i(Constants.TAG, "This is last activity in the stack")
//        }
//    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(GNLocaleManager.setLocale(base))
    }
}