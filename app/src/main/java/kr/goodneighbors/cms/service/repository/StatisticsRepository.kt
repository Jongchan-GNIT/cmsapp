@file:Suppress("LocalVariableName")

package kr.goodneighbors.cms.service.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import kr.goodneighbors.cms.service.db.StatisticsDao
import kr.goodneighbors.cms.service.model.StatisticAclItem
import kr.goodneighbors.cms.service.model.StatisticAprItem
import kr.goodneighbors.cms.service.model.StatisticCifItem
import kr.goodneighbors.cms.service.model.StatisticDropoutItem
import kr.goodneighbors.cms.service.model.StatisticGmlItem
import kr.goodneighbors.cms.service.model.StatisticsItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepository @Inject constructor(
        private val dao: StatisticsDao,
        private val commonRepository: CommonRepository,
        private val preferences: SharedPreferences
) {

    private lateinit var getPageItemResult: MutableLiveData<StatisticsItem>
    fun getPageItem(): LiveData<StatisticsItem> {
        getPageItemResult = MutableLiveData()

        Thread(Runnable {
            val supportCountry = commonRepository.findAllCommonCodeByGroupCode("79")

            getPageItemResult.postValue(StatisticsItem(codeSupportCountry = supportCountry))
        }).start()

        return getPageItemResult
    }

    private lateinit var getCifDataResult: MutableLiveData<List<StatisticCifItem>>
    fun getCifData(dnctr_cd: String, year: String): LiveData<List<StatisticCifItem>> {
        getCifDataResult = MutableLiveData()

        Thread(Runnable {
            val ctr_cd = preferences.getString("user_ctr_cd", "")!!
            val brc_cd = preferences.getString("user_brc_cd", "")!!
            val prj_cd = preferences.getString("user_prj_cd", "")!!

            getCifDataResult.postValue(dao.getCifData(dnctr_cd = dnctr_cd, ctr_cd = ctr_cd, brc_cd = brc_cd, prj_cd = prj_cd, year = year))
        }).start()

        return getCifDataResult
    }

    private lateinit var getAprDataResult: MutableLiveData<List<StatisticAprItem>>
    fun getAprData(dnctr_cd: String, year: String): LiveData<List<StatisticAprItem>> {
        getAprDataResult = MutableLiveData()

        Thread(Runnable {
            val ctr_cd = preferences.getString("user_ctr_cd", "")!!
            val brc_cd = preferences.getString("user_brc_cd", "")!!
            val prj_cd = preferences.getString("user_prj_cd", "")!!

            getAprDataResult.postValue(dao.getAprData(dnctr_cd = dnctr_cd, ctr_cd = ctr_cd, brc_cd = brc_cd, prj_cd = prj_cd, year = year))
        }).start()

        return getAprDataResult
    }

    private lateinit var getAclDataResult: MutableLiveData<List<StatisticAclItem>>
    fun getAclData(dnctr_cd: String, year: String): LiveData<List<StatisticAclItem>> {
        getAclDataResult = MutableLiveData()

        Thread(Runnable {
            val ctr_cd = preferences.getString("user_ctr_cd", "")!!
            val brc_cd = preferences.getString("user_brc_cd", "")!!
            val prj_cd = preferences.getString("user_prj_cd", "")!!

            getAclDataResult.postValue(dao.getAclData(dnctr_cd = dnctr_cd, ctr_cd = ctr_cd, brc_cd = brc_cd, prj_cd = prj_cd, year = year))
        }).start()

        return getAclDataResult
    }

    private lateinit var getDropoutDataResult: MutableLiveData<List<StatisticDropoutItem>>
    fun getDropoutData(dnctr_cd: String, year: String): LiveData<List<StatisticDropoutItem>> {
        getDropoutDataResult = MutableLiveData()

        Thread(Runnable {
            val ctr_cd = preferences.getString("user_ctr_cd", "")!!
            val brc_cd = preferences.getString("user_brc_cd", "")!!
            val prj_cd = preferences.getString("user_prj_cd", "")!!

            getDropoutDataResult.postValue(dao.getDropoutData(dnctr_cd = dnctr_cd, ctr_cd = ctr_cd, brc_cd = brc_cd, prj_cd = prj_cd, year = year))
        }).start()

        return getDropoutDataResult
    }

    private lateinit var getGmlDataResult: MutableLiveData<List<StatisticGmlItem>>
    fun getGmlData(type: String, year: String, fromMonth: String, toMonth: String): LiveData<List<StatisticGmlItem>> {
        getGmlDataResult = MutableLiveData()

        Thread(Runnable {
            when(type) {
                "GM"-> {
                    getGmlDataResult.postValue(dao.getGmData(year, fromMonth, toMonth))
                }
                "Letter"-> {
                    getGmlDataResult.postValue(dao.getLetterData(year, fromMonth, toMonth))
                }
            }
        }).start()

        return getGmlDataResult
    }

}