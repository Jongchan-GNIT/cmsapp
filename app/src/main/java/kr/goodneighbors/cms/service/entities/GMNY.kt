package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity

@Entity(tableName = GMNY.TABLE_NAME, primaryKeys = ["CHRCP_NO", "MNG_NO"])
data class GMNY(
        var CHRCP_NO: String,
        var MNG_NO: String,

        var RELSH_CD_OLD: String?,

        var RCP_DT: String?,
        var SEND_DT: String?,
        var RPLY_TMLMT: String?,

        /** GIFT MONEY */
        var GIFT_DAMT: Int?,

        var GIFT_AMT: Int?,
        var EXRATE: Int?,

        /** SPONSOR'S MESSAGE */
        var MBSH_REQ: String?,

        var MV_DT: String?,
        var AM_CHRCP_NO: String?,
        var RFND_DT: String?,


        var REGR_ID: String?,
        var REG_DT: Long?,
        var UPDR_ID: String?,
        var UPD_DT: Long?,

        var RELMEM_DNCTR_CD: String?,

        /** 결연 회원 코드 -> RELSH TABLE 조회 */
        var RELSH_CD: String?
) {
    companion object {
        const val TABLE_NAME = "GMNY"
    }
}