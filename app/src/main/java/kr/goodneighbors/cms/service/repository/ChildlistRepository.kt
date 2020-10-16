package kr.goodneighbors.cms.service.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.persistence.db.SimpleSQLiteQuery
import android.content.SharedPreferences
import kr.goodneighbors.cms.common.Constants
import kr.goodneighbors.cms.service.db.ChildlistDao
import kr.goodneighbors.cms.service.entities.APP_SEARCH_HISTORY
import kr.goodneighbors.cms.service.model.ChildlistItem
import kr.goodneighbors.cms.service.model.ChildlistSearchItem
import kr.goodneighbors.cms.service.model.VillageLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChildlistRepository @Inject constructor(
        private val childlistDao: ChildlistDao,
        private val preferences: SharedPreferences
) {
    private val logger: Logger by lazy {
        LoggerFactory.getLogger(ChildlistRepository::class.java)
    }

    private val childlist = MutableLiveData<List<ChildlistItem>>()

    fun findAll(s: ChildlistSearchItem): MutableLiveData<List<ChildlistItem>> {
        Thread(Runnable {
            logger.debug("findAll : $s")
            if (!s.searchText.isNullOrBlank()) {
                childlistDao.saveSuggestion(APP_SEARCH_HISTORY(word = s.searchText?:"", registDate = Date().time))
            }
            if (!s.searchCode.isNullOrBlank()) {
                childlistDao.saveSuggestion(APP_SEARCH_HISTORY(word = s.searchCode?:"", registDate = Date().time))
            }
            when (s.service) {
                Constants.SERVICE_CIF -> { findAllCif(s) }
                Constants.SERVICE_APR -> { findAllApr(s) }
                Constants.SERVICE_DRO -> { findAllDropout(s) }
                Constants.SERVICE_ACL -> { findAllAcl(s) }
                Constants.SERVICE_GML -> { findAllGml(s) }
                else -> { findAllChildren(s) }
            }
        }).start()

        return childlist
    }

    private fun findAllGml(s: ChildlistSearchItem) {
        var sql = """
                SELECT *
                FROM (
                    SELECT A.*
                        , '${s.service}' AS SERVICE
                         , (SELECT SPSL_CD FROM CH_SPSL_INFO WHERE RCP_NO = A.LAST_RCP_NO LIMIT 0, 1) AS CASE1
                         , (SELECT SPSL_CD FROM CH_SPSL_INFO WHERE RCP_NO = A.LAST_RCP_NO LIMIT 1, 1) AS CASE2
                         , (SELECT SPSL_CD FROM CH_SPSL_INFO WHERE RCP_NO = A.LAST_RCP_NO LIMIT 2, 1) AS CASE3
                         , (strftime('%Y','now','localtime') - SUBSTR(A.BDAY, 1, 4)) AS AGE
                         , B.YEAR
                         , B.GMNY_MNG_NO, B.GMNY_RCP_NO, B.GMNY_MM, B.GMNY_STATUS_CODE, B.GMNY_STATUS_LABEL, B.GMNY_STATUS_COLOR
                         , B.LETR_MNG_NO, B.LETR_RCP_NO, B.LETR_MM, B.LETR_STATUS_CODE, B.LETR_STATUS_LABEL, B.LETR_STATUS_COLOR
                    FROM (
                        SELECT A.*, B.SCTP_CD
                        FROM (
                            SELECT A.*
                                , B.GNDR, B.BDAY, B.VLG_CD
                            FROM (
                                SELECT
                                      IFNULL(B.CTR_CD, '') || '-' || IFNULL(B.BRC_CD, '') || IFNULL(B.PRJ_CD, '') || '-' || IFNULL(B.CH_CD, '') AS CHILD_CODE
                                    , REPLACE(IFNULL(B.CH_EFNM, '') || ' ' || IFNULL(B.CH_EMNM, '') || ' ' || IFNULL(B.CH_ELNM, ''), '  ', ' ') AS CHILD_NAME
                                    , B.CHRCP_NO, B.DNCTR_CD, B.ORG_CHRCP_NO, B.BF_CHRCP_NO
                                    , (SELECT IFNULL(CTR_CD, '') || '-' || IFNULL(BRC_CD, '') || IFNULL(PRJ_CD, '') || '-' || IFNULL(CH_CD, '') FROM CH_MST WHERE CHRCP_NO = B.BF_CHRCP_NO) AS BF_CHILD_CODE
                                    , (SELECT RCP_NO FROM RPT_BSC
                                        WHERE CHRCP_NO = B.CHRCP_NO AND RPT_DVCD IN ('1', '2') AND RPT_STCD = '1'
                                        AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                                        ORDER BY RPT_DVCD DESC, YEAR DESC LIMIT 1) AS LAST_RCP_NO
                                FROM (
                                    SELECT CHRCP_NO
                                    FROM RPT_BSC
                                    WHERE RPT_DVCD = '1' AND RPT_STCD = '1'
                                    AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                                ) A
                                INNER JOIN CH_MST B
                                ON A.CHRCP_NO = B.CHRCP_NO
                                AND B.DEL_YN != 'Y'
                                AND B.CH_STCD != '6'
                            ) A
                            INNER JOIN CH_BSC B
                            ON A.LAST_RCP_NO = B.RCP_NO
                        ) A
                        LEFT OUTER JOIN EDU B
                        ON A.LAST_RCP_NO = B.RCP_NO
                    ) A
                    INNER JOIN (
                        SELECT A.*, B.CD_ENM AS LETR_STATUS_LABEL, B.REF_1 AS LETR_STATUS_COLOR
                        FROM (
                            SELECT A.*, B.CD_ENM AS GMNY_STATUS_LABEL, B.REF_1 AS GMNY_STATUS_COLOR
                            FROM (
                                SELECT CHRCP_NO, YEAR
                                    , GMNY_MNG_NO, LETR_MNG_NO
                                    , GMNY_RCP_NO, LETR_RCP_NO
                                    , GMNY_MM, LETR_MM
                                    , GMNY_STATUS_CODE, LETR_STATUS_CODE
                                FROM (
                                    SELECT *
                                    FROM (
                                        SELECT A.MNG_NO AS GMNY_MNG_NO, NULL AS LETR_MNG_NO, B.RCP_NO AS GMNY_RCP_NO, NULL AS LETR_RCP_NO, A.CHRCP_NO, SUBSTR(A.RCP_DT, 1, 4) AS YEAR
                                            , SUBSTR(A.MNG_NO, 6, 2) AS GMNY_MM, null AS LETR_MM, IFNULL(B.RPT_STCD, '16') AS GMNY_STATUS_CODE, NULL AS LETR_STATUS_CODE
                                        FROM GMNY A
                                        LEFT OUTER JOIN (
                                            SELECT A.*, B.GMNY_MNGNO
                                            FROM (
                                                SELECT RCP_NO, CHRCP_NO, RPT_STCD, YEAR
                                                FROM RPT_BSC
                                                WHERE RPT_DVCD = '5'
                                                AND RPT_STCD NOT IN ('98', '99')
                                                AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                                            ) A
                                            INNER JOIN RPLY B
                                            ON A.RCP_NO = B.RCP_NO
                                            AND B.GMNY_MNGNO IS NOT NULL
                                        ) B
                                        ON A.CHRCP_NO = B.CHRCP_NO
                                        AND A.MNG_NO = B.GMNY_MNGNO

                                        UNION

                                        SELECT NULL, A.MNG_NO AS LETR_MNG_NO, NULL, B.RCP_NO AS LETR_RCP_NO, A.CHRCP_NO, SUBSTR(A.RCP_DT, 1, 4) AS YEAR, null, SUBSTR(A.MNG_NO, 6, 2) AS LETR_MM, NULL, IFNULL(B.RPT_STCD, '16') AS LETR_STATUS_CODE
                                        FROM LETR A
                                        LEFT OUTER JOIN (
                                            SELECT A.*, B.LETR_MNGNO
                                            FROM (
                                                 SELECT RCP_NO, CHRCP_NO, RPT_STCD, YEAR
                                                 FROM RPT_BSC
                                                 WHERE RPT_DVCD = '5'
                                                 AND RPT_STCD NOT IN ('98', '99')
                                                 AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                                            ) A
                                            INNER JOIN RPLY B
                                            ON A.RCP_NO = B.RCP_NO
                                        ) B
                                        ON A.CHRCP_NO = B.CHRCP_NO
                                        AND A.MNG_NO = B.LETR_MNGNO
                                    ) A
                                    WHERE YEAR = '${s.year}'
                                ) A
                            ) A
                            LEFT OUTER JOIN (
                                SELECT * FROM CD WHERE GRP_CD = '65' AND CD != 0
                            ) B
                            ON A.GMNY_STATUS_CODE = B.CD
                        ) A
                        LEFT OUTER JOIN (
                            SELECT * FROM CD WHERE GRP_CD = '65' AND CD != 0
                        ) B
                        ON A.LETR_STATUS_CODE = B.CD
                    ) B
                    ON A.CHRCP_NO = B.CHRCP_NO
                ) A
                WHERE CHRCP_NO IS NOT NULL
            """

        if (!s.support.isNullOrBlank()) {
            sql = "$sql AND DNCTR_CD = '${s.support}'"
        }

        if (!s.status.isNullOrBlank()) {
            sql = when(s.status) {
                "4"-> {
                    "$sql AND (GMNY_STATUS_CODE IN ('4', '5', '6', '7', '8', '9', '10', '11') OR LETR_STATUS_CODE IN ('4', '5', '6', '7', '8', '9', '10', '11'))"
                }
                else -> {
                    "$sql AND (GMNY_STATUS_CODE = '${s.status}' OR LETR_STATUS_CODE = '${s.status}')"
                }
            }
        }

        if (!s.year.isNullOrBlank()) {
            sql = "$sql AND YEAR = '${s.year}'"
        }

        if (!s.village.isNullOrBlank()) {
            sql = "$sql AND VLG_CD = '${s.village}'"
        }

        if (!s.school.isNullOrBlank()) {
            sql = "$sql AND SCTP_CD = '${s.school}'"
        }

        if (!s.gender.isNullOrBlank()) {
            sql = "$sql AND GNDR = '${s.gender}'"
        }

        if (!s.ageFrom.isNullOrBlank()) {
            sql = "$sql AND AGE >= ${s.ageFrom}"
        }

        if (!s.ageTo.isNullOrBlank()) {
            sql = "$sql AND AGE <= ${s.ageTo}"
        }

        if (!s.case1.isNullOrBlank()) {
            sql = "$sql AND (CASE1 = '${s.case1}' OR CASE2 = '${s.case1}' OR CASE3 = '${s.case1}')"
        }

        if (!s.case2.isNullOrBlank()) {
            sql = "$sql AND (CASE1 = '${s.case2}' OR CASE2 = '${s.case2}' OR CASE3 = '${s.case2}')"
        }

        if (!s.case3.isNullOrBlank()) {
            sql = "$sql AND (CASE1 = '${s.case3}' OR CASE2 = '${s.case3}' OR CASE3 = '${s.case3}')"
        }

        if (!s.searchCode.isNullOrBlank()) {
            sql = "$sql AND (CHILD_CODE = '${s.searchText}' OR BF_CHILD_CODE = '${s.searchText}')"
        }

        if (!s.searchText.isNullOrBlank()) {
            s.searchText = s.searchText!!.replace("'", "")
            sql = "$sql AND (CHILD_NAME LIKE '%${s.searchText}%' OR CHILD_CODE LIKE '%${s.searchText}%' OR BF_CHILD_CODE LIKE '%${s.searchText}%')"
        }

        sql = """$sql
            ORDER BY (
                    CASE
                        WHEN GMNY_STATUS_CODE = '12' OR LETR_STATUS_CODE = '12' THEN 1
                        WHEN GMNY_STATUS_CODE = '2'  OR LETR_STATUS_CODE = '2'  THEN 2
                        WHEN GMNY_STATUS_CODE = '15' OR LETR_STATUS_CODE = '15' THEN 3
                        WHEN GMNY_STATUS_CODE = '16' OR LETR_STATUS_CODE = '16' THEN 4
                        WHEN GMNY_STATUS_CODE = '4'  OR LETR_STATUS_CODE = '4'  THEN 5
                        WHEN GMNY_STATUS_CODE = '5'  OR LETR_STATUS_CODE = '5'  THEN 6
                        WHEN GMNY_STATUS_CODE = '6'  OR LETR_STATUS_CODE = '6'  THEN 7
                        WHEN GMNY_STATUS_CODE = '7'  OR LETR_STATUS_CODE = '7'  THEN 8
                        WHEN GMNY_STATUS_CODE = '8'  OR LETR_STATUS_CODE = '8'  THEN 9
                        WHEN GMNY_STATUS_CODE = '13' OR LETR_STATUS_CODE = '13' THEN 10
                        WHEN GMNY_STATUS_CODE = '14' OR LETR_STATUS_CODE = '14' THEN 11
                        WHEN GMNY_STATUS_CODE = '1'  OR LETR_STATUS_CODE = '1'  THEN 12
                        ELSE 99
                    END
                ), CHILD_CODE DESC, CHILD_NAME
        """

        val query = SimpleSQLiteQuery(sql)
        logger.debug("sql = ${query.sql}")

        val items = childlistDao.findAllChildList(query)
        logger.debug("findAllGml : ${items.size}")

        childlist.postValue(items)
    }

    private fun findAllDropout(s: ChildlistSearchItem) {
        var sql = """
                SELECT A.*
                FROM (
                    SELECT A.*
                        , '${s.service}' AS SERVICE
                        , (SELECT SPSL_CD FROM CH_SPSL_INFO WHERE RCP_NO = A.RCP_NO LIMIT 0, 1) AS CASE1
                        , (SELECT SPSL_CD FROM CH_SPSL_INFO WHERE RCP_NO = A.RCP_NO LIMIT 1, 1) AS CASE2
                        , (SELECT SPSL_CD FROM CH_SPSL_INFO WHERE RCP_NO = A.RCP_NO LIMIT 2, 1) AS CASE3
                        , (strftime('%Y', 'now', 'localtime') - SUBSTR(A.BDAY, 1, 4))           AS AGE
                    FROM (
                        SELECT A.*
                            , B.LETR_MNG_NO, B.LETR_RCP_NO, B.LETR_MM, B.LETR_STATUS_CODE, B.LETR_STATUS_LABEL, B.LETR_STATUS_COLOR
                        FROM (
                            SELECT A.*
                                , B.GMNY_MNG_NO, B.GMNY_RCP_NO, B.GMNY_MM, B.GMNY_STATUS_CODE, B.GMNY_STATUS_LABEL, B.GMNY_STATUS_COLOR
                            FROM (
                                SELECT A.*
                                    , B.CD     AS STATUS_CODE, B.CD_ENM AS STATUS_LABEL, B.REF_1  AS STATUS_COLOR
                                FROM (
                                    SELECT A.*, B.SCTP_CD
                                    FROM (
                                        SELECT A.*
                                            , B.GNDR, B.BDAY, B.VLG_CD
                                        FROM (
                                            SELECT A.RCP_NO, A.CHRCP_NO, A.YEAR, A.RPT_STCD
                                                , B.DNCTR_CD, B.CH_CD, B.CH_STCD
                                                , IFNULL(B.CTR_CD, '') || '-' || IFNULL(B.BRC_CD, '') || IFNULL(B.PRJ_CD, '') || '-' || IFNULL(B.CH_CD, '') AS CHILD_CODE
                                                , (SELECT IFNULL(CTR_CD, '') || '-' || IFNULL(BRC_CD, '') || IFNULL(PRJ_CD, '') || '-' || IFNULL(CH_CD, '') FROM CH_MST WHERE CHRCP_NO = B.BF_CHRCP_NO) AS BF_CHILD_CODE
                                                , REPLACE(REPLACE(IFNULL(B.CH_EFNM, '') || ' ' || IFNULL(B.CH_EMNM, '') || ' ' || IFNULL(B.CH_ELNM, ''), '  ', ' '), '  ', ' ') AS CHILD_NAME
                                                , (SELECT RCP_NO FROM RPT_BSC
                                                   WHERE CHRCP_NO = B.CHRCP_NO AND RPT_DVCD IN ('1', '2') AND RPT_STCD = '1'
                                                   AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                                                   ORDER BY RPT_DVCD DESC, YEAR DESC LIMIT 1) AS LAST_RCP_NO
                                            FROM (
                                                SELECT *
                                                FROM RPT_BSC
                                                WHERE RPT_DVCD = '3' AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                                                AND RPT_STCD NOT IN ('98', '99')
                                            ) A
                                            INNER JOIN CH_MST B
                                            ON A.CHRCP_NO = B.CHRCP_NO
                                        ) A
                                        LEFT OUTER JOIN CH_BSC B
                                        ON A.LAST_RCP_NO = B.RCP_NO
                                    ) A
                                    LEFT OUTER JOIN EDU B
                                    ON A.LAST_RCP_NO = B.RCP_NO
                                ) A
                                LEFT OUTER JOIN (
                                    SELECT * FROM CD WHERE GRP_CD = '65' AND CD != 0
                                ) B
                                ON A.RPT_STCD = B.CD
                            ) A
                            LEFT OUTER JOIN (
                                SELECT A.*, B.CD_ENM AS GMNY_STATUS_LABEL, B.REF_1 AS GMNY_STATUS_COLOR
                                FROM (
                                    SELECT A.MNG_NO AS GMNY_MNG_NO, A.RCP_NO AS GMNY_RCP_NO, SUBSTR(A.RCP_DT, 5, 2) AS GMNY_MM, A.RPT_STCD AS GMNY_STATUS_CODE, A.CHRCP_NO
                                    FROM (
                                        SELECT A.MNG_NO, B.RCP_NO, A.CHRCP_NO, A.RCP_DT, IFNULL(B.RPT_STCD, '16') AS RPT_STCD
                                            , (SELECT COUNT(*) FROM GMNY C WHERE C.CHRCP_NO = A.CHRCP_NO AND C.MNG_NO > A.MNG_NO ORDER BY C.MNG_NO DESC) AS R
                                        FROM GMNY A
                                        LEFT OUTER JOIN (
                                            SELECT A.*, B.GMNY_MNGNO
                                            FROM (
                                                SELECT RCP_NO, CHRCP_NO, RPT_STCD, YEAR
                                                FROM RPT_BSC
                                                WHERE RPT_DVCD = '5'
                                                AND RPT_STCD NOT IN ('98', '99')
                                                AND DEL_YN = 'N' AND FIDG_YN = 'Y'
                                            ) A
                                            INNER JOIN RPLY B
                                            ON A.RCP_NO = B.RCP_NO
                                        ) B
                                        ON A.CHRCP_NO = B.CHRCP_NO
                                        AND A.MNG_NO = B.GMNY_MNGNO
                                    ) A
                                    WHERE R = 0
                                ) A
                                LEFT OUTER JOIN (
                                    SELECT * FROM CD WHERE GRP_CD = '65' AND CD != 0
                                ) B
                                ON A.GMNY_STATUS_CODE = B.CD
                            ) B
                            ON A.CHRCP_NO = B.CHRCP_NO
                        ) A
                        LEFT OUTER JOIN (
                            SELECT A.*, B.CD_ENM AS LETR_STATUS_LABEL, B.REF_1 AS LETR_STATUS_COLOR
                            FROM (
                                SELECT A.MNG_NO AS LETR_MNG_NO, A.RCP_NO AS LETR_RCP_NO, SUBSTR(A.RCP_DT, 5, 2) AS LETR_MM, A.RPT_STCD AS LETR_STATUS_CODE, A.CHRCP_NO
                                FROM (
                                    SELECT A.MNG_NO, B.RCP_NO, A.CHRCP_NO, A.RCP_DT, IFNULL(B.RPT_STCD, '16') AS RPT_STCD
                                        , (SELECT COUNT(*) FROM LETR C WHERE C.CHRCP_NO = A.CHRCP_NO AND C.MNG_NO > A.MNG_NO ORDER BY C.MNG_NO DESC) AS R
                                    FROM LETR A
                                    LEFT OUTER JOIN (
                                        SELECT A.*, B.LETR_MNGNO
                                        FROM (
                                            SELECT RCP_NO, CHRCP_NO, RPT_STCD, YEAR
                                            FROM RPT_BSC
                                            WHERE RPT_DVCD = '5'
                                            AND RPT_STCD NOT IN ('98', '99')
                                            AND DEL_YN = 'N' AND FIDG_YN = 'Y'
                                        ) A
                                        INNER JOIN RPLY B
                                        ON A.RCP_NO = B.RCP_NO
                                    ) B
                                    ON A.CHRCP_NO = B.CHRCP_NO
                                    AND A.MNG_NO = B.LETR_MNGNO
                                ) A
                                WHERE R = 0
                            ) A
                            LEFT OUTER JOIN (
                                SELECT * FROM CD WHERE GRP_CD = '65' AND CD != 0
                            ) B
                            ON A.LETR_STATUS_CODE = B.CD
                        ) B
                        ON A.CHRCP_NO = B.CHRCP_NO
                    ) A
                ) A
                WHERE CHRCP_NO IS NOT NULL
            """

        if (!s.support.isNullOrBlank()) {
            sql = "$sql AND DNCTR_CD = '${s.support}'"
        }

        if (!s.status.isNullOrBlank()) {
            sql = when(s.status) {
                "4"-> {
                    "$sql AND STATUS_CODE IN ('4', '5', '6', '7', '8', '9', '10', '11')"
                }
                else -> {
                    "$sql AND STATUS_CODE = '${s.status}'"
                }
            }
        }

        if (!s.year.isNullOrBlank()) {
            sql = "$sql AND YEAR = '${s.year}'"
        }

        if (!s.village.isNullOrBlank()) {
            sql = "$sql AND VLG_CD = '${s.village}'"
        }

        if (!s.school.isNullOrBlank()) {
            sql = "$sql AND SCTP_CD = '${s.school}'"
        }

        if (!s.gender.isNullOrBlank()) {
            sql = "$sql AND GNDR = '${s.gender}'"
        }

        if (!s.ageFrom.isNullOrBlank()) {
            sql = "$sql AND AGE >= ${s.ageFrom}"
        }

        if (!s.ageTo.isNullOrBlank()) {
            sql = "$sql AND AGE <= ${s.ageTo}"
        }

        if (!s.case1.isNullOrBlank()) {
            sql = "$sql AND (CASE1 = '${s.case1}' OR CASE2 = '${s.case1}' OR CASE3 = '${s.case1}')"
        }

        if (!s.case2.isNullOrBlank()) {
            sql = "$sql AND (CASE1 = '${s.case2}' OR CASE2 = '${s.case2}' OR CASE3 = '${s.case2}')"
        }

        if (!s.case3.isNullOrBlank()) {
            sql = "$sql AND (CASE1 = '${s.case3}' OR CASE2 = '${s.case3}' OR CASE3 = '${s.case3}')"
        }

        if (!s.searchCode.isNullOrBlank()) {
            sql = "$sql AND (CHILD_CODE = '${s.searchText}' OR BF_CHILD_CODE = '${s.searchText}')"
        }

        if (!s.searchText.isNullOrBlank()) {
            s.searchText = s.searchText!!.replace("'", "")
            sql = "$sql AND (CHILD_NAME LIKE '%${s.searchText}%' OR CHILD_CODE LIKE '%${s.searchText}%' OR BF_CHILD_CODE LIKE '%${s.searchText}%')"
        }

        sql = """$sql
            ORDER BY (
                    CASE
                        WHEN RPT_STCD = '12' THEN 1
                        WHEN RPT_STCD = '2'  THEN 2
                        WHEN RPT_STCD = '15' THEN 3
                        WHEN RPT_STCD = '16' THEN 4
                        WHEN RPT_STCD = '4'  THEN 5
                        WHEN RPT_STCD = '5'  THEN 6
                        WHEN RPT_STCD = '6'  THEN 7
                        WHEN RPT_STCD = '7'  THEN 8
                        WHEN RPT_STCD = '8'  THEN 9
                        WHEN RPT_STCD = '13' THEN 10
                        WHEN RPT_STCD = '14' THEN 11
                        WHEN RPT_STCD = '1'  THEN 12
                        ELSE 99
                    END
                ), CHILD_CODE DESC, CHILD_NAME
        """

        val query = SimpleSQLiteQuery(sql)
        logger.debug("sql = ${query.sql}")

        val items = childlistDao.findAllChildList(query)
        logger.debug("findAllDropout : ${items.size}")

        childlist.postValue(items)
    }

    private fun findAllAcl(s: ChildlistSearchItem) {
        logger.debug("findAllAcl($s)")
        var sql = """
                SELECT A.*
                FROM (
                    SELECT A.*
                        , '${s.service}' AS SERVICE
                        , (SELECT SPSL_CD FROM CH_SPSL_INFO WHERE RCP_NO = A.RCP_NO LIMIT 0, 1) AS CASE1
                        , (SELECT SPSL_CD FROM CH_SPSL_INFO WHERE RCP_NO = A.RCP_NO LIMIT 1, 1) AS CASE2
                        , (SELECT SPSL_CD FROM CH_SPSL_INFO WHERE RCP_NO = A.RCP_NO LIMIT 2, 1) AS CASE3
                        , (strftime('%Y','now','localtime') - SUBSTR(A.BDAY, 1, 4)) AS AGE
                    FROM (
                        SELECT A.*, B.LETR_MNG_NO, B.LETR_RCP_NO, B.LETR_MM, B.LETR_STATUS_CODE, B.LETR_STATUS_LABEL, B.LETR_STATUS_COLOR
                        FROM (
                            SELECT A.*, B.GMNY_MNG_NO, B.GMNY_RCP_NO, B.GMNY_MM, B.GMNY_STATUS_CODE, B.GMNY_STATUS_LABEL, B.GMNY_STATUS_COLOR
                            FROM (
                                SELECT A.*, B.CD AS STATUS_CODE, B.CD_ENM AS STATUS_LABEL, B.REF_1 AS STATUS_COLOR
                                FROM (
                                    SELECT A.*, B.SCTP_CD, B.SCHL_CD
                                    FROM (
                                        SELECT A.*, B.GNDR, B.BDAY, B.VLG_CD
                                        FROM (
                                            SELECT A.*
                                            , B.RCP_NO, IFNULL(B.RPT_STCD, '16') AS RPT_STCD
                                            , CASE WHEN CH_STCD != 1 AND RPT_STCD IS NULL THEN 'N' ELSE 'Y' END AS IS_IN
                                            FROM (
                                                SELECT
                                                      B.CHRCP_NO
                                                    , IFNULL(B.CTR_CD, '') || '-' || IFNULL(B.BRC_CD, '') || IFNULL(B.PRJ_CD, '') || '-' || IFNULL(B.CH_CD, '') AS CHILD_CODE
                                                    , TRIM(REPLACE(REPLACE(IFNULL(B.CH_EFNM, '') || ' ' || IFNULL(B.CH_EMNM, '') || ' ' || IFNULL(B.CH_ELNM, ''), '  ', ' '), '  ', ' ')) AS CHILD_NAME
                                                    , B.DNCTR_CD, B.CH_STCD
                                                    , (SELECT IFNULL(CTR_CD, '') || '-' || IFNULL(BRC_CD, '') || IFNULL(PRJ_CD, '') || '-' || IFNULL(CH_CD, '') FROM CH_MST WHERE CHRCP_NO = B.BF_CHRCP_NO) AS BF_CHILD_CODE
                                                    , '${s.year}' AS YEAR
                                                    , (SELECT RCP_NO FROM RPT_BSC
                                                       WHERE CHRCP_NO = B.CHRCP_NO AND RPT_DVCD IN ('1', '2') AND RPT_STCD = '1'
                                                       AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                                                       ORDER BY RPT_DVCD DESC, YEAR DESC LIMIT 1) AS LAST_RCP_NO
                                                FROM (
                                                    SELECT CHRCP_NO
                                                    FROM RPT_BSC
                                                    WHERE RPT_DVCD = '1' AND RPT_STCD = '1' AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                                                ) A
                                                INNER JOIN (
                                                    SELECT *
                                                    FROM CH_MST
                                                    WHERE DEL_YN != 'Y'
                                                    AND CHRCP_NO IN (
                                                        SELECT CHRCP_NO
                                                        FROM RELSH
                                                        WHERE RELSH_DT < '${s.year}0401'
                                                        AND (RELCNL_DT IS NULL OR RELCNL_DT >= '${s.year}0401')
                                                        GROUP BY CHRCP_NO
                                                    )
                                                ) B
                                                ON A.CHRCP_NO = B.CHRCP_NO
                                            ) A
                                            LEFT OUTER JOIN (
                                                SELECT * FROM RPT_BSC
                                                WHERE RPT_DVCD = '4'
                                                AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                                            ) B
                                            ON A.CHRCP_NO = B.CHRCP_NO
                                            AND A.YEAR = B.YEAR

                                        ) A
                                        LEFT OUTER JOIN CH_BSC B
                                        ON A.LAST_RCP_NO = B.RCP_NO
                                        WHERE IS_IN = 'Y'
                                    ) A
                                    LEFT OUTER JOIN EDU B
                                    ON A.LAST_RCP_NO = B.RCP_NO
                                ) A
                                LEFT OUTER JOIN (
                                    SELECT * FROM CD WHERE GRP_CD = '65' AND CD != 0
                                ) B
                                ON A.RPT_STCD = B.CD
                            ) A
                            LEFT OUTER JOIN (
                                SELECT A.*, B.CD_ENM AS GMNY_STATUS_LABEL, B.REF_1 AS GMNY_STATUS_COLOR
                                FROM (
                                    SELECT A.MNG_NO AS GMNY_MNG_NO, A.RCP_NO AS GMNY_RCP_NO, SUBSTR(A.RCP_DT, 5, 2) AS GMNY_MM, A.RPT_STCD AS GMNY_STATUS_CODE, A.CHRCP_NO
                                    FROM (
                                        SELECT A.MNG_NO, B.RCP_NO, A.CHRCP_NO, A.RCP_DT, IFNULL(B.RPT_STCD, '16') AS RPT_STCD
                                             , (SELECT COUNT(*) FROM GMNY C WHERE C.CHRCP_NO = A.CHRCP_NO AND C.MNG_NO > A.MNG_NO ORDER BY C.MNG_NO DESC) AS R
                                        FROM GMNY A
                                        LEFT OUTER JOIN (
                                            SELECT A.*, B.GMNY_MNGNO
                                            FROM (
                                                SELECT RCP_NO, CHRCP_NO, RPT_STCD, YEAR
                                                FROM RPT_BSC
                                                WHERE RPT_DVCD = '5' AND RPT_STCD NOT IN ('98', '99')
                                                AND DEL_YN = 'N' AND FIDG_YN = 'Y'
                                            ) A
                                            INNER JOIN RPLY B
                                            ON A.RCP_NO = B.RCP_NO
                                        ) B
                                        ON A.CHRCP_NO = B.CHRCP_NO
                                        AND A.MNG_NO = B.GMNY_MNGNO
                                    ) A
                                    WHERE R = 1
                                ) A
                                LEFT OUTER JOIN (
                                    SELECT * FROM CD WHERE GRP_CD = '65' AND CD != 0
                                ) B
                                ON A.GMNY_STATUS_CODE = B.CD
                            ) B
                            ON A.CHRCP_NO = B.CHRCP_NO
                        ) A
                        LEFT OUTER JOIN (
                            SELECT A.*, B.CD_ENM AS LETR_STATUS_LABEL, B.REF_1 AS LETR_STATUS_COLOR
                            FROM (
                                SELECT A.MNG_NO AS LETR_MNG_NO, A.RCP_NO AS LETR_RCP_NO, SUBSTR(A.RCP_DT, 5, 2) AS LETR_MM, A.RPT_STCD AS LETR_STATUS_CODE, A.CHRCP_NO
                                FROM (
                                    SELECT A.MNG_NO, B.RCP_NO, A.CHRCP_NO, A.RCP_DT, IFNULL(B.RPT_STCD, '16') AS RPT_STCD
                                        , (SELECT COUNT(*) FROM LETR C WHERE C.CHRCP_NO = A.CHRCP_NO AND C.MNG_NO > A.MNG_NO ORDER BY C.MNG_NO DESC) AS R
                                    FROM LETR A
                                    LEFT OUTER JOIN (
                                        SELECT A.*, B.LETR_MNGNO
                                        FROM (
                                            SELECT RCP_NO, CHRCP_NO, RPT_STCD, YEAR
                                            FROM RPT_BSC
                                            WHERE RPT_DVCD = '5'
                                            AND RPT_STCD NOT IN ('98', '99')
                                            AND DEL_YN = 'N' AND FIDG_YN = 'Y'
                                        ) A
                                        INNER JOIN RPLY B
                                        ON A.RCP_NO = B.RCP_NO
                                    ) B
                                    ON A.CHRCP_NO = B.CHRCP_NO
                                    AND A.MNG_NO = B.LETR_MNGNO
                                ) A
                                WHERE R = 1
                            ) A
                            LEFT OUTER JOIN (
                                SELECT * FROM CD WHERE GRP_CD = '65' AND CD != 0
                            ) B
                            ON A.LETR_STATUS_CODE = B.CD
                        ) B
                        ON A.CHRCP_NO = B.CHRCP_NO
                    ) A
                ) A
                WHERE CHRCP_NO IS NOT NULL
           """
        if (!s.support.isNullOrBlank()) {
            sql = "$sql AND DNCTR_CD = '${s.support}'"
        }

        if (!s.status.isNullOrBlank()) {
            sql = when(s.status) {
                "4"-> {
                    "$sql AND STATUS_CODE IN ('4', '5', '6', '7', '8', '9', '10', '11')"
                }
                else -> {
                    "$sql AND STATUS_CODE = '${s.status}'"
                }
            }
        }

        if (!s.village.isNullOrBlank()) {
            sql = "$sql AND VLG_CD = '${s.village}'"
        }

        if (!s.school.isNullOrBlank()) {
            sql = "$sql AND SCHL_CD = '${s.school}'"
        }

        if (!s.gender.isNullOrBlank()) {
            sql = "$sql AND GNDR = '${s.gender}'"
        }

        if (!s.ageFrom.isNullOrBlank()) {
            sql = "$sql AND AGE >= ${s.ageFrom}"
        }

        if (!s.ageTo.isNullOrBlank()) {
            sql = "$sql AND AGE <= ${s.ageTo}"
        }

        if (!s.case1.isNullOrBlank()) {
            sql = "$sql AND (CASE1 = '${s.case1}' OR CASE2 = '${s.case1}' OR CASE3 = '${s.case1}')"
        }

        if (!s.case2.isNullOrBlank()) {
            sql = "$sql AND (CASE1 = '${s.case2}' OR CASE2 = '${s.case2}' OR CASE3 = '${s.case2}')"
        }

        if (!s.case3.isNullOrBlank()) {
            sql = "$sql AND (CASE1 = '${s.case3}' OR CASE2 = '${s.case3}' OR CASE3 = '${s.case3}')"
        }

        if (s.dropoutExpected) {
            sql = "$sql AND AGE >= 18"
        }

        if (!s.searchCode.isNullOrBlank()) {
            sql = "$sql AND (CHILD_CODE = '${s.searchText}' OR BF_CHILD_CODE = '${s.searchText}')"
        }

        if (!s.searchText.isNullOrBlank()) {
            s.searchText = s.searchText!!.replace("'", "")
            sql = "$sql AND (CHILD_NAME LIKE '%${s.searchText}%' OR CHILD_CODE LIKE '%${s.searchText}%' OR BF_CHILD_CODE LIKE '%${s.searchText}%')"
        }

        sql = """$sql
                ORDER BY (
                    CASE
                        WHEN RPT_STCD = '12' THEN 1
                        WHEN RPT_STCD = '2'  THEN 2
                        WHEN RPT_STCD = '15' THEN 3
                        WHEN RPT_STCD = '16' THEN 4
                        WHEN RPT_STCD = '4'  THEN 5
                        WHEN RPT_STCD = '5'  THEN 6
                        WHEN RPT_STCD = '6'  THEN 7
                        WHEN RPT_STCD = '7'  THEN 8
                        WHEN RPT_STCD = '8'  THEN 9
                        WHEN RPT_STCD = '13' THEN 10
                        WHEN RPT_STCD = '14' THEN 11
                        WHEN RPT_STCD = '1'  THEN 12
                        ELSE 99
                    END
                ), CHILD_CODE DESC, CHILD_NAME"""

        val query = SimpleSQLiteQuery(sql)
        logger.debug("sql = ${query.sql}")

        val items = childlistDao.findAllChildList(query)

        childlist.postValue(items)
    }

    private fun findAllApr(s: ChildlistSearchItem) {
        logger.debug("findAllApr($s)")
        var sql = """
                SELECT A.*
                FROM (
                    SELECT A.*
                        , '${s.service}' AS SERVICE
                        , (SELECT SPSL_CD FROM CH_SPSL_INFO WHERE RCP_NO = A.RCP_NO LIMIT 0, 1) AS CASE1
                        , (SELECT SPSL_CD FROM CH_SPSL_INFO WHERE RCP_NO = A.RCP_NO LIMIT 1, 1) AS CASE2
                        , (SELECT SPSL_CD FROM CH_SPSL_INFO WHERE RCP_NO = A.RCP_NO LIMIT 2, 1) AS CASE3
                        , (strftime('%Y','now','localtime') - SUBSTR(A.BDAY, 1, 4)) AS AGE
                    FROM (
                        SELECT A.*, B.LETR_MNG_NO, B.LETR_RCP_NO, B.LETR_MM, B.LETR_STATUS_CODE, B.LETR_STATUS_LABEL, B.LETR_STATUS_COLOR
                        FROM (
                            SELECT A.*, B.GMNY_MNG_NO, B.GMNY_RCP_NO, B.GMNY_MM, B.GMNY_STATUS_CODE, B.GMNY_STATUS_LABEL, B.GMNY_STATUS_COLOR
                            FROM (
                                SELECT A.*, B.CD AS STATUS_CODE, B.CD_ENM AS STATUS_LABEL, B.REF_1 AS STATUS_COLOR
                                FROM (
                                    SELECT A.*, B.SCTP_CD, B.SCHL_CD
                                    FROM (
                                        SELECT A.*, B.GNDR, B.BDAY, B.VLG_CD
                                        FROM (
                                            SELECT A.*, B.RCP_NO, IFNULL(B.RPT_STCD, CASE WHEN A.CH_STCD = '1' THEN  '16' ELSE '-1' END) AS RPT_STCD
                                            FROM (
                                                SELECT
                                                      B.CHRCP_NO
                                                    , IFNULL(B.CTR_CD, '') || '-' || IFNULL(B.BRC_CD, '') || IFNULL(B.PRJ_CD, '') || '-' || IFNULL(B.CH_CD, '') AS CHILD_CODE
                                                    , REPLACE(IFNULL(B.CH_EFNM, '') || ' ' || IFNULL(B.CH_EMNM, '') || ' ' || IFNULL(B.CH_ELNM, ''), '  ', ' ') AS CHILD_NAME
                                                    , B.DNCTR_CD
                                                    , B.CH_STCD
                                                    , (SELECT IFNULL(CTR_CD, '') || '-' || IFNULL(BRC_CD, '') || IFNULL(PRJ_CD, '') || '-' || IFNULL(CH_CD, '') FROM CH_MST WHERE CHRCP_NO = B.BF_CHRCP_NO) AS BF_CHILD_CODE
                                                    , '${s.year}' AS YEAR
                                                    , (SELECT RCP_NO FROM RPT_BSC
                                                       WHERE CHRCP_NO = B.CHRCP_NO AND RPT_DVCD IN ('1', '2') AND RPT_STCD = '1'
                                                       AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                                                       ORDER BY RPT_DVCD DESC, YEAR DESC LIMIT 1) AS LAST_RCP_NO
                                                FROM (
                                                    SELECT CHRCP_NO
                                                    FROM RPT_BSC
                                                    WHERE RPT_DVCD = '1' AND RPT_STCD = '1' AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                                                    AND APRV_DT < (('${s.year}') - 1) || '1001'
                                                ) A
                                                INNER JOIN (
                                                    SELECT *
                                                    FROM CH_MST
                                                    WHERE DEL_YN != 'Y'
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
                                        LEFT OUTER JOIN CH_BSC B
                                        ON A.LAST_RCP_NO = B.RCP_NO
                                    ) A
                                    LEFT OUTER JOIN EDU B
                                    ON A.LAST_RCP_NO = B.RCP_NO
                                ) A
                                LEFT OUTER JOIN (
                                    SELECT * FROM CD WHERE GRP_CD = '65' AND CD != 0
                                ) B
                                ON A.RPT_STCD = B.CD
                            ) A
                            LEFT OUTER JOIN (
                                SELECT A.*, B.CD_ENM AS GMNY_STATUS_LABEL, B.REF_1 AS GMNY_STATUS_COLOR
                                FROM (
                                    SELECT A.MNG_NO AS GMNY_MNG_NO, A.RCP_NO AS GMNY_RCP_NO, SUBSTR(A.RCP_DT, 5, 2) AS GMNY_MM, A.RPT_STCD AS GMNY_STATUS_CODE, A.CHRCP_NO
                                    FROM (
                                        SELECT A.MNG_NO, B.RCP_NO, A.CHRCP_NO, A.RCP_DT, IFNULL(B.RPT_STCD, '16') AS RPT_STCD
                                             , (SELECT COUNT(*) FROM GMNY C WHERE C.CHRCP_NO = A.CHRCP_NO AND C.MNG_NO > A.MNG_NO ORDER BY C.MNG_NO DESC) AS R
                                        FROM GMNY A
                                        LEFT OUTER JOIN (
                                            SELECT A.*, B.GMNY_MNGNO
                                            FROM (
                                                SELECT RCP_NO, CHRCP_NO, RPT_STCD, YEAR
                                                FROM RPT_BSC
                                                WHERE RPT_DVCD = '5'
                                                AND RPT_STCD NOT IN ('98', '99')
                                                AND DEL_YN = 'N' AND FIDG_YN = 'Y'
                                            ) A
                                            INNER JOIN RPLY B
                                            ON A.RCP_NO = B.RCP_NO
                                        ) B
                                        ON A.CHRCP_NO = B.CHRCP_NO
                                        AND A.MNG_NO = B.GMNY_MNGNO
                                    ) A
                                    WHERE R = 0
                                ) A
                                LEFT OUTER JOIN (
                                    SELECT * FROM CD WHERE GRP_CD = '65' AND CD != 0
                                ) B
                                ON A.GMNY_STATUS_CODE = B.CD
                            ) B
                            ON A.CHRCP_NO = B.CHRCP_NO
                        ) A
                        LEFT OUTER JOIN (
                            SELECT A.*, B.CD_ENM AS LETR_STATUS_LABEL, B.REF_1 AS LETR_STATUS_COLOR
                            FROM (
                                SELECT A.MNG_NO AS LETR_MNG_NO, A.RCP_NO AS LETR_RCP_NO, SUBSTR(A.RCP_DT, 5, 2) AS LETR_MM, A.RPT_STCD AS LETR_STATUS_CODE, A.CHRCP_NO
                                FROM (
                                    SELECT A.MNG_NO, B.RCP_NO, A.CHRCP_NO, A.RCP_DT, IFNULL(B.RPT_STCD, '16') AS RPT_STCD
                                        , (SELECT COUNT(*) FROM LETR C WHERE C.CHRCP_NO = A.CHRCP_NO AND C.MNG_NO > A.MNG_NO ORDER BY C.MNG_NO DESC) AS R
                                    FROM LETR A
                                    LEFT OUTER JOIN (
                                        SELECT A.*, B.LETR_MNGNO
                                        FROM (
                                            SELECT RCP_NO, CHRCP_NO, RPT_STCD, YEAR
                                            FROM RPT_BSC
                                            WHERE RPT_DVCD = '5'
                                            AND RPT_STCD NOT IN ('98', '99')
                                            AND DEL_YN = 'N' AND FIDG_YN = 'Y'
                                        ) A
                                        INNER JOIN RPLY B
                                        ON A.RCP_NO = B.RCP_NO
                                    ) B
                                    ON A.CHRCP_NO = B.CHRCP_NO
                                    AND A.MNG_NO = B.LETR_MNGNO
                                ) A
                                WHERE R = 0
                            ) A
                            LEFT OUTER JOIN (
                                SELECT * FROM CD WHERE GRP_CD = '65' AND CD != 0
                            ) B
                            ON A.LETR_STATUS_CODE = B.CD
                        ) B
                        ON A.CHRCP_NO = B.CHRCP_NO
                    ) A
                ) A
                WHERE RPT_STCD != '-1'
           """


        if (!s.support.isNullOrBlank()) {
            sql = "$sql AND DNCTR_CD = '${s.support}'"
        }

        if (!s.status.isNullOrBlank()) {
            sql = when(s.status) {
                "4"-> {
                    "$sql AND STATUS_CODE IN ('4', '5', '6', '7', '8', '9', '10', '11')"
                }
                else -> {
                    "$sql AND STATUS_CODE = '${s.status}'"
                }
            }
        }

        if (!s.village.isNullOrBlank()) {
            sql = "$sql AND VLG_CD = '${s.village}'"
        }

        if (!s.school.isNullOrBlank()) {
            sql = "$sql AND SCHL_CD = '${s.school}'"
        }

        if (!s.gender.isNullOrBlank()) {
            sql = "$sql AND GNDR = '${s.gender}'"
        }

        if (!s.ageFrom.isNullOrBlank()) {
            sql = "$sql AND AGE >= ${s.ageFrom}"
        }

        if (!s.ageTo.isNullOrBlank()) {
            sql = "$sql AND AGE <= ${s.ageTo}"
        }

        if (!s.case1.isNullOrBlank()) {
            sql = "$sql AND (CASE1 = '${s.case1}' OR CASE2 = '${s.case1}' OR CASE3 = '${s.case1}')"
        }

        if (!s.case2.isNullOrBlank()) {
            sql = "$sql AND (CASE1 = '${s.case2}' OR CASE2 = '${s.case2}' OR CASE3 = '${s.case2}')"
        }

        if (!s.case3.isNullOrBlank()) {
            sql = "$sql AND (CASE1 = '${s.case3}' OR CASE2 = '${s.case3}' OR CASE3 = '${s.case3}')"
        }

        if (!s.searchCode.isNullOrBlank()) {
            sql = "$sql AND (CHILD_CODE = '${s.searchText}' OR BF_CHILD_CODE = '${s.searchText}')"
        }

        if (!s.searchText.isNullOrBlank()) {
            s.searchText = s.searchText!!.replace("'", "")
            sql = "$sql AND (CHILD_NAME LIKE '%${s.searchText}%' OR CHILD_CODE LIKE '%${s.searchText}%' OR BF_CHILD_CODE LIKE '%${s.searchText}%')"
        }

        sql = """$sql
            ORDER BY (
                    CASE
                        WHEN RPT_STCD = '12' THEN 1
                        WHEN RPT_STCD = '2'  THEN 2
                        WHEN RPT_STCD = '15' THEN 3
                        WHEN RPT_STCD = '16' THEN 4
                        WHEN RPT_STCD = '4'  THEN 5
                        WHEN RPT_STCD = '5'  THEN 6
                        WHEN RPT_STCD = '6'  THEN 7
                        WHEN RPT_STCD = '7'  THEN 8
                        WHEN RPT_STCD = '8'  THEN 9
                        WHEN RPT_STCD = '13' THEN 10
                        WHEN RPT_STCD = '14' THEN 11
                        WHEN RPT_STCD = '1'  THEN 12
                        ELSE 99
                    END
                ), CHILD_CODE DESC, CHILD_NAME
        """

        val query = SimpleSQLiteQuery(sql)
        logger.debug("sql = ${query.sql}")

        val items = childlistDao.findAllChildList(query)

        childlist.postValue(items)
    }

    // Service - CIF
    private fun findAllCif(s: ChildlistSearchItem) {
        logger.debug("findAllCif : $s")
        var sql = """
                SELECT *
                FROM (
                    SELECT A.*
                        , '${s.service}' AS SERVICE
                        , (SELECT SPSL_CD FROM CH_SPSL_INFO WHERE RCP_NO = A.RCP_NO LIMIT 0, 1) AS CASE1
                        , (SELECT SPSL_CD FROM CH_SPSL_INFO WHERE RCP_NO = A.RCP_NO LIMIT 1, 1) AS CASE2
                        , (SELECT SPSL_CD FROM CH_SPSL_INFO WHERE RCP_NO = A.RCP_NO LIMIT 2, 1) AS CASE3
                        , (strftime('%Y','now','localtime') - SUBSTR(A.BDAY, 1, 4)) AS AGE
                    FROM (
                        SELECT A.*, B.LETR_MNG_NO, B.LETR_RCP_NO, B.LETR_MM, B.LETR_STATUS_CODE, B.LETR_STATUS_LABEL, B.LETR_STATUS_COLOR
                        FROM (
                            SELECT A.*, B.GMNY_MNG_NO, B.GMNY_RCP_NO, B.GMNY_MM, B.GMNY_STATUS_CODE, B.GMNY_STATUS_LABEL, B.GMNY_STATUS_COLOR
                            FROM (
                                SELECT A.*, B.CD AS STATUS_CODE, B.CD_ENM AS STATUS_LABEL, B.REF_1 AS STATUS_COLOR
                                FROM (
                                    SELECT A.*, B.SCTP_CD, B.SCHL_CD
                                    FROM (
                                         SELECT A.*, B.GNDR, B.BDAY, B.VLG_CD
                                         FROM (
                                              SELECT
                                                     A.RCP_NO, A.CHRCP_NO, A.YEAR, A.RPT_STCD
                                                   , B.DNCTR_CD, B.CH_CD
                                                   , (SELECT IFNULL(CTR_CD, '') || '-' || IFNULL(BRC_CD, '') || IFNULL(PRJ_CD, '') || '-' || IFNULL(CH_CD, '') FROM CH_MST WHERE CHRCP_NO = B.BF_CHRCP_NO) AS BF_CHILD_CODE
                                                   , IFNULL(B.CTR_CD, '') || '-' || IFNULL(B.BRC_CD, '') || IFNULL(B.PRJ_CD, '') || '-' || IFNULL(B.CH_CD, '') AS CHILD_CODE
                                                   , REPLACE(REPLACE(IFNULL(B.CH_EFNM, '') || ' ' || IFNULL(B.CH_EMNM, '') || ' ' || IFNULL(B.CH_ELNM, ''), '  ', ' '), '  ', ' ') AS CHILD_NAME
                                              FROM (
                                                   SELECT RCP_NO, CHRCP_NO, YEAR, RPT_STCD
                                                   FROM RPT_BSC
                                                   WHERE RPT_DVCD = '1' AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                                              ) A
                                              INNER JOIN CH_MST B
                                              ON A.CHRCP_NO = B.CHRCP_NO AND B.DEL_YN != 'Y'
                                        ) A
                                        INNER JOIN CH_BSC B
                                        ON A.RCP_NO = B.RCP_NO
                                    ) A
                                    LEFT OUTER JOIN EDU B
                                    ON A.RCP_NO = B.RCP_NO
                                ) A
                                LEFT OUTER JOIN (
                                    SELECT * FROM CD WHERE GRP_CD = '65' AND CD != 0
                                ) B
                                ON A.RPT_STCD = B.CD
                            ) A
                            LEFT OUTER JOIN (
                                SELECT A.*, B.CD_ENM AS GMNY_STATUS_LABEL, B.REF_1 AS GMNY_STATUS_COLOR
                                FROM (
                                    SELECT A.MNG_NO AS GMNY_MNG_NO, A.RCP_NO AS GMNY_RCP_NO, SUBSTR(A.RCP_DT, 5, 2) AS GMNY_MM, A.RPT_STCD AS GMNY_STATUS_CODE, A.CHRCP_NO
                                    FROM (
                                        SELECT A.MNG_NO, B.RCP_NO, A.CHRCP_NO, A.RCP_DT, IFNULL(B.RPT_STCD, '16') AS RPT_STCD
                                             , (SELECT COUNT(*) FROM GMNY C WHERE C.CHRCP_NO = A.CHRCP_NO AND C.MNG_NO > A.MNG_NO ORDER BY C.MNG_NO DESC) AS R
                                        FROM GMNY A
                                        LEFT OUTER JOIN (
                                            SELECT A.*, B.GMNY_MNGNO
                                            FROM (
                                                SELECT RCP_NO, CHRCP_NO, RPT_STCD, YEAR
                                                FROM RPT_BSC
                                                WHERE RPT_DVCD = '5'
                                                AND RPT_STCD NOT IN ('98', '99')
                                                AND DEL_YN = 'N' AND FIDG_YN = 'Y'
                                            ) A
                                            INNER JOIN RPLY B
                                            ON A.RCP_NO = B.RCP_NO
                                        ) B
                                        ON A.CHRCP_NO = B.CHRCP_NO
                                        AND A.MNG_NO = B.GMNY_MNGNO
                                    ) A
                                    WHERE R = 0
                                ) A
                                LEFT OUTER JOIN (
                                    SELECT * FROM CD WHERE GRP_CD = '65' AND CD != 0
                                ) B
                                ON A.GMNY_STATUS_CODE = B.CD
                            ) B
                            ON A.CHRCP_NO = B.CHRCP_NO
                        ) A
                        LEFT OUTER JOIN (
                            SELECT A.*, B.CD_ENM AS LETR_STATUS_LABEL, B.REF_1 AS LETR_STATUS_COLOR
                            FROM (
                                SELECT A.MNG_NO AS LETR_MNG_NO, A.RCP_NO AS LETR_RCP_NO, SUBSTR(A.RCP_DT, 5, 2) AS LETR_MM, A.RPT_STCD AS LETR_STATUS_CODE, A.CHRCP_NO
                                FROM (
                                    SELECT A.MNG_NO, B.RCP_NO, A.CHRCP_NO, A.RCP_DT, IFNULL(B.RPT_STCD, '16') AS RPT_STCD
                                        , (SELECT COUNT(*) FROM LETR C WHERE C.CHRCP_NO = A.CHRCP_NO AND C.MNG_NO > A.MNG_NO ORDER BY C.MNG_NO DESC) AS R
                                    FROM LETR A
                                    LEFT OUTER JOIN (
                                        SELECT A.*, B.LETR_MNGNO
                                        FROM (
                                            SELECT RCP_NO, CHRCP_NO, RPT_STCD, YEAR
                                            FROM RPT_BSC
                                            WHERE RPT_DVCD = '5'
                                            AND RPT_STCD NOT IN ('98', '99')
                                            AND DEL_YN = 'N' AND FIDG_YN = 'Y'
                                        ) A
                                        INNER JOIN RPLY B
                                        ON A.RCP_NO = B.RCP_NO
                                    ) B
                                    ON A.CHRCP_NO = B.CHRCP_NO
                                    AND A.MNG_NO = B.LETR_MNGNO
                                ) A
                                WHERE R = 0
                            ) A
                            LEFT OUTER JOIN (
                                SELECT * FROM CD WHERE GRP_CD = '65' AND CD != 0
                            ) B
                            ON A.LETR_STATUS_CODE = B.CD
                        ) B
                        ON A.CHRCP_NO = B.CHRCP_NO
                    ) A
                ) A
                WHERE CHRCP_NO IS NOT NULL
            """

        if (!s.support.isNullOrBlank()) {
            sql = "$sql AND DNCTR_CD = '${s.support}'"
        }

        if (!s.status.isNullOrBlank()) {
            sql = when(s.status) {
                "4"-> {
                    "$sql AND STATUS_CODE IN ('4', '5', '6', '7', '8', '9', '10', '11')"
                }
                else -> {
                    "$sql AND STATUS_CODE = '${s.status}'"
                }
            }
        }

        if (!s.year.isNullOrBlank()) {
            sql = "$sql AND YEAR = '${s.year}'"
        }

        if (!s.village.isNullOrBlank()) {
            sql = "$sql AND VLG_CD = '${s.village}'"
        }

        if (!s.school.isNullOrBlank()) {
            sql = "$sql AND SCHL_CD = '${s.school}'"
        }

        if (!s.gender.isNullOrBlank()) {
            sql = "$sql AND GNDR = '${s.gender}'"
        }

        if (!s.ageFrom.isNullOrBlank()) {
            sql = "$sql AND AGE >= ${s.ageFrom}"
        }

        if (!s.ageTo.isNullOrBlank()) {
            sql = "$sql AND AGE <= ${s.ageTo}"
        }

        if (!s.case1.isNullOrBlank()) {
            sql = "$sql AND (CASE1 = '${s.case1}' OR CASE2 = '${s.case1}' OR CASE3 = '${s.case1}')"
        }

        if (!s.case2.isNullOrBlank()) {
            sql = "$sql AND (CASE1 = '${s.case2}' OR CASE2 = '${s.case2}' OR CASE3 = '${s.case2}')"
        }

        if (!s.case3.isNullOrBlank()) {
            sql = "$sql AND (CASE1 = '${s.case3}' OR CASE2 = '${s.case3}' OR CASE3 = '${s.case3}')"
        }

        if (!s.searchCode.isNullOrBlank()) {
            sql = "$sql AND (CHILD_CODE = '${s.searchText}' OR BF_CHILD_CODE = '${s.searchText}')"
        }

        if (!s.searchText.isNullOrBlank()) {
            s.searchText = s.searchText!!.replace("'", "")
            sql = "$sql AND (CHILD_NAME LIKE '%${s.searchText}%' OR CHILD_CODE LIKE '%${s.searchText}%' OR BF_CHILD_CODE LIKE '%${s.searchText}%')"
        }

        sql = """$sql
            ORDER BY (
                    CASE
                        WHEN RPT_STCD = '12' THEN 1
                        WHEN RPT_STCD = '2'  THEN 2
                        WHEN RPT_STCD = '15' THEN 3
                        WHEN RPT_STCD = '16' THEN 4
                        WHEN RPT_STCD = '4'  THEN 5
                        WHEN RPT_STCD = '5'  THEN 6
                        WHEN RPT_STCD = '6'  THEN 7
                        WHEN RPT_STCD = '7'  THEN 8
                        WHEN RPT_STCD = '8'  THEN 9
                        WHEN RPT_STCD = '13' THEN 10
                        WHEN RPT_STCD = '14' THEN 11
                        WHEN RPT_STCD = '1'  THEN 12
                        ELSE 99
                    END
                ), CHILD_CODE DESC, CHILD_NAME
        """

        val query = SimpleSQLiteQuery(sql)
        logger.debug("sql = ${query.sql}")

        val items = childlistDao.findAllChildList(query)
        logger.debug("findAllCif : ${items.size}")

        childlist.postValue(items)
    }

    // Service - SERVICE
    private fun findAllChildren(s: ChildlistSearchItem) {
        logger.debug("findAllChildren : $s")

        var sql = """
                SELECT *
                FROM (
                    SELECT A.*
                        , B.SCTP_CD
                        , B.SCHL_CD
                        , '${s.service}' AS SERVICE
                    FROM (
                        SELECT A.*
                            , (SELECT SPSL_CD FROM CH_SPSL_INFO WHERE RCP_NO = A.LAST_RCP_NO LIMIT 0, 1) AS CASE1
                            , (SELECT SPSL_CD FROM CH_SPSL_INFO WHERE RCP_NO = A.LAST_RCP_NO LIMIT 1, 1) AS CASE2
                            , (SELECT SPSL_CD FROM CH_SPSL_INFO WHERE RCP_NO = A.LAST_RCP_NO LIMIT 2, 1) AS CASE3
                            , B.GNDR
                            , B.BDAY
                            , B.VLG_CD
                            , (strftime('%Y','now','localtime') - SUBSTR(B.BDAY, 1, 4)) AS AGE
                        FROM (
                            SELECT
                                  IFNULL(B.CTR_CD, '') || '-' || IFNULL(B.BRC_CD, '') || IFNULL(B.PRJ_CD, '') || '-' || IFNULL(B.CH_CD, '') AS CHILD_CODE
                                , REPLACE(IFNULL(B.CH_EFNM, '') || ' ' || IFNULL(B.CH_EMNM, '') || ' ' || IFNULL(B.CH_ELNM, ''), '  ', ' ') AS CHILD_NAME
                                , B.CHRCP_NO
                                , B.DNCTR_CD
                                , (SELECT IFNULL(CTR_CD, '') || '-' || IFNULL(BRC_CD, '') || IFNULL(PRJ_CD, '') || '-' || IFNULL(CH_CD, '') FROM CH_MST WHERE CHRCP_NO = B.BF_CHRCP_NO) AS BF_CHILD_CODE
                                , (SELECT RCP_NO FROM RPT_BSC
                                   WHERE CHRCP_NO = B.CHRCP_NO AND RPT_DVCD IN ('1', '2') AND RPT_STCD = '1'
                                   AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                                   ORDER BY RPT_DVCD DESC, YEAR DESC LIMIT 1) AS LAST_RCP_NO
                            FROM (
                                SELECT CHRCP_NO
                                FROM RPT_BSC
                                WHERE RPT_DVCD = '1' AND RPT_STCD = '1'
                                AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                            ) A
                            INNER JOIN CH_MST B
                            ON A.CHRCP_NO = B.CHRCP_NO
                            AND B.DEL_YN != 'Y'
                        ) A
                        INNER JOIN CH_BSC B
                        ON A.LAST_RCP_NO = B.RCP_NO
                    ) A
                    LEFT OUTER JOIN EDU B
                    ON A.LAST_RCP_NO = B.RCP_NO
                ) A
                WHERE CHRCP_NO IS NOT NULL
            """

        if (!s.support.isNullOrBlank()) {
            sql = "$sql AND DNCTR_CD = '${s.support}'"
        }

        if (!s.village.isNullOrBlank()) {
            sql = "$sql AND VLG_CD = '${s.village}'"
        }

        if (!s.school.isNullOrBlank()) {
            sql = "$sql AND SCHL_CD = '${s.school}'"
        }

        if (!s.gender.isNullOrBlank()) {
            sql = "$sql AND GNDR = '${s.gender}'"
        }

        if (!s.ageFrom.isNullOrBlank()) {
            sql = "$sql AND AGE >= ${s.ageFrom}"
        }

        if (!s.ageTo.isNullOrBlank()) {
            sql = "$sql AND AGE <= ${s.ageTo}"
        }

        if (!s.case1.isNullOrBlank()) {
            sql = "$sql AND (CASE1 = '${s.case1}' OR CASE2 = '${s.case1}' OR CASE3 = '${s.case1}')"
        }

        if (!s.case2.isNullOrBlank()) {
            sql = "$sql AND (CASE1 = '${s.case2}' OR CASE2 = '${s.case2}' OR CASE3 = '${s.case2}')"
        }

        if (!s.case3.isNullOrBlank()) {
            sql = "$sql AND (CASE1 = '${s.case3}' OR CASE2 = '${s.case3}' OR CASE3 = '${s.case3}')"
        }

        if (!s.searchCode.isNullOrBlank()) {
            sql = "$sql AND (CHILD_CODE = '${s.searchText}' OR BF_CHILD_CODE = '${s.searchText}')"
        }

        if (!s.searchText.isNullOrBlank()) {
            s.searchText = s.searchText!!.replace("'", "")
            sql = "$sql AND (CHILD_NAME LIKE '%${s.searchText}%' OR CHILD_CODE LIKE '%${s.searchText}%' OR BF_CHILD_CODE LIKE '%${s.searchText}%')"
        }

        sql = "$sql ORDER BY CHILD_CODE DESC"

        val query = SimpleSQLiteQuery(sql)
        logger.debug("sql = ${query.sql}")

        val items = childlistDao.findAllChildList(query)

        childlist.postValue(items)
    }

    private var findAllSuggestionsItems: MutableLiveData<List<APP_SEARCH_HISTORY>> = MutableLiveData()
    fun findAllSuggestions(): LiveData<List<APP_SEARCH_HISTORY>> {
        findAllSuggestionsItems = MutableLiveData()

        Thread(Runnable {
            logger.debug("findAllSuggestions")
            findAllSuggestionsItems.postValue(childlistDao.findAllSuggestions())

        }).start()

        return findAllSuggestionsItems
    }

    fun deleteSuggestion(word: String) {
        Thread(Runnable {
            childlistDao.deleteSuggestion(word)
        }).start()
    }

    private lateinit var findAllVillageLocationItems: MutableLiveData<List<VillageLocation>>
    fun findAllVillageLocation(): MutableLiveData<List<VillageLocation>> {
        findAllVillageLocationItems = MutableLiveData()
        Thread(Runnable {
            val ctrCd = preferences.getString("user_ctr_cd", "")
            val brcCd = preferences.getString("user_brc_cd", "")
            val prjCd = preferences.getString("user_prj_cd", "")

            findAllVillageLocationItems.postValue(childlistDao.findAllVillageLocation(ctrCd, brcCd, prjCd))
        }).start()
        return findAllVillageLocationItems
    }
}