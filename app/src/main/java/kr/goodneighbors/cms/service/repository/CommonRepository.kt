package kr.goodneighbors.cms.service.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import kr.goodneighbors.cms.common.GNLocaleManager
import kr.goodneighbors.cms.service.db.CommonDao
import kr.goodneighbors.cms.service.entities.BMI
import kr.goodneighbors.cms.service.entities.BRC
import kr.goodneighbors.cms.service.entities.CD
import kr.goodneighbors.cms.service.entities.CH_CUSL_INFO
import kr.goodneighbors.cms.service.entities.CTR
import kr.goodneighbors.cms.service.entities.GMNY
import kr.goodneighbors.cms.service.entities.LETR
import kr.goodneighbors.cms.service.entities.NOTI_INFO
import kr.goodneighbors.cms.service.entities.PRJ
import kr.goodneighbors.cms.service.entities.PRSN_INFO
import kr.goodneighbors.cms.service.entities.RELSH
import kr.goodneighbors.cms.service.entities.SCHL
import kr.goodneighbors.cms.service.entities.SPLY_PLAN
import kr.goodneighbors.cms.service.entities.SRVC
import kr.goodneighbors.cms.service.entities.VLG
import kr.goodneighbors.cms.service.model.CommonCode
import kr.goodneighbors.cms.service.model.SpinnerOption
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Singleton
class CommonRepository @Inject constructor(
        private val commonDao: CommonDao,
        private val preferences: SharedPreferences
) {
    private val logger: Logger by lazy {
        LoggerFactory.getLogger(CommonRepository::class.java)
    }

    fun initNotiInfo(notiInfoList: List<NOTI_INFO>) {
        Thread(Runnable {
            commonDao.initNotiInfo(notiInfoList)
        }).start()
    }

    fun initCd(cdList: List<CD>) {
        Thread(Runnable {
            commonDao.initCd(cdList)
        }).start()
    }

    fun initCtr(ctrList: List<CTR>) {
        Thread(Runnable {
            commonDao.initCtr(ctrList)
        }).start()
    }

    fun initBrc(brcList: List<BRC>) {
        Thread(Runnable {
            commonDao.initBrc(brcList)
        }).start()
    }

    fun initPrj(prjList: List<PRJ>) {
        Thread(Runnable {
            commonDao.initPrj(prjList)
        }).start()
    }

    fun initVlg(vlgList: List<VLG>) {
        Thread(Runnable {
            commonDao.initVlg(vlgList)
        }).start()
    }

    fun initSchl(schlList: List<SCHL>) {
        Thread(Runnable {
            commonDao.initSchl(schlList)
        }).start()
    }

    fun initPrsnInfo(prsnInfoList: List<PRSN_INFO>) {
        Thread(Runnable {
            commonDao.initPrsnInfo(prsnInfoList)
        }).start()
    }

    fun initSplyPlan(splyPlanList: List<SPLY_PLAN>) {
        Thread(Runnable {
            commonDao.initSplyPlan(splyPlanList)
        }).start()
    }

    fun initSrvc(srvcList: List<SRVC>) {
        Thread(Runnable {
            commonDao.initSrvc(srvcList)
            logger.debug("SRVC insert complete")
        }).start()
    }

    fun initChCuslInfo(chCuslInfoList: List<CH_CUSL_INFO>) {
        Thread(Runnable {
            commonDao.initChCuslInfo(chCuslInfoList)
        }).start()
    }

    fun initRelsh(relshList: List<RELSH>) {
        Thread(Runnable {
            commonDao.initRelsh(relshList)
        }).start()
    }

    fun initLetr(letrList: List<LETR>) {
        Thread(Runnable {
            commonDao.initLetr(letrList)
        }).start()
    }

    fun initGmny(gmnyList: ArrayList<GMNY>) {
        Thread(Runnable {
            commonDao.initGmny(gmnyList)
        }).start()
    }

    fun initBmi(bmiList: ArrayList<BMI>) {
        Thread(Runnable {
            commonDao.initBmi(bmiList)
        }).start()
    }
    
    fun findAllCommonCodeByGroupCode(grp_cd: String): List<SpinnerOption> {
        val locale = preferences.getString(GNLocaleManager.SELECTED_LANGUAGE, "en")
        return commonDao.findAllCode(grp_cd, locale)
    }

    fun findAllCommonCodeByGroupCodeToEntity(grp_cd: String): List<CD> {
        val locale = preferences.getString(GNLocaleManager.SELECTED_LANGUAGE, "en")
        return commonDao.findAllCodeToEntity(grp_cd, locale)
    }

    private val commonCode = MutableLiveData<CommonCode>()
    fun getCommonCode(): LiveData<CommonCode> {
        logger.debug("getCommonCode")
        Thread(Runnable {
            val locale = preferences.getString(GNLocaleManager.SELECTED_LANGUAGE, "en")

            val village = commonDao.findAllVillage()
            val schoolName = commonDao.findAllSchool()

            val status = commonDao.findAllStatusCode(locale)

            val mainGuardian = findAllCommonCodeByGroupCode("66")
            val relationshipWithChild = findAllCommonCodeByGroupCode("67")

            val schooltype = findAllCommonCodeByGroupCode("70")
            val wantToBe = findAllCommonCodeByGroupCode("77")
            val disability = findAllCommonCodeByGroupCode("78")
            val supportCountry = findAllCommonCodeByGroupCode("79")

            val favoriteSubject = findAllCommonCodeByGroupCode("83")
            val hobby = findAllCommonCodeByGroupCode("88")

            val interviewPlace = findAllCommonCodeByGroupCode("90")
            val illness = findAllCommonCodeByGroupCode("99")

            val personality = findAllCommonCodeByGroupCode("100")
            val fatherReason = findAllCommonCodeByGroupCode("102")
            val disabilityReason = findAllCommonCodeByGroupCode("104")
            val schooltypeReason = findAllCommonCodeByGroupCode("106")
            val illnessReason = findAllCommonCodeByGroupCode("107")

            val specialCase = findAllCommonCodeByGroupCode("115")

            val service = findAllCommonCodeByGroupCode("200")
            val dropoutPlanY = findAllCommonCodeByGroupCode("340")
            val dropoutPlanN = findAllCommonCodeByGroupCode("341")

            val ctrCd = preferences.getString("user_ctr_cd", "")
            val prjCd = preferences.getString("user_prj_cd", "")
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)

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

            val cifCommonCodeData = CommonCode(
                    status = status,
                    service = service,
                    villages = village,
                    disabillity = disability, disabillityReason = disabilityReason,
                    illness = illness, illnessReason = illnessReason,
                    schoolType = schooltype, schoolTypeReason = schooltypeReason, schoolName = schoolName,
                    supportCountry = supportCountry, supplyPlan = supplyPlanItems,
                    relationshipWithChild = relationshipWithChild,
                    interviewPlace = interviewPlace,
                    fatherReason = fatherReason, mainGuardian = mainGuardian,
                    specialCase = specialCase,
                    personality = personality,
                    favoriteSubject = favoriteSubject,
                    wantToBe = wantToBe,
                    hobby = hobby,
                    dropoutPlanN = dropoutPlanN,
                    dropoutPlanY = dropoutPlanY)

            commonCode.postValue(cifCommonCodeData)
        }).start()

        return commonCode
    }

    private val moreDialogCommonCode = MutableLiveData<CommonCode>()
    fun getMoreDialogCommonCode(): LiveData<CommonCode> {
        logger.debug("getMoreDialogCommonCode")
        Thread(Runnable {
            val village = commonDao.findAllVillage()
            val schoolName = commonDao.findAllSchool()

            val specialCase = findAllCommonCodeByGroupCode("115")

            val cifCommonCodeData = CommonCode(villages = village, specialCase = specialCase, schoolName = schoolName, supplyPlan = hashMapOf())

            moreDialogCommonCode.postValue(cifCommonCodeData)
        }).start()

        return moreDialogCommonCode
    }


}