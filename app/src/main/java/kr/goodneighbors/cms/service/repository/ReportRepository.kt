package kr.goodneighbors.cms.service.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.persistence.db.SimpleSQLiteQuery
import android.content.SharedPreferences
import android.widget.Toast
import com.google.gson.GsonBuilder
import kr.goodneighbors.cms.common.GNLocaleManager
import kr.goodneighbors.cms.extensions.toDateFormat
import kr.goodneighbors.cms.service.db.CommonDao
import kr.goodneighbors.cms.service.db.ReportDao
import kr.goodneighbors.cms.service.db.SyncDao
import kr.goodneighbors.cms.service.entities.APP_DATA_HISTORY
import kr.goodneighbors.cms.service.entities.ATCH_FILE
import kr.goodneighbors.cms.service.entities.CD
import kr.goodneighbors.cms.service.entities.CH_CUSL_INFO
import kr.goodneighbors.cms.service.entities.CH_MST
import kr.goodneighbors.cms.service.entities.MOD_HIS_INFO
import kr.goodneighbors.cms.service.entities.RPT_BSC
import kr.goodneighbors.cms.service.entities.SIBL
import kr.goodneighbors.cms.service.entities.SRVC
import kr.goodneighbors.cms.service.model.AclEditViewItem
import kr.goodneighbors.cms.service.model.AclEditViewItemSearch
import kr.goodneighbors.cms.service.model.AclListItem
import kr.goodneighbors.cms.service.model.AprEditProfileImageItem
import kr.goodneighbors.cms.service.model.AprEditViewItem
import kr.goodneighbors.cms.service.model.AprEditViewSearchItem
import kr.goodneighbors.cms.service.model.CifEditViewItem
import kr.goodneighbors.cms.service.model.CounselingListItem
import kr.goodneighbors.cms.service.model.DropoutEditItem
import kr.goodneighbors.cms.service.model.DropoutEditSearchItem
import kr.goodneighbors.cms.service.model.DuplicateChildItem
import kr.goodneighbors.cms.service.model.GiftItem
import kr.goodneighbors.cms.service.model.GmEditItem
import kr.goodneighbors.cms.service.model.GmLetterEditItem
import kr.goodneighbors.cms.service.model.GmlEditItemSearch
import kr.goodneighbors.cms.service.model.GmlListItem
import kr.goodneighbors.cms.service.model.NextCounselingIndex
import kr.goodneighbors.cms.service.model.PersonalInfoItem
import kr.goodneighbors.cms.service.model.ProfileViewItem
import kr.goodneighbors.cms.service.model.ProvidedServiceEditItem
import kr.goodneighbors.cms.service.model.ProvidedServiceEditSearchItem
import kr.goodneighbors.cms.service.model.ProvidedServiceListItem
import kr.goodneighbors.cms.service.model.ProvidedServiceRegistItem
import kr.goodneighbors.cms.service.model.ProvidedServiceRegistSearchItem
import kr.goodneighbors.cms.service.model.ReportListItem
import kr.goodneighbors.cms.service.model.SiblingInformationItem
import kr.goodneighbors.cms.service.model.SpinnerOption
import kr.goodneighbors.cms.service.model.VillageLocation
import org.jetbrains.anko.support.v4.toast
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Suppress("LocalVariableName")
@Singleton
class ReportRepository @Inject constructor(
        private val reportDao: ReportDao,
        private val syncDao: SyncDao,
        private val commonDao: CommonDao,
        private val commonRepository: CommonRepository,
        private val preferences: SharedPreferences
) {
    private val logger: Logger by lazy {
        LoggerFactory.getLogger(ReportRepository::class.java)
    }

    private val initStatusLive = MutableLiveData<String>()

    fun getInitStatus(): LiveData<String>? {
        return initStatusLive
    }

    fun initAllChild(chmstList: List<CH_MST>) {
        Thread(Runnable {
            reportDao.saveAllCH_MST(chmstList)
        }).start()
    }

    val initProgressReport = MutableLiveData<String>()

    private var reportCount = 0
    private var isFireLastReport = false
    private var totalReportCount = 0
    private var processedReportCount = 0

    fun initAll(reports: List<RPT_BSC>, startProgress: Boolean) {
        totalReportCount += reports.size
        reportCount++
        initProgressReport.postValue("$processedReportCount of $totalReportCount")
        Thread(Runnable {
            if (startProgress) {
                isFireLastReport = true
                initStatusLive.postValue("P")
            }

            logger.debug("report insert start : $reportCount")

            reportDao.initAll(reports)

            reports.forEach { report ->
                reportDao.saveRPT_BSC(report)

                if (report.CH_BSC != null) {
                    report.CH_BSC!!.RCP_NO = report.RCP_NO
                    reportDao.saveCH_BSC(report.CH_BSC!!)
                }

                if (report.FMLY != null) {
                    report.FMLY!!.RCP_NO = report.RCP_NO
                    reportDao.saveFMLY(report.FMLY!!)
                }

                if (report.INTV != null) {
                    report.INTV!!.RCP_NO = report.RCP_NO
                    reportDao.saveINTV(report.INTV!!)
                }

                if (report.LIV_COND != null) {
                    report.LIV_COND!!.RCP_NO = report.RCP_NO
                    reportDao.insertLIV_COND(report.LIV_COND!!)
                }

                if (report.SWRT != null) {
                    report.SWRT!!.RCP_NO = report.RCP_NO
                    reportDao.insertSWRT(report.SWRT!!)
                }

                if (report.HLTH != null) {
                    report.HLTH!!.RCP_NO = report.RCP_NO
                    reportDao.saveHLTH(report.HLTH!!)
                }

                if (report.REMRK != null) {
                    report.REMRK!!.RCP_NO = report.RCP_NO
                    reportDao.saveREMRK(report.REMRK!!)
                }

                if (report.EDU != null) {
                    report.EDU!!.RCP_NO = report.RCP_NO
                    reportDao.saveEDU(report.EDU!!)
                }

                if (report.RPT_DIARY != null) {
                    report.RPT_DIARY!!.RCP_NO = report.RCP_NO
                    reportDao.insertRPT_DIARY(report.RPT_DIARY!!)
                }

                if (report.ACL != null) {
                    report.ACL!!.RCP_NO = report.RCP_NO
                    reportDao.insertACL(report.ACL!!)
                }

                if (report.DROPOUT != null) {
                    report.DROPOUT!!.RCP_NO = report.RCP_NO
                    reportDao.insertDROPOUT(report.DROPOUT!!)
                }

                if (report.RPLY != null) {
                    report.RPLY!!.RCP_NO = report.RCP_NO
                    reportDao.insertRPLY(report.RPLY!!)
                }

                if (report.DROPOUT_PLAN != null) {
                    report.DROPOUT_PLAN!!.RCP_NO = report.RCP_NO
                    reportDao.insertDROPOUT_PLAN(report.DROPOUT_PLAN!!)
                }

                if (report.GIFT_BRKDW != null) {
                    report.GIFT_BRKDW!!.forEach {
                        it.RCP_NO = report.RCP_NO
                    }
                    reportDao.insertAllGIFT_BRKDW(report.GIFT_BRKDW!!)
                }

                if (report.SIBL != null) {
                    report.SIBL!!.forEach {
                        it.RCP_NO = report.RCP_NO
                    }
                    reportDao.saveAllSIBL(report.SIBL!!)
                }

                if (report.ATCH_FILE != null) {
                    report.ATCH_FILE!!.forEach {
                        it.RCP_NO = report.RCP_NO
                    }
                    reportDao.insertAllATCH_FILE(report.ATCH_FILE!!)
                }

                if (report.RETN != null) {
                    report.RETN!!.forEach {
                        it.RCP_NO = report.RCP_NO
                    }
                    reportDao.insertAllRETN(report.RETN!!)
                }

                if (report.CH_SPSL_INFO != null) {
                    report.CH_SPSL_INFO!!.forEach {
                        it.RCP_NO = report.RCP_NO
                    }
                    reportDao.saveAllCH_SPSL_INFO(report.CH_SPSL_INFO!!)
                }

                if (report.PRSN_ANS_INFO != null) {
                    report.PRSN_ANS_INFO!!.forEach {
                        it.RCP_NO = report.RCP_NO
                    }
                    reportDao.saveAllPRSN_ANS_INFO(report.PRSN_ANS_INFO!!)
                }
            }

            processedReportCount += reports.size

            initProgressReport.postValue("$processedReportCount of $totalReportCount")

            reportCount--
            if (isFireLastReport && reportCount == 0) {
                initStatusLive.postValue("D")
            }

            logger.debug("report insert finished : reportCount = $reportCount")

        }).start()
    }

    ////////////////////////// COMMON //////////////////////////////
    private lateinit var villageLocation: MutableLiveData<VillageLocation>

    fun getLocationOfVillage(code: String): MutableLiveData<VillageLocation> {
        villageLocation = MutableLiveData()

        Thread(Runnable {
            logger.debug("getLocationOfVillage : $code")
            val vill = reportDao.getLocationOfVillage(code)

            villageLocation.postValue(vill)
        }).start()

        return villageLocation
    }

    private fun generateSupplyPlan(): HashMap<String, ArrayList<SpinnerOption>> {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val ctrCd = preferences.getString("user_ctr_cd", "")
        val prjCd = preferences.getString("user_prj_cd", "")

        val supplyPlanItems = HashMap<String, ArrayList<SpinnerOption>>()

        fun generateSupplyPlanItem(dnctr_cd: String, ctr_cd: String, prj_cd: String, year: Int): ArrayList<SpinnerOption> {
            val genSupplyPlanItems = ArrayList<SpinnerOption>()
            genSupplyPlanItems.add(SpinnerOption("${year}00", "${year}00"))
            commonDao.findAllSupplyPlan(dnctr_cd, ctr_cd, prj_cd, year).forEach {
                // JAN, FEB, MAR, APRL, MAY, JUNE, JULY, AUG, SEPT, OCT, NOV, DEC
                if ((it.JAN
                                ?: "0").toInt() > 0) genSupplyPlanItems.add(SpinnerOption("${it.YEAR}01", "${it.YEAR}01"))
                if ((it.FEB
                                ?: "0").toInt() > 0) genSupplyPlanItems.add(SpinnerOption("${it.YEAR}02", "${it.YEAR}02"))
                if ((it.MAR
                                ?: "0").toInt() > 0) genSupplyPlanItems.add(SpinnerOption("${it.YEAR}03", "${it.YEAR}03"))
                if ((it.APRL
                                ?: "0").toInt() > 0) genSupplyPlanItems.add(SpinnerOption("${it.YEAR}04", "${it.YEAR}04"))
                if ((it.MAY
                                ?: "0").toInt() > 0) genSupplyPlanItems.add(SpinnerOption("${it.YEAR}05", "${it.YEAR}05"))
                if ((it.JUNE
                                ?: "0").toInt() > 0) genSupplyPlanItems.add(SpinnerOption("${it.YEAR}06", "${it.YEAR}06"))
                if ((it.JULY
                                ?: "0").toInt() > 0) genSupplyPlanItems.add(SpinnerOption("${it.YEAR}07", "${it.YEAR}07"))
                if ((it.AUG
                                ?: "0").toInt() > 0) genSupplyPlanItems.add(SpinnerOption("${it.YEAR}08", "${it.YEAR}08"))
                if ((it.SEPT
                                ?: "0").toInt() > 0) genSupplyPlanItems.add(SpinnerOption("${it.YEAR}09", "${it.YEAR}09"))
                if ((it.OCT
                                ?: "0").toInt() > 0) genSupplyPlanItems.add(SpinnerOption("${it.YEAR}10", "${it.YEAR}10"))
                if ((it.NOV
                                ?: "0").toInt() > 0) genSupplyPlanItems.add(SpinnerOption("${it.YEAR}11", "${it.YEAR}11"))
                if ((it.DEC
                                ?: "0").toInt() > 0) genSupplyPlanItems.add(SpinnerOption("${it.YEAR}12", "${it.YEAR}12"))
            }

            return genSupplyPlanItems
        }

        commonDao.findAllSupplyPlanDnctr().forEach { dnctr_cd ->
            val items = ArrayList<SpinnerOption>()
            items.addAll(generateSupplyPlanItem(dnctr_cd, ctrCd, prjCd, currentYear))
            items.addAll(generateSupplyPlanItem(dnctr_cd, ctrCd, prjCd, currentYear - 1))

            supplyPlanItems[dnctr_cd] = items
        }
        return supplyPlanItems
    }

    private var getBMIResult = MutableLiveData<CD>()
    fun getBMI(birth: String, height: String, weight: String, gender: String): MutableLiveData<CD> {
        getBMIResult = MutableLiveData()

        Thread(Runnable {
            if (birth.isNotBlank() && height.isNotBlank() && weight.isNotBlank() && gender.isNotBlank()) {
                try {
                    val locale = preferences.getString(GNLocaleManager.SELECTED_LANGUAGE, "en")

                    val d1 = Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

                    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ENGLISH)
                    val d2 = LocalDate.parse(birth, formatter)

                    val months = ChronoUnit.MONTHS.between(d2, d1)

                    // (몸무게 / ((키/100) * (키/100))) = BMI지수
                    val df = DecimalFormat("#.#")
                    df.roundingMode = RoundingMode.HALF_UP
                    val bmiIndex = df.format(weight.toDouble() / ((height.toDouble() / 100) * (height.toDouble() / 100)))

                    logger.debug("bmi index = $bmiIndex")

                    val bmiCode = commonDao.findBMI(months, gender, bmiIndex) ?: "337001"
                    val bmi = commonDao.findCode("337", bmiCode, locale)

                    getBMIResult.postValue(bmi)
                } catch (e: Exception) {
                    e.printStackTrace()
                    getBMIResult.postValue(null)
                }
            }


        }).start()

        return getBMIResult
    }

    private var findAllSiblingSponsorshipItem = MutableLiveData<List<RPT_BSC>>()
    fun findAllSiblingSponsorship(word: String?): LiveData<List<RPT_BSC>>? {
        findAllSiblingSponsorshipItem = MutableLiveData()

        Thread(Runnable {
            val items = ArrayList<RPT_BSC>()
            reportDao.findAllSiblingSponsorshipByWord("%$word%").forEach { child ->
                reportDao.findLastReportByChild(child.CHRCP_NO)?.apply {
                    CH_MST = child
                    reportDao.findATCH_FILE(rcp_no = RCP_NO, img_dvcd = "331005")?.apply {
                        ATCH_FILE = arrayListOf(this)
                    }

                    items.add(this)
                }
                child
            }

            findAllSiblingSponsorshipItem.postValue(items)
        }).start()

        return findAllSiblingSponsorshipItem
    }

    private var findAllDuplicateChildrenItem: MutableLiveData<List<DuplicateChildItem>> = MutableLiveData()
    fun findAllDuplicateChildren(firstName: String, middleName: String, lastName: String, gender: String, birth: String): LiveData<List<DuplicateChildItem>> {
        findAllDuplicateChildrenItem = MutableLiveData()

        Thread(Runnable {
            logger.debug("----------firstName = $firstName, middleName = $middleName, lastName = $lastName, gender = $gender, birth = $birth")
            val children = reportDao.findAllDuplicateChildren(firstName.toUpperCase().trim(), middleName.toUpperCase().trim(), lastName.toUpperCase().trim(), gender, birth)
            logger.debug("---------- children = $children")
            children.forEach {
                reportDao.findLastReportOfAllStateByChild(it.CHRCP_NO)?.apply {
                    reportDao.findATCH_FILE(rcp_no = RCP_NO, img_dvcd = "331001")?.apply {
                        it.FILE_PATH = "$FILE_PATH/$FILE_NM"
                    }
                }
            }


            findAllDuplicateChildrenItem.postValue(children)
        }).start()

        return findAllDuplicateChildrenItem
    }

    private lateinit var findAllSiblingSponsorshipByChildItem: MutableLiveData<List<SiblingInformationItem>>
    fun findAllSiblingSponsorshipByChild(chrcp_no: String): LiveData<List<SiblingInformationItem>> {
        findAllSiblingSponsorshipByChildItem = MutableLiveData()

        Thread(Runnable {
            val items = ArrayList<SiblingInformationItem>()
            reportDao.findLastReportByChild(chrcp_no)?.apply {
                reportDao.findAllSiblById(RCP_NO)?.forEach {
                    reportDao.findLastReportByChild(it.CHRCP_NO)?.apply {
                        val child = reportDao.findChMstById(this.CHRCP_NO)
                        val base = reportDao.findChBscById(this.RCP_NO)
                        val filePath = reportDao.findATCH_FILE(rcp_no = RCP_NO, img_dvcd = "331001")?.let { f ->
                            "${f.FILE_PATH}/${f.FILE_NM}"
                        }
                        items.add(
                                SiblingInformationItem(CHRCP_NO = child.CHRCP_NO
                                        , CTR_CD = child.CTR_CD, BRC_CD = child.BRC_CD, PRJ_CD = child.PRJ_CD, CH_CD = child.CH_CD
                                        , CH_EFNM = child.CH_EFNM
                                        ?: "", CH_EMNM = child.CH_EMNM, CH_ELNM = child.CH_ELNM ?: ""
                                        , GNDR = base?.GNDR, BDAY = base?.BDAY
                                        , FILE_PATH = filePath)
                        )
                    }

                }
            }

            findAllSiblingSponsorshipByChildItem.postValue(if (items.isEmpty()) null else items)
        }).start()

        return findAllSiblingSponsorshipByChildItem
    }

    private lateinit var findChildByIdItem: MutableLiveData<CH_MST>
    fun findChildById(_chrcp_no: String): LiveData<CH_MST> {
        findChildByIdItem = MutableLiveData()

        Thread(Runnable {
            findChildByIdItem.postValue(reportDao.findChMstById(_chrcp_no))
        }).start()

        return findChildByIdItem
    }

    private fun getReportById(rcp_no: String): RPT_BSC? {
        return reportDao.getRptBscById(rcp_no)?.let {
            with(it) {
                CH_MST = reportDao.findChMstById(CHRCP_NO)
                CH_BSC = reportDao.findChBscById(RCP_NO)
                EDU = reportDao.findEduById(RCP_NO)
                FMLY = reportDao.findFmlyById(RCP_NO)
                HLTH = reportDao.findHlthById(RCP_NO)
                INTV = reportDao.findIntvById(RCP_NO)
                REMRK = reportDao.findRemarkById(RCP_NO)
                DROPOUT_PLAN = reportDao.findDropoutPlanById(RCP_NO)
                SWRT = reportDao.findSwrtById(RCP_NO)

                SIBL = ArrayList(reportDao.findAllSiblById(RCP_NO))
                CH_SPSL_INFO = ArrayList(reportDao.findAllChSpslInfo(RCP_NO))
                PRSN_ANS_INFO = ArrayList(reportDao.findAllPrsnAnsInfo(RCP_NO))
                ATCH_FILE = ArrayList(reportDao.findAllAtchFileById(RCP_NO))

                if (SIBL.isNullOrEmpty()) SIBL = null
                if (CH_SPSL_INFO.isNullOrEmpty()) CH_SPSL_INFO = null
                if (PRSN_ANS_INFO.isNullOrEmpty()) PRSN_ANS_INFO = null
                if (ATCH_FILE.isNullOrEmpty()) ATCH_FILE = null

                this
            }
        }
    }

    ////////////////////////// CIF //////////////////////////////
    private var cifEditViewItem: MutableLiveData<CifEditViewItem> = MutableLiveData()
    fun getCifEditViewItem(rcp_no: String?): LiveData<CifEditViewItem>? {
        logger.debug("getCifEditViewItem($rcp_no)")

        cifEditViewItem = MutableLiveData()

        Thread(Runnable {
            val locale = preferences.getString(GNLocaleManager.SELECTED_LANGUAGE, "en")

            val item = CifEditViewItem(
                    codeVillage = commonDao.findAllVillage(),
                    codeSchoolName = commonDao.findAllSchool(),
                    codeSupplyPlan = generateSupplyPlan(),
                    codeDisability = commonRepository.findAllCommonCodeByGroupCode("78"),
                    codeDisabilityReason = commonRepository.findAllCommonCodeByGroupCode("104"),
                    codeIllness = commonRepository.findAllCommonCodeByGroupCode("99"),
                    codeIllnessReason = commonRepository.findAllCommonCodeByGroupCode("107"),
                    codeSchoolType = commonRepository.findAllCommonCodeByGroupCode("70"),
                    codeSchoolTypeReason = commonRepository.findAllCommonCodeByGroupCode("106"),
                    codeSupportCountry = commonRepository.findAllCommonCodeByGroupCode("79"),
                    codeRelationship = commonRepository.findAllCommonCodeByGroupCode("67"),
                    codeInterviewPlace = commonRepository.findAllCommonCodeByGroupCode("90"),
                    codeFatherReason = commonRepository.findAllCommonCodeByGroupCode("102"),
                    codeMainGuardian = commonRepository.findAllCommonCodeByGroupCode("66"),
                    codeSpecialCase = commonRepository.findAllCommonCodeByGroupCode("115"))


            val codePersonalInfoItem = ArrayList<PersonalInfoItem>()
            if (!rcp_no.isNullOrBlank()) {
                getReportById(rcp_no)?.apply {
                    logger.debug("sibl ----- $SIBL")
                    SIBL = SIBL?.let {
                        val sibl = ArrayList<SIBL>()
                        it.forEach { s ->
                            reportDao.findChMstById(s.CHRCP_NO).apply {
                                sibl.add(SIBL(RCP_NO = RCP_NO, CHRCP_NO = "${this.CTR_CD}-${this.BRC_CD}${this.PRJ_CD}-${this.CH_CD}"))
                            }
                        }
                        sibl
                    }
                    PRSN_ANS_INFO?.apply {
                        // 등록된 personal info 사용
                        commonDao.findAllPersonalInformationByReport(RCP_NO, locale).forEach {
                            codePersonalInfoItem.add(PersonalInfoItem(master = it, detail = commonRepository.findAllCommonCodeByGroupCode(it.CHGP_CD
                                    ?: "")))
                        }
                        if (codePersonalInfoItem.isNotEmpty()) {
                            item.codePersonalInfo = codePersonalInfoItem
                        }

                    } ?: run {
                        // 등록된 리포트의 저장 연도로 personal info 조회
                        commonDao.findAllPersonalInformationByYear(REG_DT?.toDateFormat("yyyy") ?: "", locale).forEach {
                            codePersonalInfoItem.add(PersonalInfoItem(master = it, detail = commonRepository.findAllCommonCodeByGroupCode(it.CHGP_CD
                                    ?: "")))
                        }
                        if (codePersonalInfoItem.isNotEmpty()) {
                            item.codePersonalInfo = codePersonalInfoItem
                        }
                    }

                    if (RPT_STCD == "2" || RPT_STCD == "15") {
                        item.returns = reportDao.findAllReturns(rcp_no = RCP_NO, locale = locale)
                    }

                    item.rpt_bsc = this
                }
            } else {
                // 현재 연도로 personal info 조회
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)

                commonDao.findAllPersonalInformationByYear(currentYear.toString(), locale).forEach {
                    codePersonalInfoItem.add(PersonalInfoItem(master = it, detail = commonRepository.findAllCommonCodeByGroupCode(it.CHGP_CD
                            ?: "")))
                }
                if (codePersonalInfoItem.isNotEmpty()) {
                    item.codePersonalInfo = codePersonalInfoItem
                }
            }

            cifEditViewItem.postValue(item)
        }).start()

        return cifEditViewItem
    }

    private lateinit var saveCifResult: MutableLiveData<RPT_BSC>
    fun saveCif(report: RPT_BSC): LiveData<RPT_BSC> {
        saveCifResult = MutableLiveData()

        Thread(Runnable {
            try {
                report.apply {
                    reportDao.saveRPT_BSC(this)

                    CH_MST?.apply { reportDao.saveCH_MST(this) }
                    CH_BSC?.apply { reportDao.saveCH_BSC(this) }
                    EDU?.apply { reportDao.saveEDU(this) }
                    FMLY?.apply { reportDao.saveFMLY(this) }
                    HLTH?.apply { reportDao.saveHLTH(this) }
                    INTV?.apply { reportDao.saveINTV(this) }
                    REMRK?.apply { reportDao.saveREMRK(this) }
                    RPT_DIARY?.apply { reportDao.saveRPT_DIARY(this) }

                    reportDao.removeAllCH_SPSL_INFOByReport(RCP_NO)
                    CH_SPSL_INFO?.apply { reportDao.saveAllCH_SPSL_INFO(this) }

                    reportDao.removeAllPRSN_ANS_INFOByReport(RCP_NO)
                    PRSN_ANS_INFO?.apply { reportDao.saveAllPRSN_ANS_INFO(this) }

                    reportDao.removeAllSIBLByReport(RCP_NO)
                    val sibl = ArrayList<SIBL>()
                    SIBL?.forEach {
                        reportDao.findChMstByCdpCode(it.CHRCP_NO)?.apply { sibl.add(SIBL(RCP_NO = RCP_NO, CHRCP_NO = this.CHRCP_NO)) }
//                        reportDao.saveAllSIBL(this)
                    }
                    if (sibl.isNotEmpty()) {
                        reportDao.saveAllSIBL(sibl)
                    }

                    reportDao.deleteFileByReport(RCP_NO)
                    ATCH_FILE?.apply { reportDao.insertAllATCH_FILE(this) }

                    saveModHisInfo(RCP_NO, if (UPD_DT == null) "C" else "U")

                    saveCifResult.postValue(getReportById(RCP_NO))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                logger.error(e.message)
            }
        }).start()

        return saveCifResult
    }

    private var aprEditViewItem: MutableLiveData<AprEditViewItem> = MutableLiveData()
    fun getAprEditViewItem(search: AprEditViewSearchItem): LiveData<AprEditViewItem> {
        logger.debug("getAprEditViewItem($search)")
        aprEditViewItem = MutableLiveData()

        Thread(Runnable {
            val locale = preferences.getString(GNLocaleManager.SELECTED_LANGUAGE, "en")

            val item = AprEditViewItem()
            item.codeVillage = commonDao.findAllVillage()
            item.codeDisability = commonRepository.findAllCommonCodeByGroupCode("78")
            item.codeDisabilityReason = commonRepository.findAllCommonCodeByGroupCode("104")
            item.codeIllness = commonRepository.findAllCommonCodeByGroupCode("99")
            item.codeIllnessReason = commonRepository.findAllCommonCodeByGroupCode("107")
            item.codeSchoolType = commonRepository.findAllCommonCodeByGroupCode("60")
            item.codeSchoolTypeReason = commonRepository.findAllCommonCodeByGroupCode("106")
            item.codeSchoolName = commonDao.findAllSchool()
            item.codeRelationship = commonRepository.findAllCommonCodeByGroupCode("67")
            item.codeInterviewPlace = commonRepository.findAllCommonCodeByGroupCode("90")
            item.codeFatherReason = commonRepository.findAllCommonCodeByGroupCode("102")
            item.codeMainGuardian = commonRepository.findAllCommonCodeByGroupCode("66")
            item.codeSpecialCase = commonRepository.findAllCommonCodeByGroupCode("115")
            item.codeFuturePlan = commonRepository.findAllCommonCodeByGroupCode("340")
            item.codeContinuReason = commonRepository.findAllCommonCodeByGroupCode("341")

            val codeMap = HashMap<String, List<CD>>()
            codeMap["351"] = commonRepository.findAllCommonCodeByGroupCodeToEntity("351") // gender
            codeMap["103"] = commonRepository.findAllCommonCodeByGroupCodeToEntity("103") // birth date
            codeMap["105"] = commonRepository.findAllCommonCodeByGroupCodeToEntity("105") // height
            codeMap["109"] = commonRepository.findAllCommonCodeByGroupCodeToEntity("109") // weight

            codeMap["270"] = commonRepository.findAllCommonCodeByGroupCodeToEntity("270") // 장애 변경 사유 질문
            codeMap["269"] = commonRepository.findAllCommonCodeByGroupCodeToEntity("269") // 누락 사유
            codeMap["252"] = commonRepository.findAllCommonCodeByGroupCodeToEntity("252") // 발생 사유
            codeMap["254"] = commonRepository.findAllCommonCodeByGroupCodeToEntity("254") // 선천적 장애 원인 사유
            codeMap["253"] = commonRepository.findAllCommonCodeByGroupCodeToEntity("253") // 선천적 장애 원인 변경 사유
            codeMap["312"] = commonRepository.findAllCommonCodeByGroupCodeToEntity("312") // 변경 사유

            codeMap["279"] = commonRepository.findAllCommonCodeByGroupCodeToEntity("279") // illness 변경 사유 질문
            codeMap["275"] = commonRepository.findAllCommonCodeByGroupCodeToEntity("275") // 중병 누락 사유
            codeMap["276"] = commonRepository.findAllCommonCodeByGroupCodeToEntity("276") // 변경 사유
            codeMap["278"] = commonRepository.findAllCommonCodeByGroupCodeToEntity("278") // 지속 사유
            codeMap["277"] = commonRepository.findAllCommonCodeByGroupCodeToEntity("277") // 원인 동일 사유
            codeMap["310"] = commonRepository.findAllCommonCodeByGroupCodeToEntity("310") // 질병 누락 사유
            codeMap["311"] = commonRepository.findAllCommonCodeByGroupCodeToEntity("311") // 발생 사유

            codeMap["265"] = commonRepository.findAllCommonCodeByGroupCodeToEntity("265") // father 추가 질의
            codeMap["266"] = commonRepository.findAllCommonCodeByGroupCodeToEntity("266") // mother 추가 질의
            codeMap["235"] = commonRepository.findAllCommonCodeByGroupCodeToEntity("235") // 부모비동거사유변경
            codeMap["237"] = commonRepository.findAllCommonCodeByGroupCodeToEntity("237") // 부모재동거사유
            codeMap["234"] = commonRepository.findAllCommonCodeByGroupCodeToEntity("234") // 부모미상사유


            item.codeMap = codeMap

            reportDao.findChMstById(search.chrcp_no).apply {
                commonDao.findCode(groupCode = "79", code = DNCTR_CD ?: "", locale = locale)?.apply {
                    item.supportCountry = CD_ENM
                }
                item.ch_mst = this
            }

            search.rcp_no?.apply {
                item.rpt_bsc = getReportById(this)
                if (item.rpt_bsc != null) {
                    if (item.rpt_bsc!!.RPT_STCD == "2" || item.rpt_bsc!!.RPT_STCD == "15") {
                        item.returns = reportDao.findAllReturns(rcp_no = item.rpt_bsc!!.RCP_NO, locale = locale)
                    }
                }
            }

            if (search.rcp_no.isNullOrBlank()) {
                reportDao.findPrevRptBscByChildAndType(chrcp_no = search.chrcp_no, rpt_dvcd = arrayOf("1", "2"), year = search.year)?.apply {
                    item.prev_rpt_bsc = getReportById(RCP_NO)

                }

            } else {
                reportDao.findPrevRptBscByChildAndTypeNotId(chrcp_no = search.chrcp_no, rpt_dvcd = arrayOf("1", "2"), year = search.year, rcp_no = search.rcp_no!!)?.apply {
                    item.prev_rpt_bsc = getReportById(RCP_NO)
                }
            }

            if(item.prev_rpt_bsc?.RPT_DVCD == "1" && item.prev_rpt_bsc?.EDU?.SCTP_CD== "1") {
                item.prev_rpt_bsc?.EDU?.SCTP_CD = "7"
            }

            if(item.prev_rpt_bsc?.FMLY?.MGDN_CD.isNullOrBlank()) {
                if(item.prev_rpt_bsc?.FMLY?.FA_NUM != "1"){
                    if(item.prev_rpt_bsc?.FMLY?.FA_LARCD =="1"){
                        item.prev_rpt_bsc?.FMLY?.FA_LARCD = "7"
                    }else if(item.prev_rpt_bsc?.FMLY?.FA_LARCD =="3"){
                        item.prev_rpt_bsc?.FMLY?.FA_LARCD = "8"
                    }
                }

                if(item.prev_rpt_bsc?.FMLY?.MO_NUM != "1"){
                    if(item.prev_rpt_bsc?.FMLY?.MO_LARCD =="1"){
                        item.prev_rpt_bsc?.FMLY?.MO_LARCD = "7"
                    }else if(item.prev_rpt_bsc?.FMLY?.MO_LARCD =="3"){
                        item.prev_rpt_bsc?.FMLY?.MO_LARCD = "8"
                    }
                }
            }


            val prevProfileImages = ArrayList<AprEditProfileImageItem>()
            reportDao.findPrevTwoYearsOfRptBscByChildAndType(chrcp_no = search.chrcp_no, year = search.year).forEach { report ->
                val prevProfileImage = AprEditProfileImageItem(year = report.YEAR ?: "")
                reportDao.findAllAtchFileById(report.RCP_NO)?.forEach {
                    when (it.IMG_DVCD) {
                        "331001" -> {
                            prevProfileImage.generalImagePath = "${it.FILE_PATH}/${it.FILE_NM}"
                        }
                        "331005" -> {
                            prevProfileImage.thumbnailImagePath = "${it.FILE_PATH}/${it.FILE_NM}"
                        }
                    }
                }
                prevProfileImages.add(prevProfileImage)
            }

            if (item.rpt_bsc == null || item.rpt_bsc!!.ATCH_FILE == null) {
                prevProfileImages.add(0, AprEditProfileImageItem(year = search.year, isEditable = true))
            } else {
                val currentProfileImage = AprEditProfileImageItem(year = search.year)
                item.rpt_bsc!!.ATCH_FILE!!.forEach {
                    when (it.IMG_DVCD) {
                        "331001" -> {
                            currentProfileImage.generalImagePath = "${it.FILE_PATH}/${it.FILE_NM}"
                        }
                        "331005" -> {
                            currentProfileImage.thumbnailImagePath = "${it.FILE_PATH}/${it.FILE_NM}"
                        }
                    }
                }
                prevProfileImages.add(0, currentProfileImage)
            }

            prevProfileImages.reverse()

            item.prevImageList = prevProfileImages

            val codePersonalInfoItem = ArrayList<PersonalInfoItem>()
            if (!search.rcp_no.isNullOrBlank()) {
                item.rpt_bsc?.apply {
                    PRSN_ANS_INFO?.apply {
                        // 등록된 personal info 사용
                        commonDao.findAllPersonalInformationByReport(RCP_NO, locale).forEach {
                            codePersonalInfoItem.add(PersonalInfoItem(master = it, detail = commonRepository.findAllCommonCodeByGroupCode(it.CHGP_CD
                                    ?: "")))
                        }
                        if (codePersonalInfoItem.isNotEmpty()) {
                            item.codePersonalInfo = codePersonalInfoItem
                        }

                    } ?: run {
                        // 등록된 리포트의 저장 연도로 personal info 조회
                        commonDao.findAllPersonalInformationByYear(search.year ?: "", locale).forEach {
                        //commonDao.findAllPersonalInformationByYear(REG_DT?.toDateFormat("yyyy") ?: "", locale).forEach {
                            codePersonalInfoItem.add(PersonalInfoItem(master = it, detail = commonRepository.findAllCommonCodeByGroupCode(it.CHGP_CD
                                    ?: "")))
                        }
                        if (codePersonalInfoItem.isNotEmpty()) {
                            item.codePersonalInfo = codePersonalInfoItem
                        }
                    }

                    item.rpt_bsc = this
                }
            } else {
                // 현재 연도로 personal info 조회
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)

                //commonDao.findAllPersonalInformationByYear(currentYear.toString(), locale).forEach {
                commonDao.findAllPersonalInformationByYear(search.year ?: "", locale).forEach {
                    codePersonalInfoItem.add(PersonalInfoItem(master = it, detail = commonRepository.findAllCommonCodeByGroupCode(it.CHGP_CD
                            ?: "")))
                }
                if (codePersonalInfoItem.isNotEmpty()) {
                    item.codePersonalInfo = codePersonalInfoItem
                }
            }

            aprEditViewItem.postValue(item)
        }).start()

        return aprEditViewItem
    }

    private lateinit var saveAprResult: MutableLiveData<RPT_BSC>
    fun saveApr(report: RPT_BSC): LiveData<RPT_BSC> {
        saveAprResult = MutableLiveData()

        Thread(Runnable {
            try {
                report.apply {
                    reportDao.saveRPT_BSC(this)

                    CH_MST?.apply { reportDao.saveCH_MST(this) }
                    CH_BSC?.apply { reportDao.saveCH_BSC(this) }
                    EDU?.apply { reportDao.saveEDU(this) }
                    FMLY?.apply { reportDao.saveFMLY(this) }
                    HLTH?.apply { reportDao.saveHLTH(this) }
                    INTV?.apply { reportDao.saveINTV(this) }
                    REMRK?.apply { reportDao.saveREMRK(this) }
                    RPT_DIARY?.apply { reportDao.saveRPT_DIARY(this) }

                    reportDao.removeAllDropoutPlan(RCP_NO)
                    DROPOUT_PLAN?.apply { reportDao.saveDROPOUT_PLAN(this) }

                    reportDao.removeAllCH_SPSL_INFOByReport(RCP_NO)
                    CH_SPSL_INFO?.apply { reportDao.saveAllCH_SPSL_INFO(this) }

                    reportDao.removeAllPRSN_ANS_INFOByReport(RCP_NO)
                    PRSN_ANS_INFO?.apply { reportDao.saveAllPRSN_ANS_INFO(this) }

                    reportDao.removeAllSIBLByReport(RCP_NO)
                    val sibl = ArrayList<SIBL>()
                    SIBL?.forEach {
                        reportDao.findChMstByCdpCode(it.CHRCP_NO)?.apply { sibl.add(SIBL(RCP_NO = RCP_NO, CHRCP_NO = this.CHRCP_NO)) }
                    }
                    if (sibl.isNotEmpty()) {
                        reportDao.saveAllSIBL(sibl)
                    }

                    reportDao.deleteFileByReport(RCP_NO)
                    ATCH_FILE?.apply { reportDao.insertAllATCH_FILE(this) }

                    saveModHisInfo(RCP_NO, if (UPD_DT == null) "C" else "U")

                    saveAprResult.postValue(this)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                logger.error(e.message)
            }
        }).start()

        return saveAprResult
    }

    private var reports = MutableLiveData<List<ReportListItem>>()
    fun findAllReportByChild(id: String): LiveData<List<ReportListItem>> {
        reports = MutableLiveData()
        Thread(Runnable {
            val locale = preferences.getString(GNLocaleManager.SELECTED_LANGUAGE, "en")

            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val currentMonth = (Calendar.getInstance().get(Calendar.MONTH) + 1)
            val lastYear = if (currentMonth >= 10) currentYear + 1 else currentYear

            val items = reportDao.findAllReportListByChild(id, lastYear, locale)

            reports.postValue(items)
        }).start()

        return reports
    }

    private var deleteReportByIdResult = MutableLiveData<Boolean>()
    fun deleteReportById(rcp_no: String): MutableLiveData<Boolean> {
        deleteReportByIdResult = MutableLiveData()
        Thread(Runnable {
            reportDao.deleteReportById(rcp_no)
            saveModHisInfo(rcp_no, "D")
            deleteReportByIdResult.postValue(true)
        }).start()
        return deleteReportByIdResult
    }

    private lateinit var aclReports: MutableLiveData<List<AclListItem>>
    fun findAllAclByChild(id: String): LiveData<List<AclListItem>> {
        aclReports = MutableLiveData()
        Thread(Runnable {
            val locale = preferences.getString(GNLocaleManager.SELECTED_LANGUAGE, "en")

            val items = reportDao.findAllAclListItemByChild(id, locale)
            aclReports.postValue(items)

        }).start()

        return aclReports
    }

    fun saveAppDataHistory(history: APP_DATA_HISTORY) {
        reportDao.saveAppDataHistory(history)
        preferences.edit()
                .putString("GN_DATA_DATE", syncDao.getLastUpdateDate()?.value?.toLong()?.toDateFormat() ?: "-")
                .apply()
    }

    private var aclEditViewItem: MutableLiveData<AclEditViewItem> = MutableLiveData()
    fun findAclEditViewItem(s: AclEditViewItemSearch): LiveData<AclEditViewItem> {
        aclEditViewItem = MutableLiveData()

        Thread(Runnable {
            val locale = preferences.getString(GNLocaleManager.SELECTED_LANGUAGE, "en")

            val item = AclEditViewItem()

            // 상단 정보
            item.profile = reportDao.findProfileHeaderItemByChild(s.chrcp_no, locale)

            // 마지막 cif or apr report
            reportDao.findLastReportByChild(s.chrcp_no)?.apply {
                item.PREV_RPT_BSC = getReportById(RCP_NO)
            }

            // 이전 ACL 정보(type, subsituted)
            val PREV_ACL_RPT_BSC = reportDao.findPrevRptBscByChildAndType(s.chrcp_no, arrayOf("4"), s.year)
            if (PREV_ACL_RPT_BSC != null) {
                item.PREV_ACL_RPT_BSC = PREV_ACL_RPT_BSC

                PREV_ACL_RPT_BSC.ACL = reportDao.findAclById(PREV_ACL_RPT_BSC.RCP_NO)
                PREV_ACL_RPT_BSC.SWRT = reportDao.findSwrtById(PREV_ACL_RPT_BSC.RCP_NO)
                PREV_ACL_RPT_BSC.ATCH_FILE = ArrayList(reportDao.findAllAtchFileById(PREV_ACL_RPT_BSC.RCP_NO))

                val lastYearTypeCode = commonDao.findCode("305", PREV_ACL_RPT_BSC.ACL?.RPT_TYPE ?: "", locale)

                item.lastYearTypeCode = lastYearTypeCode?.CD
                item.lastYearType = lastYearTypeCode?.CD_ENM ?: "-"
                item.lastYearGhostwriting = PREV_ACL_RPT_BSC.SWRT?.SWRT_YN ?: "-"
            } else {
                item.lastYearType = "-"
                item.lastYearGhostwriting = "-"
            }

            // RCP_NO가 있다면 현재 ACL 정보
            if (s.rcp_no != null && s.rcp_no!!.trim() != "") {
                val RPT_BSC = reportDao.getRptBscById(s.rcp_no!!)
                if (RPT_BSC != null) {
                    RPT_BSC.ACL = reportDao.findAclById(RPT_BSC.RCP_NO)
                    RPT_BSC.SWRT = reportDao.findSwrtById(RPT_BSC.RCP_NO)
                    RPT_BSC.REMRK = reportDao.findRemarkById(RPT_BSC.RCP_NO)
                    RPT_BSC.ATCH_FILE = ArrayList(reportDao.findAllAtchFileById(RPT_BSC.RCP_NO))
                    RPT_BSC.RPT_DIARY = reportDao.findRptDiaryById(RPT_BSC.RCP_NO)
                    RPT_BSC.INTV = reportDao.findIntvById(RPT_BSC.RCP_NO)

                    if (RPT_BSC.RPT_STCD == "2" || RPT_BSC.RPT_STCD == "15") {
                        item.returns = reportDao.findAllReturns(RPT_BSC.RCP_NO, "207", locale)
                    }

                    item.RPT_BSC = RPT_BSC
                }
            }


            // TYPE 공통코드
            item.INPUT_TYPE = commonRepository.findAllCommonCodeByGroupCode("305")

            // RELATIONSHIP WITH CHILD 공통코드
            item.INPUT_RELEATIONSHIP_WITH_CHILD = commonRepository.findAllCommonCodeByGroupCode("123")

            // REASON 공통코드
            item.INPUT_REASON = commonRepository.findAllCommonCodeByGroupCode("124")

            aclEditViewItem.postValue(item)
        }).start()

        return aclEditViewItem
    }

    private var onAclSave = MutableLiveData<Boolean>()
    fun saveAcl(report: RPT_BSC): LiveData<Boolean> {
        onAclSave = MutableLiveData()

        Thread(Runnable {
            reportDao.saveRPT_BSC(report)

            report.ACL?.let { reportDao.insertACL(it) }
            report.SWRT?.let { reportDao.insertSWRT(it) }
            report.REMRK?.let { reportDao.saveREMRK(it) }
            report.INTV?.let { reportDao.saveINTV(it) }
            report.RPT_DIARY?.let { reportDao.insertRPT_DIARY(it) }
            report.ATCH_FILE?.let { reportDao.insertAllATCH_FILE(it) }

            saveModHisInfo(report.RCP_NO, if (report.UPD_DT == null) "C" else "U")

            onAclSave.postValue(true)
        }).start()

        return onAclSave
    }

    private var profileByChild = MutableLiveData<ProfileViewItem>()
    fun findProfileByChild(no: String): LiveData<ProfileViewItem> {
        profileByChild = MutableLiveData()

        Thread(Runnable {
            val locale = preferences.getString(GNLocaleManager.SELECTED_LANGUAGE, "en")
            val item = reportDao.findProfileByChild(no, locale)

            // 이미지 파일 3장
            item.GENERAL_FILE_PATH = reportDao.findProfileDataImageByChild(no)

            item.codeInterviewPlace = commonRepository.findAllCommonCodeByGroupCode("90")
            item.codeRelationship = commonRepository.findAllCommonCodeByGroupCode("112")

            profileByChild.postValue(item)
        }).start()

        return profileByChild
    }

    private lateinit var findAllCounselingByChildResult: MutableLiveData<List<CounselingListItem>>
    fun findAllCounselingByChild(chrcp_no: String): LiveData<List<CounselingListItem>> {
        findAllCounselingByChildResult = MutableLiveData()

        Thread(Runnable {
            val locale = preferences.getString(GNLocaleManager.SELECTED_LANGUAGE, "en")
            val items = reportDao.findAllCounselingByChild(chrcp_no, locale)

            findAllCounselingByChildResult.postValue(items)
        }).start()

        return findAllCounselingByChildResult
    }

    private lateinit var getNextCounselingIndexResult: MutableLiveData<NextCounselingIndex>
    fun getNextCounselingIndex(chrcp_no: String, crt_tp: String): LiveData<NextCounselingIndex> {
        getNextCounselingIndexResult = MutableLiveData()

        Thread(Runnable {
            val index = reportDao.getNextCounselingIndex(chrcp_no, crt_tp)
            getNextCounselingIndexResult.postValue(index)
        }).start()

        return getNextCounselingIndexResult
    }

    private lateinit var saveCounselingResult: MutableLiveData<Boolean>
    fun saveCounseling(ch_cusl_info: CH_CUSL_INFO): LiveData<Boolean> {
        saveCounselingResult = MutableLiveData()
        Thread(Runnable {
            reportDao.saveCH_CUSL_INFO(ch_cusl_info)

            saveCounselingResult.postValue(true)
        }).start()
        return saveCounselingResult
    }

    private var dropoutEditItem: MutableLiveData<DropoutEditItem> = MutableLiveData()
    fun findDropoutEditViewItem(s: DropoutEditSearchItem): LiveData<DropoutEditItem>? {
        dropoutEditItem = MutableLiveData()

        Thread(Runnable {
            val locale = preferences.getString(GNLocaleManager.SELECTED_LANGUAGE, "en")
            val item = DropoutEditItem()

            // dropoutExpectedCheckBox reason 공통코드
            item.dropoutReasonList = commonRepository.findAllCommonCodeByGroupCode("80")

            // 사망 퇴소 사유
            item.deathReasonList = commonRepository.findAllCommonCodeByGroupCode("338")

            // 사망 - illness
            item.illnessList = commonRepository.findAllCommonCodeByGroupCode("99")

            // 사망 - disability
            item.disabilityList = commonRepository.findAllCommonCodeByGroupCode("78")

            // 직업
            item.jobList = commonRepository.findAllCommonCodeByGroupCode("339")

            if (!s.chrcp_no.isNullOrBlank()) {
                item.ch_mst = reportDao.findChMstById(s.chrcp_no!!)
            }

            if (!s.rcp_no.isNullOrBlank()) {
                val rpt_bsc = reportDao.getRptBscById(s.rcp_no!!)
                if (rpt_bsc != null) {
                    rpt_bsc.DROPOUT = reportDao.findDropoutById(rpt_bsc.RCP_NO)
                    rpt_bsc.REMRK = reportDao.findRemarkById(rpt_bsc.RCP_NO)

                    if (rpt_bsc.RPT_STCD == "2" || rpt_bsc.RPT_STCD == "15") {
                        item.returns = reportDao.findAllReturns(rcp_no = rpt_bsc.RCP_NO, grp_cd = "214", locale = locale)
                    }
                }

                item.rpt_bsc = rpt_bsc
            }

            dropoutEditItem.postValue(item)
        }).start()

        return dropoutEditItem
    }

    private var onSaveDropout = MutableLiveData<Boolean>()
    fun saveDropout(report: RPT_BSC): LiveData<Boolean> {
        onSaveDropout = MutableLiveData()

        Thread(Runnable {
            reportDao.saveRPT_BSC(report)
            report.DROPOUT?.apply { reportDao.insertDROPOUT(this) }
            report.REMRK?.apply { reportDao.saveREMRK(this) }
            report.INTV?.apply { reportDao.saveINTV(this) }
            report.RPT_DIARY?.apply { reportDao.insertRPT_DIARY(this) }

            saveModHisInfo(report.RCP_NO, if (report.UPD_DT == null) "C" else "U")

            onSaveDropout.postValue(true)
        }).start()

        return onSaveDropout
    }

    ////////////////////////// provided service //////////////////////////////
    private var providedServiceListItem: MutableLiveData<List<ProvidedServiceListItem>> = MutableLiveData()

    fun findAllProvidedServiceByChild(chrcp_no: String): LiveData<List<ProvidedServiceListItem>> {
        providedServiceListItem = MutableLiveData()

        Thread(Runnable {
            val locale = preferences.getString(GNLocaleManager.SELECTED_LANGUAGE, "en")
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val minYear = currentYear - 1

            providedServiceListItem.postValue(reportDao.findAllProvidedServiceByChild(chrcp_no, "${minYear}0101", locale))
        }).start()

        return providedServiceListItem
    }

    private var providedServiceEditSearchItem: MutableLiveData<List<ProvidedServiceEditItem>> = MutableLiveData()
    fun findAllProvidedServiceEditItemByChild(searchItem: ProvidedServiceEditSearchItem): LiveData<List<ProvidedServiceEditItem>> {
        logger.debug("findAllProvidedServiceEditItemByChild($searchItem)")
        providedServiceEditSearchItem = MutableLiveData()

        Thread(Runnable {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val minYear = currentYear - 1

            val list = reportDao.findAllProvidedServiceByChildAndService(
                    chrcp_no = searchItem.chrcp_no,
                    svobj_dvcd = searchItem.svobj_dvcd,
                    bsst_cd = searchItem.bsst_cd,
                    spbd_cd = searchItem.spbd_cd,
                    year = "${minYear}0101")

            val items = ArrayList<ProvidedServiceEditItem>()
            list.forEachIndexed { index, item ->
                items.add(ProvidedServiceEditItem(RNUM = list.size - index, srvc = item))
            }
            providedServiceEditSearchItem.postValue(items)

        }).start()

        return providedServiceEditSearchItem
    }

    private var providedServiceRegistItem: MutableLiveData<List<ProvidedServiceRegistItem>> = MutableLiveData()
    fun findAllProvidedServiceRegistItem(search: ProvidedServiceRegistSearchItem): LiveData<List<ProvidedServiceRegistItem>>? {
        logger.debug("findAllProvidedServiceRegistItem($search)")
        providedServiceRegistItem = MutableLiveData()
        val locale = preferences.getString(GNLocaleManager.SELECTED_LANGUAGE, "en")
        Thread(Runnable {
            var masterSql = """
                SELECT *
                FROM (
                    SELECT A.*
                    FROM (
                        SELECT A.GRP_CD, CD, IFNULL(CASE UPPER('$locale') WHEN 'FR' THEN CD_FRNM WHEN 'ES' THEN CD_ESNM ELSE CD_ENM END, CD_ENM) AS CD_ENM, CHGP_CD, SORT_ORDR
                            , CASE WHEN GRP_CD = '240' THEN '1' ELSE '2' END AS REF_1
                        FROM CD A
                        WHERE GRP_CD IN ('240', '241')
                        AND USE_YN = 'Y'
                        AND CD != '0'
                    ) A
                    INNER JOIN CD B
                    ON A.CHGP_CD = B.GRP_CD
                    AND B.USE_YN = 'Y'
                    AND B.CD != '0'
                    AND IFNULL(CASE UPPER('$locale') WHEN 'FR' THEN B.CD_FRNM WHEN 'ES' THEN B.CD_ESNM ELSE B.CD_ENM END, B.CD_ENM) LIKE ?
                    @@@checkedItem@@@
                ) A
                GROUP BY GRP_CD, CD, CD_ENM, CHGP_CD, SORT_ORDR, REF_1
                ORDER BY A.REF_1, A.SORT_ORDR
            """

            masterSql = if (search.checkedItems != null && search.checkedItems.isNotEmpty()) {
                val unionQuery = ArrayList<String>()
                search.checkedItems.forEach { checkedItem ->
                    unionQuery.add(" SELECT '${checkedItem.detail_group_cd}', '${checkedItem.detail_cd}' ")
                }

                val checkedItemMasterQuery = " OR (A.CHGP_CD = B.GRP_CD AND (B.GRP_CD, B.CD) IN ( ${unionQuery.joinToString(" UNION ")} ))"

                masterSql.replace("@@@checkedItem@@@", checkedItemMasterQuery)
            } else {
                masterSql.replace("@@@checkedItem@@@", "")
            }

            val masterQuery = SimpleSQLiteQuery(masterSql, arrayOf("%${search.search}%"))

            logger.debug("sql = ${masterQuery.sql}")

            val masters = ArrayList(commonDao.findAllProvidedServiceItem(masterQuery))
            logger.debug("masters : $masters")

            val items = ArrayList<ProvidedServiceRegistItem>()
            masters.forEach {
                if (!it.CHGP_CD.isNullOrBlank()) {
                    var detailSql = """
                        SELECT B.GRP_CD, B.CD, B.GRPCD_KNM, B.GRPCD_ENM, B.CD_KNM, IFNULL(CASE UPPER('$locale') WHEN 'FR' THEN B.CD_FRNM WHEN 'ES' THEN B.CD_ESNM ELSE B.CD_ENM END, B.CD_ENM) AS CD_ENM,
                               B.EXPL, B.USE_YN, B.SORT_ORDR, B.CHGP_CD, B.REGR_ID, B.REG_DT, B.UPDR_ID, B.UPD_DT, B.EXPL_ENG, B.REF_1, B.REF_2
                        FROM CD B
                        WHERE B.USE_YN = 'Y'
                        AND B.CD != '0'
                        AND B.GRP_CD = ?
                        AND IFNULL(CASE UPPER('$locale') WHEN 'FR' THEN B.CD_FRNM WHEN 'ES' THEN B.CD_ESNM ELSE B.CD_ENM END, B.CD_ENM) LIKE ?
                        @@@checkedItem@@@
                        ORDER BY SORT_ORDR
                    """

                    detailSql = if (search.checkedItems != null && search.checkedItems.isNotEmpty()) {
                        val unionQuery = ArrayList<String>()
                        search.checkedItems.forEach { checkedItem ->
                            if (it.CHGP_CD == checkedItem.detail_group_cd) {
                                unionQuery.add(" SELECT '${checkedItem.detail_group_cd}', '${checkedItem.detail_cd}' ")
                            }
                        }

                        val checkedItemDetailQuery = " OR (B.GRP_CD, B.CD) IN ( ${unionQuery.joinToString(" UNION ")} )"

                        detailSql.replace("@@@checkedItem@@@", checkedItemDetailQuery)
                    } else {
                        detailSql.replace("@@@checkedItem@@@", "")
                    }

                    val detailQuery = SimpleSQLiteQuery(detailSql, arrayOf(it.CHGP_CD!!, "%${search.search}%"))
//                    val details = commonDao.findAllProvidedServiceDetail(it.CHGP_CD!!, "%$search%")
                    val details = commonDao.findAllProvidedServiceItem(detailQuery)
                    items.add(ProvidedServiceRegistItem(master = it, detailes = details))
                }
            }

            providedServiceRegistItem.postValue(items)

        }).start()

        return providedServiceRegistItem
    }

    private var saveSrvcResult: MutableLiveData<Boolean> = MutableLiveData()
    fun saveSrvc(items: List<SRVC>): MutableLiveData<Boolean> {
        saveSrvcResult = MutableLiveData()
        Thread(Runnable {
            if (items.isNotEmpty()) {
                var lastIndex = reportDao.findlastIndexOfService(items[0].CHRCP_NO, items[0].CRT_TP)
                items.forEach {
                    lastIndex++
                    it.SEQ_NO = lastIndex

                    logger.debug("item : $it")
                }
                reportDao.saveAllSRVC(items)
            }

            saveSrvcResult.postValue(true)
        }).start()

        return saveSrvcResult
    }

    private var editSrvcResult: MutableLiveData<Boolean> = MutableLiveData()
    fun editSrvc(items: List<SRVC>): MutableLiveData<Boolean> {
        editSrvcResult = MutableLiveData()
        Thread(Runnable {
            if (items.isNotEmpty()) {
                reportDao.saveAllSRVC(items)
            }

            editSrvcResult.postValue(true)
        }).start()

        return editSrvcResult
    }

    ////////////////////////// GML //////////////////////////////
    private var findAllGmlByChildResult = MutableLiveData<List<GmlListItem>>()

    fun findAllGmlByChild(chrcp_no: String): LiveData<List<GmlListItem>>? {
        findAllGmlByChildResult = MutableLiveData()

        Thread(Runnable {
            val sql = """
                SELECT *
                FROM (
                    SELECT A.*, B.CD_ENM AS RPT_STNM, C.SWRT_YN
                    , (SELECT FILE_PATH || '/' || FILE_NM
                        FROM ATCH_FILE
                        WHERE RCP_NO = A.RCP_NO AND IMG_DVCD = '331001'
                        AND FILE_PATH IS NOT NULL AND FILE_PATH != '' AND FILE_NM IS NOT NULL AND FILE_NM != ''
                        ORDER BY SEQ_NO
                        LIMIT 1
                    ) AS FILE_PATH
                    FROM (
                        SELECT 'G' AS TYPE, A.MNG_NO, B.RCP_NO, A.CHRCP_NO, A.RCP_DT, IFNULL(B.RPT_STCD, '16') AS RPT_STCD, B.APRV_DT
                        FROM GMNY A
                        LEFT OUTER JOIN (
                            SELECT A.*, B.GMNY_MNGNO
                            FROM (
                                SELECT RCP_NO, CHRCP_NO, RPT_STCD, APRV_DT
                                FROM RPT_BSC
                                WHERE RPT_DVCD = '5'
                                AND RPT_STCD NOT IN ('98', '99')
                                AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                            ) A
                            INNER JOIN RPLY B
                            ON A.RCP_NO = B.RCP_NO
                            AND B.GMNY_MNGNO IS NOT NULL
                        ) B
                        ON A.CHRCP_NO = B.CHRCP_NO
                        AND A.MNG_NO = B.GMNY_MNGNO
                        AND A.MV_DT IS NULL
                        AND A.RFND_DT IS NULL

                        UNION

                        SELECT 'L' AS TYPE, A.MNG_NO, B.RCP_NO, A.CHRCP_NO, A.RCP_DT, IFNULL(B.RPT_STCD, '16') AS RPT_STCD, B.APRV_DT
                        FROM LETR A
                        LEFT OUTER JOIN (
                            SELECT A.*, B.LETR_MNGNO
                            FROM (
                                 SELECT RCP_NO, CHRCP_NO, RPT_STCD, APRV_DT
                                 FROM RPT_BSC
                                 WHERE RPT_DVCD = '5'
                                 AND RPT_STCD NOT IN ('98', '99')
                                 AND DEL_YN != 'Y' AND FIDG_YN = 'Y'
                            ) A
                            INNER JOIN RPLY B
                            ON A.RCP_NO = B.RCP_NO
                        ) B
                        ON A.CHRCP_NO = B.CHRCP_NO
                        AND A.MNG_NO = B.LETR_MNGNO
                    ) A
                    LEFT OUTER JOIN (
                        SELECT * FROM CD WHERE GRP_CD = '65' AND CD != 0
                    ) B
                    ON A.RPT_STCD = B.CD
                    LEFT OUTER JOIN SWRT C
                    ON A.RCP_NO = C.RCP_NO
                ) A
                WHERE CHRCP_NO = '$chrcp_no'
                AND RCP_DT >= (strftime('%Y','now','localtime') - 2) || '0101'
                ORDER BY RCP_DT DESC, TYPE
            """
            val query = SimpleSQLiteQuery(sql)
            val items = reportDao.findAllGmlByChild(query)
            findAllGmlByChildResult.postValue(items)
        }).start()

        return findAllGmlByChildResult
    }

    private lateinit var findGmEditItemResult: MutableLiveData<GmEditItem>
    fun findGmEditItem(search: GmlEditItemSearch): LiveData<GmEditItem> {
        logger.debug("findGmEditItem($search)")
        findGmEditItemResult = MutableLiveData()

        Thread(Runnable {
            val locale = preferences.getString(GNLocaleManager.SELECTED_LANGUAGE, "en")
            val item = reportDao.findGmProfile(search.chrcp_no, search.mng_no, locale)
            if (!search.rcp_no.isNullOrBlank()) {
                item.rpt_bsc = getReportById(search.rcp_no!!)

                if (item.RPT_STCD == "2" || item.RPT_STCD == "15") {
                    item.returns = reportDao.findAllReturns(search.rcp_no!!, "207", locale)
                }
            }

            val gifts = LinkedList<GiftItem>()
            commonRepository.findAllCommonCodeByGroupCodeToEntity("246").forEachIndexed { index, cd ->
                if (!cd.CHGP_CD.isNullOrBlank()) {
                    reportDao.findAllGiftItems(grp_cd = cd.CHGP_CD!!, rcp_no = search.rcp_no ?: "", locale = locale)?.apply {
                        logger.debug("details : ${GsonBuilder().create().toJson(this)}")
                        gifts.add(GiftItem(master = cd, detail = this))
                    }
                }
            }
            if (!gifts.isNullOrEmpty()) item.gifts = gifts

            findGmEditItemResult.postValue(item)
        }).start()

        return findGmEditItemResult
    }

    private lateinit var saveGmResult: MutableLiveData<Boolean>
    fun saveGm(report: RPT_BSC): LiveData<Boolean> {
        saveGmResult = MutableLiveData()

        Thread(Runnable {
            report.apply {
                reportDao.saveRPT_BSC(this)
                RPLY?.apply { reportDao.insertRPLY(this) }
                REMRK?.apply { reportDao.saveREMRK(this) }
                RPT_DIARY?.apply { reportDao.saveRPT_DIARY(this) }
                INTV?.apply { reportDao.saveINTV(this) }

                reportDao.deleteFileByReport(RCP_NO)
                ATCH_FILE?.apply { reportDao.insertAllATCH_FILE(this) }

                reportDao.removeAllGIFT_BRKDWByReport(this.RCP_NO)
                GIFT_BRKDW?.apply {
                    logger.debug("GIFT_BRKDW : $this")
                    reportDao.insertAllGIFT_BRKDW(this)
                }

                saveModHisInfo(RCP_NO, if (UPD_DT == null) "C" else "U")
            }

            saveGmResult.postValue(true)

        }).start()

        return saveGmResult
    }

    private lateinit var findGmLetterEditItemResult: MutableLiveData<GmLetterEditItem>
    fun findGmLetterEditItem(search: GmlEditItemSearch): LiveData<GmLetterEditItem> {
        logger.debug("findGmEditItem($search)")
        findGmLetterEditItemResult = MutableLiveData()

        Thread(Runnable {
            val locale = preferences.getString(GNLocaleManager.SELECTED_LANGUAGE, "en")
            val item = reportDao.findGmLetterProfile(search.chrcp_no, search.mng_no, locale)
            if (!search.rcp_no.isNullOrBlank()) {
                item.rpt_bsc = getReportById(search.rcp_no!!)

                if (item.RPT_STCD == "2" || item.RPT_STCD == "15") {
                    item.returns = reportDao.findAllReturns(rcp_no = item.RCP_NO ?: "", grp_cd = "207", locale = locale)
                }
            }

            reportDao.findLastReportByChild(search.chrcp_no)?.apply {
                item.PREV_RPT_BSC = getReportById(RCP_NO)
            }

            reportDao.findPrevGmLetterReport(search.chrcp_no, search.mng_no)?.apply {
                item.PREV_LETR_RPT_BSC = this
                SWRT = reportDao.findSwrtById(RCP_NO)
            }

            item.codeRelationship = commonRepository.findAllCommonCodeByGroupCode("123")
            item.codeReason = commonRepository.findAllCommonCodeByGroupCode("124")

            findGmLetterEditItemResult.postValue(item)
        }).start()

        return findGmLetterEditItemResult
    }

    private lateinit var saveGmLetterResult: MutableLiveData<Boolean>
    fun saveGmLetter(report: RPT_BSC): LiveData<Boolean> {
        saveGmLetterResult = MutableLiveData()

        Thread(Runnable {
            report.apply {
                reportDao.saveRPT_BSC(this)
                RPLY?.apply { reportDao.insertRPLY(this) }
                REMRK?.apply { reportDao.saveREMRK(this) }
                RPT_DIARY?.apply { reportDao.saveRPT_DIARY(this) }
                INTV?.apply { reportDao.saveINTV(this) }
                SWRT?.apply { reportDao.insertSWRT(this) }

                reportDao.deleteFileByReport(RCP_NO)
                ATCH_FILE?.apply { reportDao.insertAllATCH_FILE(this) }

                saveModHisInfo(RCP_NO, if (UPD_DT == null) "C" else "U")
            }

            saveGmLetterResult.postValue(true)

        }).start()

        return saveGmLetterResult
    }

    private lateinit var findAllFilesResult: MutableLiveData<List<ATCH_FILE>>
    fun findAllFiles(rcp_no: String): LiveData<List<ATCH_FILE>> {
        findAllFilesResult = MutableLiveData()

        Thread(Runnable {
            findAllFilesResult.postValue(reportDao.findAllAtchFileById(rcp_no))
        }).start()

        return findAllFilesResult
    }

    private fun saveModHisInfo(rcp_no: String, mode: String? = null) {
        val imei= preferences.getString("GN_ID", "") ?: ""
        val userid = preferences.getString("userid", "") ?: ""

        val crud = if (mode == null) {
            val history = reportDao.findModHisInfo(rcp_no)

            if (history == null) "C" else "U"
        }
        else mode

        val nextSeq = reportDao.getNextModHisInfoSeq(imei)?.value?.toInt() ?: 1
        reportDao.saveMOD_HIS_INFO(MOD_HIS_INFO(SEQ_NO = nextSeq, INIT_TYPE = imei, RCP_NO = rcp_no, CRUD = crud, REGR_ID = userid, REG_DT = Date().time))

    }
}