@file:Suppress("FunctionName")

package kr.goodneighbors.cms.service.db

import android.arch.persistence.db.SupportSQLiteQuery
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.arch.persistence.room.RawQuery
import kr.goodneighbors.cms.service.entities.ACL
import kr.goodneighbors.cms.service.entities.APP_DATA_HISTORY
import kr.goodneighbors.cms.service.entities.ATCH_FILE
import kr.goodneighbors.cms.service.entities.CD
import kr.goodneighbors.cms.service.entities.CH_BSC
import kr.goodneighbors.cms.service.entities.CH_CUSL_INFO
import kr.goodneighbors.cms.service.entities.CH_MST
import kr.goodneighbors.cms.service.entities.CH_SPSL_INFO
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
import kr.goodneighbors.cms.service.entities.PRSN_ANS_INFO
import kr.goodneighbors.cms.service.entities.REMRK
import kr.goodneighbors.cms.service.entities.RETN
import kr.goodneighbors.cms.service.entities.RPLY
import kr.goodneighbors.cms.service.entities.RPT_BSC
import kr.goodneighbors.cms.service.entities.RPT_DIARY
import kr.goodneighbors.cms.service.entities.SIBL
import kr.goodneighbors.cms.service.entities.SRVC
import kr.goodneighbors.cms.service.entities.SWRT
import kr.goodneighbors.cms.service.model.AclListItem
import kr.goodneighbors.cms.service.model.CounselingListItem
import kr.goodneighbors.cms.service.model.DuplicateChildItem
import kr.goodneighbors.cms.service.model.GiftItemDetail
import kr.goodneighbors.cms.service.model.GmEditItem
import kr.goodneighbors.cms.service.model.GmLetterEditItem
import kr.goodneighbors.cms.service.model.GmlListItem
import kr.goodneighbors.cms.service.model.NextCounselingIndex
import kr.goodneighbors.cms.service.model.ProfileHeaderItem
import kr.goodneighbors.cms.service.model.ProfileViewItem
import kr.goodneighbors.cms.service.model.ProvidedServiceListItem
import kr.goodneighbors.cms.service.model.ReportListItem
import kr.goodneighbors.cms.service.model.ReturnItem
import kr.goodneighbors.cms.service.model.StringResult
import kr.goodneighbors.cms.service.model.VillageLocation

@Dao
interface ReportDao {

