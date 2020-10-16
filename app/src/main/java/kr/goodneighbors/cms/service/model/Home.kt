package kr.goodneighbors.cms.service.model

import android.arch.persistence.room.Ignore
import kr.goodneighbors.cms.service.entities.NOTI_INFO

data class HomeItem(
        var CHILD_COUNT: Int? = null,
        var LAST_UPDATE_DATE: Long? = null,

        var HAS_NEW_NOTICE: String? = null,

        @Ignore var CIF_STATE: HomeState? = null,
        @Ignore var APR_STATE: HomeState? = null,
        @Ignore var ACL_STATE: HomeState? = null,
        @Ignore var GML_STATE: HomeState? = null,

        @Ignore var NOTI_ITEMS: List<NOTI_INFO>? = null
)

data class HomeState(
        var E_COUNT: String? = null,
        var P_COUNT: String? = null,
        var PC: String? = null,
        var FROM_DATE: String? = null,
        var TO_DATE: String? = null
)

data class NoticeItem(
        var SEQ_NO: String? = null,
        var CTGY_CD: String? = null,
        var TTL: String? = null,
        var CTS: String? = null,
        var REG_DT: Long? = null,
        var CTGY_NM: String? = null,

        @Ignore var IS_SELECTED: Boolean = false
)