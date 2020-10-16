package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity

@Entity(tableName = BMI.TABLE_NAME, primaryKeys = ["MONTHS", "GNDR"])
data class BMI(
        var MONTHS: Int,
        var GNDR: String,
        var MIN_NUM: Float?,
        var MAX_NUM: Float?,
        var REG_DT: Long?
) {
    companion object {
        const val TABLE_NAME = "BMI"
    }
}