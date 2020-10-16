package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.Entity
import com.google.gson.annotations.Expose

@Suppress("ClassName")
@Entity(tableName = ATCH_FILE.TABLE_NAME, primaryKeys = ["RCP_NO", "SEQ_NO"])
data class ATCH_FILE(
        @Expose var RCP_NO: String,
        @Expose var SEQ_NO: Int,

        @Expose var FILE_DVCD: String? = null,
        @Expose var FILE_NM: String? = null,
        @Expose var IMG_DVCD: String? = null,
        @Expose var FILE_PATH: String? = null
) {
    companion object {
        const val TABLE_NAME = "ATCH_FILE"
    }
}