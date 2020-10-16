@file:Suppress("PrivatePropertyName")

package kr.goodneighbors.cms.service.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import kr.goodneighbors.cms.App
import kr.goodneighbors.cms.service.entities.CD
import kr.goodneighbors.cms.service.entities.RPT_BSC
import kr.goodneighbors.cms.service.model.CifEditViewItem
import kr.goodneighbors.cms.service.model.DuplicateChildItem
import kr.goodneighbors.cms.service.model.VillageLocation
import kr.goodneighbors.cms.service.repository.CommonRepository
import kr.goodneighbors.cms.service.repository.ReportRepository
import javax.inject.Inject

class CifViewModel : ViewModel() {
    @Inject
    lateinit var commonRepository: CommonRepository

    @Inject
    lateinit var reportRepository: ReportRepository

    init {
        App.appComponent.inject(this)
    }

    private var rcp_no = MutableLiveData<String>()
    private var cifEditViewItem: LiveData<CifEditViewItem> = Transformations.switchMap(rcp_no) {
        reportRepository.getCifEditViewItem(it)
    }

    fun getCifEditViewItem(): LiveData<CifEditViewItem> {
        return cifEditViewItem
    }

    fun setRcpNo(r: String?) {
        rcp_no.postValue(r)
    }

    fun save(report: RPT_BSC): LiveData<RPT_BSC> {
        return reportRepository.saveCif(report)
    }

    fun getBMI(birth: String, height: String, weight: String, gender: String): LiveData<CD> {
        return reportRepository.getBMI(birth, height, weight, gender)
    }

    fun findAllDuplicateChildren(firstName: String, middleName: String, lastName: String, gender: String, birth: String): LiveData<List<DuplicateChildItem>> {
        return reportRepository.findAllDuplicateChildren(firstName, middleName, lastName, gender, birth)
    }

    fun getLocationOfVillage(code: String): MutableLiveData<VillageLocation> {
        return reportRepository.getLocationOfVillage(code)
    }
}