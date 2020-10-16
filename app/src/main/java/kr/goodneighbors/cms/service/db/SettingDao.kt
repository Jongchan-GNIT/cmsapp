package kr.goodneighbors.cms.service.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import kr.goodneighbors.cms.service.entities.ATCH_FILE
import kr.goodneighbors.cms.service.entities.CH_CUSL_INFO

@Dao
interface SettingDao {
    @Query("""
        SELECT B.*
        FROM (
            SELECT RCP_NO
            FROM RPT_BSC
            WHERE YEAR < (STRFTIME('%Y', 'now', 'localtime') - 2)
        ) A
        INNER JOIN ATCH_FILE B
        ON A.RCP_NO = B.RCP_NO
        AND B.FILE_PATH IS NOT NULL AND B.FILE_PATH != ''
        AND B.FILE_NM IS NOT NULL AND B.FILE_NM != ''
    """)
    fun findAllPastReportContentFile(): List<ATCH_FILE>?


    @Query("""
        SELECT *
        FROM CH_CUSL_INFO
        WHERE STRFTIME('%Y', DATETIME(REG_DT / 1000, 'unixepoch')) < (STRFTIME('%Y', 'now', 'localtime') - 1)
        AND IMG_FP IS NOT NULL AND IMG_FP != ''
        AND IMG_NM IS NOT NULL AND IMG_NM != ''
    """)
    fun findAllPastCouseingContentFile(): List<CH_CUSL_INFO>?
}