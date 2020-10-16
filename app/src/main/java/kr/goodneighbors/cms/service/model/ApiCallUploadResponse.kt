package kr.goodneighbors.cms.service.model

data class ApiCallUploadResponse(
        val code: String,
        val message: String,
        val data: Any?
)