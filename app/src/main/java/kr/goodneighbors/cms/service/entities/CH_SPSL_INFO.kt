package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import com.google.gson.annotations.Expose

/** 아동 특별관리 정보 테이블 */
@Suppress("ClassName")
@Entity(tableName = CH_SPSL_INFO.TABLE_NAME, primaryKeys = ["RCP_NO", "SPSL_CD"])
data class CH_SPSL_INFO (
        /** 접수번호 */
        @Expose var RCP_NO: String,

        /** 특별관리 코드 */
        @Expose var SPSL_CD: String
) {
    companion object {
        const val TABLE_NAME = "CH_SPSL_INFO"
    }
}