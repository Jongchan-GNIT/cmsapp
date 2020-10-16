package kr.goodneighbors.cms.service.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import com.google.gson.GsonBuilder
import kr.goodneighbors.cms.service.api.ServerApi
import kr.goodneighbors.cms.service.model.ApiCallUploadResponse
import kr.goodneighbors.cms.service.model.ApiDownloadListParam
import kr.goodneighbors.cms.service.model.ApiDownloadListRsponse
import kr.goodneighbors.cms.service.model.DeviceVerifyParameter
import kr.goodneighbors.cms.service.model.DeviceVerifyResponse
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ServerRepository @Inject constructor(
        val serverApi: ServerApi,
        val preferences: SharedPreferences
) {
    private val logger: Logger by lazy {
        LoggerFactory.getLogger(ServerRepository::class.java)
    }

    private lateinit var loadStatusLive: MutableLiveData<Boolean>
    private lateinit var errorStatusLive: MutableLiveData<String>

    var verifyInfo: MutableLiveData<DeviceVerifyResponse> = MutableLiveData()

    // 단말기 등록
    fun getDeviceVerify(imei: String): LiveData<DeviceVerifyResponse> {
        loadStatusLive = MutableLiveData()
        errorStatusLive = MutableLiveData()

        val call: Call<DeviceVerifyResponse> =  serverApi.getDeviceVerify(DeviceVerifyParameter(imei))
        call.enqueue(object : Callback<DeviceVerifyResponse> {
            override fun onResponse(call: Call<DeviceVerifyResponse>, response: Response<DeviceVerifyResponse>) {
                logger.debug("${response.body()}")
                if (response!!.isSuccessful) {
                    val deviceVerifyResponse = response.body() ?: return

                    preferences.edit()
                            .putString("ctr_cd", deviceVerifyResponse.data?.ctr_cd)
                            .putString("brc_cd", deviceVerifyResponse.data?.brc_cd)
                            .putString("prj_cd", deviceVerifyResponse.data?.prj_cd)
                            .putString("aes_key", deviceVerifyResponse.data?.aes_key)
                            .apply()

                    verifyInfo.value = deviceVerifyResponse
                }
                //else errorStatusLive.postValue("Error! Server returned: ${response.code()}")
                else {
                    if (response.errorBody() != null) {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            logger.debug("errorBody : ${response.errorBody().toString()} /// jsonobject : ${jObjError}")

                            val deviceVerifyResponse = DeviceVerifyResponse(jObjError.getString("code"), jObjError.getString("message"))
                            logger.debug("error : $deviceVerifyResponse")

                            errorStatusLive.postValue(deviceVerifyResponse.message)
                        } catch (e: Exception) {
                            logger.error("Exception :  ", e)
                            errorStatusLive.postValue("Error! Server returned: ${response.code()}")
                        }

                    }
                    else {
                        logger.debug("error body is null")
                        errorStatusLive.postValue("Error! Server returned: ${response.code()}")
                    }
                }
            }

            override fun onFailure(call: Call<DeviceVerifyResponse>, t: Throwable) {
                logger.debug("ServerRepository.getDeviceVerify : $t")

                if (t is IOException)
                    errorStatusLive.postValue("Error! Please check internet connection!")
                else errorStatusLive.postValue("Error. ${t.localizedMessage ?: "Unknown"}")
            }
        })
        return verifyInfo
    }

    fun getLoadingStatus(): LiveData<Boolean> {
        return loadStatusLive
    }

    fun getErrorStatus(): LiveData<String> {
        return errorStatusLive
    }

    private lateinit var downloadList: MutableLiveData<ApiDownloadListRsponse>
    fun findAllDownloadList(param: ApiDownloadListParam): LiveData<ApiDownloadListRsponse> {
        loadStatusLive = MutableLiveData()
        errorStatusLive = MutableLiveData()

        downloadList = MutableLiveData()

        val gson = GsonBuilder().create()

        val token = preferences.getString("aes_key", "") ?: ""
        param.user_id = preferences.getString("userid", "") ?: ""

        val params = gson.toJson(param)
        logger.debug("----------params : $params")
        val call: Call<ApiDownloadListRsponse> =  serverApi.getDownloadList(token, params)

        call.enqueue(object : Callback<ApiDownloadListRsponse> {
            override fun onResponse(call: Call<ApiDownloadListRsponse>, response: Response<ApiDownloadListRsponse>) {
                logger.debug("response body : ${response.body()}")
                if (response!!.isSuccessful) {
                    val r = response.body() ?: return

                    downloadList.postValue(r)
                }
                //else errorStatusLive.postValue("Error! Server returned: ${response.code()}")
                else {
                    if (response.errorBody() != null) {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            logger.debug("errorBody : ${response.errorBody().toString()} /// jsonobject : ${jObjError}")

//                            val deviceVerifyResponse = gson.fromJson(jObjError.toString(), DeviceVerifyResponse::class.java)
                            val r = ApiDownloadListRsponse(code = jObjError.getString("code"), message = jObjError.getString("message"))
                            logger.debug("error : $r")

//                            verifyInfo.value = deviceVerifyResponse
                            errorStatusLive.postValue(r.message)
                        } catch (e: Exception) {
                            logger.error("Exception :  ", e)
                            errorStatusLive.postValue("Error! Server returned: ${response.code()}")
                        }

                    }
                    else {
                        logger.debug("error body is null")
                        errorStatusLive.postValue("Error! Server returned: ${response.code()}")
                    }
                }
            }

            override fun onFailure(call: Call<ApiDownloadListRsponse>, t: Throwable) {
                logger.debug("ServerRepository.findAllDownloadList : $t")

                if (t is IOException)
                    errorStatusLive.postValue("Error! Please check internet connection!")
                else errorStatusLive.postValue("Error. ${t?.localizedMessage ?: "Unknown"}")
            }
        })
        return downloadList
    }

    private lateinit var callUploadAPIResult: MutableLiveData<String>
    fun callUploadAPI(path: String): LiveData<String> {
        callUploadAPIResult = MutableLiveData()

        val token = preferences.getString("aes_key", "")
        val param = hashMapOf<String, String>()
        param["user_id"] = preferences.getString("userid", "") ?: ""
        param["data_file"] = path

        logger.debug("callUploadAPI($param)")

        val call: Call<ApiCallUploadResponse> =  serverApi.callUploadAPI(token, param)
        call.enqueue(object : Callback<ApiCallUploadResponse> {
            override fun onResponse(call: Call<ApiCallUploadResponse>, response: Response<ApiCallUploadResponse>) {
                logger.debug("callUploadAPI.onResponse - response body : ${response.body()}")
                if (response.isSuccessful) {
                    logger.debug("callUploadAPI.onResponse Success !!!")
                    callUploadAPIResult.postValue("SUCCESS")
                }
                else {
                    if (response.errorBody() != null) {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            logger.error( "errorBody : ${response.errorBody().toString()} /// jsonobject : $jObjError")

                            val r = ApiCallUploadResponse(code = jObjError.getString("code"), message = jObjError.getString("message"), data = null)
                            logger.error("error : $r")

                            callUploadAPIResult.postValue(r.message)
                        } catch (e: Exception) {
                            logger.error( "Exception :  $e")
                            callUploadAPIResult.postValue("Error! Server returned: ${response.code()}")
                        }

                    }
                    else {
                        logger.error("error body is null")
                        callUploadAPIResult.postValue("Error! Server returned: ${response.code()}")
                    }
                }
            }

            override fun onFailure(call: Call<ApiCallUploadResponse>, t: Throwable) {
                logger.debug("ServerRepository.callUploadAPI : $t")

                if (t is IOException) {
                    callUploadAPIResult.postValue("Error! Please check internet connection!")
                }
                else {
                    callUploadAPIResult.postValue("Error! ${t.localizedMessage ?: "Unknown"}")
                }
            }
        })

        return callUploadAPIResult
    }
}