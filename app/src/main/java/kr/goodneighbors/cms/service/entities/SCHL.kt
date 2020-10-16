package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity

@Entity(tableName = SCHL.TABLE_NAME, primaryKeys = ["SCHL_CD", "CTR_CD", "BRC_CD", "PRJ_CD"])
data class SCHL(
        /** 학교 코드 */
        var SCHL_CD: String,

        /** 국가 코드 */
        var CTR_CD: String,

        /** 지부 코드 */
        var BRC_CD: String,

        /** 사업장 코드 */
        var PRJ_CD: String,

        /** 학교 명 */
        var SCHL_NM: String?,

        /** 학교 유형 코드 */
        var SCTP_CD: String?,

        /** 아동 수 */
        var GN_CH_NUM: String?,

        /** 연결 담당자 명 */
        var CTSTAF_NM: String?,

        /** 연결 담당자 전화번호 */
        var CTSTAF_TELNO: String?,

        var REGR_ID: String?,

        var REG_DT: Long?,

        var UPDR_ID: String?,

        var UPD_DT: Long?,

        var ACT_YN: String?,
        var CDNT_LAT: String?,
        var CDNT_LONG: String?,
        var VLG_CD: String?
) {
    companion object {
        const val TABLE_NAME = "SCHL"
    }
}