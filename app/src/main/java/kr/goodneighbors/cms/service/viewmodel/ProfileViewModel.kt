package kr.goodneighbors.cms.service.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import kr.goodneighbors.cms.App
import kr.goodneighbors.cms.service.entities.CH_CUSL_INFO
import kr.goodneighbors.cms.service.entities.RPT_BSC
import kr.goodneighbors.cms.service.model.CounselingListItem
import kr.goodneighbors.cms.service.model.DropoutEditItem
import kr.goodneighbors.cms.service.model.DropoutEditSearchItem
import kr.goodneighbors.cms.service.model.NextCounselingIndex
import kr.goodneighbors.cms.service.model.ProfileViewItem
import kr.goodneighbors.cms.service.repository.CommonRepository
import kr.goodneighbors.cms.service.repository.ReportRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

class ProfileViewModel: ViewModel() {
    @Inject
    lateinit var commonRepository: CommonRepository

    @Inject
    lateinit var reportRepository: ReportRepository

    init {
        App.appComponent.inject(this)
    }

    private val log: Logger by lazy {
        LoggerFactory.getLogger(ProfileViewModel::class.java)
    }

    @Suppress("PrivatePropertyName")
    private var chrcp_no = MutableLiveData<String>()
    private var reports: LiveData<ProfileViewItem> = Transformations.switchMap(chrcp_no) { no->
        reportRepository.findProfileByChild(no)
    }

    fun getProfileData(): LiveData<ProfileViewItem> {
        return reports
    }

    fun setId(_chrcp_no: String) {
        chrcp_no.postValue(_chrcp_no)
    }

    private var dropoutEditViewItemSearch = MutableLiveData<DropoutEditSearchItem>()
    private var dropoutEditViewItem: LiveData<DropoutEditItem> = Transformations.switchMap(dropoutEditViewItemSearch) {
        reportRepository.findDropoutEditViewItem(it)
    }
    fun getDropoutEditViewItem(): LiveData<DropoutEditItem> {
        return dropoutEditViewItem
    }
    fun setDropoutEditViewItemSearch(s: DropoutEditSearchItem) {
        dropoutEditViewItemSearch.postValue(s)
    }

    fun saveDropout(report: RPT_BSC): LiveData<Boolean> {
        return reportRepository.saveDropout(report)
    }

    private var counselingListItemTrigger = MutableLiveData<Long>()
    private var counselingListItem: LiveData<List<CounselingListItem>> = Transformations.switchMap(counselingListItemTrigger) {
        reportRepository.findAllCounselingByChild(chrcp_no.value!!)
    }
    fun getCounselingList(): LiveData<List<CounselingListItem>> {
        return counselingListItem
    }

    fun setCounselingListTrigger(time: Long) {
        counselingListItemTrigger.postValue(time)
    }

    fun getNextCounselingIndex(chrcp_no: String, crt_tp: String): LiveData<NextCounselingIndex> {
        return reportRepository.getNextCounselingIndex(chrcp_no, crt_tp)
    }

    fun saveCounseling(ch_cusl_info: CH_CUSL_INFO):LiveData<Boolean> {
        return reportRepository.saveCounseling(ch_cusl_info)
    }
}