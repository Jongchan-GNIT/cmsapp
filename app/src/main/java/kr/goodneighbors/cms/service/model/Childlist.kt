package kr.goodneighbors.cms.service.model

data class ChildlistSearchItem(
        var support: String? = null,
        var service: String? = null,
        var year: String? = null,
        var status: String? = null,
        var village: String? = null,
        var school: String? = null,
        var gender: String? = null,
        var ageFrom: String? = null,
        var ageTo: String? = null,
        var case1: String? = null,
        var case2: String? = null,
        var case3: String? = null,
        var dropoutExpected: Boolean = false,
        var searchText: String? = null,
        var searchCode: String? = null
)

data class ChildlistItem(
        var SERVICE: String,
        var ROWNUM: Int,

        var CHILD_CODE: String,
        var CHILD_NAME: String,
        var AGE: Int,
        var CH_CD: String ?= null,
        var CH_STCD: String ?= null,

        var CASE1: String ?= null,
        var CASE2: String ?= null,
        var CASE3: String ?= null,

        var RCP_NO: String ?= null,
        var CHRCP_NO: String ?= null,
        var YEAR: String ?= null,

        var STATUS_CODE: String ?= null,
        var STATUS_LABEL: String ?= null,
        var STATUS_COLOR: String ?= null,

        var GMNY_MNG_NO: String ?= null,
        var GMNY_RCP_NO: String ?= null,
        var GMNY_MM: String ?= null,
        var GMNY_STATUS_CODE: String ?= null,
        var GMNY_STATUS_LABEL: String ?= null,
        var GMNY_STATUS_COLOR: String ?= null,

        var LETR_MNG_NO: String ?= null,
        var LETR_RCP_NO: String ?= null,
        var LETR_MM: String ?= null,
        var LETR_STATUS_CODE: String ?= null,
        var LETR_STATUS_LABEL: String ?= null,
        var LETR_STATUS_COLOR: String ?= null,

        var IS_SELECTED: Boolean = false

)