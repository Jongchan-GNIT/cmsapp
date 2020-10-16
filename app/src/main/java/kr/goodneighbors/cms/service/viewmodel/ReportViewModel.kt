package kr.goodneighbors.cms.service.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import kr.goodneighbors.cms.App
import kr.goodneighbors.cms.service.entities.APP_DATA_HISTORY
import kr.goodneighbors.cms.service.entities.ATCH_FILE
import kr.goodneighbors.cms.service.entities.CD
import kr.goodneighbors.cms.service.entities.CH_MST
import kr.goodneighbors.cms.service.entities.RPT_BSC
import kr.goodneighbors.cms.service.model.AprEditViewItem
import kr.goodneighbors.cms.service.model.AprEditViewSearchItem
import kr.goodneighbors.cms.service.model.ReportListItem
import kr.goodneighbors.cms.service.model.SiblingInformationItem
import kr.goodneighbors.cms.service.model.VillageLocation
import kr.goodneighbors.cms.service.repository.ReportRepository
import javax.inject.Inject

class ReportViewModel : ViewModel() {
    @Inject
    lateinit var reportRepository: ReportRepository

    init {
        App.appComponent.inject(this)
    }

    private val initFlag = MutableLiveData<Boolean>()

    val initProgress: LiveData<String> = Transformations.switchMap(initFlag)
    {
        reportRepository.getInitStatus()
    }

    val initProgressReport: LiveData<String> = reportRepository.initProgressReport

    fun initAllChild(chmstList: List<CH_MST>) {
        reportRepository.initAllChild(chmstList)
    }

//    fun saveRPT_BSC(entry: RPT_BSC) {
//        reportRepository.saveRPT_BSC(entry)
//    }

    fun initAll(entries: List<RPT_BSC>, startProgress: Boolean = false) {
        if (startProgress) {
            initFlag.postValue(true)
        }
        reportRepository.initAll(entries, startProgress)
    }

    private val reportByChildId = MutableLiveData<String>()
    private val reportByChild: LiveData<List<ReportListItem>> = Transformations.switchMap(reportByChildId)
    { id ->
        reportRepository.findAllReportByChild(id)
    }
    fun findAllReportByChild(): LiveData<List<ReportListItem>> {
        return reportByChild
    }
    fun setFindAllReportByChild(chrcp_no: String) {
        reportByChildId.postValue(chrcp_no)
    }

    fun deleteReportById(rcp_no: String): LiveData<Boolean> {
        return reportRepository.deleteReportById(rcp_no)
    }

    fun saveAppDataHistory(history: APP_DATA_HISTORY) {
        reportRepository.saveAppDataHistory(history)
    }

    private var aprEditViewSearchItem = MutableLiveData<AprEditViewSearchItem>()
    private var aprEditViewItem: LiveData<AprEditViewItem> = Transformations.switchMap(aprEditViewSearchItem) {
        reportRepository.getAprEditViewItem(it)
    }
    fun getAprEditViewItem(): LiveData<AprEditViewItem> {
        return aprEditViewItem
    }
    fun setAprEditViewSearchItem(search: AprEditViewSearchItem) {
        aprEditViewSearchItem.postValue(search)
    }

    fun save(report: RPT_BSC): LiveData<RPT_BSC> {
        return reportRepository.saveApr(report)
    }

    fun getBMI(birth: String, height: String, weight: String, gender: String): LiveData<CD> {
        return reportRepository.getBMI(birth, height, weight, gender)
    }

    private var findAllSiblingSponsorshipItemWord = MutableLiveData<String>()
    private var findAllSiblingSponsorshipItem: LiveData<List<RPT_BSC>> = Transformations.switchMap(findAllSiblingSponsorshipItemWord) {
        reportRepository.findAllSiblingSponsorship(it)
    }
    fun findAllSiblingSponsorship(): LiveData<List<RPT_BSC>> {
        return findAllSiblingSponsorshipItem
    }

    fun setFindAllSiblingSponsorship(word: String) {
        findAllSiblingSponsorshipItemWord.postValue(word)
    }

    fun findAllSiblingSponsorshipByChild(chrcp_no: String): LiveData<List<SiblingInformationItem>> {
        return reportRepository.findAllSiblingSponsorshipByChild(chrcp_no)
    }

    fun getLocationOfVillage(code: String): MutableLiveData<VillageLocation> {
        return reportRepository.getLocationOfVillage(code)
    }

    fun findAllFiles(rcp_no: String): LiveData<List<ATCH_FILE>> {
        return reportRepository.findAllFiles(rcp_no)
    }


}