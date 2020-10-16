package kr.goodneighbors.cms.service.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import kr.goodneighbors.cms.App
import kr.goodneighbors.cms.service.repository.SettingRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

class SettingViewModel: ViewModel() {
    @Inject
    lateinit var repository: SettingRepository

    init {
        App.appComponent.inject(this)
    }

    private val log: Logger by lazy {
        LoggerFactory.getLogger(SettingViewModel::class.java)
    }

    fun deletePastContentFile():LiveData<Boolean>  {
        return repository.deletePastContentFile()
    }
}