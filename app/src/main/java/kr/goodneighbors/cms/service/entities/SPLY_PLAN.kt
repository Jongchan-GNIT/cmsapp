package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity

@Entity(tableName = SPLY_PLAN.TABLE_NAME, primaryKeys = ["DNCTR_CD", "CTR_CD", "BRC_CD", "PRJ_CD", "YEAR"])
data class SPLY_PLAN(
        var DNCTR_CD: String,
        var CTR_CD: String,
        var BRC_CD: String,
        var PRJ_CD: String,
        var YEAR: String,

        var JAN: String?,
        var FEB: String?,
        var MAR: String?,
        var APRL: String?,
        var MAY: String?,
        var JUNE: String?,
        var JULY: String?,
        var AUG: String?,
        var SEPT: String?,
        var OCT: String?,
        var NOV: String?,
        var DEC: String?,
        var ETC: String?,
        var REMRK: String?,

        var REGR_ID: String?,
        var REG_DT: Long?,
        var UPDR_ID: String?,
        var UPD_DT: Long?
) {
    companion object {
        const val TABLE_NAME = "SPLY_PLAN"
    }
}