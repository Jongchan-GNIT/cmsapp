package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity

/** 마을 정보 */
@Entity(tableName = VLG.TABLE_NAME, primaryKeys = ["CTR_CD", "BRC_CD", "PRJ_CD", "VLG_CD"])
data class VLG(
        /** PK 국가 코드 */
        var CTR_CD: String,

        /** PK 지부 코드 */
        var BRC_CD: String,

        /** PK 사업장 코드 */
        var PRJ_CD: String,

        /** PK 마을 코드 */
        var VLG_CD: String,


        /** 마을 명 */
        var VLG_NM: String?,

        /** 주소 */
        var ADDR: String?,

        /** 사업 관계 여부 */
        var RL_BSN_YN: String?,

        /** 학교 수 */
        var SC_NUM: String?,

        /** 거리 */
        var DIST_CPT: String?,

        /** 아동 수 */
        var GN_CH_NUM: String?,

        /** 선물금 여부 */
        var GMNY_YN: String?,

        var REGR_ID: String?,
        var REG_DT: Long?,
        var UPDR_ID: String?,
        var UPD_DT: Long?,

        /** 위도 */
        var CDNT_LAT: String?,

        /** 경도 */
        var CDNT_LONG: String?,

        var ACT_YN: String?
) {
    companion object {
        const val TABLE_NAME = "VLG"
    }
}