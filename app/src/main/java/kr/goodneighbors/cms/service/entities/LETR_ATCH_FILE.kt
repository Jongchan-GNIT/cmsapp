package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity

@Suppress("ClassName")
@Entity(tableName = LETR_ATCH_FILE.TABLE_NAME, primaryKeys = ["SEQ_NO", "CHRCP_NO", "MNG_NO"])
data class LETR_ATCH_FILE(
        var SEQ_NO: Int,
        var CHRCP_NO: String,
        var MNG_NO: String,

        var FILE_DVCD: String?,
        var FILE_NM: String?,
        var FILE_PATH: String?
) {
    companion object {
        const val TABLE_NAME = "LETR_ATCH_FILE"
    }
}