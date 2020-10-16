package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore

@Entity(tableName = LETR.TABLE_NAME, primaryKeys = ["CHRCP_NO", "MNG_NO"])
data class LETR(
        var CHRCP_NO: String = "",
        var MNG_NO: String = "",

        var RELSH_CD_OLD: String? = null,
        var RCP_DT: String? = null,
        var SEND_DT: String? = null,
        var RPLY_TMLMT: String? = null,
        /** 동봉물품 - ENCLOSED LIST VHCHAR(1000)*/
        var ENCLO_ARTCL: String? = null,

        var REGR_ID: String? = null,
        var REG_DT: Long? = null,
        var UPDR_ID: String? = null,
        var UPD_DT: Long? = null,

        /** SPONSER'S LETTER */
        var TRAN_ADCT: String? = null,
        var TRAN_DVCD: String? = null,
        var TRAN_STCD: String? = null,
        var TRANR_NM: String? = null,
        var REVW_NM: String? = null,
        var RELMEM_DNCTR_CD: String? = null,
        var RELSH_CD: String? = null,

        @Ignore
        var LETR_ATCH_FILE: List<LETR_ATCH_FILE>? = null
) {
    companion object {
        const val TABLE_NAME = "LETR"
    }
}