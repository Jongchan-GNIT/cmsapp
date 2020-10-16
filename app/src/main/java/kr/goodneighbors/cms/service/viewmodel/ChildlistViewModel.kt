package kr.goodneighbors.cms.service.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import kr.goodneighbors.cms.App
import kr.goodneighbors.cms.service.entities.APP_SEARCH_HISTORY
import kr.goodneighbors.cms.service.model.ChildlistItem
import kr.goodneighbors.cms.service.model.ChildlistSearchItem
import kr.goodneighbors.cms.service.model.VillageLocation
import kr.goodneighbors.cms.service.repository.ChildlistRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject

class ChildlistViewModel : ViewModel() {
    @Inject
    lateinit var childlistRepository: ChildlistRepository

    init {
        App.appComponent.inject(this)
    }

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(ChildlistViewModel::class.java)
    }

    // 검색
    private var searchItem = MutableLiveData<ChildlistSearchItem>()
    private var childList: LiveData<List<ChildlistItem>> = Transformations.switchMap(searchItem)
    { input: ChildlistSearchItem ->
        childlistRepository.findAll(input)
    }

    fun getChildList(): LiveData<List<ChildlistItem>> {
        logger.debug("getChildList")
        return childList
    }

    fun getSearchOptions(): ChildlistSearchItem? {
        return searchItem.value
    }

    fun setSearchOptions(s: ChildlistSearchItem) {
        logger.debug("setSearchOptions : $s")
        searchItem.postValue(s)
    }

    private var suggestionsTrigger: MutableLiveData<Long> = MutableLiveData()
    private var suggestions: LiveData<List<APP_SEARCH_HISTORY>> = Transformations.switchMap(suggestionsTrigger) {
        childlistRepository.findAllSuggestions()
    }
    fun findAllSuggestions(): LiveData<List<APP_SEARCH_HISTORY>> {
     return suggestions
    }
    fun setFindAllSuggestions() {
        suggestionsTrigger.postValue(Date().time)
    }

    fun deleteSuggestion(word: String) {
        childlistRepository.deleteSuggestion(word)
    }

    fun findAllVillageLocation(): MutableLiveData<List<VillageLocation>> {
        return childlistRepository.findAllVillageLocation()
    }
}