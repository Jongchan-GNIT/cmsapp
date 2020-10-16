package kr.goodneighbors.cms.service.model

import kr.goodneighbors.cms.service.entities.CD
import kr.goodneighbors.cms.service.entities.CH_MST
import kr.goodneighbors.cms.service.entities.RPT_BSC

data class AprEditViewItem(
        var codeVillage: List<SpinnerOption> ?= null,

        var codeDisability: List<SpinnerOption> ?= null,
        var codeDisabilityReason: List<SpinnerOption> ?= null,

        var codeIllness: List<SpinnerOption> ?= null,
        var codeIllnessReason: List<SpinnerOption> ?= null,

        var codeSchoolType: List<SpinnerOption> ?= null,
        var codeSchoolTypeReason: List<SpinnerOption> ?= null,
        var codeSchoolName: List<SpinnerOption> ?= null,

        var codeRelationship: List<SpinnerOption> ?= null,
        var codeInterviewPlace: List<SpinnerOption> ?= null,

        var codeFatherReason: List<SpinnerOption> ?= null,
        var codeMainGuardian: List<SpinnerOption> ?= null,
        var codeSpecialCase: List<SpinnerOption> ?= null,

        var codeFuturePlan: List<SpinnerOption> ?= null,
        var codeContinuReason: List<SpinnerOption> ?= null,

        var codePersonalInfo: List<PersonalInfoItem> ?= null,

        var ch_mst: CH_MST ?= null,
        var rpt_bsc: RPT_BSC ?= null,
        var prev_rpt_bsc: RPT_BSC ?= null,

        var returns: List<ReturnItem> ?= null,

        var prevImageList: List<AprEditProfileImageItem> ?= null,

        var supportCountry: String ?= null,

        var codeMap: HashMap<String, List<CD>> ?= null
) {
    fun getCodeGroup(group: String): List<CD>? {
        return codeMap?.get(group)
    }

    fun getCode(group: String, code: String): CD? {
        codeMap?.get(group)?.forEach {
            if (code == it.CD) return it
        }
        return null
    }
}

data class AprEditViewSearchItem(
        var chrcp_no: String,
        var rcp_no: String ?= null,
        var year: String
)

data class AprEditProfileImageItem(
        var year: String,
        var generalImagePath: String ?= null,
        var thumbnailImagePath: String ?= null,
        var isEditable: Boolean = false
)

data class ReportListItem(
        var IS_SELECTED: Boolean ?= false,

        var CHRCP_NO: String,
        var RCP_NO: String ?= null,
        var RPT_DVCD: String,
        var YEAR: String,
        var RPT_STCD: String,
        var RPT_STNM: String?=null,
        var APRV_DT: String ?= null,
        var LAST_RCP_NO: String ?= null,
        var CHILD_NAME: String ?= null,
        var GNDR: String ?= null,
        var BDAY: String ?= null,
        var AGE: Int?= null,
        var VLG_CD: String ?= null,
        var HS_ADDR: String ?= null,
        var HS_ADDR_DTL: String ?= null,
        var SCTP_CD: String ?= null,
        var SCHL_CD: String ?= null,
        var GRAD: String ?= null,
        var BMI_CD: String ?= null,
        var DISB_CD: String ?= null,
        var ILNS_CD: String ?= null,
        var FA_LTYN: String ?= null,
        var MO_LTYN: String ?= null,
        var EBRO_LTNUM: String ?= null,
        var ESIS_LTNUM: String ?= null,
        var YBRO_LTNUM: String ?= null,
        var YSIS_LTNUM: String ?= null,
        var PLAN_YN: String ?= null,
        var FTPLN_CD: String ?= null,
        var FTPLN_DTL: String ?= null,
        var CTNSPN_RNCD: String ?= null,
        var CTNSPN_DTL: String ?= null,
        var REMRK_ENG: String ?= null,
        var CASE1: String ?= null,
        var CASE2: String ?= null,
        var CASE3: String ?= null,
        var SIBLING1: String ?= null,
        var SIBLING2: String ?= null,
        var VLG_NM: String ?= null,
        var SCTP_NM: String ?= null,
        var SCHL_NM: String ?= null,
        var BMI_NM: String ?= null,
        var DISB_NM: String ?= null,
        var ILNS_NM: String ?= null,
        var FTPLN_NM: String ?= null,
        var CTNSPN_RNNM: String ?= null,
        var CASE1_NM: String ?= null,
        var CASE2_NM: String ?= null,
        var CASE3_NM: String ?= null,
        var GENERAL_FILE_PATH: String ?= null,
        var THUMB_FILE_PATH: String ?= null
)