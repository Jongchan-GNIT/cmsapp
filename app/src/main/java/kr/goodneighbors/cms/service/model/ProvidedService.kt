package kr.goodneighbors.cms.service.model

import kr.goodneighbors.cms.service.entities.CD
import kr.goodneighbors.cms.service.entities.SRVC

data class ProvidedServiceEditItem(
        val RNUM: Int,
        val srvc: SRVC,
        var isSelected: Boolean = false,
        var isEdited: Boolean = false,
        var isDeleted: Boolean = false
)

data class ProvidedServiceEditSearchItem(
        val chrcp_no: String,
        val svobj_dvcd: String,
        val bsst_cd: String,
        val spbd_cd: String
)

data class ProvidedServiceRegistSearchItem(
    val search: String,
    val checkedItems: List<ProvidedServiceRegistCheckedItem> ?= null
)

data class ProvidedServiceRegistCheckedItem(
        var type: String,
        var master_cd: String,
        var detail_group_cd: String,
        var detail_cd: String
)


data class ProvidedServiceListItem(
        val CHRCP_NO: String,
        val SVOBJ_DVCD: String,
        val BSST_CD: String,
        val SPBD_CD: String,

        val TITLE: String,
        val COUNT: Int,
        val PRVD_DT: String
)

data class ProvidedServiceRegistItem(
        var master: CD,
        var detailes: List<CD>
)