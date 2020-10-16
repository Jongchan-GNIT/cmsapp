@file:Suppress("FunctionName")

package kr.goodneighbors.cms.service.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import kr.goodneighbors.cms.service.entities.ACL
import kr.goodneighbors.cms.service.entities.APP_DATA_HISTORY
import kr.goodneighbors.cms.service.entities.ATCH_FILE
import kr.goodneighbors.cms.service.entities.BMI
import kr.goodneighbors.cms.service.entities.BRC
import kr.goodneighbors.cms.service.entities.CD
import kr.goodneighbors.cms.service.entities.CH_BSC
import kr.goodneighbors.cms.service.entities.CH_CUSL_INFO
import kr.goodneighbors.cms.service.entities.CH_MST
import kr.goodneighbors.cms.service.entities.CH_SPSL_INFO
import kr.goodneighbors.cms.service.entities.CTR
import kr.goodneighbors.cms.service.entities.DROPOUT
import kr.goodneighbors.cms.service.entities.DROPOUT_PLAN
import kr.goodneighbors.cms.service.entities.EDU
import kr.goodneighbors.cms.service.entities.FMLY
import kr.goodneighbors.cms.service.entities.GIFT_BRKDW
import kr.goodneighbors.cms.service.entities.GMNY
import kr.goodneighbors.cms.service.entities.HLTH
import kr.goodneighbors.cms.service.entities.INTV
import kr.goodneighbors.cms.service.entities.LETR
import kr.goodneighbors.cms.service.entities.LIV_COND
import kr.goodneighbors.cms.service.entities.MOD_HIS_INFO
import kr.goodneighbors.cms.service.entities.NOTI_INFO
import kr.goodneighbors.cms.service.entities.PRJ
import kr.goodneighbors.cms.service.entities.PRSN_ANS_INFO
import kr.goodneighbors.cms.service.entities.PRSN_INFO
import kr.goodneighbors.cms.service.entities.RELSH
import kr.goodneighbors.cms.service.entities.REMRK
import kr.goodneighbors.cms.service.entities.RETN
import kr.goodneighbors.cms.service.entities.RPLY
import kr.goodneighbors.cms.service.entities.RPT_BSC
import kr.goodneighbors.cms.service.entities.RPT_DIARY
import kr.goodneighbors.cms.service.entities.SCHL
import kr.goodneighbors.cms.service.entities.SIBL
import kr.goodneighbors.cms.service.entities.SPLY_PLAN
import kr.goodneighbors.cms.service.entities.SRVC
import kr.goodneighbors.cms.service.entities.SWRT
import kr.goodneighbors.cms.service.entities.USER_INFO
import kr.goodneighbors.cms.service.entities.VLG
import kr.goodneighbors.cms.service.model.StringResult

@Dao
interface SyncDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    fun saveAppDataHistory(history: APP_DATA_HISTORY)

    @Query("SELECT FILE_ID FROM APP_DATA_HISTORY WHERE TYPE <> 'EXPORT' ORDER BY REGIST_DATE")
    fun findAllAppDataImportHistory(): List<String>

    @Query("SELECT IFNULL(MAX(DATE), 0) FROM APP_DATA_HISTORY WHERE TYPE <> 'IMPORT'")
