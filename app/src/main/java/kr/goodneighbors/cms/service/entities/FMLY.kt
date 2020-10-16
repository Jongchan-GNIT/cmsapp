package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = FMLY.TABLE_NAME)
data class FMLY(
        @PrimaryKey
        @Expose var RCP_NO: String,

        @Expose var GFA_NUM: String? = null,
        @Expose var GMO_NUM: String? = null,
        @Expose var FA_NUM: String? = null,
        @Expose var BIOFA_YN: String? = null,
        @Expose var MO_NUM: String? = null,
        @Expose var BIOMO_YN: String? = null,
        @Expose var EBRO_NUM: String? = null,
        @Expose var YBRO_NUM: String? = null,
        @Expose var ESIS_NUM: String? = null,
        @Expose var YSIS_NUM: String? = null,
        @Expose var GFA_LTYN: String? = null,
        @Expose var GMO_LTYN: String? = null,
        @Expose var FA_LTYN: String? = null,
        @Expose var MO_LTYN: String? = null,
        @Expose var EBRO_LTNUM: String? = null,
        @Expose var YBRO_LTNUM: String? = null,
        @Expose var ESIS_LTNUM: String? = null,
        @Expose var YSIS_LTNUM: String? = null,
        @Expose var RLTV_LTNUM: String? = null,
        @Expose var NOFMLY_LTNUM: String? = null,
        @Expose var FA_LARCD: String? = null,
        @Expose var FA_LRAQCD: String? = null,
        @Expose var FA_LRAACD: String? = null,
        @Expose var MO_LARCD: String? = null,
        @Expose var MO_LRAQCD: String? = null,
        @Expose var MO_LRAACD: String? = null,
        @Expose var GFA_OPCD: String? = null,
        @Expose var GMO_OPCD: String? = null,
        @Expose var FA_OPCD: String? = null,
        @Expose var MO_OPCD: String? = null,
        @Expose var GFA_ILYN: String? = null,
        @Expose var GMO_ILYN: String? = null,
        @Expose var FA_ILYN: String? = null,
        @Expose var MO_ILYN: String? = null,
        @Expose var GFA_DBYN: String? = null,
        @Expose var GMO_DBYN: String? = null,
        @Expose var FA_DBYN: String? = null,
        @Expose var MO_DBYN: String? = null,
        @Expose var GFA_ILCD: String? = null,
        @Expose var GMO_ILCD: String? = null,
        @Expose var FA_ILCD: String? = null,
        @Expose var MO_ILCD: String? = null,
        @Expose var GFA_DBCD: String? = null,
        @Expose var GMO_DBCD: String? = null,
        @Expose var FA_DBCD: String? = null,
        @Expose var MO_DBCD: String? = null,
        @Expose var MON_INCM: String? = null,
        @Expose var MICM_RLCD: String? = null,
        @Expose var MICM_OPCD: String? = null,
        @Expose var MICM_DFYN: String? = null,
        @Expose var GFA_NM: String? = null,
        @Expose var GMO_NM: String? = null,
        @Expose var FA_NM: String? = null,
        @Expose var MO_NM: String? = null,
        @Expose var MGDN_CD: String? = null,
        @Expose var MGDN_NM: String? = null
) {
    companion object {
        const val TABLE_NAME = "FMLY"
    }
}