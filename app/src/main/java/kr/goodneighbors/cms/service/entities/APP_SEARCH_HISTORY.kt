package kr.goodneighbors.cms.service.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Suppress("ClassName")
@Entity(tableName = APP_SEARCH_HISTORY.TABLE_NAME)
data class APP_SEARCH_HISTORY(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "ID")
        val id: Int? = null,

        @ColumnInfo(name = "WORD")
        val word: String,

        @ColumnInfo(name = "REGIST_DATE")
        val registDate: Long
) {
    companion object {
        const val TABLE_NAME = "APP_SEARCH_HISTORY"
    }
}