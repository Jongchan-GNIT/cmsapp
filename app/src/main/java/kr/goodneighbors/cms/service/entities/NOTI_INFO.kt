package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = NOTI_INFO.TABLE_NAME)
data class NOTI_INFO (
        @PrimaryKey
        var SEQ_NO: Int,

        var CTGY_CD: String?,

        var TTL: String?,

        var CTS: String?,

        var VIEW_NUM: Int?,

        var REGR_ID: String?,
        var REG_DT: Long?,
        var UPDR_ID: String?,
        var UPD_DT: Long?
) {
    companion object {
        const val TABLE_NAME = "NOTI_INFO"
    }
}