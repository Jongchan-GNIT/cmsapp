package kr.goodneighbors.cms.di

import android.arch.persistence.room.Room
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import kr.goodneighbors.cms.service.db.CmsDb
import javax.inject.Singleton

@Module(includes = arrayOf(AppModule::class))
class DataModule {

    @Singleton
    @Provides
    fun preference(context: Context): SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)

    @Singleton
    @Provides
    fun appDatabase(context: Context): CmsDb = Room.databaseBuilder(context,
            CmsDb::class.java, CmsDb.DB_NAME).build()

    @Provides
    @Singleton
    fun userInfoDao(cmsDb: CmsDb) = cmsDb.userInfoDao()

    @Provides
    @Singleton
    fun reportDao(cmsDb: CmsDb) = cmsDb.reportDao()

    @Provides
    @Singleton
    fun commonDao(cmsDb: CmsDb) = cmsDb.commonDao()

    @Provides
    @Singleton
    fun childlistDao(cmsDb: CmsDb) = cmsDb.childlistDao()

    @Provides
    @Singleton
    fun syncDao(cmsDb: CmsDb) = cmsDb.syncDao()

    @Provides
    @Singleton
    fun propDao(cmsDb: CmsDb) = cmsDb.propDao()

    @Provides
    @Singleton
    fun homeDao(cmsDb: CmsDb) = cmsDb.homeDao()

    @Provides
    @Singleton
    fun statisticsDao(cmsDb: CmsDb) = cmsDb.statisticsDao()

    @Provides
    @Singleton
    fun settingDao(cmsDb: CmsDb) = cmsDb.settingDao()
}