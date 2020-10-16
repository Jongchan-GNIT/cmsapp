package kr.goodneighbors.cms.service.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import kr.goodneighbors.cms.service.entities.ACL
import kr.goodneighbors.cms.service.entities.APP_DATA_HISTORY
import kr.goodneighbors.cms.service.entities.APP_SEARCH_HISTORY
import kr.goodneighbors.cms.service.entities.ATCH_FILE
import kr.goodneighbors.cms.service.entities.BMI
import kr.goodneighbors.cms.service.entities.BRC
import kr.goodneighbors.cms.service.entities.CD
import kr.goodneighbors.cms.service.entities.CH_BSC
import kr.goodneighbors.cms.service.entities.CH_CUSL_INFO
import kr.goodneighbors.cms.service.entities.CH_MST
import kr.goodneighbors.cms.service.entities.CH_SPSL_INFO
import kr.goodneighbors.cms.service.entities.CTR
import kr.goodneighbors.cms.service.entities.DROPOUT
import kr.goodneighbors.cms.service.entities.DROPOUT_PLAN
import kr.goodneighbors.cms.service.entities.EDU
import kr.goodneighbors.cms.service.entities.FMLY
import kr.goodneighbors.cms.service.entities.GIFT_BRKDW
import kr.goodneighbors.cms.service.entities.GMNY
import kr.goodneighbors.cms.service.entities.HLTH
import kr.goodneighbors.cms.service.entities.INTV
import kr.goodneighbors.cms.service.entities.LETR
import kr.goodneighbors.cms.service.entities.LETR_ATCH_FILE
import kr.goodneighbors.cms.service.entities.LIV_COND
import kr.goodneighbors.cms.service.entities.MOD_HIS_INFO
import kr.goodneighbors.cms.service.entities.NOTI_INFO
import kr.goodneighbors.cms.service.entities.PRJ
import kr.goodneighbors.cms.service.entities.PRSN_ANS_INFO
import kr.goodneighbors.cms.service.entities.PRSN_INFO
import kr.goodneighbors.cms.service.entities.RELSH
import kr.goodneighbors.cms.service.entities.REMRK
import kr.goodneighbors.cms.service.entities.RETN
import kr.goodneighbors.cms.service.entities.RPLY
import kr.goodneighbors.cms.service.entities.RPT_BSC
import kr.goodneighbors.cms.service.entities.RPT_DIARY
import kr.goodneighbors.cms.service.entities.SCHL
import kr.goodneighbors.cms.service.entities.SIBL
import kr.goodneighbors.cms.service.entities.SPLY_PLAN
import kr.goodneighbors.cms.service.entities.SRVC
import kr.goodneighbors.cms.service.entities.SWRT
import kr.goodneighbors.cms.service.entities.USER_INFO
import kr.goodneighbors.cms.service.entities.VLG

@Database(entities = [
    ACL::class, ATCH_FILE::class,
    BMI::class, BRC::class,
    CD::class, CH_BSC::class, CH_CUSL_INFO::class, CH_MST::class, CH_SPSL_INFO::class, CTR::class,
    DROPOUT::class, DROPOUT_PLAN::class,
    EDU::class,
    FMLY::class,
    GIFT_BRKDW::class, GMNY::class,
    HLTH::class,
    INTV::class,
    LETR::class, LETR_ATCH_FILE::class, LIV_COND::class,
    MOD_HIS_INFO::class,
    NOTI_INFO::class,
    PRJ::class, PRSN_ANS_INFO::class, PRSN_INFO::class,
    RELSH::class, REMRK::class, RETN::class, RPLY::class, RPT_BSC::class, RPT_DIARY::class,
    SCHL::class, SIBL::class, SPLY_PLAN::class, SRVC::class, SWRT::class,
    USER_INFO::class,
    VLG::class,
    APP_DATA_HISTORY::class, APP_SEARCH_HISTORY::class
], version = 1, exportSchema = false)
abstract class CmsDb : RoomDatabase() {
    abstract fun userInfoDao(): UserInfoDao

    abstract fun reportDao(): ReportDao

    abstract fun commonDao(): CommonDao

    abstract fun childlistDao(): ChildlistDao

    abstract fun syncDao(): SyncDao

    abstract fun propDao(): PropDao

    abstract fun homeDao(): HomeDao

    abstract fun statisticsDao(): StatisticsDao

    abstract fun settingDao(): SettingDao

    companion object {
        const val DB_NAME = "gn.db"
    }
}