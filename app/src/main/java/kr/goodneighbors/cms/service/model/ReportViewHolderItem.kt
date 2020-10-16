package kr.goodneighbors.cms.service.model

import kr.goodneighbors.cms.service.entities.CD
import kr.goodneighbors.cms.service.entities.CH_MST
import kr.goodneighbors.cms.service.entities.RPT_BSC

data class ReportViewHolderItem(
        val child: CH_MST?,
        val report: RPT_BSC?,
        val service: String,
        val status: CD?
)
