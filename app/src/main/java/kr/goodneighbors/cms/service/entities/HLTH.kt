package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = HLTH.TABLE_NAME)
data class HLTH(
        @PrimaryKey
        @Expose var RCP_NO: String,

        @Expose var HGHT: Int? = null,
        @Expose var WGHT: Int? = null,
        @Expose var BDLVL_CD: String? = null,
        @Expose var HGHT_RNCD: String? = null,
        @Expose var WGHT_RNCD: String? = null,
        @Expose var ILNS_YN: String? = null,
        @Expose var ILNS_CD: String? = null,
        @Expose var ILNS_ESYN: String? = null,
        @Expose var ETC_ILNS: String? = null,
        @Expose var ILNS_RNCD: String? = null,
        @Expose var ILAQ_CD: String? = null,
        @Expose var ILAA_CD: String? = null,
        @Expose var IRAQ_CD: String? = null,
        @Expose var IRAA_CD: String? = null,
        @Expose var DISB_YN: String? = null,
        @Expose var DISB_CD: String? = null,
        @Expose var DISB_ESYN: String? = null,
        @Expose var DISB_RNCD: String? = null,
        @Expose var DBAQ_CD: String? = null,
        @Expose var DBAA_CD: String? = null,
        @Expose var DRAQ_CD: String? = null,
        @Expose var DRAA_CD: String? = null,
        @Expose var BMI_CD: String? = null
) {
    companion object {
        const val TABLE_NAME = "HLTH"
    }
}