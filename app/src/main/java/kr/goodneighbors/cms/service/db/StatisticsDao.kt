package kr.goodneighbors.cms.service.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import kr.goodneighbors.cms.service.entities.NOTI_INFO
import kr.goodneighbors.cms.service.model.HomeItem
import kr.goodneighbors.cms.service.model.HomeState
import kr.goodneighbors.cms.service.model.NoticeItem
import kr.goodneighbors.cms.service.model.StatisticAclItem
import kr.goodneighbors.cms.service.model.StatisticAprItem
import kr.goodneighbors.cms.service.model.StatisticCifItem
import kr.goodneighbors.cms.service.model.StatisticDropoutItem
import kr.goodneighbors.cms.service.model.StatisticGmlItem

@Dao
interface StatisticsDao {
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
                    , IFNULL(CASE UPPER(:locale)
                        WHEN 'FR' THEN CD_FRNM
                        WHEN 'ES' THEN CD_ESNM
                        ELSE CD_ENM
                      END, CD_ENM) AS CD_ENM
                FROM CD WHERE GRP_CD = '335'
            ) B
            ON A.CTGY_CD = B.CD
            ORDER BY A.REG_DT DESC
        ) A
    """)
    fun findAllNoticeItem(locale: String): List<NoticeItem>

    @Query("""
        WITH TARGET_MONTH AS (
            SELECT 1 AS MM
            UNION
            SELECT MM + 1 FROM TARGET_MONTH
            WHERE MM < 12
        )
        SELECT SPLY_MON, SUB, AP, RE, WA, NR
            , ROUND(AP / (SUB * 1.0) * 100, 2) AS AP_PCT
            , ROUND(RE / (SUB * 1.0) * 100, 2) AS RE_PCT
            , ROUND(WA / (SUB * 1.0) * 100, 2) AS WA_PCT
            , ROUND(NR / (SUB * 1.0) * 100, 2) AS NR_PCT
        FROM (
            SELECT SPLY_MON, SUB, AP, RE, WA, CASE WHEN NR < 0 THEN 0 ELSE NR END AS NR
            FROM (
                SELECT SPLY_MON, SUB, AP, RE, WA, SUB - (AP + RE + WA) AS NR
                FROM (
                    SELECT *
                    FROM (
                        SELECT MM
                             , CASE MM
                                 WHEN 1 THEN JAN
                                 WHEN 2 THEN FEB
                                 WHEN 3 THEN MAR
                                 WHEN 4 THEN APRL
                                 WHEN 5 THEN MAY
                                 WHEN 6 THEN JUNE
                                 WHEN 7 THEN JULY
                                 WHEN 8 THEN AUG
                                 WHEN 9 THEN SEPT
                                 WHEN 10 THEN OCT
                                 WHEN 11 THEN NOV
                                 WHEN 12 THEN DEC
                              END AS SUB
                        FROM TARGET_MONTH A
                        LEFT OUTER JOIN (
                            SELECT *
                            FROM SPLY_PLAN
                            WHERE DNCTR_CD = :dnctr_cd AND YEAR = :year AND CTR_CD = :ctr_cd AND BRC_CD = :brc_cd AND PRJ_CD = :prj_cd
                        ) B
                    ) A
                    WHERE CAST(SUB AS NUMERIC) != 0
                ) A
                LEFT OUTER JOIN (
                    SELECT SPLY_MON, SUM(AP) AS AP, SUM(RE) AS RE, SUM(WA) AS WA
                    FROM (
                        SELECT SPLY_MON
                            , CASE RPT_STCD WHEN 'AP' THEN 1 ELSE 0 END AS AP
                            , CASE RPT_STCD WHEN 'RE' THEN 1 ELSE 0 END AS RE
                            , CASE RPT_STCD WHEN 'WA' THEN 1 ELSE 0 END AS WA
                        FROM (
                            SELECT *
                            FROM (
                                SELECT SPLY_MON
                                    , CASE
                                        WHEN RPT_STCD = '1' THEN 'AP'
                                        WHEN RPT_STCD = '2' OR RPT_STCD = '15' THEN 'RE'
                                        WHEN RPT_STCD = '4' OR RPT_STCD = '5' OR RPT_STCD = '6'
                                                 OR RPT_STCD = '7' OR RPT_STCD = '8' OR RPT_STCD = '9'
                                                 OR RPT_STCD = '10' OR RPT_STCD = '11' THEN 'WA'
                                        ELSE '-'
                                      END AS RPT_STCD

                                FROM RPT_BSC
                                WHERE YEAR = :year AND RPT_DVCD = '1' AND DEL_YN = 'N' AND FIDG_YN = 'Y'
                                AND SPLY_MON IS NOT NULL AND SPLY_MON != '00'
                            ) A
                            WHERE RPT_STCD != '-'
                        ) A
                    ) A
                    GROUP BY SPLY_MON
                ) B
                ON A.MM = CAST(B.SPLY_MON AS NUMERIC)
            ) A
        ) A
    """)
    fun getCifData(dnctr_cd: String, ctr_cd: String, brc_cd: String, prj_cd: String, year: String): List<StatisticCifItem>

    @Query("""
        SELECT SUB, AP, RE, WA, NR
            , ROUND(AP / (SUB * 1.0) * 100, 2) AS AP_PCT
            , ROUND(RE / (SUB * 1.0) * 100, 2) AS RE_PCT
            , ROUND(WA / (SUB * 1.0) * 100, 2) AS WA_PCT
            , ROUND(NR / (SUB * 1.0) * 100, 2) AS NR_PCT
        FROM (
            SELECT COUNT(*) AS SUB
                , SUM(CASE RPT_STCD WHEN 'AP' THEN 1 ELSE 0 END )AS AP
                , SUM(CASE RPT_STCD WHEN 'RE' THEN 1 ELSE 0 END) AS RE
                , SUM(CASE RPT_STCD WHEN 'WA' THEN 1 ELSE 0 END) AS WA
                , SUM(CASE RPT_STCD WHEN 'NR' THEN 1 ELSE 0 END) AS NR
            FROM (
                SELECT CASE
                    WHEN RPT_STCD = '1' THEN 'AP'
                    WHEN RPT_STCD = '2' OR RPT_STCD = '15' THEN 'RE'
                    WHEN RPT_STCD = '4' OR RPT_STCD = '5' OR RPT_STCD = '6'
                             OR RPT_STCD = '7' OR RPT_STCD = '8' OR RPT_STCD = '9'
                             OR RPT_STCD = '10' OR RPT_STCD = '11' THEN 'WA'
                    WHEN RPT_STCD = '16' THEN 'NR'
                    ELSE 'NR'
                  END AS RPT_STCD
                FROM (
                    SELECT A.*, B.RCP_NO, IFNULL(B.RPT_STCD, '16') AS RPT_STCD
                    FROM (
                        SELECT
                              B.CHRCP_NO
                            , B.DNCTR_CD
                            , B.CH_STCD
                            , :year AS YEAR
                        FROM (
                            SELECT CHRCP_NO
                            FROM RPT_BSC
                            WHERE RPT_DVCD = '1' AND RPT_STCD = '1' AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                            AND APRV_DT < (:year - 1) || '1001'
                        ) A
                        INNER JOIN (
                            SELECT *
                            FROM CH_MST
                            WHERE DNCTR_CD = :dnctr_cd AND CTR_CD = :ctr_cd AND BRC_CD = :brc_cd AND PRJ_CD = :prj_cd AND DEL_YN != 'Y'
                            AND CHRCP_NO NOT IN (
                                SELECT CHRCP_NO
                                FROM RPT_BSC
                                WHERE RPT_DVCD = '3' AND RPT_STCD = '1' AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                                AND APRV_DT < (:year - 1) || '1001'
                            )
                        ) B
                        ON A.CHRCP_NO = B.CHRCP_NO
                    ) A
                    LEFT OUTER JOIN (
                        SELECT *
                        FROM RPT_BSC
                        WHERE RPT_DVCD = '2' AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                    ) B
                    ON A.CHRCP_NO = B.CHRCP_NO
                    AND A.YEAR = B.YEAR
                ) A
            ) A
        ) A
    """)
    fun getAprData(dnctr_cd: String, ctr_cd: String, brc_cd: String, prj_cd: String, year: String): List<StatisticAprItem>

    @Query("""
        SELECT SUB, AP, RE, WA, NR
            , ROUND(AP / (SUB * 1.0) * 100, 2) AS AP_PCT
            , ROUND(RE / (SUB * 1.0) * 100, 2) AS RE_PCT
            , ROUND(WA / (SUB * 1.0) * 100, 2) AS WA_PCT
            , ROUND(NR / (SUB * 1.0) * 100, 2) AS NR_PCT
        FROM (
            SELECT COUNT(*) AS SUB
                , SUM(CASE RPT_STCD WHEN 'AP' THEN 1 ELSE 0 END )AS AP
                , SUM(CASE RPT_STCD WHEN 'RE' THEN 1 ELSE 0 END) AS RE
                , SUM(CASE RPT_STCD WHEN 'WA' THEN 1 ELSE 0 END) AS WA
                , SUM(CASE RPT_STCD WHEN 'NR' THEN 1 ELSE 0 END) AS NR
            FROM (
                SELECT CASE
                    WHEN RPT_STCD = '1' THEN 'AP'
                    WHEN RPT_STCD = '2' OR RPT_STCD = '15' THEN 'RE'
                    WHEN RPT_STCD = '4' OR RPT_STCD = '5' OR RPT_STCD = '6'
                             OR RPT_STCD = '7' OR RPT_STCD = '8' OR RPT_STCD = '9'
                             OR RPT_STCD = '10' OR RPT_STCD = '11' THEN 'WA'
                    WHEN RPT_STCD = '16' THEN 'NR'
                    ELSE 'NR'
                  END AS RPT_STCD
                FROM (
                    SELECT A.*
                        , B.RCP_NO, IFNULL(B.RPT_STCD, '16') AS RPT_STCD
                    FROM (
                        SELECT
                              B.CHRCP_NO
                            , B.DNCTR_CD, B.CH_STCD
                            , :year AS YEAR
                        FROM (
                            SELECT CHRCP_NO
                            FROM RPT_BSC
                            WHERE RPT_DVCD = '1' AND RPT_STCD = '1' AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                        ) A
                        INNER JOIN (
                            SELECT *
                            FROM CH_MST
                            WHERE DNCTR_CD = :dnctr_cd AND CTR_CD = :ctr_cd AND BRC_CD = :brc_cd AND PRJ_CD = :prj_cd AND DEL_YN != 'Y'
                            AND CHRCP_NO IN (
                                SELECT CHRCP_NO
                                FROM RELSH
                                WHERE RELSH_DT < :year || '0401'
                                AND (RELCNL_DT IS NULL OR RELCNL_DT >= :year || '0401')
                                GROUP BY CHRCP_NO
                            )
                        ) B
                        ON A.CHRCP_NO = B.CHRCP_NO
                    ) A
                    LEFT OUTER JOIN (
                        SELECT * FROM RPT_BSC
                        WHERE RPT_DVCD = '4' AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                    ) B
                    ON A.CHRCP_NO = B.CHRCP_NO
                    AND A.YEAR = B.YEAR
                ) A
            ) A
        ) A
    """)
    fun getAclData(dnctr_cd: String, ctr_cd: String, brc_cd: String, prj_cd: String, year: String): List<StatisticAclItem>

    @Query("""
        SELECT REG_CNT, AP, RE, WA
            , ROUND(AP / (REG_CNT * 1.0) * 100, 2) AS AP_PCT
            , ROUND(RE / (REG_CNT * 1.0) * 100, 2) AS RE_PCT
            , ROUND(WA / (REG_CNT * 1.0) * 100, 2) AS WA_PCT
        FROM (
            SELECT
                  (SELECT COUNT(*)
                    FROM RPT_BSC
                    WHERE RPT_DVCD = '1' AND RPT_STCD = '1' AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                    AND APRV_DT <= :year || '1231') AS REG_CNT
                , SUM(CASE RPT_STCD WHEN 'AP' THEN 1 ELSE 0 END )AS AP
                , SUM(CASE RPT_STCD WHEN 'RE' THEN 1 ELSE 0 END) AS RE
                , SUM(CASE RPT_STCD WHEN 'WA' THEN 1 ELSE 0 END) AS WA
            FROM (
                SELECT *
                FROM (
                    SELECT CHRCP_NO
                        , CASE
                            WHEN RPT_STCD = '1' THEN 'AP'
                            WHEN RPT_STCD = '2' OR RPT_STCD = '15' THEN 'RE'
                            WHEN RPT_STCD = '4' OR RPT_STCD = '5' OR RPT_STCD = '6'
                                     OR RPT_STCD = '7' OR RPT_STCD = '8' OR RPT_STCD = '9'
                                     OR RPT_STCD = '10' OR RPT_STCD = '11' THEN 'WA'
                            ELSE 'NR'
                          END AS RPT_STCD
                    FROM RPT_BSC
                    WHERE YEAR = :year AND RPT_DVCD = '3' AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                ) A
                INNER JOIN (
                    SELECT *
                    FROM CH_MST
                    WHERE DNCTR_CD = :dnctr_cd AND CTR_CD = :ctr_cd AND BRC_CD = :brc_cd AND PRJ_CD = :prj_cd AND DEL_YN != 'Y'
                ) B
                ON A.CHRCP_NO = B.CHRCP_NO
            ) A
        ) A
    """)
    fun getDropoutData(dnctr_cd: String, ctr_cd: String, brc_cd: String, prj_cd: String, year: String): List<StatisticDropoutItem>

    @Query("""
        WITH TARGET_MONTH AS (
            SELECT 1 AS MM
            UNION
            SELECT MM + 1 FROM TARGET_MONTH
            WHERE MM < 12
        )
        SELECT MONTH, CNT, AMT, AP, RE, WA, NR
            , ROUND(AP / (CNT * 1.0) * 100, 2) AS AP_PCT
            , ROUND(RE / (CNT * 1.0) * 100, 2) AS RE_PCT
            , ROUND(WA / (CNT * 1.0) * 100, 2) AS WA_PCT
            , ROUND(NR / (CNT * 1.0) * 100, 2) AS NR_PCT
        FROM (
            SELECT SUBSTR('0' || A.MM, -2, 2) AS MONTH, IFNULL(CNT, 0) AS CNT, IFNULL(AMT, 0) AS AMT
                 , IFNULL(AP, 0) AS AP, IFNULL(RE, 0) AS RE, IFNULL(WA, 0) AS WA, IFNULL(NR, 0) AS NR
            FROM (
                SELECT * FROM TARGET_MONTH
            ) A
            LEFT OUTER JOIN (
                SELECT MONTH, COUNT(MONTH) AS CNT
                    , SUM(AMT) AS AMT
                    , SUM(CASE RPT_STCD WHEN 'AP' THEN 1 ELSE 0 END )AS AP
                    , SUM(CASE RPT_STCD WHEN 'RE' THEN 1 ELSE 0 END) AS RE
                    , SUM(CASE RPT_STCD WHEN 'WA' THEN 1 ELSE 0 END) AS WA
                    , SUM(CASE RPT_STCD WHEN 'NR' THEN 1 ELSE 0 END) AS NR
                FROM (
                    SELECT MONTH, AMT
                        , CASE
                            WHEN RPT_STCD = '1' THEN 'AP'
                            WHEN RPT_STCD = '2' OR RPT_STCD = '15' THEN 'RE'
                            WHEN RPT_STCD = '4' OR RPT_STCD = '5' OR RPT_STCD = '6'
                                     OR RPT_STCD = '7' OR RPT_STCD = '8' OR RPT_STCD = '9'
                                     OR RPT_STCD = '10' OR RPT_STCD = '11' THEN 'WA'
                            WHEN RPT_STCD = '16' THEN 'NR'
                            ELSE 'NR'
                          END AS RPT_STCD
                    FROM (
                        SELECT A.MONTH, A.AMT, IFNULL(B.RPT_STCD, '16') AS RPT_STCD
                        FROM (
                            SELECT *
                            FROM (
                                SELECT SUBSTR(RCP_DT, 1, 4) AS YEAR, SUBSTR(RCP_DT, 5, 2) AS MONTH, GIFT_DAMT AS AMT, CHRCP_NO, MNG_NO, RCP_DT
                                FROM GMNY
                                WHERE MV_DT IS NULL
                                AND RFND_DT IS NULL
                            ) A
                            WHERE YEAR = :year
                        ) A
                        LEFT OUTER JOIN (
                            SELECT A.*, B.GMNY_MNGNO
                            FROM (
                                SELECT RCP_NO, CHRCP_NO, RPT_STCD FROM RPT_BSC
                                WHERE RPT_DVCD = '5' AND DEL_YN != 'Y' AND FIDG_YN = 'Y' AND YEAR = :year
                            ) A
                            INNER JOIN RPLY B
                            ON A.RCP_NO = B.RCP_NO
                        ) B
                        ON A.CHRCP_NO = B.CHRCP_NO
                        AND A.MNG_NO = B.GMNY_MNGNO
                    ) A
                ) A
                GROUP BY MONTH
            ) B
            ON A.MM = CAST(B.MONTH AS NUMERIC)
        ) A
        WHERE MONTH >= :fromMonth AND MONTH <= :toMonth
    """)
    fun getGmData(year: String, fromMonth: String, toMonth: String): List<StatisticGmlItem>


    @Query("""
        WITH TARGET_MONTH AS (
            SELECT 1 AS MM
            UNION
            SELECT MM + 1 FROM TARGET_MONTH
            WHERE MM < 12
        )
        SELECT MONTH, CNT, AMT, AP, RE, WA, NR
            , IFNULL(ROUND(AP / (CNT * 1.0) * 100, 2), 0) AS AP_PCT
            , IFNULL(ROUND(RE / (CNT * 1.0) * 100, 2), 0) AS RE_PCT
            , IFNULL(ROUND(WA / (CNT * 1.0) * 100, 2), 0) AS WA_PCT
            , IFNULL(ROUND(NR / (CNT * 1.0) * 100, 2), 0) AS NR_PCT
        FROM (
            SELECT SUBSTR('0' || A.MM, -2, 2) AS MONTH, IFNULL(CNT, 0) AS CNT, IFNULL(AMT, 0) AS AMT
                 , IFNULL(AP, 0) AS AP, IFNULL(RE, 0) AS RE, IFNULL(WA, 0) AS WA, IFNULL(NR, 0) AS NR
            FROM (
                SELECT * FROM TARGET_MONTH
            ) A
            LEFT OUTER JOIN (
                SELECT MONTH, COUNT(MONTH) AS CNT
                    , SUM(AMT) AS AMT
                    , SUM(CASE RPT_STCD WHEN 'AP' THEN 1 ELSE 0 END )AS AP
                    , SUM(CASE RPT_STCD WHEN 'RE' THEN 1 ELSE 0 END) AS RE
                    , SUM(CASE RPT_STCD WHEN 'WA' THEN 1 ELSE 0 END) AS WA
                    , SUM(CASE RPT_STCD WHEN 'NR' THEN 1 ELSE 0 END) AS NR
                FROM (
                    SELECT MONTH, AMT
                        , CASE
                            WHEN RPT_STCD = '1' THEN 'AP'
                            WHEN RPT_STCD = '2' OR RPT_STCD = '15' THEN 'RE'
                            WHEN RPT_STCD = '4' OR RPT_STCD = '5' OR RPT_STCD = '6'
                                     OR RPT_STCD = '7' OR RPT_STCD = '8' OR RPT_STCD = '9'
                                     OR RPT_STCD = '10' OR RPT_STCD = '11' THEN 'WA'
                            WHEN RPT_STCD = '16' THEN 'NR'
                            ELSE 'NR'
                          END AS RPT_STCD
                    FROM (
                        SELECT A.MONTH, A.AMT, IFNULL(B.RPT_STCD, '16') AS RPT_STCD
                        FROM (
                            SELECT *
                            FROM (
                                SELECT SUBSTR(RCP_DT, 1, 4) AS YEAR, SUBSTR(RCP_DT, 5, 2) AS MONTH, NULL AS AMT, CHRCP_NO, MNG_NO, RCP_DT
                                FROM LETR
                            ) A
                            WHERE YEAR = :year
                        ) A
                        LEFT OUTER JOIN (
                            SELECT A.*, B.LETR_MNGNO
                            FROM (
                                SELECT RCP_NO, CHRCP_NO, RPT_STCD FROM RPT_BSC
                                WHERE RPT_DVCD = '5' AND DEL_YN != 'Y' AND FIDG_YN = 'Y' AND YEAR = :year
                            ) A
                            INNER JOIN RPLY B
                            ON A.RCP_NO = B.RCP_NO
                        ) B
                        ON A.CHRCP_NO = B.CHRCP_NO
                        AND A.MNG_NO = B.LETR_MNGNO
                    ) A
                ) A
                GROUP BY MONTH
            ) B
            ON A.MM = CAST(B.MONTH AS NUMERIC)
        ) A
        WHERE MONTH >= :fromMonth AND MONTH <= :toMonth
    """)
    fun getLetterData(year: String, fromMonth: String, toMonth: String): List<StatisticGmlItem>
}