package kr.goodneighbors.cms.service.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import kr.goodneighbors.cms.App
import kr.goodneighbors.cms.service.entities.CH_MST
import kr.goodneighbors.cms.service.entities.SRVC
import kr.goodneighbors.cms.service.model.ProvidedServiceEditItem
import kr.goodneighbors.cms.service.model.ProvidedServiceEditSearchItem
import kr.goodneighbors.cms.service.model.ProvidedServiceListItem
import kr.goodneighbors.cms.service.model.ProvidedServiceRegistItem
import kr.goodneighbors.cms.service.model.ProvidedServiceRegistSearchItem
import kr.goodneighbors.cms.service.repository.ReportRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

class ProvidedServiceViewModel : ViewModel() {
    @Inject
    lateinit var reportRepository: ReportRepository

    init {
        App.appComponent.inject(this)
    }

    private val log: Logger by lazy {
        LoggerFactory.getLogger(ProvidedServiceViewModel::class.java)
    }

    @Suppress("PrivatePropertyName")
    private var chrcp_no = MutableLiveData<String>()
    private var reports: LiveData<List<ProvidedServiceListItem>> = Transformations.switchMap(chrcp_no) { no ->
        reportRepository.findAllProvidedServiceByChild(no)
    }

    fun getReports(): LiveData<List<ProvidedServiceListItem>> {
        return reports
    }

    fun setId(_chrcp_no: String) {
        chrcp_no.postValue(_chrcp_no)
    }

    fun getChild(_chrcp_no: String) : LiveData<CH_MST> {
        return reportRepository.findChildById(_chrcp_no)
    }

    private var providedServiceEditSearch = MutableLiveData<ProvidedServiceEditSearchItem>()
    private var providedServiceEditSearchItem: LiveData<List<ProvidedServiceEditItem>> = Transformations.switchMap(providedServiceEditSearch) { item ->
        reportRepository.findAllProvidedServiceEditItemByChild(item)
    }

    fun getServiceEditList(): LiveData<List<ProvidedServiceEditItem>> {
        return providedServiceEditSearchItem
    }

    fun setServiceEditSearch(item: ProvidedServiceEditSearchItem) {
        providedServiceEditSearch.postValue(item)
    }


    private var providedServiceRegistItemSearch = MutableLiveData<ProvidedServiceRegistSearchItem>()
    private var providedServiceRegistItem: LiveData<List<ProvidedServiceRegistItem>> = Transformations.switchMap(providedServiceRegistItemSearch) { search ->
        reportRepository.findAllProvidedServiceRegistItem(search)
    }
    fun getServiceRegistItem(): LiveData<List<ProvidedServiceRegistItem>> {
        return providedServiceRegistItem
    }
    fun setServiceRegistItemSearch(search: ProvidedServiceRegistSearchItem) {
        providedServiceRegistItemSearch.postValue(search)
    }

    fun saveServiceRegistItem(items: List<SRVC>): LiveData<Boolean> {
        return reportRepository.saveSrvc(items)
    }

    fun saveServiceEditItems(items: ArrayList<SRVC>): MutableLiveData<Boolean> {
        return reportRepository.editSrvc(items)
    }
}