package kr.goodneighbors.cms.service.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import kr.goodneighbors.cms.service.entities.CD
import kr.goodneighbors.cms.service.entities.USER_INFO

@Dao
interface UserInfoDao {
    @Query("SELECT * FROM USER_INFO WHERE ID = :id LIMIT 1")
    fun findUserInfoById(id: String): LiveData<USER_INFO>

    @Query("""
        SELECT B.*
        FROM (
            SELECT B.CTNT_CD
            FROM (
                SELECT CTR_CD
                FROM USER_INFO WHERE ID = :id
            ) A
            INNER JOIN CTR B
            ON A.CTR_CD = B.CTR_CD
        ) A
        INNER JOIN (
            SELECT *
            FROM CD
            WHERE GRP_CD = '4'
            AND USE_YN = 'Y'
        ) B
        ON A.CTNT_CD = B.CD
    """)
    fun findBucketInfo(id: String): LiveData<CD>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDummyData(userinfo: USER_INFO)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun initAll(userinfoList: List<USER_INFO>)

    @Query("SELECT ID FROM  USER_INFO WHERE NM = :userName AND BIRTH = :birth AND EMAIL = :email LIMIT 1")
    fun findUserName(userName: String, birth: String, email: String): LiveData<String>

    @Query("SELECT * FROM USER_INFO WHERE ID = :userId AND BIRTH = :birth AND EMAIL = :email LIMIT 1")
    fun findPassword(userId: String, birth: String, email: String): LiveData<USER_INFO>
}