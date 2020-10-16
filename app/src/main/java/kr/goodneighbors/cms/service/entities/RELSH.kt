package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity

@Entity(tableName = RELSH.TABLE_NAME, primaryKeys = ["CHRCP_NO", "SEQ_NO"])
data class RELSH (
        var CHRCP_NO: String,
        var SEQ_NO: Int,

        var RELSH_CD_OLD: String?,

        var RELMEM_CD: String?,
        var RELMEM_NM: String?,
        var RELMEM_DNCTR_CD: String?,
        var RELSH_DT: String?,
        var RELCNL_DT: String?,
        var RELCNL_RNCD: String?,

        var REGR_ID: String?,
        var REG_DT: Long?,
        var UPDR_ID: String?,
        var UPD_DT: Long?,

        var RELSH_CD: String?
) {
    companion object {
        const val TABLE_NAME = "RELSH"
    }
}