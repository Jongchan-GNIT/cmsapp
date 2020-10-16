package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import com.google.gson.annotations.Expose

@Entity(tableName = SIBL.TABLE_NAME, primaryKeys = ["RCP_NO", "CHRCP_NO"])
data class SIBL (
        @Expose var RCP_NO: String,
        @Expose var CHRCP_NO: String
) {
    companion object {
        const val TABLE_NAME = "SIBL"
    }
}