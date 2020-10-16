package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = SWRT.TABLE_NAME)
data class SWRT(
        @PrimaryKey
        @Expose var RCP_NO: String,

        @Expose var SWRT_YN: String ?= null,
        @Expose var SWRT_RNCD: String ?= null,
        @Expose var ATGRP_CMYN: String ?= null,
        @Expose var SWRTR_NM: String ?= null,
        @Expose var SWRTR_RLCD: String ?= null,
        @Expose var SWRTR_CFYN: String ?= null,
        @Expose var SWRTR_CRCD: String ?= null
) {
    companion object {
        const val TABLE_NAME = "SWRT"
    }
}