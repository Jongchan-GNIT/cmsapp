package kr.goodneighbors.cms.service.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import kr.goodneighbors.cms.App
import kr.goodneighbors.cms.service.entities.RPT_BSC
import kr.goodneighbors.cms.service.model.AclEditViewItem
import kr.goodneighbors.cms.service.model.AclEditViewItemSearch
import kr.goodneighbors.cms.service.model.AclListItem
import kr.goodneighbors.cms.service.repository.CommonRepository
import kr.goodneighbors.cms.service.repository.ReportRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

class AclViewModel: ViewModel() {
    @Inject
    lateinit var commonRepository: CommonRepository

    @Inject
    lateinit var reportRepository: ReportRepository

    init {
        App.appComponent.inject(this)
    }

    private val log: Logger by lazy {
        LoggerFactory.getLogger(AclViewModel::class.java)
    }

    @Suppress("PrivatePropertyName")
    private var chrcp_no = MutableLiveData<String>()
    private var reports: LiveData<List<AclListItem>> = Transformations.switchMap(chrcp_no) { no->
        reportRepository.findAllAclByChild(no)
    }

    fun getReports(): LiveData<List<AclListItem>> {
        return reports
    }

    fun setId(_chrcp_no: String) {
        chrcp_no.postValue(_chrcp_no)
    }

    private var aclEditViewItemSearch = MutableLiveData<AclEditViewItemSearch>()
    private var aclEditViewItem: LiveData<AclEditViewItem> = Transformations.switchMap(aclEditViewItemSearch) {
        reportRepository.findAclEditViewItem(it)
    }
    fun getAclEditViewItem(): LiveData<AclEditViewItem> {
        return aclEditViewItem
    }
    fun setAclEditViewItemSearch(s: AclEditViewItemSearch) {
        aclEditViewItemSearch.postValue(s)
    }

    fun save(report: RPT_BSC): LiveData<Boolean> {
        return reportRepository.saveAcl(report)
    }
}