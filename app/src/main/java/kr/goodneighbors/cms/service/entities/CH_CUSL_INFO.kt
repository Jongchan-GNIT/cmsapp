@file:Suppress("ClassName")

package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import com.google.gson.annotations.Expose

/** 아동 상담정보 테이블 */
@Entity(tableName = CH_CUSL_INFO.TABLE_NAME, primaryKeys = ["CHRCP_NO", "CRT_TP", "SEQ_NO"])
data class CH_CUSL_INFO(
        /** 아동접수 번호 */
        @Expose var CHRCP_NO: String,

        /** 생성 구분 */
        @Expose var CRT_TP: String = "W",

        /** 일련번호 */
        @Expose var SEQ_NO: Long,

        /** 특별관리 접수번호
         * 최신 보고서(1, 2) 스페셜 케이스가 있을 경우 리포트 rcp_no
         */
        @Expose var SPRCP_NO: String? = null,
        /** 인터뷰 담당자 - 로그인 사용자 이름*/
        @Expose var INTVR_NM: String? = null,
        /** 인터뷰 장소 코드 */
        @Expose var INTPLC_CD: String? = null,
        /** 응답자 관계 코드 */
        @Expose var RSPN_RLCD: String? = null,
        /** 상담 내용 - 4000byte */
        @Expose var CUSL_CTS: String? = null,
        /** 상담 일자 - today(yyyyMMdd) */
        @Expose var CUSL_DT: String? = null,
        /** 매니저 의견 */
        @Expose var MNG_OPN: String? = null,
        /** 상담 상태코드 TS or WACDP */
        @Expose var CUSL_STCD: String? = null,
        /** 이미지 파일경로 */
        @Expose var IMG_FP: String? = null,
        /** 이미지 파일명 */
        @Expose var IMG_NM: String? = null,
        /** 삭제 여부 */
        @Expose var DEL_YN: String? = "N",


        @Expose var REGR_ID: String? = null,
        @Expose var REG_DT: Long? = null,
        @Expose var UPDR_ID: String? = null,
        @Expose var UPD_DT: Long? = null,

        var APP_MODIFY_DATE: Long? = null
) {
    companion object {
        const val TABLE_NAME = "CH_CUSL_INFO"
    }
}