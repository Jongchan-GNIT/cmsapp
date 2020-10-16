package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import com.google.gson.annotations.Expose

/** 개인질문 답변 정보 테이블 */
@Suppress("ClassName")
@Entity(tableName = PRSN_ANS_INFO.TABLE_NAME, primaryKeys = ["RCP_NO", "PRSN_CD"])
data class PRSN_ANS_INFO (
        /** 접수번호 */
        @Expose var RCP_NO: String,

        /** 질문 코드 */
        @Expose var PRSN_CD: String,

        /** 답변 코드 */
        @Expose var ANS_CD: String? = null,

        @Expose var REGR_ID: String? = null,
        @Expose var REG_DT: Long? = null,
        @Expose var UPDR_ID: String? = null,
        @Expose var UPD_DT: Long? = null
) {
    companion object {
        const val TABLE_NAME = "PRSN_ANS_INFO"
    }
}