package kr.goodneighbors.cms.service.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import kr.goodneighbors.cms.App
import kr.goodneighbors.cms.service.entities.BMI
import kr.goodneighbors.cms.service.entities.BRC
import kr.goodneighbors.cms.service.entities.CD
import kr.goodneighbors.cms.service.entities.CH_CUSL_INFO
import kr.goodneighbors.cms.service.entities.CTR
import kr.goodneighbors.cms.service.entities.GMNY
import kr.goodneighbors.cms.service.entities.LETR
import kr.goodneighbors.cms.service.entities.NOTI_INFO
import kr.goodneighbors.cms.service.entities.PRJ
import kr.goodneighbors.cms.service.entities.PRSN_INFO
import kr.goodneighbors.cms.service.entities.RELSH
import kr.goodneighbors.cms.service.entities.SCHL
import kr.goodneighbors.cms.service.entities.SPLY_PLAN
import kr.goodneighbors.cms.service.entities.SRVC
import kr.goodneighbors.cms.service.entities.VLG
import kr.goodneighbors.cms.service.model.CommonCode
import kr.goodneighbors.cms.service.repository.CommonRepository
import java.util.*
import javax.inject.Inject

class CommonViewModel : ViewModel() {
    @Inject
    lateinit var commonRepository: CommonRepository

    init {
        App.appComponent.inject(this)
    }

    fun initNotiInfo(notiInfo: List<NOTI_INFO>) {
        commonRepository.initNotiInfo(notiInfo)
    }

    fun initCd(cdList: List<CD>) {
        commonRepository.initCd(cdList)
    }

    fun initCtr(ctrList: List<CTR>) {
        commonRepository.initCtr(ctrList)
    }

    fun initBrc(brcList: List<BRC>) {
        commonRepository.initBrc(brcList)
    }

    fun initPrj(prjList: List<PRJ>) {
        commonRepository.initPrj(prjList)
    }

    fun initVlg(vlgList: List<VLG>) {
        commonRepository.initVlg(vlgList)
    }

    fun initSchl(schlList: List<SCHL>) {
        commonRepository.initSchl(schlList)
    }

    fun initPrsnInfo(prsnInfoList: List<PRSN_INFO>) {
        commonRepository.initPrsnInfo(prsnInfoList)
    }

    fun initSplyPlan(splyPlanList: List<SPLY_PLAN>) {
        commonRepository.initSplyPlan(splyPlanList)
    }

    fun initSrvc(srvcList: List<SRVC>) {
        commonRepository.initSrvc(srvcList)
    }

    fun initChCuslInfo(chCuslInfoList: List<CH_CUSL_INFO>) {
        commonRepository.initChCuslInfo(chCuslInfoList)
    }

    fun initRelsh(relshList: List<RELSH>) {
        commonRepository.initRelsh(relshList)
    }

    fun initLetr(letrList: List<LETR>) {
        commonRepository.initLetr(letrList)
    }

    fun initGmny(gmnyList: ArrayList<GMNY>) {
        commonRepository.initGmny(gmnyList)
    }

    fun initBmi(bmiList: ArrayList<BMI>) {
        commonRepository.initBmi(bmiList)
    }

    fun getMoreDialogCommonCode(): LiveData<CommonCode> = commonRepository.getMoreDialogCommonCode()

    private var commonCodeTrigger = MutableLiveData<Long>()
    private var commonCode: LiveData<CommonCode> = Transformations.switchMap(commonCodeTrigger) {
        commonRepository.getCommonCode()
    }
    fun getCommonCodeItems(): LiveData<CommonCode> {
        return commonCode
    }
    fun setCommonCodeItems() {
        commonCodeTrigger.postValue(Date().time)
    }
}