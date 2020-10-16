package kr.goodneighbors.cms.service.model

data class ApiDownloadListParam(
        var user_id: String = "",
        var page: Int = 1,
        var rows: Int = 100,
        var _search: Boolean = false
)

data class ApiDownloadListRsponse(
        val code: String,
        val message: String,
        val data: ResponseData? = null
) {
    data class ResponseData(
            val last_dl_reg_dt: String,
            val page_info: PageInfo,
            val file_list: List<FileList>
    )

    data class PageInfo(
            val rows:	Int,
            val page:	Int,
            val sidx:	String,
            val sord:	String,
            val records:	Int,
            val total:	Int
    )

    data class FileList(
            val no:	Int,
            val seq_no:	String,
            val ctr_cd:	String,
            val brc_cd:	String,
            val prj_cd:	String,
            val sync_file_tpcd:	String,
            val file_path:	String,
            val file_nm:	String,
            val regr_id:	String,
            val reg_dt:	String,

            var status: FileStatus = FileStatus.ReadyToDownload
    ) {
        val fileName: String
            get() = file_nm.substringBeforeLast(".")
        val extension: String
            get() = file_nm.substringAfterLast(".")
        val fileId: String
            get() = file_nm.substringBeforeLast("_")
    }

    enum class FileStatus {
        Standby, ReadyToDownload, Downloading, DownloadComplete, Syncing, SyncComplete
    }
}