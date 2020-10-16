package kr.goodneighbors.cms

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.support.v7.app.AppCompatDelegate
import ch.qos.logback.classic.android.BasicLogcatConfigurator
import com.facebook.stetho.Stetho
import kr.goodneighbors.cms.common.Constants
import kr.goodneighbors.cms.common.GNLocaleManager
import kr.goodneighbors.cms.di.AppComponent
import kr.goodneighbors.cms.di.AppModule
import kr.goodneighbors.cms.di.DaggerAppComponent
import org.acra.ACRA
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraMailSender
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@AcraCore(buildConfigClass = BuildConfig::class)
@AcraMailSender(mailTo = Constants.ACRA_MAIL_SENDER)
//@AcraScheduler(requiresNetworkType = JobRequest.NetworkType.UNMETERED,
//        requiresBatteryNotLow = true)
class App : Application() {
    companion object {
        lateinit var appComponent: AppComponent
    }

    private val logger: Logger by lazy {
        BasicLogcatConfigurator.configureDefaultContext()
        LoggerFactory.getLogger(App::class.java)
    }

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG)
            Stetho.initializeWithDefaults(this)

        initializeDagger()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(GNLocaleManager.setLocale(base))

        ACRA.init(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        GNLocaleManager.setLocale(this)
        logger.debug("onConfigurationChanged: " + newConfig.locale.getLanguage())
    }

    private fun initializeDagger() {
        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()
    }
}