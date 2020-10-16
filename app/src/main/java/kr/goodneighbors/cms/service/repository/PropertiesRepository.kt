package kr.goodneighbors.cms.service.repository

import kr.goodneighbors.cms.service.db.PropDao
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class PropertiesRepository @Inject constructor(
        private val dao: PropDao
)