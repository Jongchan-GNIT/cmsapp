package kr.goodneighbors.cms.di

import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import dagger.Module
import dagger.Provides
import kr.goodneighbors.cms.BuildConfig
import kr.goodneighbors.cms.common.Constants
import kr.goodneighbors.cms.service.api.ServerApi
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.*
import javax.inject.Singleton

@Module(includes = arrayOf(AppModule::class))
class NetworkModule {

    @Provides
    @Singleton
    fun retrofit(gsonConverterFactory: GsonConverterFactory, okHttpClient: OkHttpClient) =
            Retrofit.Builder().baseUrl(Constants.API_URL)
                    .addConverterFactory(gsonConverterFactory)
                    .client(okHttpClient)
                    .build()

    @Provides
    @Singleton
    fun gsonConverterFactory() = GsonConverterFactory.create()

    @Provides
    @Singleton
    fun okhttpClient(cache: Cache): OkHttpClient {
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        val client = OkHttpClient.Builder()
                .cache(cache)
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val orginUrl = chain.request()?.url()

                    val urlString = orginUrl?.url().toString().replace("{", "%7B").replace("}", "%7D")

                    val request = chain.request()?.newBuilder()
                            ?.addHeader("Content-Type", "application/json;charset=UTF-8")
                            ?.addHeader("User-Agent", "GoodNeighbors CMS Mobile Application")
                            ?.url(urlString)
                            ?.tag(urlString)
                            ?.build()

                    chain.proceed(request)
                }

        //Add StethoInterceptor only when app is in debug mode
        if (BuildConfig.DEBUG) {
            client.addNetworkInterceptor(StethoInterceptor())
        }
        return client.build()
    }

    @Provides
    @Singleton
    fun cache(context: Context) = Cache(context.cacheDir, 10 * 1024 * 1024)

    @Provides
    @Singleton
    fun servierApiService(retrofit: Retrofit): ServerApi = retrofit.create(ServerApi::class.java)
}
