package kr.goodneighbors.cms.service.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import kr.goodneighbors.cms.service.db.UserInfoDao
import kr.goodneighbors.cms.service.entities.CD
import kr.goodneighbors.cms.service.entities.USER_INFO
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserInfoRepository @Inject constructor(
        private val userInfoDao: UserInfoDao
) {
    private val logger: Logger by lazy {
        LoggerFactory.getLogger(UserInfoRepository::class.java)
    }

    private val inputedUserInfo = MutableLiveData<USER_INFO>()

    val userinfo: LiveData<USER_INFO> = Transformations.switchMap(inputedUserInfo) {
        userInfoDao.findUserInfoById(it.ID)
    }

    fun findUserInfo(_userinfo: USER_INFO): LiveData<USER_INFO> {
        inputedUserInfo.value = _userinfo

        return userinfo
    }

    fun findBucketInfo(id: String): LiveData<CD> {
        return userInfoDao.findBucketInfo(id)
    }

    fun initAll(userinfoList: ArrayList<USER_INFO>) {
        Thread(Runnable {
            userInfoDao.initAll(userinfoList)
        }).start()
    }

    fun findUserId(userName: String, birth: String, email: String): LiveData<String>? {
        logger.debug("userName = $userName, birth = $birth, email = $email")
        return userInfoDao.findUserName(userName, birth, email)
    }

    fun findPassword(userId: String, birth: String, email: String): LiveData<USER_INFO>? {
        return userInfoDao.findPassword(userId, birth, email)
    }
}