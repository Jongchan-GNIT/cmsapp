package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import com.google.gson.annotations.Expose

@Suppress("ClassName")
@Entity(tableName = GIFT_BRKDW.TABLE_NAME, primaryKeys = ["RCP_NO", "SEQ_NO"])
data class GIFT_BRKDW(
        @Expose var RCP_NO: String,
        @Expose var SEQ_NO: Int,

        /** 대분류 */
        @Expose var GIFT_BCD: String?,
        /** 소분류 */
        @Expose var GIFT_SCD: String?,
        /** 소분류 코드의 CD.CD_ENM */
        @Expose var GIFT_DTBD: String?,
        /** 항목없음, 필드 입력값 없음, 사용 안함 */
        @Expose var GIFT_LAMT: Double?=null,
        /** 달러 금액 */
        @Expose var GIFT_DAMT: Double?=null,
        /** 개수 **/
        @Expose var GIFT_NUM: String?=null
) {
    companion object {
        const val TABLE_NAME = "GIFT_BRKDW"
    }
}