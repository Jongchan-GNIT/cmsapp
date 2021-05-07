package kr.goodneighbors.cms.service.model

import kr.goodneighbors.cms.service.entities.RPT_BSC

data class AclEditViewItem(
        var profile: ProfileHeaderItem ?= null,

        var lastYearTypeCode: String? = null,
        var lastYearType: String? = null,
        var lastYearGhostwriting: String? = null,

        var FILE_PATH: String ?= null,

        var PREV_RPT_BSC: RPT_BSC? = null,
        var PREV_ACL_RPT_BSC: RPT_BSC? = null,
        var RPT_BSC: RPT_BSC? = null,

        var returns: List<ReturnItem> ?= null,

        var INPUT_TYPE: List<SpinnerOption>? = null,
        var INPUT_RELEATIONSHIP_WITH_CHILD: List<SpinnerOption>? = null,
        var INPUT_REASON: List<SpinnerOption>? = null
)

data class AclEditViewItemSearch(
        var chrcp_no: String,
        var rcp_no: String? = null,
        var year: String
)

data class AclListItem(
        var CHRCP_NO: String ?= null,
        var YEAR: String ?= null,
        var RCP_NO: String ?= null,
        var RPT_STCD: String ?= null,
        var APRV_DT: String ?= null,
        var RPT_TYPE: String ?= null,
        var SWRT_YN: String ?= null,
        var RPT_TYPE_NM: String ?= null,
        var RPT_STNM: String ?= null,
        var GENERAL_FILE_PATH: String ?= null
)

data class AclImageItem(
        var path: String? = null
)
