package kr.goodneighbors.cms.common

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.preference.PreferenceManager
import java.util.*

object GNLocaleManager {
    const val SELECTED_LANGUAGE = "GN_CURRENT_USER_LANGUAGE"

    fun setLocale(context: Context?): Context {
        return updateResources(context!!, getCurrentLanguage(context)!!)
    }

    inline fun setNewLocale(context: Context, language: String) {

        persistLanguagePreference(context, language)
        updateResources(context, language)
    }

    inline fun getCurrentLanguage(context: Context?): String? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)

        return sharedPref.getString(SELECTED_LANGUAGE, "")
    }

    fun persistLanguagePreference(context: Context, language: String) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.edit().putString(SELECTED_LANGUAGE, language).apply()
    }

    fun updateResources(context: Context, language: String): Context {

        var contextFun = context

        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)

        if (Build.VERSION.SDK_INT >= 17) {
            configuration.setLocale(locale)
            contextFun = context.createConfigurationContext(configuration)
        } else {
            configuration.locale = locale
            resources.updateConfiguration(configuration, resources.getDisplayMetrics())
        }
        return contextFun
    }
}