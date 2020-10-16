package kr.goodneighbors.cms.service.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import kr.goodneighbors.cms.service.entities.NOTI_INFO
import kr.goodneighbors.cms.service.model.HomeItem
import kr.goodneighbors.cms.service.model.HomeState
import kr.goodneighbors.cms.service.model.NoticeItem

@Dao
interface HomeDao {
    @Query("""
        SELECT *
        FROM (
            SELECT COUNT(*) AS CHILD_COUNT
            , CASE WHEN (SELECT COUNT(*) FROM NOTI_INFO WHERE strftime('%Y-%m-%d', datetime(REG_DT / 1000, 'unixepoch')) > (SELECT DATE('NOW', '-7 DAYS'))) > 0 THEN 'Y' ELSE 'N' END AS HAS_NEW_NOTICE
            , (SELECT MAX(A.DATE) FROM APP_DATA_HISTORY A WHERE TYPE IN ('INIT', 'IMPORT')) AS LAST_UPDATE_DATE
            FROM (
                SELECT CHRCP_NO
                FROM RPT_BSC
                WHERE RPT_DVCD = '1' AND RPT_STCD = '1'
                AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
            ) A
            INNER JOIN CH_MST B
            ON A.CHRCP_NO = B.CHRCP_NO
            AND B.DEL_YN != 'Y'
            AND B.CTR_CD = :ctrCd
            AND B.BRC_CD = :brcCd
            AND B.PRJ_CD = :prjCd
        ) A
    """)
    fun getHomeItem(ctrCd: String, brcCd: String, prjCd: String): HomeItem

    @Query("""SELECT * FROM NOTI_INFO ORDER BY REG_DT DESC LIMIT 4""")
    fun findAllNotiInfo(): List<NOTI_INFO>

    @Query("""
        SELECT E_COUNT, P_COUNT, E_COUNT / (P_COUNT * 1.0) * 100 AS PC
            , '01-O1-' || STRFTIME('%Y', 'NOW') AS FROM_DATE
            , STRFTIME('%d-%m-%Y', 'NOW') AS TO_DATE
        FROM (
            SELECT
                (SELECT COUNT(*) FROM RPT_BSC WHERE RPT_DVCD = 1 AND YEAR = STRFTIME('%Y', 'NOW')
                    AND SPLY_MON <= STRFTIME('%m', 'NOW')
                    AND DEL_YN = 'N' AND FIDG_YN = 'Y'
                ) AS E_COUNT
                , IFNULL(CASE STRFTIME('%m', 'NOW')
                    WHEN '01' THEN JAN
                    WHEN '02' THEN JAN + FEB
                    WHEN '03' THEN JAN + FEB + MAR
                    WHEN '04' THEN JAN + FEB + MAR + APRL
                    WHEN '05' THEN JAN + FEB + MAR + APRL + MAY
                    WHEN '06' THEN JAN + FEB + MAR + APRL + MAY + JUNE
                    WHEN '07' THEN JAN + FEB + MAR + APRL + MAY + JUNE + JULY
                    WHEN '08' THEN JAN + FEB + MAR + APRL + MAY + JUNE + JULY + AUG
                    WHEN '09' THEN JAN + FEB + MAR + APRL + MAY + JUNE + JULY + AUG + SEPT
                    WHEN '10' THEN JAN + FEB + MAR + APRL + MAY + JUNE + JULY + AUG + SEPT + OCT
                    WHEN '11' THEN JAN + FEB + MAR + APRL + MAY + JUNE + JULY + AUG + SEPT + OCT + NOV
                    WHEN '12' THEN JAN + FEB + MAR + APRL + MAY + JUNE + JULY + AUG + SEPT + OCT + NOV + DEC
                END, 0) AS P_COUNT
            FROM (
                SELECT *
                FROM (
                    SELECT STRFTIME('%Y', 'NOW') AS YEAR
                ) A
                LEFT OUTER JOIN (
                    SELECT STRFTIME('%Y', 'NOW') AS YEAR, SUM(JAN) AS JAN, SUM(FEB) AS FEB, SUM(MAR) AS MAR, SUM(APRL) AS APRL, SUM(MAY) AS MAY, SUM(JUNE) AS JUNE
                         , SUM(JULY) AS JULY, SUM(AUG) AS AUG, SUM(SEPT) AS SEPT, SUM(OCT) AS OCT, SUM(NOV) AS NOV, SUM(DEC) AS DEC
                    FROM SPLY_PLAN
                    WHERE CTR_CD = :ctr_cd AND BRC_CD = :brc_cd AND PRJ_CD = :prj_cd AND YEAR = STRFTIME('%Y', 'NOW')
                    GROUP BY CTR_CD, BRC_CD, PRJ_CD, YEAR
                ) B
                ON A.YEAR = B.YEAR
            ) A
        ) A
    """)
    fun getCifState(ctr_cd: String, brc_cd: String, prj_cd: String): HomeState

