package kr.goodneighbors.cms.service.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import kr.goodneighbors.cms.App
import kr.goodneighbors.cms.service.model.DeviceVerifyResponse
import kr.goodneighbors.cms.service.repository.ServerRepository
import javax.inject.Inject

class DeviceVerifyViewModel : ViewModel() {
    @Inject
    lateinit var serverRepository: ServerRepository
    private val deviceImei = MutableLiveData<String>()

    init {
        App.appComponent.inject(this)
    }

    var verifyInfo: LiveData<DeviceVerifyResponse> = Transformations.switchMap(deviceImei)
    {imei->
        serverRepository.getDeviceVerify(imei = imei)
    }

    val loadStatus: LiveData<Boolean> = Transformations.switchMap(deviceImei)
    { _ ->
        serverRepository.getLoadingStatus()
    }

    val errorStatus : LiveData<String> = Transformations.switchMap(deviceImei)
    { _ ->
        serverRepository.getErrorStatus()
    }

    fun setDeviceImei(imei: String) {
        deviceImei.value = imei
    }
}