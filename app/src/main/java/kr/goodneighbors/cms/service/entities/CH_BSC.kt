package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.Expose

@Suppress("ClassName")
@Entity(tableName = CH_BSC.TABLE_NAME)
data class CH_BSC(
        @PrimaryKey
        @Expose var RCP_NO: String,

        @Expose var GNDR: String? = null,
        @Expose var GNDR_CRCD: String? = null,
        @Expose var BDAY: String? = null,
        @Expose var BDAY_ESYN: String? = null,
        @Expose var BDAY_CRCD: String? = null,
        @Expose var BCTF_YN: String? = null,
        @Expose var HS_ADDR: String? = null,
        @Expose var TEL_NO: String? = null,
        @Expose var PRSN: String? = null,
        @Expose var TOBE_CD: String? = null,
        @Expose var HOBY_CD: String? = null,
        @Expose var LABR_YN: String? = null,
        @Expose var SMNG_NCYN: String? = null,
        @Expose var CH_EFNM: String? = null,
        @Expose var CH_ELNM: String? = null,
        @Expose var VLG_CD: String? = null,
        @Expose var AGE: String? = null,
        @Expose var HS_ADDR_DTL: String? = null,
        @Expose var LAST_RPT_YN: String? = null,
        @Expose var CHRCP_NO: String? = null
) {
    companion object {
        const val TABLE_NAME = "CH_BSC"
    }
}