    @Query("""
        SELECT E_COUNT, P_COUNT, ROUND((E_COUNT / (P_COUNT * 1.0)) * 100, 2) AS PC
            , '01-O1-' || STRFTIME('%Y', 'NOW') AS FROM_DATE
            , STRFTIME('%d-%m-%Y', 'NOW') AS TO_DATE
        FROM (
            SELECT
                (SELECT COUNT(*) FROM RPT_BSC WHERE RPT_DVCD = '2' AND YEAR = STRFTIME('%Y', 'NOW') AND DEL_YN = 'N') AS E_COUNT,
                (SELECT COUNT(*) FROM CH_MST WHERE CTR_CD = :ctr_cd AND BRC_CD = :brc_cd AND PRJ_CD = :prj_cd AND CH_STCD = '1' AND DEL_YN = 'N'
                    AND CHRCP_NO IN (
                        SELECT CHRCP_NO
                        FROM RPT_BSC
                        WHERE RPT_DVCD = '1' AND RPT_STCD = '1' AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                        AND APRV_DT < (STRFTIME('%Y', 'NOW') - 1) || '1001'
                    )
                ) AS P_COUNT
        ) A
    """)
    fun getAprState(ctr_cd: String, brc_cd: String, prj_cd: String): HomeState

    @Query("""
        SELECT E_COUNT, P_COUNT, ROUND((E_COUNT / (P_COUNT * 1.0)) * 100, 2) AS PC
            , '01-O1-' || STRFTIME('%Y', 'NOW') AS FROM_DATE
            , STRFTIME('%d-%m-%Y', 'NOW') AS TO_DATE
        FROM (
            SELECT
                (SELECT COUNT(*) FROM RPT_BSC WHERE RPT_DVCD = '4' AND YEAR = STRFTIME('%Y', 'NOW') AND DEL_YN = 'N') AS E_COUNT,
                (SELECT COUNT(*)
                FROM (
                    SELECT CHRCP_NO
                    FROM RPT_BSC
                    WHERE RPT_DVCD = '1' AND RPT_STCD = '1' AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                ) A
                INNER JOIN (
                    SELECT *
                    FROM CH_MST
                    WHERE CTR_CD = :ctr_cd AND BRC_CD = :brc_cd AND PRJ_CD = :prj_cd AND CH_STCD = '1' AND DEL_YN != 'Y'
                    AND CHRCP_NO IN (
                        SELECT CHRCP_NO
                        FROM RELSH
                        WHERE RELSH_DT < STRFTIME('%Y', 'NOW') || '0401'
                        AND (RELCNL_DT IS NULL OR RELCNL_DT >= STRFTIME('%Y', 'NOW') || '0401')
                        GROUP BY CHRCP_NO
                    )
                ) B
                ON A.CHRCP_NO = B.CHRCP_NO) AS P_COUNT
        ) A
    """)
    fun getAclState(ctr_cd: String, brc_cd: String, prj_cd: String): HomeState

    @Query("""
        SELECT E_COUNT, P_COUNT, ROUND((E_COUNT / (P_COUNT * 1.0)) * 100, 2) AS PC
            , '01-O1-' || STRFTIME('%Y', 'NOW') AS FROM_DATE
            , STRFTIME('%d-%m-%Y', 'NOW') AS TO_DATE
        FROM (
            SELECT
                (SELECT COUNT(*) FROM RPT_BSC WHERE RPT_DVCD = '5' AND YEAR = STRFTIME('%Y', 'NOW') AND DEL_YN = 'N') AS E_COUNT,
                (SELECT COUNT(*)
                    FROM (
                        SELECT CHRCP_NO
                        FROM GMNY
                        WHERE SUBSTR(RCP_DT, 1, 4) = STRFTIME('%Y', 'NOW')
                        AND MV_DT IS NULL
                        AND RFND_DT IS NULL
                        AND CHRCP_NO IN (
                            SELECT CHRCP_NO FROM CH_MST WHERE CTR_CD = :ctr_cd AND BRC_CD = :brc_cd AND PRJ_CD = :prj_cd AND CH_STCD = '1' AND DEL_YN = 'N'
                        )

                        UNION

                        SELECT CHRCP_NO
                        FROM LETR
                        WHERE SUBSTR(RCP_DT, 1, 4) = STRFTIME('%Y', 'NOW')
                        AND CHRCP_NO IN (
                            SELECT CHRCP_NO FROM CH_MST WHERE CTR_CD = :ctr_cd AND BRC_CD = :brc_cd AND PRJ_CD = :prj_cd AND CH_STCD = '1' AND DEL_YN = 'N'
                        )
                    ) A
                ) AS P_COUNT
        ) A
    """)
    fun getGmlState(ctr_cd: String, brc_cd: String, prj_cd: String): HomeState

    @Query("""
        SELECT *
        FROM (
            SELECT A.SEQ_NO, A.CTGY_CD, A.TTL, A.CTS, A.REG_DT, B.CD_ENM AS CTGY_NM
            FROM NOTI_INFO A
            LEFT OUTER JOIN (
                SELECT CD
                    , IFNULL(CASE UPPER(:locale) WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) AS CD_ENM
                FROM CD WHERE GRP_CD = '335'
            ) B
            ON A.CTGY_CD = B.CD
            ORDER BY A.REG_DT DESC
        ) A
    """)
    fun findAllNoticeItem(locale: String): List<NoticeItem>
}