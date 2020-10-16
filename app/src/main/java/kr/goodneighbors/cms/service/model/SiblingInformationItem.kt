package kr.goodneighbors.cms.service.model

data class SiblingInformationItem(
        val CHRCP_NO: String,
        val CTR_CD: String,
        val BRC_CD: String,
        val PRJ_CD: String,
        val CH_CD: String ?= null,
        val CH_EFNM: String,
        val CH_EMNM: String ?= null,
        val CH_ELNM: String,
        var GNDR: String ?= null,
        var BDAY: String ?= null,
        var APRV_DT: String ?= null,
        var FILE_PATH: String ?= null
)