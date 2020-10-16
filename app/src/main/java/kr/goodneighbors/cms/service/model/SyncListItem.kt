package kr.goodneighbors.cms.service.model

import kr.goodneighbors.cms.common.ProcessState

data class SyncListItem(
        var index: Int,
        var path: String,
        var state: ProcessState
) {
    val directory: String
            get() = path.substringBeforeLast("/")
    val fullName: String
            get() = path.substringAfterLast("/")
    val fileName: String
            get() = fullName.substringBeforeLast(".")
    val extension: String
            get() = fullName.substringAfterLast(".")
    val fileId: String
            get() = fileName.substringBeforeLast("_")
}
