package kr.goodneighbors.cms.service.model

import kr.goodneighbors.cms.service.entities.CH_CUSL_INFO
import kr.goodneighbors.cms.service.entities.CH_MST
import kr.goodneighbors.cms.service.entities.MOD_HIS_INFO
import kr.goodneighbors.cms.service.entities.RPT_BSC
import kr.goodneighbors.cms.service.entities.SRVC

data class ExportDataItem(
        var children: List<CH_MST> ?= null,
        var reports: List<RPT_BSC> ?= null,
        var services: List<SRVC> ?= null,
        var counseling: List<CH_CUSL_INFO> ?= null,
        var history: List<MOD_HIS_INFO> ?= null
)