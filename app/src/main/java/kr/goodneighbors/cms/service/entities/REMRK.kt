package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = REMRK.TABLE_NAME)
data class REMRK(
        @PrimaryKey
        @Expose var RCP_NO: String,

        @Expose var REMRK_LANG: String? = null,
        @Expose var REMRK_LOCLANG: String? = null,
        @Expose var REMRK_ENG: String? = null,
        @Expose var REMRK_KOR: String? = null,
        @Expose var REMRK_KOR_APR: String? = null,
        @Expose var REMRK_ENTRN: String? = null,
        @Expose var REMRK_ENTRN_APR: String? = null
) {
    companion object {
        const val TABLE_NAME = "REMRK"
    }
}