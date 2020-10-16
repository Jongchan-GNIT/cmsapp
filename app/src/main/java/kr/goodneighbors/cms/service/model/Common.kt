package kr.goodneighbors.cms.service.model

data class ProfileHeaderItem(
        var CHRCP_NO: String ?= null,
        var RCP_NO: String ?= null,
        var RPT_DVCD: String ?= null,
        var YEAR: String ?= null,
        var APRV_DT: String ?= null,
        var CHILD_NAME: String ?= null,
        var CHILD_CODE: String ?= null,
        var CH_STCD: String ?= null,
        var GNDR: String ?= null,
        var BDAY: String ?= null,
        var VLG_CD: String ?= null,
        var TEL_NO: String ?= null,
        var AGE: String ?= null,
        var SCTP_CD: String ?= null,
        var SCHL_CD: String ?= null,
        var GRAD: String ?= null,
        var FA_LTYN: String ?= null,
        var MO_LTYN: String ?= null,
        var EBRO_LTNUM: String ?= null,
        var ESIS_LTNUM: String ?= null,
        var MGDN_CD: String ?= null,
        var MGDN_NM: String ?= null,
        var VLG_NM: String ?= null,
        var VLG_LAT: String ?= null,
        var VLG_LONG: String ?= null,
        var SIBLING1: String ?= null,
        var SIBLING2: String ?= null,
        var SCTP_NM: String ?= null,
        var SCHL_NM: String ?= null,
        var MGDN_CD_NM: String ?= null,
        var THUMB_FILE_PATH: String ?= null
)

data class StringResult(
        var value: String ?= null
)