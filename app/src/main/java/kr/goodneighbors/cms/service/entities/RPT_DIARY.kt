package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.Expose

@Suppress("ClassName")
@Entity(tableName = RPT_DIARY.TABLE_NAME)
data class RPT_DIARY(
        @PrimaryKey
        @Expose var RCP_NO: String,

        @Expose var INVSTR_EXDT: Long ?= null,
        @Expose var PRJ_RCDT: Long ?= null,
        @Expose var PRJ_EXDT: Long ?= null,
        @Expose var BRC_RCDT: Long ?= null,
        @Expose var BRC_PSDT: Long ?= null,
        @Expose var BRC_XSDT: Long ?= null,
        @Expose var BRC_EXDT: Long ?= null,
        @Expose var BRC_RTDT: Long ?= null,
        @Expose var DNCTR_RCDT: Long ?= null,
        @Expose var DNCTR_PSDT: Long ?= null,
        @Expose var DNCTR_XSDT: Long ?= null,
        @Expose var DNCTR_RTDT: Long ?= null,
        @Expose var APRV_DWDT: Long ?= null,
        @Expose var RETN_DWDT: Long ?= null,
        @Expose var AID_RCDT: Long ?= null,
        @Expose var MBSH_SDDT: Long ?= null,
        @Expose var BRC_LAST_RCDT: Long ?= null,
        @Expose var BRC_LAST_XSDT: Long ?= null,
        @Expose var BRC_LAST_RTDT: Long ?= null,
        @Expose var DNCTR_LAST_RCDT: Long ?= null
) {
    companion object {
        const val TABLE_NAME = "RPT_DIARY"
    }
}