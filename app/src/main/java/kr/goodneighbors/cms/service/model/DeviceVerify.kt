package kr.goodneighbors.cms.service.model

data class DeviceVerifyParameter(val imei_num: String)

data class DeviceVerifyResponse(val code: String, val message: String, val data: Item? = null) {
//    constructor(code: String, message: String) : this(code, message, null)

    data class Item (val ctr_cd: String,
                     val brc_cd: String,
                     val prj_cd: String,
                     val aes_key: String
    )
}