    //    @Query("Select * from " + CityEntity.TABLE_NAME + " LIMIT :limit OFFSET :offset")
//    fun loadLocalCities(offset: Int, limit: Int): LiveData<List<CityEntity>>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insertAllCities(cities: List<CityEntity>?)
//
//    @Query("Select count(*) from " + CityEntity.TABLE_NAME)
//    fun loadCitiesCount(): Long
//
//    @Query("DELETE FROM " + CityEntity.TABLE_NAME)
//    fun deleteAll()
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAppDataHistory(history: APP_DATA_HISTORY)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllCH_MST(chmstList: List<CH_MST>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveCH_MST(entity: CH_MST)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRPT_BSC(entity: RPT_BSC)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun initAll(entity: List<RPT_BSC>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveCH_BSC(entity: CH_BSC)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun initAllCH_BSC(entity: ArrayList<CH_BSC>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveFMLY(entity: FMLY)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveINTV(intv: INTV)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLIV_COND(liV_COND: LIV_COND)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSWRT(swrt: SWRT)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveHLTH(hlth: HLTH)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveREMRK(remrk: REMRK)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRPT_DIARY(rpt_diary: RPT_DIARY)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveEDU(edu: EDU)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveDROPOUT_PLAN(entity: DROPOUT_PLAN)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRPT_DIARY(rpT_DIARY: RPT_DIARY)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertACL(acl: ACL)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDROPOUT(dropout: DROPOUT)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRPLY(rply: RPLY)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDROPOUT_PLAN(dropoutPlan: DROPOUT_PLAN)

    @Query("""DELETE FROM DROPOUT_PLAN WHERE RCP_NO = :rcp_no""")
    fun removeAllDropoutPlan(rcp_no: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllGIFT_BRKDW(brkdw: List<GIFT_BRKDW>)

    @Query("""DELETE FROM GIFT_BRKDW WHERE RCP_NO = :rcp_no""")
    fun removeAllGIFT_BRKDWByReport(rcp_no: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllSIBL(sibl: List<SIBL>)

    @Query("""DELETE FROM SIBL WHERE RCP_NO = :rcp_no""")
    fun removeAllSIBLByReport(rcp_no: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllATCH_FILE(atcH_FILE: List<ATCH_FILE>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertATCH_FILE(atch_file: ATCH_FILE)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllRETN(retn: List<RETN>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllCH_SPSL_INFO(cH_SPSL_INFO: List<CH_SPSL_INFO>)

    @Query("""DELETE FROM CH_SPSL_INFO WHERE RCP_NO = :rcp_no""")
    fun removeAllCH_SPSL_INFOByReport(rcp_no: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllPRSN_ANS_INFO(prsn_ans_info: List<PRSN_ANS_INFO>)

    @Query("""DELETE FROM PRSN_ANS_INFO WHERE RCP_NO = :rcp_no""")
    fun removeAllPRSN_ANS_INFOByReport(rcp_no: String)

    @Query("SELECT * FROM CH_MST WHERE CHRCP_NO = :chrcp_no LIMIT 1")
    fun findChMstById(chrcp_no: String): CH_MST

    @Query("""
        SELECT *
        FROM CH_MST
        WHERE IFNULL(CTR_CD, '') || '-' || IFNULL(BRC_CD, '') || IFNULL(PRJ_CD, '') || '-' || IFNULL(CH_CD, '') = :cdp_cd
    """)
    fun findChMstByCdpCode(cdp_cd: String): CH_MST?

    @Query("SELECT * FROM RPT_BSC WHERE CHRCP_NO = :chrcp_no AND RPT_DVCD = :rpt_dvcd AND DEL_YN != 'Y' LIMIT 1")
    fun findRptBscByChildAndType(chrcp_no: String, rpt_dvcd: Int): RPT_BSC?

    @Query("""
        SELECT *
        FROM RPT_BSC
        WHERE RPT_DVCD IN (:rpt_dvcd)
        AND RPT_STCD = '1'
        AND CHRCP_NO = :chrcp_no
        AND YEAR < :year
        AND DEL_YN != 'Y'
        ORDER BY YEAR DESC
        LIMIT 1
    """)
    fun findPrevRptBscByChildAndType(chrcp_no: String, rpt_dvcd: Array<String>, year: String): RPT_BSC?

    @Query("""
        SELECT *
        FROM RPT_BSC
        WHERE RPT_DVCD IN (:rpt_dvcd)
        AND RPT_STCD = '1'
        AND CHRCP_NO = :chrcp_no
        AND YEAR < :year
        AND RCP_NO != :rcp_no
        AND DEL_YN != 'Y'
        ORDER BY YEAR DESC
        LIMIT 1
    """)
    fun findPrevRptBscByChildAndTypeNotId(chrcp_no: String, rpt_dvcd: Array<String>, year: String, rcp_no: String): RPT_BSC?

    @Query("""
        SELECT *
        FROM RPT_BSC
        WHERE RPT_DVCD IN ('1', '2')
        AND RPT_STCD = '1'
        AND CHRCP_NO = :chrcp_no
        AND YEAR < :year
        AND DEL_YN != 'Y'
        ORDER BY YEAR DESC
        LIMIT 2
    """)
    fun findPrevTwoYearsOfRptBscByChildAndType(chrcp_no: String, year: String): List<RPT_BSC>

    @Query("SELECT * FROM RPT_BSC WHERE CHRCP_NO = :chrcp_no AND RPT_DVCD = '1' AND RPT_STCD = '1' AND DEL_YN != 'Y' LIMIT 1")
    fun findCompleteCifByChild(chrcp_no: String): RPT_BSC?

    @Query("SELECT * FROM RPT_BSC WHERE CHRCP_NO = :chrcp_no AND YEAR = :year AND RPT_DVCD = '2' AND DEL_YN != 'Y' LIMIT 1")
    fun findAprByChildAndYear(chrcp_no: String, year: String): RPT_BSC?

    @Query("SELECT * FROM RPT_BSC WHERE RCP_NO = :rcp_no")
    fun getRptBscById(rcp_no: String): RPT_BSC?

    @Query("UPDATE RPT_BSC SET DEL_YN = 'Y' WHERE RCP_NO = :rcp_no")
    fun deleteReportById(rcp_no: String)

    @Query("""
        SELECT *
        FROM RPT_BSC
        WHERE CHRCP_NO = :id
            AND RPT_DVCD IN (:services)
            AND FIDG_YN = 'Y'
            AND DEL_YN != 'Y'
        ORDER BY RPT_DVCD DESC, YEAR DESC, APRV_DT DESC
    """)
    fun findAllReportList(id: String, services: Array<String> = arrayOf("1", "2")): List<RPT_BSC>

    @Query("""
        SELECT *
        FROM (
            SELECT *
            FROM RPT_BSC
            WHERE RPT_DVCD IN (:services)
            AND RPT_STCD = '1' AND FIDG_YN = 'Y' AND DEL_YN != 'Y'
            AND CHRCP_NO = :chrcp_no
            ORDER BY YEAR DESC
            LIMIT 1
        ) A
    """)
    fun findLastReportByChild(chrcp_no: String, services: Array<String> = arrayOf("1", "2")): RPT_BSC?

    @Query("""
        SELECT *
        FROM (
            SELECT *
            FROM RPT_BSC
            WHERE RPT_DVCD IN (:services)
            AND FIDG_YN = 'Y' AND DEL_YN != 'Y'
            AND CHRCP_NO = :chrcp_no
            ORDER BY YEAR DESC
            LIMIT 1
        ) A
    """)
    fun findLastReportOfAllStateByChild(chrcp_no: String, services: Array<String> = arrayOf("1", "2")): RPT_BSC?

    @Query("SELECT * FROM RPT_BSC WHERE CHRCP_NO = :chrcp_no AND YEAR = :year AND RPT_DVCD = '4' LIMIT 1")
    fun findAclByChildAndYear(chrcp_no: String, year: String): RPT_BSC?

    // ATCH_FILE
    @Query("SELECT IFNULL(MAX(SEQ_NO), 0) FROM ATCH_FILE WHERE RCP_NO = :rcp_no")
    fun findLastSeqATCH_FILE(rcp_no: String): Int

    @Query("SELECT * FROM ATCH_FILE WHERE RCP_NO = :rcp_no AND IMG_DVCD = :img_dvcd")
    fun findATCH_FILE(rcp_no: String, img_dvcd: String): ATCH_FILE?

    @Query("SELECT * FROM ATCH_FILE WHERE RCP_NO = :rcp_no")
    fun findAllAtchFileById(rcp_no: String): List<ATCH_FILE>?

    @Query("""
        SELECT IFNULL(B.FILE_PATH, '') || '/' || IFNULL(B.FILE_NM, '')
        FROM (
            SELECT RCP_NO, YEAR, RPT_STCD
            FROM RPT_BSC
            WHERE CHRCP_NO = :chrcp_no
            AND FIDG_YN = 'Y' AND DEL_YN != 'Y'
            AND RPT_STCD = '1' AND RPT_DVCD IN ('1', '2')
        ) A
        INNER JOIN ATCH_FILE B
        ON A.RCP_NO = B.RCP_NO
        AND B.IMG_DVCD = '331001'
        ORDER BY RPT_STCD DESC, YEAR DESC
        LIMIT 3
    """)
    fun findProfileDataImageByChild(chrcp_no: String): List<String>

    @Query("DELETE FROM ATCH_FILE WHERE RCP_NO = :rcp_no")
    fun deleteFileByReport(rcp_no: String)

    // ACL
    @Query("SELECT * FROM ACL WHERE RCP_NO = :rcp_no")
    fun findAclById(rcp_no: String): ACL?


    // ACL
    @Query("""
        SELECT *
        FROM (
            SELECT A.*, B.SWRT_YN
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM)
                FROM CD WHERE GRP_CD = '305' AND CD = A.RPT_TYPE) AS RPT_TYPE_LABEL
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM)
                FROM CD WHERE GRP_CD = '65' AND CD = A.RPT_STCD) AS RPT_STCD_LABEL
            FROM (
                SELECT A.*, B.RPT_TYPE
                FROM (
                    SELECT A.*, B.FILE_PATH, B.FILE_NM
                    FROM (
                        SELECT CHRCP_NO, RCP_NO, YEAR, RPT_STCD, APRV_DT
                        FROM RPT_BSC
                        WHERE CHRCP_NO = :chrcp_no
                        AND YEAR = :year
                        AND RPT_DVCD = '4'
                        AND DEL_YN = 'N'
                    ) A
                    LEFT OUTER JOIN ATCH_FILE B
                    ON A.RCP_NO = B.RCP_NO
                ) A
                LEFT JOIN ACL B
                ON A.RCP_NO = B.RCP_NO
            ) A
            LEFT OUTER JOIN SWRT B
            ON A.RCP_NO = B.RCP_NO
            LIMIT 1
        ) A
        """)
    fun findAclListItemByChildAndYear(chrcp_no: String, year: String, locale: String): AclListItem?

    @Query("""
        WITH TARGET_YEARS AS (
            SELECT CASE WHEN CAST(strftime('%m','now','localtime') AS INTEGER) >= 4 THEN strftime('%Y','now','localtime') ELSE strftime('%Y','now','localtime') - 1 END AS YEAR
            UNION
            SELECT YEAR - 1 FROM TARGET_YEARS
            WHERE YEAR > CASE WHEN CAST(strftime('%m','now','localtime') AS INTEGER) >= 4 THEN strftime('%Y','now','localtime') ELSE strftime('%Y','now','localtime') - 1 END - 1
        )
        SELECT A.*
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM)
                FROM CD WHERE GRP_CD = '305' AND CD = A.RPT_TYPE) AS RPT_TYPE_NM
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM)
                FROM CD WHERE GRP_CD = '65' AND CD = A.RPT_STCD) AS RPT_STNM
            , (SELECT IFNULL(FILE_PATH, '') || '/' || IFNULL(FILE_NM, '') FROM ATCH_FILE WHERE RCP_NO = A.RCP_NO AND IMG_DVCD = '331001' LIMIT 1) AS GENERAL_FILE_PATH
        FROM (
            SELECT A.CHRCP_NO, A.YEAR, B.RCP_NO, IFNULL(B.RPT_STCD, '16') AS RPT_STCD, B.APRV_DT
                , C.RPT_TYPE, D.SWRT_YN
            FROM (
                SELECT A.YEAR || '' AS YEAR, CHRCP_NO
                FROM TARGET_YEARS A
                LEFT OUTER JOIN (
                    SELECT *
                    FROM CH_MST
                    WHERE CHRCP_NO = :chrcp_no
                ) B
                ON B.CH_STCD = '1' 
            ) A
            LEFT OUTER JOIN (
                SELECT * FROM RPT_BSC
                WHERE CHRCP_NO = :chrcp_no AND RPT_DVCD = '4' AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
            ) B
                ON A.CHRCP_NO = B.CHRCP_NO
                AND A.YEAR = B.YEAR
            LEFT OUTER JOIN ACL C
                ON B.RCP_NO = C.RCP_NO
            LEFT OUTER JOIN SWRT D
                ON B.RCP_NO = D.RCP_NO
        ) A
    """)
    fun findAllAclListItemByChild(chrcp_no: String, locale: String): List<AclListItem>

    // CH_BSC
    @Query("SELECT * FROM CH_BSC WHERE RCP_NO = :rcp_no")
    fun findChBscById(rcp_no: String): CH_BSC?

    // EDU
    @Query("SELECT * FROM EDU WHERE RCP_NO = :rcp_no")
    fun findEduById(rcp_no: String): EDU?

    // FMLY
    @Query("SELECT * FROM FMLY WHERE RCP_NO = :rcp_no")
    fun findFmlyById(rcp_no: String): FMLY?

    // HLTH
    @Query("SELECT * FROM HLTH WHERE RCP_NO = :rcp_no")
    fun findHlthById(rcp_no: String): HLTH?

    // INTV
    @Query("SELECT * FROM INTV WHERE RCP_NO = :rcp_no")
    fun findIntvById(rcp_no: String): INTV?

    // REMRK
    @Query("SELECT * FROM REMRK WHERE RCP_NO = :rcp_no")
    fun findRemarkById(rcp_no: String): REMRK?

    // DROPOUT_PLAN
    @Query("SELECT * FROM DROPOUT_PLAN WHERE RCP_NO = :rcp_no")
    fun findDropoutPlanById(rcp_no: String): DROPOUT_PLAN?

    // DROPOUT
    @Query("SELECT * FROM DROPOUT WHERE RCP_NO = :rcp_no")
    fun findDropoutById(rcp_no: String): DROPOUT?

    // RPLY
    @Query("SELECT * FROM RPLY WHERE RCP_NO = :rcp_no")
    fun findRplyById(rcp_no: String): RPLY?

    // SIBL
    @Query("SELECT * FROM SIBL WHERE RCP_NO = :rcp_no")
    fun findAllSiblById(rcp_no: String): List<SIBL>?

    // CH_SPSL_INFO
    @Query("SELECT * FROM CH_SPSL_INFO WHERE RCP_NO = :rcp_no")
    fun findAllChSpslInfo(rcp_no: String): List<CH_SPSL_INFO>?

    @Query("""
        SELECT B.*
        FROM (
            SELECT SPSL_CD
            FROM CH_SPSL_INFO
            WHERE RCP_NO = :rcp_no
        ) A
        INNER JOIN (
            SELECT GRP_CD
                , CD
                , GRPCD_KNM
                , GRPCD_ENM
                , CD_KNM
                , IFNULL(CASE UPPER(:locale)
                                WHEN 'FR' THEN CD_FRNM
                                WHEN 'ES' THEN CD_ESNM
                                ELSE CD_ENM
                              END, CD_ENM) AS CD_ENM
                , EXPL
                , USE_YN
                , SORT_ORDR
                , CHGP_CD
                , REGR_ID, REG_DT
                , UPDR_ID, UPD_DT
                , EXPL_ENG
                , REF_1, REF_2
                , CD_FRNM, CD_ESNM
            FROM CD WHERE GRP_CD = '115' AND USE_YN = 'Y' AND CD != 0
        ) B
        ON A.SPSL_CD = B.CD
        ORDER BY SORT_ORDR
    """)
    fun findAllChSpslInfoCode(rcp_no: String, locale: String): List<CD>?

    // PRSN_ANS_INFO
    @Query("SELECT * FROM PRSN_ANS_INFO WHERE RCP_NO = :rcp_no")
    fun findAllPrsnAnsInfo(rcp_no: String): List<PRSN_ANS_INFO>?

    // SWRT
    @Query("SELECT * FROM SWRT WHERE RCP_NO = :rcp_no")
    fun findSwrtById(rcp_no: String): SWRT?

    // RPT_DIARY
    @Query("SELECT * FROM RPT_DIARY WHERE RCP_NO = :rcp_no")
    fun findRptDiaryById(rcp_no: String): RPT_DIARY?

    // LIV_COND
    @Query("SELECT * FROM LIV_COND WHERE RCP_NO = :rcp_no")
    fun findLivCondById(rcp_no: String): LIV_COND?

    // GIFT_BRKDW
    @Query("SELECT * FROM GIFT_BRKDW WHERE RCP_NO = :rcp_no")
    fun findAllGiftBrkdwById(rcp_no: String): List<GIFT_BRKDW>?


    // CH_CUSL_INFO
    @Query("""
        SELECT *
        FROM CH_CUSL_INFO
        WHERE CHRCP_NO = :chrcp_no
        AND CUSL_STCD = '1'
        ORDER BY REG_DT DESC
        LIMIT 1
    """)
    fun findLastChCuslInfo(chrcp_no: String): CH_CUSL_INFO?


    @Query("""
            SELECT MASTER_CD, CHGP_CD, CD, TITLE, MASTER_ORDER, SORT_ORDR, CHRCP_NO, SVOBJ_DVCD, BSST_CD, SPBD_CD, MAX(PRVD_DT) AS PRVD_DT, COUNT(*) AS COUNT
            FROM (
                SELECT A.*, B.CD, IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) AS TITLE, SORT_ORDR
                FROM (
                    SELECT CD AS MASTER_CD
                        , IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) AS MASTER_CD_ENM
                        , CHGP_CD, SORT_ORDR AS MASTER_ORDER, CASE WHEN GRP_CD = '240' THEN '1' ELSE '2' END DVCD
                    FROM CD
                    WHERE GRP_CD IN ('240', '241')
                    AND USE_YN = 'Y'
                    AND CD != '0'
                ) A
                INNER JOIN CD B
                ON A.CHGP_CD = B.GRP_CD
                AND B.CD != 0
                AND B.USE_YN = 'Y'
            ) A
            INNER JOIN (
                SELECT *
                FROM SRVC
                WHERE CHRCP_NO = :chrcp_no
                AND PRVD_DT >= :min_date
                AND DEL_YN != 'Y'
            ) B
            ON A.DVCD = B.SVOBJ_DVCD
            AND A.MASTER_CD = B.BSST_CD
            AND A.CD = B.SPBD_CD
            GROUP BY MASTER_CD, CHGP_CD, CD, TITLE, MASTER_ORDER, SORT_ORDR, CHRCP_NO, SVOBJ_DVCD, BSST_CD, SPBD_CD
            ORDER BY MASTER_ORDER, SORT_ORDR
    """)
    fun findAllProvidedServiceByChild(chrcp_no: String, min_date: String, locale: String): List<ProvidedServiceListItem>

    @Query("""
        SELECT *
        FROM SRVC
        WHERE CHRCP_NO = :chrcp_no
        AND SVOBJ_DVCD = :svobj_dvcd
        AND BSST_CD = :bsst_cd
        AND SPBD_CD = :spbd_cd
        AND PRVD_DT >= :year
        AND DEL_YN != 'Y'
    """)
    fun findAllProvidedServiceByChildAndService(chrcp_no: String, svobj_dvcd: String, bsst_cd: String, spbd_cd: String, year: String): List<SRVC>

    @Query("""
        SELECT IFNULL(MAX(SEQ_NO), 0)
        FROM SRVC
        WHERE CHRCP_NO = :chrcp_no
        AND CRT_TP = :crt_tp
    """)
    fun findlastIndexOfService(chrcp_no: String, crt_tp: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllSRVC(srvcList: List<SRVC>)

    @Query("""
        SELECT *
        FROM CH_MST
        WHERE CH_STCD = '1'
        AND DEL_YN != 'Y'
        AND (CH_CD LIKE :s OR CH_EFNM LIKE :s OR CH_EMNM LIKE :s OR CH_ELNM LIKE :s)
    """)
    fun findAllSiblingSponsorshipByWord(s: String): List<CH_MST>

    @Query("""
        SELECT *
        FROM (
            SELECT A.CHRCP_NO, A.CTR_CD, A.BRC_CD, A.PRJ_CD, A.CH_CD, A.CH_EFNM, A.CH_EMNM, A.CH_ELNM, B.BDAY, B.GNDR
            FROM (
                SELECT *
                FROM CH_MST
                WHERE TRIM(UPPER(CH_EFNM)) = :firstName
                AND TRIM(UPPER(IFNULL(CH_EMNM, ''))) = :middleName
                AND TRIM(UPPER(CH_ELNM)) = :lastName
                AND DEL_YN != 'Y'
            ) A
            INNER JOIN CH_BSC B
            ON A.CHRCP_NO = B.CHRCP_NO
            AND B.BDAY = :birth
            AND B.GNDR = :gender
            GROUP BY A.CHRCP_NO, A.CTR_CD, A.BRC_CD, A.PRJ_CD, A.CH_CD, A.CH_EFNM, A.CH_EMNM, A.CH_ELNM, B.BDAY, B.GNDR
        ) A
    """)
    fun findAllDuplicateChildren(firstName: String, middleName: String, lastName: String, gender: String, birth: String): List<DuplicateChildItem>

    @Query("""
        SELECT *
        FROM CH_MST
        WHERE CHRCP_NO = :chrcp_no
        AND DEL_YN != 'Y'

    """)
    fun findAllSiblingSponsorshipByChild(chrcp_no: String): List<DuplicateChildItem>

    @RawQuery(observedEntities = [GMNY::class, LETR::class, RPLY::class, RPT_BSC::class])
    fun findAllGmlByChild(query: SupportSQLiteQuery): List<GmlListItem>

    @Query("""
        SELECT A.*
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = CASE WHEN A.PROF_RPT_DVCD = '1' THEN '70' ELSE '60' END AND CD = A.SCTP_CD) AS SCTP_NM
            , (SELECT SCHL_NM FROM SCHL WHERE SCHL_CD = A.SCHL_CD LIMIT 1) AS SCHL_NM
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '66' AND CD = A.MGDN_CD) AS MGDN_CD_NM
            , (SELECT IFNULL(FILE_PATH, '') || '/' || IFNULL(FILE_NM, '') FROM ATCH_FILE WHERE RCP_NO = A.PROF_RCP_NO AND IMG_DVCD = '331005' LIMIT 1) AS THUMB_FILE_PATH
            , (SELECT IFNULL(FILE_PATH, '') || '/' || IFNULL(FILE_NM, '') FROM ATCH_FILE WHERE RCP_NO = A.PROF_RCP_NO AND IMG_DVCD = '331001' LIMIT 1) AS FILE_PATH
        FROM (
            SELECT A.*
                , REPLACE(IFNULL(B.CH_EFNM, '') || ' ' || IFNULL(B.CH_EMNM, '') || ' ' || IFNULL(B.CH_ELNM, ''), '  ', ' ') AS CHILD_NAME
                , IFNULL(B.CTR_CD, '') || '-' || IFNULL(B.BRC_CD, '') || IFNULL(B.PRJ_CD, '') || '-' || IFNULL(B.CH_CD, '') AS CHILD_CODE
                , B.CH_STCD
                , C.GNDR, C.BDAY, C.VLG_CD, C.TEL_NO, IFNULL(C.AGE, A.PROF_YEAR - SUBSTR(C.BDAY, 1, 4)) AS AGE
                , D.SCTP_CD, D.SCHL_CD, D.GRAD
                , E.FA_LTYN, E.MO_LTYN, E.EBRO_LTNUM, E.ESIS_LTNUM, E.YBRO_LTNUM, E.YSIS_LTNUM, E.MGDN_CD, E.MGDN_NM
                , F.VLG_NM, F.CDNT_LAT AS VLG_LAT, F.CDNT_LONG AS VLG_LONG
                , (SELECT IFNULL(CTR_CD, '') || '-' || IFNULL(BRC_CD, '') || IFNULL(PRJ_CD, '') || '-' || IFNULL(CH_CD, '') FROM CH_MST
                   WHERE CHRCP_NO IN (SELECT CHRCP_NO FROM SIBL WHERE RCP_NO = A.PROF_RCP_NO LIMIT 0, 1)) AS SIBLING1
                , (SELECT IFNULL(CTR_CD, '') || '-' || IFNULL(BRC_CD, '') || IFNULL(PRJ_CD, '') || '-' || IFNULL(CH_CD, '') FROM CH_MST
                   WHERE CHRCP_NO IN (SELECT CHRCP_NO FROM SIBL WHERE RCP_NO = A.PROF_RCP_NO LIMIT 1, 1)) AS SIBLING2
            FROM (
                SELECT A.*
                , PROF_RCP_NO, PROF_RPT_DVCD, PROF_YEAR, PROF_APRV_DT
                , C.RELMEM_NM
                FROM (
                    SELECT A.CHRCP_NO, A.MNG_NO, A.RELSH_CD, A.GIFT_DAMT, A.MBSH_REQ, SUBSTR(A.RCP_DT, 1, 4) AS YEAR
                    , B.RCP_NO, IFNULL(B.RPT_STCD, '16') RPT_STCD
                    FROM (
                        SELECT *
                        FROM GMNY
                        WHERE CHRCP_NO = :chrcp_no
                        AND MNG_NO = :mng_no
                    ) A
                    LEFT OUTER JOIN (
                        SELECT A.*, B.GMNY_MNGNO
                        FROM (
                            SELECT RCP_NO, CHRCP_NO, RPT_STCD
                            FROM RPT_BSC
                            WHERE CHRCP_NO = :chrcp_no
                            AND RPT_DVCD = '5'
                            AND RPT_STCD NOT IN ('98', '99')
                            AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                        ) A
                        INNER JOIN RPLY B
                        ON A.RCP_NO = B.RCP_NO
                        AND B.GMNY_MNGNO IS NOT NULL
                    ) B
                    ON A.CHRCP_NO = B.CHRCP_NO
                    AND A.MNG_NO = B.GMNY_MNGNO
                    AND A.MV_DT IS NULL
                    AND A.RFND_DT IS NULL
                ) A
                INNER JOIN (
                    SELECT A.CHRCP_NO, A.RCP_NO AS PROF_RCP_NO, A.RPT_DVCD AS PROF_RPT_DVCD, A.YEAR AS PROF_YEAR
                        , A.APRV_DT AS PROF_APRV_DT
                    FROM RPT_BSC A
                    WHERE CHRCP_NO = :chrcp_no
                    AND RPT_DVCD IN ('1', '2') AND DEL_YN != 'Y' AND FIDG_YN = 'Y' AND RPT_STCD = '1'
                    ORDER BY RPT_DVCD DESC, YEAR DESC
                    LIMIT 1
                ) B
                ON A.CHRCP_NO = B.CHRCP_NO
                INNER JOIN RELSH C
                ON A.CHRCP_NO = C.CHRCP_NO
                AND A.RELSH_CD = C.RELSH_CD
                AND (C.RELCNL_DT IS NULL OR C.RELCNL_DT >= A.YEAR || '0401')
                LIMIT 1
            ) A
            LEFT OUTER JOIN CH_MST B
            ON A.CHRCP_NO = B.CHRCP_NO
            LEFT OUTER JOIN CH_BSC C
            ON A.PROF_RCP_NO = C.RCP_NO
            LEFT OUTER JOIN EDU D
            ON A.PROF_RCP_NO = D.RCP_NO
            LEFT OUTER JOIN FMLY E
            ON A.PROF_RCP_NO = E.RCP_NO
            LEFT OUTER JOIN VLG F
            ON C.VLG_CD = F.VLG_CD
        ) A
    """)
    fun findGmProfile(chrcp_no: String, mng_no: String, locale: String): GmEditItem

    @Query("""
        SELECT *
        FROM (
            SELECT A.GRP_CD, A.MASTER_CD AS GIFT_BCD, A.CD AS GIFT_SCD, A.CD_ENM AS GIFT_DTBD
                 , B.RCP_NO, B.SEQ_NO, B.GIFT_DAMT, B.GIFT_NUM
            FROM (
                SELECT A.CD AS MASTER_CD, A.SORT_ORDR AS MASTER_SORT_ORDR, B.GRP_CD, B.CD, B.CD_KNM, B.CD_ENM, B.SORT_ORDR
                FROM (
                    SELECT GRP_CD, CD, IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) AS CD_ENM, CHGP_CD, SORT_ORDR
                    FROM CD WHERE GRP_CD = '246' AND CD != 0 AND USE_YN = 'Y'
                ) A
                INNER JOIN (
                    SELECT GRP_CD, CD, CD_KNM, IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) AS CD_ENM, SORT_ORDR FROM CD WHERE GRP_CD = :grp_cd AND CD != 0 AND USE_YN = 'Y'
                ) B
                ON A.CHGP_CD = B.GRP_CD
            ) A
            LEFT OUTER JOIN (
               SELECT * FROM GIFT_BRKDW WHERE RCP_NO = :rcp_no
            ) B
            ON A.MASTER_CD = B.GIFT_BCD
            AND A.CD = B.GIFT_SCD
            ORDER BY MASTER_SORT_ORDR, SORT_ORDR
        ) A
    """)
    fun findAllGiftItems(grp_cd: String, rcp_no: String, locale: String): List<GiftItemDetail>?

    @Query("""
        SELECT *
        FROM (
            SELECT A.RCP_NO, A.RETN_ITCD, A.RTRN_BCD, A.RTRN_SCD, A.RTRN_DETL, A.CD_ENM AS RTRN_BCD_LABEL
            , B.CD_ENM AS RTRN_SCD_LABEL
            FROM (
                SELECT A.RCP_NO, A.RETN_ITCD, A.RTRN_BCD, A.RTRN_SCD, A.RTRN_DETL
                    , B.CD_ENM, B.CHGP_CD
                FROM (
                    SELECT *
                    FROM RETN A
                    WHERE RCP_NO = :rcp_no
                    AND RETN_CNT = (SELECT MAX(RETN_CNT) FROM RETN WHERE RCP_NO = :rcp_no)
                ) A
                INNER JOIN (
                    SELECT GRP_CD
                        , CD
                        , GRPCD_KNM
                        , GRPCD_ENM
                        , CD_KNM
                        , IFNULL(CASE UPPER(:locale)
                                        WHEN 'FR' THEN CD_FRNM
                                        WHEN 'ES' THEN CD_ESNM
                                        ELSE CD_ENM
                                      END, CD_ENM) AS CD_ENM
                        , EXPL
                        , USE_YN
                        , SORT_ORDR
                        , CHGP_CD
                        , REGR_ID, REG_DT
                        , UPDR_ID, UPD_DT
                        , EXPL_ENG
                        , REF_1, REF_2
                        , CD_FRNM, CD_ESNM
                    FROM CD
                    WHERE GRP_CD = :grp_cd
                    AND USE_YN = 'Y' and CD != '0'
                ) B
                ON A.RTRN_BCD = B.CD
            ) A
            LEFT OUTER JOIN (
                SELECT GRP_CD
                    , CD
                    , GRPCD_KNM
                    , GRPCD_ENM
                    , CD_KNM
                    , IFNULL(CASE UPPER(:locale)
                                    WHEN 'FR' THEN CD_FRNM
                                    WHEN 'ES' THEN CD_ESNM
                                    ELSE CD_ENM
                                  END, CD_ENM) AS CD_ENM
                    , EXPL
                    , USE_YN
                    , SORT_ORDR
                    , CHGP_CD
                    , REGR_ID, REG_DT
                    , UPDR_ID, UPD_DT
                    , EXPL_ENG
                    , REF_1, REF_2
                    , CD_FRNM, CD_ESNM
                FROM CD
            ) B
            ON A.CHGP_CD = B.GRP_CD
            AND B.CD = A.RTRN_SCD
        ) A
    """)
    fun findAllReturns(rcp_no: String, grp_cd: String = "111", locale: String): List<ReturnItem>

    @Query("""
        WITH TARGET_YEARS AS (
            SELECT (SELECT MIN(YEAR) FROM RPT_BSC WHERE CHRCP_NO = :chrcp_no AND RPT_DVCD = '1' AND DEL_YN != 'Y' AND FIDG_YN = 'Y') AS TYEAR
                , (SELECT MAX(YEAR) FROM RPT_BSC WHERE CHRCP_NO = :chrcp_no AND RPT_DVCD = '3' AND DEL_YN != 'Y' AND FIDG_YN = 'Y') AS DROP_YEAR
            UNION
            SELECT TYEAR + 1, DROP_YEAR FROM TARGET_YEARS
            WHERE CAST(TYEAR AS INT) < CASE WHEN DROP_YEAR IS NULL THEN :lastYear ELSE DROP_YEAR END
        )
        SELECT A.*
            , A.YEAR - SUBSTR(A.BDAY, 1, 4) AS AGE
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '65' AND CD = A.RPT_STCD) AS RPT_STNM
            , (SELECT VLG_NM FROM VLG WHERE VLG_CD = A.VLG_CD LIMIT 1) AS VLG_NM
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = CASE WHEN A.RPT_DVCD = '1' THEN '70' ELSE '60' END AND CD = A.SCTP_CD) AS SCTP_NM
            , (SELECT SCHL_NM FROM SCHL WHERE SCHL_CD = A.SCHL_CD LIMIT 1) AS SCHL_NM
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '337' AND CD = A.BMI_CD) AS BMI_NM
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '78' AND CD = A.DISB_CD) AS DISB_NM
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '99' AND CD = A.ILNS_CD) AS ILNS_NM
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '340' AND CD = A.FTPLN_CD) AS FTPLN_NM
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '341' AND CD = A.CTNSPN_RNCD) AS CTNSPN_RNNM
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '115' AND CD = A.CASE1) AS CASE1_NM
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '115' AND CD = A.CASE2) AS CASE2_NM
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '115' AND CD = A.CASE3) AS CASE3_NM
            , (SELECT IFNULL(FILE_PATH, '') || '/' || IFNULL(FILE_NM, '') FROM ATCH_FILE WHERE RCP_NO = A.LAST_RCP_NO AND IMG_DVCD = '331001' LIMIT 1) AS GENERAL_FILE_PATH
            , (SELECT IFNULL(FILE_PATH, '') || '/' || IFNULL(FILE_NM, '') FROM ATCH_FILE WHERE RCP_NO = A.LAST_RCP_NO AND IMG_DVCD = '331005' LIMIT 1) AS THUMB_FILE_PATH
        FROM (
            SELECT A.*
                , IFNULL(A.TMP_RPT_STCD, CASE WHEN B.CH_STCD = '1' THEN  '16' ELSE '-1' END) AS RPT_STCD
                , REPLACE(IFNULL(B.CH_EFNM, '') || ' ' || IFNULL(B.CH_EMNM, '') || ' ' || IFNULL(B.CH_ELNM, ''), '  ', ' ') AS CHILD_NAME
                , C.GNDR, C.BDAY, C.VLG_CD, C.HS_ADDR, C.HS_ADDR_DTL
                , D.SCTP_CD, D.SCHL_CD, D.GRAD
                , E.BMI_CD, E.DISB_CD, E.ILNS_CD
                , F.FA_LTYN, F.MO_LTYN, F.EBRO_LTNUM, F.ESIS_LTNUM, F.YBRO_LTNUM, F.YSIS_LTNUM
                , G.PLAN_YN, G.FTPLN_CD, G.FTPLN_DTL, G.CTNSPN_RNCD, G.CTNSPN_DTL
                , H.REMRK_ENG
                , (SELECT SPSL_CD FROM CH_SPSL_INFO WHERE RCP_NO = A.LAST_RCP_NO LIMIT 0, 1) AS CASE1
                , (SELECT SPSL_CD FROM CH_SPSL_INFO WHERE RCP_NO = A.LAST_RCP_NO LIMIT 1, 1) AS CASE2
                , (SELECT SPSL_CD FROM CH_SPSL_INFO WHERE RCP_NO = A.LAST_RCP_NO LIMIT 2, 1) AS CASE3
                , (SELECT IFNULL(CTR_CD, '') || '-' || IFNULL(BRC_CD, '') || IFNULL(PRJ_CD, '') || '-' || IFNULL(CH_CD, '') FROM CH_MST
                   WHERE CHRCP_NO IN (SELECT CHRCP_NO FROM SIBL WHERE RCP_NO = A.LAST_RCP_NO LIMIT 0, 1)) AS SIBLING1
                , (SELECT IFNULL(CTR_CD, '') || '-' || IFNULL(BRC_CD, '') || IFNULL(PRJ_CD, '') || '-' || IFNULL(CH_CD, '') FROM CH_MST
                   WHERE CHRCP_NO IN (SELECT CHRCP_NO FROM SIBL WHERE RCP_NO = A.LAST_RCP_NO LIMIT 1, 1)) AS SIBLING2
            FROM (
                SELECT *
                FROM (
                    SELECT :chrcp_no AS CHRCP_NO, B.RCP_NO, IFNULL(B.RPT_DVCD, '2') AS RPT_DVCD, A.TYEAR AS YEAR
                         , B.RPT_STCD AS TMP_RPT_STCD, B.APRV_DT, B.LAST_RCP_NO, CIF_APR_DT
                    FROM (
                        SELECT TYEAR
                            , (SELECT APRV_DT FROM RPT_BSC
                              WHERE CHRCP_NO = :chrcp_no AND RPT_DVCD IN ('1') AND RPT_STCD = '1'
                              AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                              ORDER BY RPT_DVCD DESC, YEAR DESC LIMIT 1) AS CIF_APR_DT
                        FROM TARGET_YEARS
                        ORDER BY CAST(TYEAR AS INT) DESC
                    ) A
                    LEFT OUTER JOIN (
                        SELECT A.*, CASE WHEN RPT_DVCD = '3'
                            THEN (SELECT RCP_NO FROM RPT_BSC
                                WHERE CHRCP_NO = A.CHRCP_NO AND RPT_DVCD IN ('1', '2') AND RPT_STCD = '1'
                                AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                                ORDER BY RPT_DVCD DESC, YEAR DESC LIMIT 1)
                            ELSE A.RCP_NO END AS LAST_RCP_NO
                        FROM RPT_BSC A
                        WHERE CHRCP_NO = :chrcp_no
                        AND RPT_DVCD IN ('1', '2', '3') AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                    ) B
                    ON A.TYEAR = CAST(B.YEAR AS INT)
                ) A
                WHERE RPT_DVCD != '2' OR (RPT_DVCD = '2' AND (YEAR - 1) || '1001' > CIF_APR_DT)
            ) A
            LEFT OUTER JOIN CH_MST B
            ON A.CHRCP_NO = B.CHRCP_NO
            LEFT OUTER JOIN CH_BSC C
            ON A.LAST_RCP_NO = C.RCP_NO
            LEFT OUTER JOIN EDU D
            ON A.LAST_RCP_NO = D.RCP_NO
            LEFT OUTER JOIN HLTH E
            ON A.LAST_RCP_NO = E.RCP_NO
            LEFT OUTER JOIN FMLY F
            ON A.LAST_RCP_NO = F.RCP_NO
            LEFT OUTER JOIN DROPOUT_PLAN G
            ON A.LAST_RCP_NO = G.RCP_NO
            LEFT OUTER JOIN REMRK H
            ON A.LAST_RCP_NO = H.RCP_NO
        ) A
        WHERE RPT_STCD != '-1'
        ORDER BY CAST(RPT_DVCD AS INT) DESC, CAST(YEAR AS INT) DESC
    """)
    fun findAllReportListByChild(chrcp_no: String, lastYear: Int, locale: String): List<ReportListItem>

    @Query(""" 
        SELECT A.*
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = CASE WHEN A.RPT_DVCD = '1' THEN '70' ELSE '60' END AND CD = A.SCTP_CD) AS SCTP_NM
            , (SELECT SCHL_NM FROM SCHL WHERE SCHL_CD = A.SCHL_CD LIMIT 1) AS SCHL_NM
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '66' AND CD = A.MGDN_CD) AS MGDN_CD_NM
            , (SELECT IFNULL(FILE_PATH, '') || '/' || IFNULL(FILE_NM, '') FROM ATCH_FILE WHERE RCP_NO = A.RCP_NO AND IMG_DVCD = '331005' LIMIT 1) AS THUMB_FILE_PATH
        FROM (
            SELECT A.*
                , REPLACE(IFNULL(B.CH_EFNM, '') || ' ' || IFNULL(B.CH_EMNM, '') || ' ' || IFNULL(B.CH_ELNM, ''), '  ', ' ') AS CHILD_NAME
                , IFNULL(B.CTR_CD, '') || '-' || IFNULL(B.BRC_CD, '') || IFNULL(B.PRJ_CD, '') || '-' || IFNULL(B.CH_CD, '') AS CHILD_CODE
                , B.CH_STCD
                , C.GNDR, C.BDAY, C.VLG_CD, C.TEL_NO, (A.NOW_YEAR - SUBSTR(C.BDAY, 1, 4)) AS AGE
                , D.SCTP_CD, D.SCHL_CD, D.GRAD
                , E.FA_LTYN, E.MO_LTYN, E.EBRO_LTNUM, E.ESIS_LTNUM, E.YBRO_LTNUM, E.YSIS_LTNUM, E.MGDN_CD, E.MGDN_NM
                , F.VLG_NM, F.CDNT_LAT AS VLG_LAT, F.CDNT_LONG AS VLG_LONG
                , (SELECT IFNULL(CTR_CD, '') || '-' || IFNULL(BRC_CD, '') || IFNULL(PRJ_CD, '') || '-' || IFNULL(CH_CD, '') FROM CH_MST
                   WHERE CHRCP_NO IN (SELECT CHRCP_NO FROM SIBL WHERE RCP_NO = A.RCP_NO LIMIT 0, 1)) AS SIBLING1
                , (SELECT IFNULL(CTR_CD, '') || '-' || IFNULL(BRC_CD, '') || IFNULL(PRJ_CD, '') || '-' || IFNULL(CH_CD, '') FROM CH_MST
                   WHERE CHRCP_NO IN (SELECT CHRCP_NO FROM SIBL WHERE RCP_NO = A.RCP_NO LIMIT 1, 1)) AS SIBLING2
            FROM (
                SELECT A.CHRCP_NO, A.RCP_NO , A.RPT_DVCD , A.YEAR, A.APRV_DT
                       , (SELECT (CASE WHEN CAST(strftime('%m','now','localtime') AS INTEGER)  >= 4 THEN strftime('%Y','now','localtime') ELSE strftime('%Y','now','localtime') - 1 END)) AS NOW_YEAR
                FROM RPT_BSC A
                WHERE CHRCP_NO = :chrcp_no
                AND RPT_DVCD IN ('1', '2') AND DEL_YN != 'Y' AND FIDG_YN = 'Y' AND RPT_STCD = '1'
                ORDER BY RPT_DVCD DESC, YEAR DESC
                LIMIT 1
            ) A
            LEFT OUTER JOIN CH_MST B
            ON A.CHRCP_NO = B.CHRCP_NO
            LEFT OUTER JOIN CH_BSC C
            ON A.RCP_NO = C.RCP_NO
            LEFT OUTER JOIN EDU D
            ON A.RCP_NO = D.RCP_NO
            LEFT OUTER JOIN FMLY E
            ON A.RCP_NO = E.RCP_NO
            LEFT OUTER JOIN VLG F
            ON C.VLG_CD = F.VLG_CD
        ) A
    """)
    fun findProfileHeaderItemByChild(chrcp_no: String, locale: String): ProfileHeaderItem

    @Query("""
        SELECT A.*
            , (SELECT MAX(APRV_DT) FROM RPT_BSC WHERE RPT_STCD = '1' AND RPT_DVCD = '1' AND DEL_YN = 'N' AND FIDG_YN = 'Y' AND CHRCP_NO = A.CHRCP_NO) AS CIF_APRV_DT
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '65' AND CD = A.RPT_STCD) AS RPT_STNM
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = CASE WHEN A.RPT_DVCD = '1' THEN '70' ELSE '60' END AND CD = A.SCTP_CD) AS SCTP_NM
            , (SELECT SCHL_NM FROM SCHL WHERE SCHL_CD = A.SCHL_CD LIMIT 1) AS SCHL_NM
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '337' AND CD = A.BMI_CD) AS BMI_NM
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '78' AND CD = A.DISB_CD) AS DISB_NM
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '66' AND CD = A.MGDN_CD) AS MGDN_CD_NM
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '99' AND CD = A.ILNS_CD) AS ILNS_NM
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '340' AND CD = A.FTPLN_CD) AS FTPLN_NM
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '341' AND CD = A.CTNSPN_RNCD) AS CTNSPN_RNNM
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '80' AND CD = A.DROP_RNCD) AS DROP_RNNM
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '115' AND CD = A.CASE1) AS CASE1_NM
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '115' AND CD = A.CASE2) AS CASE2_NM
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '115' AND CD = A.CASE3) AS CASE3_NM
            , (SELECT IFNULL(FILE_PATH, '') || '/' || IFNULL(FILE_NM, '') FROM ATCH_FILE WHERE RCP_NO = A.RCP_NO AND IMG_DVCD = '331005' LIMIT 1) AS THUMB_FILE_PATH
            , (SELECT REG_DT FROM CH_CUSL_INFO WHERE CHRCP_NO = A.CHRCP_NO AND DEL_YN = 'N' ORDER BY SEQ_NO LIMIT 1) AS CH_CUSL_INFO_REG_DT
        FROM (
            SELECT A.*
                , REPLACE(IFNULL(B.CH_EFNM, '') || ' ' || IFNULL(B.CH_EMNM, '') || ' ' || IFNULL(B.CH_ELNM, ''), '  ', ' ') AS CHILD_NAME
                , IFNULL(B.CTR_CD, '') || '-' || IFNULL(B.BRC_CD, '') || IFNULL(B.PRJ_CD, '') || '-' || IFNULL(B.CH_CD, '') AS CHILD_CODE
                , B.CH_STCD, B.ORG_CHRCP_NO, B.BF_CHRCP_NO
                , (SELECT IFNULL(CTR_CD, '') || '-' || IFNULL(BRC_CD, '') || IFNULL(PRJ_CD, '') || '-' || IFNULL(CH_CD, '') FROM CH_MST WHERE CHRCP_NO = B.BF_CHRCP_NO) AS BF_CHILD_CODE
                , C.GNDR, C.BDAY, C.VLG_CD, C.HS_ADDR, C.HS_ADDR_DTL, C.TEL_NO, IFNULL(C.AGE, A.YEAR - SUBSTR(C.BDAY, 1, 4)) AS AGE
                , D.SCTP_CD, D.SCHL_CD, D.GRAD
                , E.BMI_CD, E.DISB_CD, E.ILNS_CD
                , F.FA_LTYN, F.MO_LTYN, F.EBRO_LTNUM, F.ESIS_LTNUM, F.YBRO_LTNUM, F.YSIS_LTNUM, F.MGDN_CD, F.MGDN_NM
                , G.PLAN_YN, G.FTPLN_CD, G.FTPLN_DTL, G.CTNSPN_RNCD, G.CTNSPN_DTL
                , H.REMRK_ENG
                , I.DROP_RNCD
                , J.VLG_NM, J.CDNT_LAT AS VLG_LAT, J.CDNT_LONG AS VLG_LONG
                , K.REMRK_ENG AS DROPOUT_REMRK_ENG
                , (SELECT SPSL_CD FROM CH_SPSL_INFO WHERE RCP_NO = A.RCP_NO LIMIT 0, 1) AS CASE1
                , (SELECT SPSL_CD FROM CH_SPSL_INFO WHERE RCP_NO = A.RCP_NO LIMIT 1, 1) AS CASE2
                , (SELECT SPSL_CD FROM CH_SPSL_INFO WHERE RCP_NO = A.RCP_NO LIMIT 2, 1) AS CASE3
                , (SELECT IFNULL(CTR_CD, '') || '-' || IFNULL(BRC_CD, '') || IFNULL(PRJ_CD, '') || '-' || IFNULL(CH_CD, '') FROM CH_MST
                   WHERE CHRCP_NO IN (SELECT CHRCP_NO FROM SIBL WHERE RCP_NO = A.RCP_NO LIMIT 0, 1)) AS SIBLING1
                , (SELECT IFNULL(CTR_CD, '') || '-' || IFNULL(BRC_CD, '') || IFNULL(PRJ_CD, '') || '-' || IFNULL(CH_CD, '') FROM CH_MST
                   WHERE CHRCP_NO IN (SELECT CHRCP_NO FROM SIBL WHERE RCP_NO = A.RCP_NO LIMIT 1, 1)) AS SIBLING2
            FROM (
                SELECT A.*, B.RCP_NO AS DROP_RCP_NO, B.RPT_STCD AS DROP_RPT_STCD
                FROM (
                    SELECT A.CHRCP_NO, A.RCP_NO, A.RPT_DVCD, A.YEAR
                        , A.RPT_STCD, A.APRV_DT, A.REG_DT
                    FROM RPT_BSC A
                    WHERE CHRCP_NO = :chrcp_no
                    AND RPT_DVCD IN ('1', '2') AND DEL_YN != 'Y' AND FIDG_YN = 'Y' AND RPT_STCD = '1'
                    ORDER BY RPT_DVCD DESC, YEAR DESC
                    LIMIT 1
                ) A
                LEFT OUTER JOIN RPT_BSC B
                ON A.CHRCP_NO = B.CHRCP_NO
                AND B.RPT_DVCD = 3 AND B.FIDG_YN = 'Y' AND B.DEL_YN = 'N' AND B.RPT_STCD NOT IN ('98', '99')
                ORDER BY B.REG_DT DESC
                LIMIT 1
            ) A
            LEFT OUTER JOIN CH_MST B
            ON A.CHRCP_NO = B.CHRCP_NO
            LEFT OUTER JOIN CH_BSC C
            ON A.RCP_NO = C.RCP_NO
            LEFT OUTER JOIN EDU D
            ON A.RCP_NO = D.RCP_NO
            LEFT OUTER JOIN HLTH E
            ON A.RCP_NO = E.RCP_NO
            LEFT OUTER JOIN FMLY F
            ON A.RCP_NO = F.RCP_NO
            LEFT OUTER JOIN DROPOUT_PLAN G
            ON A.RCP_NO = G.RCP_NO
            LEFT OUTER JOIN REMRK H
            ON A.RCP_NO = H.RCP_NO
            LEFT OUTER JOIN DROPOUT I
            ON A.DROP_RCP_NO = I.RCP_NO
            LEFT OUTER JOIN VLG J
            ON C.VLG_CD = J.VLG_CD
            LEFT OUTER JOIN REMRK K
            ON A.DROP_RCP_NO = K.RCP_NO
        ) A
    """)
    fun findProfileByChild(chrcp_no: String, locale: String): ProfileViewItem

    @Query("""
        SELECT A.*
        , SUBSTR(CUSL_DT, 1, 6) AS YYYYMM
        , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '90' AND CD = A.INTPLC_CD) AS INTPLC_NM
        , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '112' AND CD = A.RSPN_RLCD) AS RSPN_RLNM
        , (SELECT COUNT(*) + 1 FROM CH_CUSL_INFO C WHERE C.CHRCP_NO = A.CHRCP_NO AND SUBSTR(C.CUSL_DT, 1, 4) = SUBSTR(A.CUSL_DT, 1, 4) AND C.DEL_YN = 'N' AND C.REG_DT < A.REG_DT) AS RN
        FROM CH_CUSL_INFO A
        WHERE A.CHRCP_NO = :chrcp_no
        AND A.DEL_YN = 'N'
        ORDER BY CUSL_DT DESC, REG_DT DESC, CUSL_STCD DESC
    """)
    fun findAllCounselingByChild(chrcp_no: String, locale: String): List<CounselingListItem>

    @Query("""
        SELECT
          (SELECT COUNT(*) + 1
            FROM CH_CUSL_INFO A
            WHERE A.CHRCP_NO = :chrcp_no
            AND A.DEL_YN = 'N'
            AND SUBSTR(A.CUSL_DT, 1, 4) = strftime('%Y', 'now', 'localtime')) AS VISIT
        , (SELECT IFNULL(MAX(SEQ_NO), 0) + 1
            FROM CH_CUSL_INFO A
            WHERE A.CHRCP_NO = :chrcp_no
            AND A.CRT_TP = :crt_tp) AS NEXT_SEQ
    """)
    fun getNextCounselingIndex(chrcp_no: String, crt_tp: String): NextCounselingIndex

//    @Query("""
//        SELECT IFNULL(MAX(SEQ_NO), 0) + 1 AS "index"
//        FROM CH_CUSL_INFO A
//        WHERE A.CHRCP_NO = :chrcp_no
//        AND A.CRT_TP = :crt_tp
//    """)
//    fun getNextCounselingSeq(chrcp_no: String, crt_tp: String): NextCounselingIndex

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveCH_CUSL_INFO(ch_cusl_info: CH_CUSL_INFO)

    @Query("""
        SELECT VLG_CD, VLG_NM, CDNT_LAT AS LAT, CDNT_LONG AS LNG
        FROM VLG WHERE VLG_CD = :code
    """)
    fun getLocationOfVillage(code: String): VillageLocation

    @Query("""
        SELECT A.*
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = CASE WHEN A.PROF_RPT_DVCD = '1' THEN '70' ELSE '60' END AND CD = A.SCTP_CD) AS SCTP_NM
            , (SELECT SCHL_NM FROM SCHL WHERE SCHL_CD = A.SCHL_CD LIMIT 1) AS SCHL_NM
            , (SELECT IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) FROM CD WHERE GRP_CD = '66' AND CD = A.MGDN_CD) AS MGDN_CD_NM
            , (SELECT IFNULL(FILE_PATH, '') || '/' || IFNULL(FILE_NM, '') FROM ATCH_FILE WHERE RCP_NO = A.PROF_RCP_NO AND IMG_DVCD = '331005' LIMIT 1) AS THUMB_FILE_PATH
            , (SELECT IFNULL(FILE_PATH, '') || '/' || IFNULL(FILE_NM, '') FROM ATCH_FILE WHERE RCP_NO = A.PROF_RCP_NO AND IMG_DVCD = '331001' LIMIT 1) AS FILE_PATH
        FROM (
            SELECT A.*
                , REPLACE(IFNULL(B.CH_EFNM, '') || ' ' || IFNULL(B.CH_EMNM, '') || ' ' || IFNULL(B.CH_ELNM, ''), '  ', ' ') AS CHILD_NAME
                , IFNULL(B.CTR_CD, '') || '-' || IFNULL(B.BRC_CD, '') || IFNULL(B.PRJ_CD, '') || '-' || IFNULL(B.CH_CD, '') AS CHILD_CODE
                , B.CH_STCD
                , C.GNDR, C.BDAY, C.VLG_CD, C.TEL_NO, IFNULL(C.AGE, A.PROF_YEAR - SUBSTR(C.BDAY, 1, 4)) AS AGE
                , D.SCTP_CD, D.SCHL_CD, D.GRAD
                , E.FA_LTYN, E.MO_LTYN, E.EBRO_LTNUM, E.ESIS_LTNUM, E.YBRO_LTNUM, E.YSIS_LTNUM, E.MGDN_CD, E.MGDN_NM
                , F.VLG_NM, F.CDNT_LAT AS VLG_LAT, F.CDNT_LONG AS VLG_LONG
                , (SELECT IFNULL(CTR_CD, '') || '-' || IFNULL(BRC_CD, '') || IFNULL(PRJ_CD, '') || '-' || IFNULL(CH_CD, '') FROM CH_MST
                   WHERE CHRCP_NO IN (SELECT CHRCP_NO FROM SIBL WHERE RCP_NO = A.PROF_RCP_NO LIMIT 0, 1)) AS SIBLING1
                , (SELECT IFNULL(CTR_CD, '') || '-' || IFNULL(BRC_CD, '') || IFNULL(PRJ_CD, '') || '-' || IFNULL(CH_CD, '') FROM CH_MST
                   WHERE CHRCP_NO IN (SELECT CHRCP_NO FROM SIBL WHERE RCP_NO = A.PROF_RCP_NO LIMIT 1, 1)) AS SIBLING2
            FROM (
                SELECT A.*
                    , PROF_RCP_NO, PROF_RPT_DVCD, PROF_YEAR, PROF_APRV_DT
                    , C.RELMEM_NM
                FROM (
                    SELECT A.*, SUBSTR(A.RCP_DT, 1, 4) AS YEAR
                    , B.RCP_NO, IFNULL(B.RPT_STCD, '16') RPT_STCD
                    FROM (
                        SELECT *
                        FROM LETR
                        WHERE CHRCP_NO = :chrcp_no
                        AND MNG_NO = :mng_no
                    ) A
                    LEFT OUTER JOIN (
                        SELECT A.*, B.LETR_MNGNO
                        FROM (
                            SELECT RCP_NO, CHRCP_NO, RPT_STCD
                            FROM RPT_BSC
                            WHERE CHRCP_NO = :chrcp_no
                            AND RPT_DVCD = '5'
                            AND RPT_STCD NOT IN ('98', '99')
                            AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                        ) A
                        INNER JOIN RPLY B
                        ON A.RCP_NO = B.RCP_NO
                        AND B.LETR_MNGNO IS NOT NULL
                    ) B
                    ON A.CHRCP_NO = B.CHRCP_NO
                    AND A.MNG_NO = B.LETR_MNGNO
                ) A
                INNER JOIN (
                    SELECT A.CHRCP_NO, A.RCP_NO AS PROF_RCP_NO, A.RPT_DVCD AS PROF_RPT_DVCD, A.YEAR AS PROF_YEAR
                        , A.APRV_DT AS PROF_APRV_DT
                    FROM RPT_BSC A
                    WHERE CHRCP_NO = :chrcp_no
                    AND RPT_DVCD IN ('1', '2') AND DEL_YN != 'Y' AND FIDG_YN = 'Y' AND RPT_STCD = '1'
                    ORDER BY RPT_DVCD DESC, YEAR DESC
                    LIMIT 1
                ) B
                ON A.CHRCP_NO = B.CHRCP_NO
                INNER JOIN RELSH C
                ON A.CHRCP_NO = C.CHRCP_NO
                AND A.RELSH_CD = C.RELSH_CD
                AND (C.RELCNL_DT IS NULL OR C.RELCNL_DT >= A.YEAR || '0401')
                LIMIT 1
            ) A
            LEFT OUTER JOIN CH_MST B
            ON A.CHRCP_NO = B.CHRCP_NO
            LEFT OUTER JOIN CH_BSC C
            ON A.PROF_RCP_NO = C.RCP_NO
            LEFT OUTER JOIN EDU D
            ON A.PROF_RCP_NO = D.RCP_NO
            LEFT OUTER JOIN FMLY E
            ON A.PROF_RCP_NO = E.RCP_NO
            LEFT OUTER JOIN VLG F
            ON C.VLG_CD = F.VLG_CD
        ) A
    """)
    fun findGmLetterProfile(chrcp_no: String, mng_no: String, locale: String): GmLetterEditItem


    @Query("""
        SELECT B.*
        FROM (
            SELECT *
            FROM LETR
            WHERE CHRCP_NO = :chrcp_no
            AND MNG_NO < :mng_no
        ) A
        INNER JOIN (
            SELECT A.*, B.LETR_MNGNO
            FROM (
                SELECT *
                FROM RPT_BSC
                WHERE CHRCP_NO = :chrcp_no
                AND RPT_DVCD = '5'
                AND RPT_STCD NOT IN ('98', '99')
                AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
            ) A
            INNER JOIN RPLY B
            ON A.RCP_NO = B.RCP_NO
            AND B.LETR_MNGNO IS NOT NULL
        ) B
        ON A.CHRCP_NO = B.CHRCP_NO
        AND A.MNG_NO = B.LETR_MNGNO
        ORDER BY RCP_DT DESC
        LIMIT 1
    """)
    fun findPrevGmLetterReport(chrcp_no: String, mng_no: String): RPT_BSC?

    @Query("""
        SELECT *
        FROM MOD_HIS_INFO
        WHERE RCP_NO = :rcp_no
        LIMIT 1
    """)
    fun findModHisInfo(rcp_no: String): MOD_HIS_INFO?

    @Query("""
        SELECT IFNULL(MAX(SEQ_NO), 0) + 1 AS value
        FROM MOD_HIS_INFO
        WHERE INIT_TYPE = :imei
    """)
    fun getNextModHisInfoSeq(imei: String): StringResult?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveMOD_HIS_INFO(mod_his_info: MOD_HIS_INFO)

}