package kr.goodneighbors.cms.service.api

import kr.goodneighbors.cms.service.model.ApiCallUploadResponse
import kr.goodneighbors.cms.service.model.ApiDownloadListRsponse
import kr.goodneighbors.cms.service.model.DeviceVerifyParameter
import kr.goodneighbors.cms.service.model.DeviceVerifyResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ServerApi {
//    @Headers("Content-Type: application/json;charset=UTF-8", "User-Agent: GoodNeighbors CMS Mobile Application")
    @POST("/apis/mo/init/api-000-0000")
    fun getDeviceVerify(@Body param: DeviceVerifyParameter): Call<DeviceVerifyResponse>

//    @Headers("Content-Type: application/json;charset=UTF-8", "User-Agent: GoodNeighbors CMS Mobile Application")
    @GET("/apis/bo/sync/api-100-0001")
    fun getDownloadList(@Header("access_token") token:String, @Query("params") params: String): Call<ApiDownloadListRsponse>

    @POST("/apis/bo/sync/api-100-0002")
    fun callUploadAPI(@Header("access_token") token: String, @Body params: Map<String, String>): Call<ApiCallUploadResponse>
}