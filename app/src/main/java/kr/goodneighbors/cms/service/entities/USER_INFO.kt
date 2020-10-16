package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = USER_INFO.TABLE_NAME)
data class USER_INFO(
        /** 사용자 아이디 */
        @PrimaryKey
        var ID: String,

        /** 패스워드 */
        var PW: String,

        /** 사용자 명 **/
        var NM: String? = null,

        /** 국가 코드 */
        var CTR_CD: String? = null,

        /** 지부 코드 */
        var BRC_CD: String? = null,

        /** 사업장 코드 */
        var PRJ_CD: String? = null,

        /** 권한 코드 */
        var AUTH_CD: String? = null,

        /** 생일 */
        var BIRTH: String? = null,

        /** 이메일 */
        var EMAIL: String? = null,

        /** 사용여부 */
        var USE_YN: String? = null,

        var REGR_ID: String? = null,
        var REG_DT: Long? = null,
        var UPDR_ID: String? = null,
        var UPD_DT: Long? = null,

        /** 모금국가 코드 */
        var DNCTR_CD: String? = null,

        /** 패스워드 확인 */
        var PW_CONF: String? = null
) {
    companion object {
        const val TABLE_NAME = "USER_INFO"
    }
}