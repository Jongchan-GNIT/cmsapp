package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = CTR.TABLE_NAME)
data class CTR (
        @PrimaryKey
        var CTR_CD: String,

        var CTNT_CD: String?,

        var CTR_KNM: String?,

        var CTR_ENM: String?,

        var CPTL: String?,

        var TMDF: Int?,

        var REGR_ID: String?,
        var REG_DT: Long?,
        var UPDR_ID: String?,
        var UPD_DT: Long?,

        var GRTNG: String?
) {
    companion object {
        const val TABLE_NAME = "CTR"
    }
}