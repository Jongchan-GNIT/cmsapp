package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.Expose

@Suppress("ClassName")
@Entity(tableName = LIV_COND.TABLE_NAME)
data class LIV_COND(
        @PrimaryKey
        @Expose var RCP_NO: String,

        @Expose var HHTP_CD: String?,
        @Expose var HHTP_ETC: String?,
        @Expose var LHTP_CD: String?,
        @Expose var LHTP_ETC: String?,
        @Expose var SHTP_CD: String?,
        @Expose var SHTP_ETC: String?,
        @Expose var HMAT_CD: String?,
        @Expose var HMAT_ETC: String?,
        @Expose var WDWS_IMPYN: String?,
        @Expose var WDWS_CD: String?,
        @Expose var WDWS_ETC: String?,
        @Expose var DDWS_IMPYN: String?,
        @Expose var DDWS_CD: String?,
        @Expose var DDWS_ETC: String?,
        @Expose var WTCR_CD: String?,
        @Expose var WTCR_ETC: String?,
        @Expose var TWAY_CD: String?,
        @Expose var TWAY_ETC: String?,
        @Expose var ELTP_CD: String?,
        @Expose var ELTP_ETC: String?,
        @Expose var TLUT_CD: String?,
        @Expose var TLUT_ETC: String?,
        @Expose var TLTP_CD: String?,
        @Expose var TLTP_ETC: String?
) {
    companion object {
        const val TABLE_NAME = "LIV_COND"
    }
}