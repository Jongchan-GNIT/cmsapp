package kr.goodneighbors.cms.common

class Constants {
    companion object {
        const val BUILD = "dev"   //if (BuildConfig.DEBUG) {"dev"} else {"real"}
        const val TAG = "MY_CHILD"

        const val DIR_HOME = "GoodNeighbors"
        const val DIR_TEMP = "tmp"
        const val DIR_INIT = "init"
        const val DIR_CONTENTS = "contents"
        const val DIR_DOWNLOAD = "downloads"
        const val DIR_IMPORT = "import"
        const val DIR_EXPORT = "export"
        const val DIR_EXPORT_OFFLINE = "export_offline"
        const val DIR_FAIL = "fail"
        const val DIR_LOG = "logs"

        // ACRA
        const val ACRA_MAIL_SENDER = "gnitcenter@gmail.com"

        // AWS
        const val CF = "http://d20ljwr68qg2dx.cloudfront.net"

        // API
        @Suppress("ConstantConditionIf")
        val API_URL = if (BUILD == "real") "http://mychild.goodneighbors.org:18080" else "http://52.79.47.95:18080"

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        const val SERVICE_SERVICE = ""
        const val SERVICE_CIF = "1"
        const val SERVICE_APR = "2"
        const val SERVICE_DRO = "3"
        const val SERVICE_ACL = "4"
        const val SERVICE_GML = "5"
    }
}