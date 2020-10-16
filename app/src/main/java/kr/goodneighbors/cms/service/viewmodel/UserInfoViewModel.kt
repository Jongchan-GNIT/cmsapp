package kr.goodneighbors.cms.service.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import kr.goodneighbors.cms.App
import kr.goodneighbors.cms.service.entities.CD
import kr.goodneighbors.cms.service.entities.USER_INFO
import kr.goodneighbors.cms.service.repository.UserInfoRepository
import java.util.*
import javax.inject.Inject

class UserInfoViewModel : ViewModel() {
    @Inject
    lateinit var userInfoRepository: UserInfoRepository

    private val inputedUserInfo = MutableLiveData<USER_INFO>()

    init {
        App.appComponent.inject(this)
    }

    val userinfo: LiveData<USER_INFO> = Transformations.switchMap(inputedUserInfo)
    { _userInfo ->
        userInfoRepository.findUserInfo(_userInfo)
    }

    fun login(id: String, pw: String) {
        val user = USER_INFO(ID = id, PW = pw)
        inputedUserInfo.postValue(user)
    }


    fun findBucketInfo(id: String): LiveData<CD> = userInfoRepository.findBucketInfo(id)

    fun initAll(userinfoList: ArrayList<USER_INFO>) {
        userInfoRepository.initAll(userinfoList)
    }

    private val idUserName = MutableLiveData<String>()
    private val idBirth = MutableLiveData<String>()
    private val idEmail = MutableLiveData<String>()
    private val idCheck = MutableLiveData<Long>()

    val userName: LiveData<String> = Transformations.switchMap(idCheck) {
        userInfoRepository.findUserId(idUserName.value!!, idBirth.value!!, idEmail.value!!)
    }

    fun findUserId(userName: String, birth: String, email: String) {
        idUserName.postValue(userName)
        idBirth.postValue(birth)
        idEmail.postValue(email)

        idCheck.postValue(Date().time)
    }

    private val pwUserId = MutableLiveData<String>()
    private val pwBirth = MutableLiveData<String>()
    private val pwEmail = MutableLiveData<String>()
    private val pwCheck = MutableLiveData<Long>()

    val password: LiveData<USER_INFO> = Transformations.switchMap(pwCheck) {
        userInfoRepository.findPassword(pwUserId.value!!, pwBirth.value!!, pwEmail.value!!)
    }

    fun findPassword(userId: String, birth: String, email: String) {
        pwUserId.postValue(userId)
        pwBirth.postValue(birth)
        pwEmail.postValue(email)

        pwCheck.postValue(Date().time)
    }
}