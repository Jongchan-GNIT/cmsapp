package kr.goodneighbors.cms.service.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

data class CommonCode(
        var status: List<SpinnerOption>? = null,
        var service: List<SpinnerOption>? = null,
        var villages: List<SpinnerOption>? = null,
        var disabillity: List<SpinnerOption>? = null,
        var disabillityReason: List<SpinnerOption>? = null,
        var illness: List<SpinnerOption>? = null,
        var illnessReason: List<SpinnerOption>? = null,
        var schoolType: List<SpinnerOption>? = null,
        var schoolTypeReason: List<SpinnerOption>? = null,
        var schoolName: List<SpinnerOption>? = null,
        var supportCountry: List<SpinnerOption>? = null,
        var relationshipWithChild: List<SpinnerOption>? = null,
        var interviewPlace: List<SpinnerOption>? = null,
        var fatherReason: List<SpinnerOption>? = null,
        var mainGuardian: List<SpinnerOption>? = null,
        var specialCase: List<SpinnerOption>? = null,

        var personality: List<SpinnerOption>? = null,
        var favoriteSubject: List<SpinnerOption>? = null,
        var wantToBe: List<SpinnerOption>? = null,
        var hobby: List<SpinnerOption>? = null,

        var dropoutPlanY: List<SpinnerOption>? = null,
        var dropoutPlanN: List<SpinnerOption>? = null,

        var supplyPlan: HashMap<String, ArrayList<SpinnerOption>>
) {
    fun getSupplyPlan(dnctr_cd: String): ArrayList<SpinnerOption> {
        return if (supplyPlan.containsKey(dnctr_cd)) { supplyPlan[dnctr_cd]!! }
        else {
            val currentYear =  Calendar.getInstance().get(Calendar.YEAR)
            val genSupplyPlanItems = ArrayList<SpinnerOption>()
            genSupplyPlanItems.add(SpinnerOption("${currentYear}00", "${currentYear}00"))
            genSupplyPlanItems.add(SpinnerOption("${currentYear-1}00", "${currentYear-1}00"))

            genSupplyPlanItems
        }
    }
}

@Parcelize
data class VillageLocation(
        var VLG_CD: String ?= null,
        var VLG_NM: String ?= null,
        var LAT: String ?= null,
        var LNG: String ?= null
) : Parcelable