package kr.goodneighbors.cms.service.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import kr.goodneighbors.cms.App
import kr.goodneighbors.cms.service.model.StatisticAclItem
import kr.goodneighbors.cms.service.model.StatisticAprItem
import kr.goodneighbors.cms.service.model.StatisticCifItem
import kr.goodneighbors.cms.service.model.StatisticDropoutItem
import kr.goodneighbors.cms.service.model.StatisticGmlItem
import kr.goodneighbors.cms.service.model.StatisticsItem
import kr.goodneighbors.cms.service.repository.StatisticsRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

class StatisticsViewModel: ViewModel() {
    @Inject
    lateinit var repository: StatisticsRepository

    init {
        App.appComponent.inject(this)
    }

    private val log: Logger by lazy {
        LoggerFactory.getLogger(StatisticsViewModel::class.java)
    }

    fun getPageItem():LiveData<StatisticsItem> {
        return repository.getPageItem()
    }

    fun getCifData(dnctr_cd: String, year: String): LiveData<List<StatisticCifItem>> {
        return repository.getCifData(dnctr_cd, year)
    }

    fun getAprData(dnctr_cd: String, year: String): LiveData<List<StatisticAprItem>> {
        return repository.getAprData(dnctr_cd, year)
    }

    fun getAclData(dnctr_cd: String, year: String): LiveData<List<StatisticAclItem>> {
        return repository.getAclData(dnctr_cd, year)
    }

    fun getDropoutData(dnctr_cd: String, year: String): LiveData<List<StatisticDropoutItem>> {
        return repository.getDropoutData(dnctr_cd, year)
    }

    fun getGmlData(type: String, year: String, fromMonth: String, toMonth: String): LiveData<List<StatisticGmlItem>> {
        return repository.getGmlData(type,  year, fromMonth, toMonth)
    }
}