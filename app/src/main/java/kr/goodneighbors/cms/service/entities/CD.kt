package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity

@Entity(tableName = CD.TABLE_NAME, primaryKeys = ["GRP_CD", "CD"])
data class CD(
        var GRP_CD: String,
        var CD: String,

        var GRPCD_KNM: String? = null,
        var GRPCD_ENM: String? = null,

        var CD_KNM: String? = null,
        var CD_ENM: String? = null,

        var EXPL: String? = null,

        var USE_YN: String? = null,

        var SORT_ORDR: Int? = null,

        var CHGP_CD: String? = null,

        var REGR_ID: String? = null,
        var REG_DT: Long? = null,
        var UPDR_ID: String? = null,
        var UPD_DT: Long? = null,

        var EXPL_ENG: String? = null,
        var REF_1: String? = null,
        var REF_2: String? = null,

        var CD_FRNM: String? = null,
        var CD_ESNM: String? = null
) {
    companion object {
        const val TABLE_NAME = "CD"
    }
}