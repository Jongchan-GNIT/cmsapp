package kr.goodneighbors.cms.service.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import kr.goodneighbors.cms.common.ProcessState
import kr.goodneighbors.cms.extensions.toDateFormat
import kr.goodneighbors.cms.service.db.ReportDao
import kr.goodneighbors.cms.service.db.SyncDao
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
import kr.goodneighbors.cms.service.model.ExportDataItem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("FunctionName")
@Singleton
class SyncRepository @Inject constructor(
        private val dao: SyncDao,
        private val reportDao: ReportDao,
        private val preferences: SharedPreferences
) {
    private val logger: Logger by lazy {
        LoggerFactory.getLogger(SyncRepository::class.java)
    }

    private val initStatusLive = MutableLiveData<String>()

    fun getInitStatus(): LiveData<String> {
        return initStatusLive
    }

    private lateinit var findAllAppDataImportHistoryData: MutableLiveData<List<String>>
    fun findAllAppDataImportHistory(): LiveData<List<String>> {
        findAllAppDataImportHistoryData = MutableLiveData()
        Thread(Runnable {
            val item = dao.findAllAppDataImportHistory()
            findAllAppDataImportHistoryData.postValue(item)
        }).start()
        return findAllAppDataImportHistoryData
    }

    fun saveAppDataHistory(history: APP_DATA_HISTORY) {
        Thread(Runnable {
            dao.saveAppDataHistory(history)

            preferences.edit()
                    .putString("GN_DATA_DATE", dao.getLastUpdateDate()?.value?.toLong()?.toDateFormat() ?: "-")
                    .apply()
        }).start()
    }

    fun saveAllChild(chmstList: List<CH_MST>) {
        Thread(Runnable {
            logger.debug("saveAllChild start")

            chmstList.forEach {
                val dbChild = dao.findCH_MST(it.CHRCP_NO)

                if (dbChild == null) {
                    dao.saveCH_MST(it)
                }
                else {
                    val dbLastDate = (dbChild.UPD_DT?:dbChild.REG_DT)?:0
                    val serverLastDate = (it.UPD_DT?:it.REG_DT)?:0

                    if (serverLastDate > dbLastDate) {
                        dao.saveCH_MST(it)
                    }
                }
            }

            logger.debug("saveAllChild finish")
        }).start()
    }

    private lateinit var saveAllReportResult:MutableLiveData<ProcessState>
    fun saveAllReport(reports: List<RPT_BSC>, sourcePath: String, destPath: String): LiveData<ProcessState> {
        saveAllReportResult = MutableLiveData()
        Thread(Runnable {
            logger.debug("saveAllReport start")
            try {
                reports.forEach {
                    val dbReport = dao.findRPT_BSC(it.RCP_NO)

                    if (dbReport == null) {
                        saveNewerReport(it, sourcePath, destPath)
                    } else {
                        val dbLastDate = ((dbReport.LAST_UPD_DT ?: dbReport.UPD_DT) ?: dbReport.REG_DT) ?: 0
                        val serverLastDate = ((it.LAST_UPD_DT ?: it.UPD_DT) ?: it.REG_DT) ?: 0

                        val serverRptStcd = it.RPT_STCD

                        if (serverLastDate > dbLastDate || serverRptStcd == "1" || serverRptStcd == "2" || serverRptStcd == "3" || serverRptStcd == "4" || serverRptStcd == "5" || serverRptStcd == "6" || serverRptStcd == "7" || serverRptStcd == "8" || serverRptStcd == "14") {
                            saveNewerReport(it, sourcePath, destPath)
                        }
                    }
                }

                logger.debug("saveAllReport finish")

                saveAllReportResult.postValue(ProcessState.DONE)
            }
            catch (e: Exception) {
                e.printStackTrace()
                logger.error("saveAllReport : ", e)
                saveAllReportResult.postValue(ProcessState.ERROR)
            }
        }).start()

        return saveAllReportResult
    }

    private fun saveNewerReport(report: RPT_BSC, sourcePath: String, destPath: String) {
        dao.saveRPT_BSC(report)

        if (report.CH_BSC != null) {
            report.CH_BSC!!.RCP_NO = report.RCP_NO
            dao.saveCH_BSC(report.CH_BSC!!)
        }

        if (report.FMLY != null) {
            report.FMLY!!.RCP_NO = report.RCP_NO
            dao.saveFMLY(report.FMLY!!)
        }

        if (report.INTV != null) {
            report.INTV!!.RCP_NO = report.RCP_NO
            dao.saveINTV(report.INTV!!)
        }

        if (report.LIV_COND != null) {
            report.LIV_COND!!.RCP_NO = report.RCP_NO
            dao.saveLIV_COND(report.LIV_COND!!)
        }

        if (report.SWRT != null) {
            report.SWRT!!.RCP_NO = report.RCP_NO
            dao.saveSWRT(report.SWRT!!)
        }

        if (report.HLTH != null) {
            report.HLTH!!.RCP_NO = report.RCP_NO
            dao.saveHLTH(report.HLTH!!)
        }

        if (report.REMRK != null) {
            report.REMRK!!.RCP_NO = report.RCP_NO
            dao.saveREMRK(report.REMRK!!)
        }

        if (report.EDU != null) {
            report.EDU!!.RCP_NO = report.RCP_NO
            dao.saveEDU(report.EDU!!)
        }

        if (report.RPT_DIARY != null) {
            report.RPT_DIARY!!.RCP_NO = report.RCP_NO
            dao.saveRPT_DIARY(report.RPT_DIARY!!)
        }

        if (report.ACL != null) {
            report.ACL!!.RCP_NO = report.RCP_NO
            dao.saveACL(report.ACL!!)
        }

        if (report.DROPOUT != null) {
            report.DROPOUT!!.RCP_NO = report.RCP_NO
            dao.saveDROPOUT(report.DROPOUT!!)
        }

        if (report.RPLY != null) {
            report.RPLY!!.RCP_NO = report.RCP_NO
            dao.saveRPLY(report.RPLY!!)
        }

        if (report.DROPOUT_PLAN != null) {
            report.DROPOUT_PLAN!!.RCP_NO = report.RCP_NO
            dao.saveDROPOUT_PLAN(report.DROPOUT_PLAN!!)
        }

        if (report.GIFT_BRKDW != null) {
            report.GIFT_BRKDW!!.forEach {
                it.RCP_NO = report.RCP_NO
            }
            dao.saveAllGIFT_BRKDW(report.GIFT_BRKDW!!)
        }

        if (report.SIBL != null) {
            report.SIBL!!.forEach {
                it.RCP_NO = report.RCP_NO
            }
            dao.saveAllSIBL(report.SIBL!!)
        }

        if (report.ATCH_FILE != null) {
            report.ATCH_FILE!!.forEach {
                it.RCP_NO = report.RCP_NO

                try {
                    val source = File("$sourcePath/${it.FILE_PATH ?: ""}", it.FILE_NM ?: "")
                    if (source.exists()) {
                        val targetDir = File("$destPath/${it.FILE_PATH ?: ""}")
                        if (!targetDir.exists()) {
                            targetDir.mkdirs()
                        }
                        val targetFile = File(targetDir, it.FILE_NM)
                        source.copyTo(targetFile, true)
                    }
                }
                catch(e: Exception) {
                    e.printStackTrace()
                }
            }
            dao.saveAllATCH_FILE(report.ATCH_FILE!!)
        }

        if (report.RETN != null) {
            report.RETN!!.forEach {
                it.RCP_NO = report.RCP_NO

            }
            dao.saveAllRETN(report.RETN!!)
        }

        if (report.CH_SPSL_INFO != null) {
            report.CH_SPSL_INFO!!.forEach {
                it.RCP_NO = report.RCP_NO
            }
            dao.saveAllCH_SPSL_INFO(report.CH_SPSL_INFO!!)
        }

        if (report.PRSN_ANS_INFO != null) {
            report.PRSN_ANS_INFO!!.forEach {
                it.RCP_NO = report.RCP_NO
            }
            dao.saveAllPRSN_ANS_INFO(report.PRSN_ANS_INFO!!)
        }
    }

    fun saveAllSRVC(srvcList: List<SRVC>) {
        Thread(Runnable {
            logger.debug("saveAllSRVC start")
//            dao.deleteAllSRVC()
            dao.saveAllSRVC(srvcList)
            logger.debug("saveAllSRVC finish")
        }).start()
    }

    fun saveAllCH_CUSL_INFO(chCuslInfoList: List<CH_CUSL_INFO>, sourcePath: String, destPath: String) {
        fun saveNewCH_CUSL_INFO(item: CH_CUSL_INFO, sourcePath: String, destPath: String) {
            dao.saveCH_CUSL_INFO(item)

            try {
                val source = File("$sourcePath/${item.IMG_FP ?: ""}", item.IMG_NM ?: "")
                if (source.exists()) {
                    val targetDir = File("$destPath/${item.IMG_FP ?: ""}")
                    if (!targetDir.exists()) {
                        targetDir.mkdirs()
                    }
                    val targetFile = File(targetDir, item.IMG_NM)
                    source.copyTo(targetFile, true)
                }
            }
            catch(e: Exception) {
                e.printStackTrace()
            }
        }

        Thread(Runnable {
            logger.debug("saveAllCH_CUSL_INFO start")
//            dao.deleteAllCH_CUSL_INFO()
//            dao.saveAllCH_CUSL_INFO(chCuslInfoList)
            logger.debug("saveAllCH_CUSL_INFO finish")

            chCuslInfoList.forEach {
                val dbItem = dao.findCH_CUSL_INFO(it.CHRCP_NO, it.CRT_TP, it.SEQ_NO)
                if (dbItem == null) {
                    saveNewCH_CUSL_INFO(it, sourcePath, destPath)
                }
                else {
                    val dbLastDate = (dbItem.UPD_DT?:dbItem.REG_DT)?:0
                    val serverLastDate = (it.UPD_DT?:it.REG_DT)?:0

                    if (serverLastDate > dbLastDate) {
                        saveNewCH_CUSL_INFO(it, sourcePath, destPath)
                    }
                }
            }
        }).start()
    }

    fun saveAllRELSH(relshList: List<RELSH>) {
        Thread(Runnable {
            logger.debug("saveAllRELSH start")
//            dao.deleteAllRELSH()
            dao.saveAllRELSH(relshList)
            logger.debug("saveAllRELSH finish")
        }).start()
    }

    fun saveAllLETR(letrList: List<LETR>) {
        Thread(Runnable {
            logger.debug("saveAllLETR start")
//            dao.deleteAllLETR()
            dao.saveAllLETR(letrList)
            logger.debug("saveAllLETR finish")
        }).start()
    }

    fun saveAllGMNY(gmnyList: ArrayList<GMNY>) {
        Thread(Runnable {
            logger.debug("saveAllGMNY start")
//            dao.deleteAllGMNY()
            dao.saveAllGMNY(gmnyList)
            logger.debug("saveAllGMNY finish")
        }).start()
    }

    // 기초데이터
    fun saveAllNOTI_INFO(notiInfoList: List<NOTI_INFO>) {
        Thread(Runnable {
            logger.debug("saveAllNOTI_INFO start")
            dao.deleteAllNOTI_INFO()
            dao.saveAllNOTI_INFO(notiInfoList)
            logger.debug("saveAllNOTI_INFO finish")
        }).start()
    }

    fun saveAllCD(cdList: List<CD>) {
        Thread(Runnable {
            logger.debug("saveAllCD start")
            dao.deleteAllCD()
            dao.saveAllCD(cdList)
            logger.debug("saveAllCD finish")
        }).start()
    }

    fun saveAllCTR(ctrList: List<CTR>) {
        Thread(Runnable {
            logger.debug("saveAllCTR start")
            dao.deleteAllCTR()
            dao.saveAllCTR(ctrList)
            logger.debug("saveAllCTR finish")
        }).start()
    }

    fun saveAllBRC(brcList: List<BRC>) {
        Thread(Runnable {
            logger.debug("saveAllBRC start")
            dao.deleteAllBRC()
            dao.saveAllBRC(brcList)
            logger.debug("saveAllBRC finish")
        }).start()
    }

    fun saveAllBMI(bmiList: List<BMI>) {
        Thread(Runnable {
            logger.debug("saveAllBMI start")
            dao.deleteAllBMI()
            dao.saveAllBMI(bmiList)
            logger.debug("saveAllBMI finish")
        }).start()
    }

    fun saveAllPRJ(prjList: List<PRJ>) {
        Thread(Runnable {
            logger.debug("saveAllPRJ start")
            dao.deleteAllPRJ()
            dao.saveAllPRJ(prjList)
            logger.debug("saveAllPRJ finish")
        }).start()
    }

    fun saveAllVLG(vlgList: List<VLG>) {
        Thread(Runnable {
            logger.debug("saveAllVLG start")
            dao.deleteAllVLG()
            dao.saveAllVLG(vlgList)
            logger.debug("saveAllVLG finish")
        }).start()
    }

    fun saveAllSCHL(schlList: List<SCHL>) {
        Thread(Runnable {
            logger.debug("saveAllSCHL start")
            dao.deleteAllSCHL()
            dao.saveAllSCHL(schlList)
            logger.debug("saveAllSCHL finish")
        }).start()
    }

    fun saveAllPRSN_INFO(prsnInfoList: List<PRSN_INFO>) {
        Thread(Runnable {
            logger.debug("saveAllPRSN_INFO start")
            dao.deleteAllPRSN_INFO()
            dao.saveAllPRSN_INFO(prsnInfoList)
            logger.debug("saveAllPRSN_INFO finish")
        }).start()
    }

    fun saveAllSPLY_PLAN(splyPlanList: List<SPLY_PLAN>) {
        Thread(Runnable {
            logger.debug("saveAllSPLY_PLAN start")
            dao.deleteAllSPLY_PLAN()
            dao.saveAllSPLY_PLAN(splyPlanList)
            logger.debug("saveAllSPLY_PLAN finish")
        }).start()
    }

    fun saveAllUSER_INFO(userinfoList: ArrayList<USER_INFO>) {
        Thread(Runnable {
            logger.debug("saveAllUSER_INFO start")
            dao.deleteAllUSER_INFO()
            dao.saveAllUSER_INFO(userinfoList)
            logger.debug("saveAllUSER_INFO finish")
        }).start()
    }

    private var findAllExportDataItem: MutableLiveData<ExportDataItem> = MutableLiveData()
    fun findAllExportData(): LiveData<ExportDataItem> {
        findAllExportDataItem = MutableLiveData()

        Thread(Runnable {
            val lastHistory = dao.findAppDataLastExportDatetime()
            val children = dao.findAllExportDataOfChild(lastHistory)
            val reports = dao.findAllExportDataOfReport(lastHistory)
            reports.forEach {report->
                report.ACL = reportDao.findAclById(report.RCP_NO)
                report.CH_BSC = reportDao.findChBscById(report.RCP_NO)
                report.DROPOUT_PLAN = reportDao.findDropoutPlanById(report.RCP_NO)
                report.EDU = reportDao.findEduById(report.RCP_NO)
                report.FMLY = reportDao.findFmlyById(report.RCP_NO)
                report.HLTH = reportDao.findHlthById(report.RCP_NO)
                report.INTV = reportDao.findIntvById(report.RCP_NO)
                report.REMRK = reportDao.findRemarkById(report.RCP_NO)
                report.RPT_DIARY = reportDao.findRptDiaryById(report.RCP_NO)
                report.SWRT = reportDao.findSwrtById(report.RCP_NO)
                report.DROPOUT = reportDao.findDropoutById(report.RCP_NO)
                report.RPLY = reportDao.findRplyById(report.RCP_NO)
                report.LIV_COND = reportDao.findLivCondById(report.RCP_NO)

                reportDao.findAllAtchFileById(report.RCP_NO)?.apply { report.ATCH_FILE = ArrayList(this) }
                reportDao.findAllSiblById(report.RCP_NO)?.apply {report.SIBL = ArrayList(this) }
                reportDao.findAllChSpslInfo(report.RCP_NO)?.apply {report.CH_SPSL_INFO = ArrayList(this) }
                reportDao.findAllPrsnAnsInfo(report.RCP_NO)?.apply {report.PRSN_ANS_INFO = ArrayList(this) }
                reportDao.findAllGiftBrkdwById(report.RCP_NO)?.apply { report.GIFT_BRKDW = ArrayList(this) }
            }

            val history = dao.findAllExportDataOfHistory(lastHistory)
            val counseling = dao.findAllExportDataOfCounseling(lastHistory)
            val service = dao.findAllExportDataOfProviedService(lastHistory)

            findAllExportDataItem.postValue(ExportDataItem(children = children, reports = reports, services = service, counseling = counseling, history = history))

        }).start()

        return findAllExportDataItem
    }


}