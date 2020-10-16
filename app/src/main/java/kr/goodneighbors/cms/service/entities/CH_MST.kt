package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import com.google.gson.annotations.Expose

@Suppress("ClassName")
@Entity(tableName = CH_MST.TABLE_NAME,
        primaryKeys = ["CHRCP_NO", "CTR_CD", "BRC_CD", "PRJ_CD"])
data class CH_MST(
        @Expose var CHRCP_NO: String,
        @Expose var CTR_CD: String,
        @Expose var BRC_CD: String,
        @Expose var PRJ_CD: String,

        @Expose var CH_CD: String? = null,
        @Expose var DNCTR_CD: String? = null,
        @Expose var CH_STCD: String? = null,
        @Expose var CH_KNM: String? = null,
        @Expose var CH_EFNM: String? = null,
        @Expose var CH_ELNM: String? = null,
        @Expose var CH_LLNM: String? = null,

        @Expose var REGR_ID: String? = null,
        @Expose var REG_DT: Long? = null,
        @Expose var UPDR_ID: String? = null,
        @Expose var UPD_DT: Long? = null,

        @Expose var CH_EMNM: String? = null,
        @Expose var DEL_YN: String? = null,

        @Expose var ORG_CHRCP_NO: String? = null,
        @Expose var BF_CHRCP_NO: String? = null,

        var APP_MODIFY_DATE: Long? = null
) {
    companion object {
        const val TABLE_NAME = "CH_MST"
    }
}