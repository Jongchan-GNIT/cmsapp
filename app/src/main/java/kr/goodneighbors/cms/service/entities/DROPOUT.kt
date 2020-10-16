package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = DROPOUT.TABLE_NAME)
data class DROPOUT(
        @PrimaryKey
        @Expose var RCP_NO: String,

        @Expose var DROP_RNCD: String?= null,
        @Expose var MVLC_DIST: String?= null,
        @Expose var MVLC_NM: String?= null,
        @Expose var DROP_DT: String?= null,
        @Expose var DTH_RNCD: String?= null,
        @Expose var WORK_CD: String?= null,
        @Expose var DTH_DTL_RNCD: String?= null
) {
    companion object {
        const val TABLE_NAME = "DROPOUT"
    }
}