//    @Query("SELECT IFNULL(MAX(DATE), 0) FROM APP_DATA_HISTORY WHERE TYPE = 'INIT'")
    fun findAppDataLastExportDatetime(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllCH_MST(chmstList: List<CH_MST>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveCH_MST(entity: CH_MST)

    @Query("SELECT * FROM CH_MST WHERE CHRCP_NO = :chrcp_no")
    fun findCH_MST(chrcp_no: String): CH_MST?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllRPT_BSC(entity: List<RPT_BSC>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRPT_BSC(entity: RPT_BSC)

    @Query("SELECT * FROM RPT_BSC WHERE RCP_NO = :rcp_no")
    fun findRPT_BSC(rcp_no: String): RPT_BSC?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveCH_BSC(entity: CH_BSC)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllCH_BSC(entity: ArrayList<CH_BSC>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveFMLY(entity: FMLY)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveINTV(entity: INTV)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveLIV_COND(entity: LIV_COND)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveSWRT(entity: SWRT)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveHLTH(entity: HLTH)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveREMRK(entity: REMRK)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveEDU(entity: EDU)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRPT_DIARY(entity: RPT_DIARY)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveACL(entity: ACL)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveDROPOUT(entity: DROPOUT)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRPLY(entity: RPLY)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveDROPOUT_PLAN(entity: DROPOUT_PLAN)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllGIFT_BRKDW(entity: List<GIFT_BRKDW>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllSIBL(entity: List<SIBL>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllATCH_FILE(entity: List<ATCH_FILE>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllRETN(entity: List<RETN>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllCH_SPSL_INFO(entity: List<CH_SPSL_INFO>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllPRSN_ANS_INFO(entity: List<PRSN_ANS_INFO>)

    // 공통코드
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllNOTI_INFO(notiInfoList: List<NOTI_INFO>)

    @Query("DELETE FROM " + NOTI_INFO.TABLE_NAME)
    fun deleteAllNOTI_INFO()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllCD(cdList: List<CD>)

    @Query("DELETE FROM " + CD.TABLE_NAME)
    fun deleteAllCD()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllCTR(ctrList: List<CTR>)

    @Query("DELETE FROM " + CTR.TABLE_NAME)
    fun deleteAllCTR()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllBRC(brcList: List<BRC>)

    @Query("DELETE FROM " + BRC.TABLE_NAME)
    fun deleteAllBRC()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllBMI(brcList: List<BMI>)

    @Query("DELETE FROM " + BMI.TABLE_NAME)
    fun deleteAllBMI()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllPRJ(prjList: List<PRJ>)

    @Query("DELETE FROM " + PRJ.TABLE_NAME)
    fun deleteAllPRJ()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllVLG(vlgList: List<VLG>)

    @Query("DELETE FROM " + VLG.TABLE_NAME)
    fun deleteAllVLG()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllSCHL(schlList: List<SCHL>)

    @Query("DELETE FROM " + SCHL.TABLE_NAME)
    fun deleteAllSCHL()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllPRSN_INFO(prsnInfoList: List<PRSN_INFO>)

    @Query("DELETE FROM " + PRSN_INFO.TABLE_NAME)
    fun deleteAllPRSN_INFO()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllSPLY_PLAN(splyPlanList: List<SPLY_PLAN>)

    @Query("DELETE FROM " + SPLY_PLAN.TABLE_NAME)
    fun deleteAllSPLY_PLAN()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllSRVC(srvcList: List<SRVC>)

    @Query("DELETE FROM " + SRVC.TABLE_NAME)
    fun deleteAllSRVC()

    @Query("""
        SELECT *
        FROM CH_CUSL_INFO
        WHERE CHRCP_NO = :chrcp_no
        AND CRT_TP = :crt_tp
        AND SEQ_NO = :seq_no
        """)
    fun findCH_CUSL_INFO(chrcp_no: String, crt_tp: String, seq_no: Long): CH_CUSL_INFO?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveCH_CUSL_INFO(chCuslInfo: CH_CUSL_INFO)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllCH_CUSL_INFO(chCuslInfoList: List<CH_CUSL_INFO>)

    @Query("DELETE FROM " + CH_CUSL_INFO.TABLE_NAME)
    fun deleteAllCH_CUSL_INFO()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllRELSH(relshList: List<RELSH>)

    @Query("DELETE FROM " + RELSH.TABLE_NAME)
    fun deleteAllRELSH()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllLETR(letrList: List<LETR>)

    @Query("DELETE FROM " + LETR.TABLE_NAME)
    fun deleteAllLETR()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllGMNY(gmnyList: ArrayList<GMNY>)

    @Query("DELETE FROM " + GMNY.TABLE_NAME)
    fun deleteAllGMNY()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllUSER_INFO(userinfoList: List<USER_INFO>)

    @Query("DELETE FROM " + USER_INFO.TABLE_NAME)
    fun deleteAllUSER_INFO()

    @Query("SELECT * FROM CH_MST WHERE APP_MODIFY_DATE > :lastHistory ORDER BY REG_DT DESC")
    fun findAllExportDataOfChild(lastHistory: Long): List<CH_MST>

    @Query("SELECT * FROM RPT_BSC WHERE APP_MODIFY_DATE > :lastHistory AND YEAR IS NOT NULL ORDER BY REG_DT DESC")
    fun findAllExportDataOfReport(lastHistory: Long): List<RPT_BSC>

    @Query("SELECT * FROM SRVC WHERE APP_MODIFY_DATE > :lastHistory ORDER BY REG_DT DESC")
    fun findAllExportDataOfProviedService(lastHistory: Long): List<SRVC>

    @Query("SELECT * FROM CH_CUSL_INFO WHERE APP_MODIFY_DATE > :lastHistory ORDER BY REG_DT DESC")
    fun findAllExportDataOfCounseling(lastHistory: Long): List<CH_CUSL_INFO>

    @Query("SELECT * FROM MOD_HIS_INFO WHERE REG_DT > :lastHistory ORDER BY REG_DT DESC")
    fun findAllExportDataOfHistory(lastHistory: Long): List<MOD_HIS_INFO>

//    @Query("SELECT MAX(DATE) AS value FROM APP_DATA_HISTORY WHERE TYPE IN ('INIT', 'IMPORT')")
    @Query("SELECT MAX(DATE) AS value FROM APP_DATA_HISTORY WHERE TYPE = 'INIT'")
    fun getLastUpdateDate(): StringResult?
}