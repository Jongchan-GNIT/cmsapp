package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = EDU.TABLE_NAME)
data class EDU(
        @PrimaryKey
        @Expose var RCP_NO: String,

        @Expose var SCTP_CD: String? = null,
        @Expose var SCTP_ETC: String? = null,
        @Expose var PRSCH_RNCD: String? = null,
        @Expose var PRSCH_RFYN: String? = null,
        @Expose var SCAQ1_CD: String? = null,
        @Expose var SCAA1_CD: String? = null,
        @Expose var SCAQ2_CD: String? = null,
        @Expose var SCAA2_CD: String? = null,
        @Expose var SCHL_ENM: String? = null,
        @Expose var SCHL_KNM: String? = null,
        @Expose var GRAD: String? = null,
        @Expose var ATTRT_STCD: String? = null,
        @Expose var ATTRT_PRCD: String? = null,
        @Expose var PRFSUB_CD: String? = null,
        @Expose var SCHL_CD: String? = null
) {
    companion object {
        const val TABLE_NAME = "EDU"
    }
}