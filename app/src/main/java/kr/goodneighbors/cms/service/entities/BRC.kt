package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity

@Entity(tableName = BRC.TABLE_NAME, primaryKeys = ["CTR_CD", "BRC_CD"])
data class BRC(
        var CTR_CD: String,
        var BRC_CD: String,

        var BRC_KNM: String?,
        var BRC_ENM: String?,

        var BMNG_KNM: String?,
        var BMNG_ENM: String?,

        var DEV_YN: String?,

        var BRC_SDT: String?,
        var BRC_EDT: String?,

        var MAIL: String?,

        var TELNO1: String?,
        var TEL1: String?,

        var TELNO2: String?,
        var TEL2: String?,

        var HMPG: String?,

        var ADDR: String?,

        var BRC_INTR: String?,

        var ORDR: Int?,

        var REGR_ID: String?,
        var REG_DT: Long?,
        var UPDR_ID: String?,
        var UPD_DT: Long?

) {
    companion object {
        const val TABLE_NAME = "BRC"
    }
}