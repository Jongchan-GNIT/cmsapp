package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = INTV.TABLE_NAME)
data class INTV(
        @PrimaryKey
        @Expose var RCP_NO: String,

        @Expose var INTVR_NM: String? = null,
        @Expose var RSPN_NM: String? = null,
        @Expose var RSPN_RLCD: String? = null,
        @Expose var INTV_DT: String? = null,
        @Expose var INTPLC_CD: String? = null
) {
    companion object {
        const val TABLE_NAME = "INTV"
    }
}