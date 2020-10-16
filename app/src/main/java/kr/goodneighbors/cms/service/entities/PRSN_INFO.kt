package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = PRSN_INFO.TABLE_NAME)
data class PRSN_INFO(
        /** 일련번호 */
        @PrimaryKey
        var SEQ_NO: Int,
        
        /** 년도 */
        var YEAR: String?,

        /** 질문 코드 */
        var PRSN_CD: String?,

        /** 삭제여부 */
        var DEL_YN: String?,

        var REGR_ID: String?,
        var REG_DT: Long?,
        var UPDR_ID: String?,
        var UPD_DT: Long?
) {
    companion object {
        const val TABLE_NAME = "PRSN_INFO"
    }
}