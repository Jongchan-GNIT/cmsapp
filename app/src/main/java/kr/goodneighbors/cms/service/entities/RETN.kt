package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import com.google.gson.annotations.Expose

/**
 * 리턴 사유 그룹코드
 * CIF, APR -> GRP_CD = 111
 * ACL, GML -> GRP_CD = 207
 * DROP-OUT -> GRP_CD = 214
 */
@Entity(tableName = RETN.TABLE_NAME, primaryKeys = ["RCP_NO", "SEQ_NO", "RETN_CNT"])
data class RETN(
        /** 접수번호 */
        @Expose var RCP_NO: String,

        /** 리턴항목구분 - 고정 2 */
        @Expose var RETN_ITCD: String?,
        /** 리턴사유 - 대분류 */
        @Expose var RTRN_BCD: String?,
        /** 리턴상세 사유 */
        @Expose var RTRN_SCD: String?,
        /** 리턴 리마크 */
        @Expose var RTRN_DETL: String?,
        /** 순번 */
        @Expose var SEQ_NO: Int,
        /** 차수 - max 값 사용 */
        @Expose var RETN_CNT: Int
) {
    companion object {
        const val TABLE_NAME = "RETN"
    }
}