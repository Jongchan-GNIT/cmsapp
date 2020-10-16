package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = RPLY.TABLE_NAME)
data class RPLY(
        @PrimaryKey
        @Expose var RCP_NO: String,

        /** GM 일때 */
        @Expose var GMNY_MNGNO: String? = null,
        /** L 일때 */
        @Expose var LETR_MNGNO: String? = null,

        /** 결연회원코드 */
        @Expose var RELSH_CD: String? = null,
        /** 방문일자 */
        @Expose var VISIT_DT: String? = null,
        /** 방문자명(USER_NM) */
        @Expose var VSTR_NM: String? = null,
        /** USER_INFO.AUTH_CD 33000?(4->1, 5->4) */
        @Expose var VSTP_CD: String? = null,

        @Expose var QCD_1ST: String? = null,
        @Expose var QCD_2ND: String? = null,
        @Expose var QCD_3RD: String? = null,
        @Expose var QCD_4TH: String? = null,
        @Expose var QCD_5TH: String? = null,
        @Expose var QCD_6TH: String? = null,
        @Expose var QCD_7TH: String? = null,
        @Expose var QCD_8TH: String? = null,
        @Expose var QCD_9TH: String? = null,
        @Expose var QCD_10TH: String? = null,
        @Expose var ACD_1ST: String? = null,
        @Expose var ACD_2ND: String? = null,
        @Expose var ACD_3RD: String? = null,
        @Expose var ACD_4TH: String? = null,
        @Expose var ACD_5TH: String? = null,
        @Expose var ACD_6TH: String? = null,
        @Expose var ACD_7TH: String? = null,
        @Expose var ACD_8TH: String? = null,
        @Expose var ACD_9TH: String? = null,
        @Expose var ACD_10TH: String? = null,
        @Expose var CH_ADCT: String? = null,
        /** L 일때 , GM일때 NULL */
        @Expose var CHRPL_ENG: String? = null,
        @Expose var CHRPL_KOR: String? = null,
        @Expose var EXRATE: Double? = null,
        @Expose var TRANR_NM: String? = null,
        @Expose var REVW_NM: String? = null,
        @Expose var TRNS_CONT: String? = null
) {
    companion object {
        const val TABLE_NAME = "RPLY"
    }
}