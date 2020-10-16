package kr.goodneighbors.cms.service.model

import kr.goodneighbors.cms.service.entities.CH_MST
import kr.goodneighbors.cms.service.entities.RPT_BSC

data class ChildReport (
        var ch_mst: CH_MST? = null,

        var rpt_bsc: RPT_BSC? = null,

        var pre_rpt_bsc: RPT_BSC ?= null,

        var isSelected: Boolean = false
)