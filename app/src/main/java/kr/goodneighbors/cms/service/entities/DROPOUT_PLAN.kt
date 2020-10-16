package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.Expose

/** 퇴소 계획 테이블 */
@Suppress("ClassName")
@Entity(tableName = DROPOUT_PLAN.TABLE_NAME)
data class DROPOUT_PLAN(
        /** 접수번호 */
        @PrimaryKey
        @Expose var RCP_NO: String,

        /** 퇴소계획여부 */
        @Expose var PLAN_YN: String? = null,
        /** 계획 코드 */
        @Expose var FTPLN_CD: String? = null,
        /** 계획상세 */
        @Expose var FTPLN_DTL: String? = null,
        /** 지속지원사유코드 */
        @Expose var CTNSPN_RNCD: String? = null,
        /** 지속지원상세 */
        @Expose var CTNSPN_DTL: String? = null
) {
    companion object {
        const val TABLE_NAME = "DROPOUT_PLAN"
    }
}