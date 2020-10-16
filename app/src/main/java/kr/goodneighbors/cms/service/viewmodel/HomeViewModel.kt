package kr.goodneighbors.cms.service.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import kr.goodneighbors.cms.App
import kr.goodneighbors.cms.service.model.HomeItem
import kr.goodneighbors.cms.service.model.NoticeItem
import kr.goodneighbors.cms.service.repository.HomeRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

class HomeViewModel: ViewModel() {
    @Inject
    lateinit var repository: HomeRepository

    init {
        App.appComponent.inject(this)
    }

    private val log: Logger by lazy {
        LoggerFactory.getLogger(HomeViewModel::class.java)
    }

    fun getHomeItem():LiveData<HomeItem> {
        return repository.getHomeItem()
    }

    fun findAllNoticeItem(): LiveData<List<NoticeItem>> {
        return repository.findAllNoticeItem()
    }
}