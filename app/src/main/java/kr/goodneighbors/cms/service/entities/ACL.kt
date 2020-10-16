package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import com.google.gson.annotations.Expose

@Entity(tableName = ACL.TABLE_NAME, primaryKeys = ["RCP_NO"])
data class ACL(
        @Expose var RCP_NO: String,

        @Expose var ENG_TRANSL: String ?= null,
        @Expose var KOR_TRANSL: String ?= null,
        @Expose var QCD_1ST: String ?= null,
        @Expose var QCD_2ND: String ?= null,
        @Expose var QCD_3RD: String ?= null,
        @Expose var QCD_4TH: String ?= null,
        @Expose var QCD_5TH: String ?= null,
        @Expose var QCD_6TH: String ?= null,
        @Expose var QCD_7TH: String ?= null,
        @Expose var QCD_8TH: String ?= null,
        @Expose var QCD_9TH: String ?= null,
        @Expose var QCD_10TH: String ?= null,
        @Expose var ACD_1ST: String ?= null,
        @Expose var ACD_2ND: String ?= null,
        @Expose var ACD_3RD: String ?= null,
        @Expose var ACD_4TH: String ?= null,
        @Expose var ACD_5TH: String ?= null,
        @Expose var ACD_6TH: String ?= null,
        @Expose var ACD_7TH: String ?= null,
        @Expose var ACD_8TH: String ?= null,
        @Expose var ACD_9TH: String ?= null,
        @Expose var ACD_10TH: String ?= null,
        @Expose var TRANR_NM: String ?= null,
        @Expose var REVW_NM: String ?= null,
        @Expose var TRNS_CONT: String ?= null,
        @Expose var RPT_TYPE: String ?= null
) {
    companion object {
        const val TABLE_NAME = "ACL"
    }
}