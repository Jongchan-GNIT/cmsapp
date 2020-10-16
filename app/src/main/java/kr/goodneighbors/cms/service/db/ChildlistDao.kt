package kr.goodneighbors.cms.service.db

import android.arch.persistence.db.SupportSQLiteQuery
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.arch.persistence.room.RawQuery
import kr.goodneighbors.cms.service.entities.APP_SEARCH_HISTORY
import kr.goodneighbors.cms.service.entities.CD
import kr.goodneighbors.cms.service.entities.CH_MST
import kr.goodneighbors.cms.service.entities.CH_SPSL_INFO
import kr.goodneighbors.cms.service.entities.RPT_BSC
import kr.goodneighbors.cms.service.model.ChildlistItem
import kr.goodneighbors.cms.service.model.SpinnerOption
import kr.goodneighbors.cms.service.model.VillageLocation

@Dao
interface ChildlistDao {
    @Query("""SELECT * FROM CH_MST WHERE CHRCP_NO = :id""")
    fun findChildById(id: String): CH_MST

    @RawQuery(observedEntities = [CH_MST::class, RPT_BSC::class])
    fun findAllChildList(query: SupportSQLiteQuery) : List<ChildlistItem>

    @RawQuery(observedEntities = [RPT_BSC::class])
    fun findAllReport(query: SupportSQLiteQuery): List<RPT_BSC>

    @Query("""
        SELECT *
        FROM RPT_BSC
        WHERE RPT_DVCD = :rpt_dvcd
        AND YEAR = :year
        AND FIDG_YN = 'Y'
        AND DEL_YN != 'Y'
        ORDER BY REG_DT DESC
    """)
    fun findAllReport(rpt_dvcd: String, year: String): List<RPT_BSC>

    @Query("SELECT * FROM CD WHERE GRP_CD = :group AND CD = :code")
    fun findCode(group: String, code: String): CD

    @Query("""
        SELECT IFNULL(A.SPSL_CD, '') AS "key", IFNULL(B.CD_ENM, 'Empty Value') AS "value"
        FROM (
           SELECT SPSL_CD
           FROM CH_SPSL_INFO
           WHERE RCP_NO = :rcp_no
        ) A
        LEFT JOIN CD B
        ON A.SPSL_CD = B.CD
        AND B.GRP_CD = '115'
        AND B.USE_YN = 'Y'
        AND B.CD != '0'
        GROUP BY B.CD, B.CD_ENM
        ORDER BY B.SORT_ORDR
    """)
    fun findCaseByReport(rcp_no: String): List<SpinnerOption>

    @RawQuery(observedEntities = [RPT_BSC::class, CH_SPSL_INFO::class])
    fun findCase(query: SupportSQLiteQuery): List<SpinnerOption>?


    @Query("""
        SELECT *
        FROM APP_SEARCH_HISTORY
        GROUP BY WORD
        ORDER BY REGIST_DATE DESC
        LIMIT 10
    """)
    fun findAllSuggestions(): List<APP_SEARCH_HISTORY>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveSuggestion(history: APP_SEARCH_HISTORY)

    @Query("""
        DELETE FROM APP_SEARCH_HISTORY
        WHERE WORD = :word
    """)
    fun deleteSuggestion(word: String)

    @Query("""
        SELECT VLG_CD, VLG_NM, CDNT_LAT AS LAT, CDNT_LONG AS LNG
        FROM VLG
        WHERE CTR_CD = :ctr_cd AND BRC_CD = :brc_cd AND PRJ_CD = :prj_cd
        AND CDNT_LAT IS NOT NULL AND CDNT_LAT != ''
        AND CDNT_LONG IS NOT NULL AND CDNT_LONG != ''
    """)
    fun findAllVillageLocation(ctr_cd: String, brc_cd: String, prj_cd: String): List<VillageLocation>
}