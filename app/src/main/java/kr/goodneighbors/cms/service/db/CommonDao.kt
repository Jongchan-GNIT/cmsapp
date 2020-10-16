package kr.goodneighbors.cms.service.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.db.SupportSQLiteQuery
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.arch.persistence.room.RawQuery
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
import kr.goodneighbors.cms.service.model.SpinnerOption

@Dao
interface CommonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun initNotiInfo(notiInfoList: List<NOTI_INFO>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun initCd(cdList: List<CD>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun initCtr(ctrList: List<CTR>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun initBrc(brcList: List<BRC>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun initPrj(prjList: List<PRJ>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun initVlg(vlgList: List<VLG>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun initSchl(schlList: List<SCHL>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun initPrsnInfo(prsnInfoList: List<PRSN_INFO>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun initSplyPlan(splyPlanList: List<SPLY_PLAN>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun initSrvc(srvcList: List<SRVC>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun initChCuslInfo(chCuslInfoList: List<CH_CUSL_INFO>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun initRelsh(relshList: List<RELSH>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun initLetr(letrList: List<LETR>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun initGmny(gmnyList: ArrayList<GMNY>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun initBmi(bmiList: ArrayList<BMI>)


    @Query("""
        SELECT
            IFNULL(CASE
                WHEN :bmiIndex < MIN_NUM THEN '337002'
                WHEN :bmiIndex BETWEEN MIN_NUM AND MAX_NUM THEN '337003'
                WHEN :bmiIndex > MAX_NUM THEN '337004'
                ELSE '337001'
            END, '337001') as bmi
        FROM BMI
        WHERE MONTHS = :months
        AND GNDR = :gender
    """)
    fun findBMI(months: Long, gender: String, bmiIndex: String): String?

    @Query("""
        SELECT CD AS "key"
            , IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) AS "value"
        FROM CD
        WHERE GRP_CD = :grp_cd AND CD != 0 AND USE_YN = 'Y'
        ORDER BY SORT_ORDR
        """)
    fun findAllCodeLiveData(grp_cd: String, locale: String): LiveData<List<SpinnerOption>>

    @Query("""
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
        WHERE GRP_CD = :groupCode AND CD = :code AND USE_YN = 'Y'
        ORDER BY SORT_ORDR
        """)
    fun findCode(groupCode: String, code: String, locale: String): CD?

    @Query("""
        SELECT CD AS "key"
            , IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) AS "value"
        FROM CD
        WHERE GRP_CD = :grp_cd AND CD != 0 AND USE_YN = 'Y'
        ORDER BY SORT_ORDR
        """)
    fun findAllCode(grp_cd: String, locale: String): List<SpinnerOption>

    @Query("""
        SELECT CD AS "key"
            , IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) AS "value"
        FROM CD
        WHERE GRP_CD = '65'
        AND CD NOT IN ('0', '5', '6', '7', '8', '9', '10', '11', '98', '99')
        AND USE_YN = 'Y'
        ORDER BY SORT_ORDR
        """)
    fun findAllStatusCode(locale: String): List<SpinnerOption>

    @Query("""
        SELECT
              GRP_CD
            , CD
            , GRPCD_KNM
            , GRPCD_ENM
            , CD_KNM
            , IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) AS CD_ENM
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
        WHERE GRP_CD = :grp_cd AND CD != 0 AND USE_YN = 'Y'
        ORDER BY SORT_ORDR
        """)
    fun findAllCodeToEntity(grp_cd: String, locale: String): List<CD>

    @Query("""
        SELECT VLG_CD AS "key", VLG_NM AS "value"
        FROM VLG
        """)
    fun findAllVillageLiveData(): LiveData<List<SpinnerOption>>

    @Query("""
        SELECT VLG_CD AS "key", VLG_NM AS "value"
        FROM VLG
        """)
    fun findAllVillage(): List<SpinnerOption>

    @Query("""
        SELECT *
        FROM VLG
        WHERE VLG_CD = :vlg_cd
        """)
    fun findVillageByCd(vlg_cd: String): VLG?

    @Query("""
        SELECT SCHL_CD AS "key", SCHL_NM AS "value"
        FROM SCHL
        """)
    fun findAllSchoolLiveData(): LiveData<List<SpinnerOption>>

    @Query("""
        SELECT SCHL_CD AS "key", SCHL_NM AS "value"
        FROM SCHL
        """)
    fun findAllSchool(): List<SpinnerOption>

    @Query("SELECT * FROM SCHL WHERE SCHL_CD = :schl_cd")
    fun findSchoolById(schl_cd: String): SCHL?


    @Query("""
        SELECT YEAR || '-00' AS "key", YEAR || '00' AS "value"
        FROM SPLY_PLAN
    """)
    fun findAllSupplyPlanLiveData(): LiveData<List<SpinnerOption>>

    @Query("""
        SELECT *
        FROM SPLY_PLAN
        WHERE DNCTR_CD = :dnctr_cd
            AND CTR_CD = :ctr_cd
            AND PRJ_CD = :prj_cd
            AND YEAR = :year
    """)
    fun findAllSupplyPlan(dnctr_cd: String, ctr_cd: String, prj_cd: String, year: Int): List<SPLY_PLAN>

    @Query("SELECT DNCTR_CD FROM SPLY_PLAN GROUP BY DNCTR_CD")
    fun findAllSupplyPlanDnctr(): List<String>


    @Query("""
        SELECT *
        FROM (
            SELECT A.*
            FROM (
                SELECT A.GRP_CD, CD
                    , IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) AS CD_ENM, CHGP_CD, SORT_ORDR
                    , CASE WHEN GRP_CD = '240' THEN '1' ELSE '2' END AS REF_1
                FROM CD A
                WHERE GRP_CD IN ('240', '241')
                AND USE_YN = 'Y'
                AND CD != '0'
            ) A
            INNER JOIN CD B
            ON A.CHGP_CD = B.GRP_CD
            AND B.USE_YN = 'Y'
            AND B.CD != '0'
            AND B.CD_ENM LIKE :search
        ) A
        GROUP BY GRP_CD, CD, CD_ENM, CHGP_CD, SORT_ORDR, REF_1
        ORDER BY A.REF_1, A.SORT_ORDR
    """)
    fun findAllProvidedServiceMaster(search: String, locale: String): List<CD>

    @Query("""
        SELECT GRP_CD
            , CD
            , GRPCD_KNM
            , GRPCD_ENM
            , CD_KNM
            , IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) AS CD_ENM
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
        WHERE USE_YN = 'Y'
        AND CD != '0'
        AND GRP_CD = :groupCode
        AND CD_ENM LIKE :search
        ORDER BY SORT_ORDR
    """)
    fun findAllProvidedServiceDetail(groupCode: String, search: String, locale: String): List<CD>


    @RawQuery(observedEntities = [CD::class])
    fun findAllProvidedServiceItem(query: SupportSQLiteQuery): List<CD>

    @Query("""
        SELECT *
        FROM (
            SELECT B.GRP_CD
                , B.CD
                , B.GRPCD_KNM
                , B.GRPCD_ENM
                , B.CD_KNM
                , IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN B.CD_FRNM WHEN 'ES' THEN B.CD_ESNM ELSE B.CD_ENM END, B.CD_ENM) AS CD_ENM
                , B.EXPL
                , B.USE_YN
                , B.SORT_ORDR
                , B.CHGP_CD
                , B.REGR_ID, B.REG_DT
                , B.UPDR_ID, B.UPD_DT
                , B.EXPL_ENG
                , B.REF_1, B.REF_2
                , B.CD_FRNM, B.CD_ESNM
            FROM (
                SELECT *
                FROM PRSN_INFO
                WHERE YEAR = :year
            ) A
            INNER JOIN CD B
            ON A.PRSN_CD = B.CD
            AND B.GRP_CD = '336'
            AND B.USE_YN = 'Y'
            ORDER BY B.SORT_ORDR
        ) A
    """)
    fun findAllPersonalInformationByYear(year: String, locale: String): List<CD>

    @Query("""
        SELECT *
        FROM (
            SELECT B.GRP_CD
                , B.CD
                , B.GRPCD_KNM
                , B.GRPCD_ENM
                , B.CD_KNM
                , IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN B.CD_FRNM WHEN 'ES' THEN B.CD_ESNM ELSE B.CD_ENM END, B.CD_ENM) AS CD_ENM
                , B.EXPL
                , B.USE_YN
                , B.SORT_ORDR
                , B.CHGP_CD
                , B.REGR_ID, B.REG_DT
                , B.UPDR_ID, B.UPD_DT
                , B.EXPL_ENG
                , B.REF_1, B.REF_2
                , B.CD_FRNM, B.CD_ESNM
            FROM (
                SELECT PRSN_CD
                FROM PRSN_ANS_INFO
                WHERE RCP_NO = :rcp_no
            ) A
            INNER JOIN CD B
            ON A.PRSN_CD = B.CD
            AND B.GRP_CD = '336'
            AND B.USE_YN = 'Y'
            ORDER BY B.SORT_ORDR
        ) A
    """)
    fun findAllPersonalInformationByReport(rcp_no: String, locale: String): List<CD>
}