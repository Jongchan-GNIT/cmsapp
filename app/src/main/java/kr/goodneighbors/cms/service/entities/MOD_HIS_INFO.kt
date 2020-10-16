package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import com.google.gson.annotations.Expose

@Entity(tableName = MOD_HIS_INFO.TABLE_NAME, primaryKeys = ["SEQ_NO", "INIT_TYPE"])
data class MOD_HIS_INFO(
        /** 일련번호 NUMBER */
        @Expose var SEQ_NO: Int,
        /** 진입구분 IMEI varchar2(20) */
        @Expose var INIT_TYPE: String,
        /** 접수번호 varchar2(21) */
        @Expose var RCP_NO: String,
        /** 등록/조회/수정/삭제 구분 varchar2(1) */
        @Expose var CRUD: String,
        /** 등록자 */
        @Expose var REGR_ID: String,
        /** 등록일 */
        @Expose var REG_DT: Long
) {
    companion object {
        const val TABLE_NAME = "MOD_HIS_INFO"
    }
}