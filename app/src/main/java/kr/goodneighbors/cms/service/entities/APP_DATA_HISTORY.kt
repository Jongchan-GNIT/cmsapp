package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Suppress("ClassName")
@Entity(tableName = APP_DATA_HISTORY.TABLE_NAME)
data class APP_DATA_HISTORY(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "ID")
        val id: Int? = null,

        @ColumnInfo(name = "FILE_ID")
        val fileId: String,

        @ColumnInfo(name = "DATE")
        val datetime: Long,

        @ColumnInfo(name = "TYPE")
        val type: String,

        @ColumnInfo(name = "REGIST_DATE")
        val registDate: Long
) {
    companion object {
        const val TABLE_NAME = "APP_DATA_HISTORY"
    }
}