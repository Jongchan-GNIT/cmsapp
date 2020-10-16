package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity

@Entity(tableName = PRJ.TABLE_NAME, primaryKeys = ["CTR_CD", "BRC_CD", "PRJ_CD"])
data class PRJ (
        var CTR_CD: String,
        var BRC_CD: String,
        var PRJ_CD: String,

        var PRJ_KNM: String?,
        var PRJ_ENM: String?,

        var ADDR: String?,

        var PRJ_INTR: String?,

        var STAF_NUM: String?,

        var CPTL_DIST: String?,

        var TRFFMN_CD: String?,

        var PRJ_SDT: String?,
        var PRJ_EDT: String?,

        var END_RSN: String?,

        var RELBSN_YN: String?,

        var NOREL_DSDT: String?,

        var GMNY_YN: String?,

        var PRJ_MAIL: String?,

        var RELCHIF_NM: String?,

        var RELCHIF_TELNO: String?,

        var RELSTAF_MAIL: String?,

        var RELSTAF_NM: String?,

        var RELSTAF_TELNO: String?,

        var REGR_ID: String?,
        var REG_DT: Long?,
        var UPDR_ID: String?,
        var UPD_DT: Long?
) {
    companion object {
        const val TABLE_NAME = "PRJ"
    }
}