package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import com.google.gson.annotations.Expose


@Suppress("ClassName")
@Entity(tableName = RPT_BSC.TABLE_NAME,
        primaryKeys = ["CHRCP_NO", "RCP_NO"])
data class RPT_BSC(
        @Expose var CHRCP_NO: String = "",
        @Expose var RCP_NO: String = "",

        @Expose var RPT_DVCD: String? = null,
        @Expose var YEAR: String? = null,
        @Expose var SPLY_MON: String? = null,
        @Expose var DEGR: Int? = null,
        @Expose var EXPT_YN: String? = null,
        @Expose var PSCRN_YN: String? = null,
        @Expose var XSCRN_YN: String? = null,
        @Expose var RPT_STCD: String? = null,
        @Expose var APRV_DT: String? = null,
        @Expose var FRCP_NO: String? = null,
        @Expose var FIDG_YN: String? = null,
        @Expose var DCMT_YN: String? = null,

        @Expose var REGR_ID: String? = null,
        @Expose var REG_DT: Long? = null,
        @Expose var UPDR_ID: String? = null,
        @Expose var UPD_DT: Long? = null,

        @Expose var CRM_IF_YN: String? = null,
        @Expose var LAST_UPD_DT: Long? = null,
        @Expose var DEL_YN: String? = null,

        @Expose var DROP_RCP_NO: String? = null,
        @Expose var UNABLE_REG_YN: String? = null,

        @Ignore var CH_MST: CH_MST ?= null,

        @Ignore
        @Expose var CH_BSC: CH_BSC? = null,

        @Ignore
        @Expose var FMLY: FMLY? = null,

        @Ignore
        @Expose var INTV: INTV? = null,

        @Ignore
        @Expose var LIV_COND: LIV_COND? = null,

        @Ignore
        @Expose var SWRT: SWRT? = null,

        @Ignore
        @Expose var HLTH: HLTH? = null,

        @Ignore
        @Expose var REMRK: REMRK? = null,

        @Ignore
        @Expose var EDU: EDU? = null,

        @Ignore
        @Expose var RPT_DIARY: RPT_DIARY? = null,

        @Ignore
        @Expose var ACL: ACL? = null,

        @Ignore
        @Expose var DROPOUT: DROPOUT? = null,

        @Ignore
        @Expose var RPLY: RPLY? = null,

        @Ignore
        @Expose var DROPOUT_PLAN: DROPOUT_PLAN? = null,

        @Ignore
        @Expose var GIFT_BRKDW: ArrayList<GIFT_BRKDW>? = null,

        @Ignore
        @Expose var SIBL: ArrayList<SIBL>? = null,

        @Ignore
        @Expose var ATCH_FILE: ArrayList<ATCH_FILE>? = null,

        @Ignore
        @Expose var RETN: ArrayList<RETN>? = null,

        @Ignore
        @Expose var CH_SPSL_INFO: ArrayList<CH_SPSL_INFO>? = null,

        @Ignore
        @Expose var PRSN_ANS_INFO: ArrayList<PRSN_ANS_INFO>? = null,

        var APP_MODIFY_DATE: Long? = null
) {
    companion object {
        const val TABLE_NAME = "RPT_BSC"
    }
}
