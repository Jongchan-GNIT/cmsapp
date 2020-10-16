package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import com.google.gson.annotations.Expose

@Entity(tableName = SRVC.TABLE_NAME, primaryKeys = ["CHRCP_NO", "SEQ_NO", "CRT_TP"])
data class SRVC(
        @Expose var CHRCP_NO: String,
        @Expose var SEQ_NO: Int,

        @Expose var SVOBJ_DVCD: String ?= null,
        @Expose var BSST_CD: String ?= null,
        @Expose var SPBD_CD: String ?= null,
        @Expose var SPBD_DETL: String ?= null,
        @Expose var PRVD_DT: String ?= null,

        @Expose var REGR_ID: String ?= null,
        @Expose var REG_DT: Long ?= null,
        @Expose var UPDR_ID: String ?= null,
        @Expose var UPD_DT: Long ?= null,

        @Expose var CRT_TP: String,
        @Expose var DEL_YN: String ?= null,

        var APP_MODIFY_DATE: Long? = null
) {
    companion object {
        const val TABLE_NAME = "SRVC"
    }
}