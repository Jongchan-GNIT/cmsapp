package kr.goodneighbors.cms.service.model

import kr.goodneighbors.cms.service.entities.CD
import kr.goodneighbors.cms.service.entities.RPT_BSC
import java.util.*

data class CifEditViewItem(
        var codeVillage: List<SpinnerOption> ?= null,

        var codeDisability: List<SpinnerOption> ?= null,
        var codeDisabilityReason: List<SpinnerOption> ?= null,

        var codeIllness: List<SpinnerOption> ?= null,
        var codeIllnessReason: List<SpinnerOption> ?= null,

        var codeSchoolType: List<SpinnerOption> ?= null,
        var codeSchoolTypeReason: List<SpinnerOption> ?= null,
        var codeSchoolName: List<SpinnerOption> ?= null,

        var codeSupportCountry: List<SpinnerOption> ?= null,
        var codeSupplyPlan: HashMap<String, ArrayList<SpinnerOption>>,

        var codeRelationship: List<SpinnerOption> ?= null,
        var codeInterviewPlace: List<SpinnerOption> ?= null,

        var codeFatherReason: List<SpinnerOption> ?= null,
        var codeMainGuardian: List<SpinnerOption> ?= null,
        var codeSpecialCase: List<SpinnerOption> ?= null,

        var codePersonalInfo: List<PersonalInfoItem> ?= null,

        var rpt_bsc: RPT_BSC ?= null,
        var returns: List<ReturnItem> ?= null
) {
    fun getSupplyPlan(dnctr_cd: String): ArrayList<SpinnerOption> {
        return if (codeSupplyPlan.containsKey(dnctr_cd)) { codeSupplyPlan[dnctr_cd]!! }
        else {
            val currentYear =  Calendar.getInstance().get(Calendar.YEAR)
            val genSupplyPlanItems = ArrayList<SpinnerOption>()
            genSupplyPlanItems.add(SpinnerOption("${currentYear}00", "${currentYear}00"))
            genSupplyPlanItems.add(SpinnerOption("${currentYear-1}00", "${currentYear-1}00"))

            genSupplyPlanItems
        }
    }
}

data class ReturnItem(
        val RCP_NO: String,
        val RETN_ITCD: String ?= null,
        val RTRN_BCD: String ?= null,
        val RTRN_BCD_LABEL: String ?= null,
        val RTRN_SCD: String ?= null,
        val RTRN_SCD_LABEL: String ?= null,
        val RTRN_DETL: String ?= null
)

data class PersonalInfoItem(
        val master: CD,
        val detail: List<SpinnerOption>
)

data class DuplicateChildItem(
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