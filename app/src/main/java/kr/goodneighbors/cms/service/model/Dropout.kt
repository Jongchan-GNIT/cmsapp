package kr.goodneighbors.cms.service.model

import kr.goodneighbors.cms.service.entities.CH_MST
import kr.goodneighbors.cms.service.entities.RPT_BSC

data class DropoutEditItem(
        var dropoutReasonList: List<SpinnerOption>? = null,
        var deathReasonList: List<SpinnerOption>? = null,
        var illnessList: List<SpinnerOption>? = null,
        var disabilityList: List<SpinnerOption>? = null,
        var jobList: List<SpinnerOption>? = null,

        var ch_mst: CH_MST?= null,
        var rpt_bsc: RPT_BSC ?= null,
        var returns: List<ReturnItem> ?= null
)

data class DropoutEditSearchItem(
        var chrcp_no: String? = null,
        var rcp_no: String? = null
)