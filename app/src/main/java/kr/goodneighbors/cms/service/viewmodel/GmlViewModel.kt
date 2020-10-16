package kr.goodneighbors.cms.service.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import kr.goodneighbors.cms.App
import kr.goodneighbors.cms.service.entities.RPT_BSC
import kr.goodneighbors.cms.service.model.GmEditItem
import kr.goodneighbors.cms.service.model.GmLetterEditItem
import kr.goodneighbors.cms.service.model.GmlEditItemSearch
import kr.goodneighbors.cms.service.model.GmlListItem
import kr.goodneighbors.cms.service.repository.CommonRepository
import kr.goodneighbors.cms.service.repository.ReportRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

class GmlViewModel: ViewModel() {
    @Inject
    lateinit var commonRepository: CommonRepository

    @Inject
    lateinit var reportRepository: ReportRepository

    init {
        App.appComponent.inject(this)
    }

    private val log: Logger by lazy {
        LoggerFactory.getLogger(GmlViewModel::class.java)
    }

    @Suppress("PrivatePropertyName")
    private var chrcp_no = MutableLiveData<String>()
    private var reports: LiveData<List<GmlListItem>> = Transformations.switchMap(chrcp_no) { no->
        reportRepository.findAllGmlByChild(no)
    }

    fun getReports(): LiveData<List<GmlListItem>> {
        return reports
    }

    fun setGetReportsTrigger(_chrcp_no: String) {
        chrcp_no.postValue(_chrcp_no)
    }

    private var gmEditItemSearch = MutableLiveData<GmlEditItemSearch>()
    private var gmEditItem: LiveData<GmEditItem> = Transformations.switchMap(gmEditItemSearch) {
        reportRepository.findGmEditItem(it)
    }
    fun getGmEditItem(): LiveData<GmEditItem> {
        return gmEditItem
    }
    fun setGmEditItemSearch(search: GmlEditItemSearch) {
        gmEditItemSearch.postValue(search)
    }

    fun saveGm(report: RPT_BSC): LiveData<Boolean> {
        return reportRepository.saveGm(report)
    }

    private var gmLetterEditItemSearch = MutableLiveData<GmlEditItemSearch>()
    private var gmLetterEditItem: LiveData<GmLetterEditItem> = Transformations.switchMap(gmLetterEditItemSearch) {
        reportRepository.findGmLetterEditItem(it)
    }
    fun getGmLetterEditItem(): LiveData<GmLetterEditItem> {
        return gmLetterEditItem
    }
    fun setGmLetterEditItemSearch(search: GmlEditItemSearch) {
        gmLetterEditItemSearch.postValue(search)
    }

    fun saveGmLetter(report: RPT_BSC): LiveData<Boolean> {
        return reportRepository.saveGmLetter(report)
    }
}