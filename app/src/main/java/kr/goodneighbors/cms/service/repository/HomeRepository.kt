package kr.goodneighbors.cms.service.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import kr.goodneighbors.cms.common.GNLocaleManager
import kr.goodneighbors.cms.service.db.HomeDao
import kr.goodneighbors.cms.service.model.HomeItem
import kr.goodneighbors.cms.service.model.NoticeItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
        private val dao: HomeDao,
        private val preferences: SharedPreferences
) {

    private lateinit var homeItem: MutableLiveData<HomeItem>
    fun getHomeItem(): LiveData<HomeItem> {
        homeItem = MutableLiveData()

        Thread(Runnable {
            val ctrCd = preferences.getString("user_ctr_cd", "")!!
            val brcCd = preferences.getString("user_brc_cd", "")!!
            val prjCd = preferences.getString("user_prj_cd", "")!!

            val item = dao.getHomeItem(ctrCd, brcCd, prjCd)
            item.NOTI_ITEMS = dao.findAllNotiInfo()

            item.CIF_STATE = dao.getCifState(ctrCd, brcCd, prjCd)
            item.APR_STATE = dao.getAprState(ctrCd, brcCd, prjCd)
            item.ACL_STATE = dao.getAclState(ctrCd, brcCd, prjCd)
            item.GML_STATE = dao.getGmlState(ctrCd, brcCd, prjCd)

            homeItem.postValue(item)
        }).start()

        return homeItem
    }

    private lateinit var findAllNoticeItemResult:MutableLiveData<List<NoticeItem>>
    fun findAllNoticeItem(): LiveData<List<NoticeItem>> {
        findAllNoticeItemResult = MutableLiveData()

        Thread(Runnable {
            val locale = preferences.getString(GNLocaleManager.SELECTED_LANGUAGE, "en")

            val items = dao.findAllNoticeItem(locale)
            findAllNoticeItemResult.postValue(items)
        }).start()

        return findAllNoticeItemResult
    }

}