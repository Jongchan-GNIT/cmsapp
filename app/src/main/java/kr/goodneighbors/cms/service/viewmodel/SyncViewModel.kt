package kr.goodneighbors.cms.service.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import kr.goodneighbors.cms.App
import kr.goodneighbors.cms.common.ProcessState
import kr.goodneighbors.cms.service.entities.APP_DATA_HISTORY
import kr.goodneighbors.cms.service.entities.BMI
import kr.goodneighbors.cms.service.entities.BRC
import kr.goodneighbors.cms.service.entities.CD
import kr.goodneighbors.cms.service.entities.CH_CUSL_INFO
import kr.goodneighbors.cms.service.entities.CH_MST
import kr.goodneighbors.cms.service.entities.CTR
import kr.goodneighbors.cms.service.entities.GMNY
import kr.goodneighbors.cms.service.entities.LETR
import kr.goodneighbors.cms.service.entities.NOTI_INFO
import kr.goodneighbors.cms.service.entities.PRJ
import kr.goodneighbors.cms.service.entities.PRSN_INFO
import kr.goodneighbors.cms.service.entities.RELSH
import kr.goodneighbors.cms.service.entities.RPT_BSC
import kr.goodneighbors.cms.service.entities.SCHL
import kr.goodneighbors.cms.service.entities.SPLY_PLAN
import kr.goodneighbors.cms.service.entities.SRVC
import kr.goodneighbors.cms.service.entities.USER_INFO
import kr.goodneighbors.cms.service.entities.VLG
import kr.goodneighbors.cms.service.model.ApiDownloadListParam
import kr.goodneighbors.cms.service.model.ApiDownloadListRsponse
import kr.goodneighbors.cms.service.model.ExportDataItem
import kr.goodneighbors.cms.service.repository.ServerRepository
import kr.goodneighbors.cms.service.repository.SyncRepository
import javax.inject.Inject

@Suppress("FunctionName")
class SyncViewModel : ViewModel() {
    @Inject
    lateinit var repository: SyncRepository

    @Inject
    lateinit var server: ServerRepository

    init {
        App.appComponent.inject(this)
    }

    // 임포트 히스토리 조회
    fun findAllAppDataImportHistory(): LiveData<List<String>> {
        return repository.findAllAppDataImportHistory()
    }

    // 동기화 히스토리 등록
    fun saveAppDataHistory(history: APP_DATA_HISTORY) {
        repository.saveAppDataHistory(history)
    }

    fun saveAllCH_MST(chmstList: List<CH_MST>) {
        repository.saveAllChild(chmstList)
    }

    fun saveAllRPT_BSC(entries: List<RPT_BSC>, sourcePath: String, destPath: String): LiveData<ProcessState> {
        return repository.saveAllReport(entries, sourcePath, destPath)
    }

    fun saveAllNOTI_INFO(notiInfo: List<NOTI_INFO>) {
        repository.saveAllNOTI_INFO(notiInfo)
    }

    fun saveAllCD(cdList: List<CD>) {
        repository.saveAllCD(cdList)
    }

    fun saveAllCTR(ctrList: List<CTR>) {
        repository.saveAllCTR(ctrList)
    }

    fun saveAllBRC(brcList: List<BRC>) {
        repository.saveAllBRC(brcList)
    }

    fun saveAllBMI(bmiList: List<BMI>) {
        repository.saveAllBMI(bmiList)
    }

    fun saveAllPRJ(prjList: List<PRJ>) {
        repository.saveAllPRJ(prjList)
    }

    fun saveAllVLG(vlgList: List<VLG>) {
        repository.saveAllVLG(vlgList)
    }

    fun saveAllSCHL(schlList: List<SCHL>) {
        repository.saveAllSCHL(schlList)
    }

    fun saveAllPRSN_INFO(prsnInfoList: List<PRSN_INFO>) {
        repository.saveAllPRSN_INFO(prsnInfoList)
    }

    fun saveAllSPLY_PLAN(splyPlanList: List<SPLY_PLAN>) {
        repository.saveAllSPLY_PLAN(splyPlanList)
    }

    fun saveAllSRVC(srvcList: List<SRVC>) {
        repository.saveAllSRVC(srvcList)
    }

    fun saveAllCH_CUSL_INFO(chCuslInfoList: List<CH_CUSL_INFO>, sourcePath: String, destPath: String) {
        repository.saveAllCH_CUSL_INFO(chCuslInfoList, sourcePath, destPath)
    }

    fun saveAllRELSH(relshList: List<RELSH>) {
        repository.saveAllRELSH(relshList)
    }

    fun saveAllLETR(letrList: List<LETR>) {
        repository.saveAllLETR(letrList)
    }

    fun saveAllGMNY(gmnyList: ArrayList<GMNY>) {
        repository.saveAllGMNY(gmnyList)
    }

    fun saveAllUSER_INFO(userinfoList: ArrayList<USER_INFO>) {
        repository.saveAllUSER_INFO(userinfoList)
    }

    fun findAllExportData(): LiveData<ExportDataItem> {
        return repository.findAllExportData()
    }

    private val isRequestListener = MutableLiveData<Boolean>()
    val loadStatus: LiveData<Boolean> = Transformations.switchMap(isRequestListener)
    {
        server.getLoadingStatus()
    }

    val errorStatus : LiveData<String> = Transformations.switchMap(isRequestListener)
    {
        server.getErrorStatus()
    }
    fun findAllDownloadList(): LiveData<ApiDownloadListRsponse> {
        isRequestListener.postValue(true)
        return server.findAllDownloadList(ApiDownloadListParam())
    }

    fun callUploadAPI(path: String): LiveData<String> {
        return server.callUploadAPI(path)
    }
}