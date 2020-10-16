package kr.goodneighbors.cms.ui.init

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.JsonParser
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.common.Constants
import kr.goodneighbors.cms.service.entities.APP_DATA_HISTORY
import kr.goodneighbors.cms.service.entities.BMI
import kr.goodneighbors.cms.service.entities.BRC
import kr.goodneighbors.cms.service.entities.CD
import kr.goodneighbors.cms.service.entities.CH_CUSL_INFO
import kr.goodneighbors.cms.service.entities.CH_MST
import kr.goodneighbors.cms.service.entities.CTR
import kr.goodneighbors.cms.service.entities.GMNY
import kr.goodneighbors.cms.service.entities.LETR
import kr.goodneighbors.cms.service.entities.NOTI_INFO
import kr.goodneighbors.cms.service.entities.PRJ
import kr.goodneighbors.cms.service.entities.PRSN_INFO
import kr.goodneighbors.cms.service.entities.RELSH
import kr.goodneighbors.cms.service.entities.RPT_BSC
import kr.goodneighbors.cms.service.entities.SCHL
import kr.goodneighbors.cms.service.entities.SPLY_PLAN
import kr.goodneighbors.cms.service.entities.SRVC
import kr.goodneighbors.cms.service.entities.USER_INFO
import kr.goodneighbors.cms.service.entities.VLG
import kr.goodneighbors.cms.service.viewmodel.CommonViewModel
import kr.goodneighbors.cms.service.viewmodel.ReportViewModel
import kr.goodneighbors.cms.service.viewmodel.UserInfoViewModel
import kr.goodneighbors.cms.ui.BaseActivityFragment
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.button
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.progressBar
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.space
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textResource
import org.jetbrains.anko.textSizeDimen
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.view
import org.jetbrains.anko.wrapContent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileFilter
import java.nio.file.Files
import java.util.*


class InitSyncFragment : BaseActivityFragment() {
    companion object {
        fun newInstance(): InitSyncFragment {
            return InitSyncFragment()
        }
    }

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(InitSyncFragment::class.java)
    }

    private val ui = FragmentUI()

    private val commonViewModel: CommonViewModel by lazy {
        ViewModelProviders.of(this).get(CommonViewModel::class.java)
    }

    private val userInfoViewModel: UserInfoViewModel by lazy {
        ViewModelProviders.of(this).get(UserInfoViewModel::class.java)
    }

    private val reportViewModel: ReportViewModel by lazy {
        ViewModelProviders.of(this).get(ReportViewModel::class.java)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        reportViewModel.initProgress.observe(this, Observer {
            logger.debug("reportViewModel.initProgress : $it")
            when (it) {
                "P" -> {
                    ui.progressTextView.textResource = R.string.message_data_read_completed
                }
                "D" -> {
                    ui.progressTextView.textResource = R.string.message_finished_initialization

                    val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                    sharedPref.edit().putBoolean("init", true).apply()

                    changeFragment.onChangeFragment(SigninFragment.newInstance(), false)
                }
            }
        })

        reportViewModel.initProgressReport.observe(this, Observer {
            ui.progressDatabaseTextView.text = "Processing report data : ${it ?: ""}"
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        logger.debug("InitSyncFragment.onCreateView")
        val v = ui.createView(AnkoContext.create(requireContext(), this))

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        if (sharedPref.getBoolean("init", false)) {
            changeFragment.onChangeFragment(SigninFragment.newInstance(), false)
        }

        ui.startButton.onClick {
            ui.startButton.visibility = View.GONE
            ui.syncProgressBar.visibility = View.VISIBLE

            ui.menualContainer.visibility = View.VISIBLE
            ui.errorMenualContainer.visibility = View.GONE

            initialization()
        }

        return v
    }

    private fun onErrorListener() {
        runOnUiThread {
            ui.menualContainer.visibility = View.GONE
            ui.errorMenualContainer.visibility = View.VISIBLE

            ui.startButton.visibility = View.VISIBLE
            ui.syncProgressBar.visibility = View.GONE

            ui.progressDatabaseTextView.visibility = View.GONE
        }
    }

    private fun onProgressMessage(message: String) {
        runOnUiThread {
            ui.progressTextView.text = message
        }
    }

    /**
     * File unzip
     */
    private fun unzip(zipedFile: File, targetPath: String) {
        val zipFile = net.lingala.zip4j.core.ZipFile(zipedFile)
        zipFile.extractAll(targetPath)
    }

    private fun initialization() {
        doAsync {
            try {
                val sdMain = Environment.getExternalStorageDirectory()
                val initDir = File("$sdMain/${Constants.DIR_HOME}/${Constants.DIR_INIT}")

                logger.debug("list : ${initDir.list().size}")
                // 초기화 디렉토리 탐색

                onProgressMessage("Navigate to the initialization directory")

                val initFiles = File("$sdMain/${Constants.DIR_HOME}/${Constants.DIR_INIT}")
                        .listFiles(FileFilter {
                            it.isFile && it.name != ".nomedia" && it.name.endsWith(".zip")
                        })

                if (initFiles.isEmpty()) {
                    onProgressMessage("Initialization file not found.")
                    onErrorListener()
                    return@doAsync
                }

                initFiles.forEach { initFile ->
                    logger.debug("find file : ${initFile.toPath()} /// ${initFile.name}")
                    onProgressMessage("Initiate initialization. : ${initFile.name}")

                    // unzip 대상 디렉토리가 존재하면 삭제
                    val tempTargetDir = File("${initFile.parentFile.toPath()}", initFile.nameWithoutExtension)
                    if (tempTargetDir.exists() && tempTargetDir.isDirectory) {
                        logger.debug("디렉토리 발견..제거..")

                        tempTargetDir.deleteRecursively()
                    }

                    // unzip 대상 디렉토리 생성
                    val targetDir = File("${initFile.parentFile.toPath()}", initFile.nameWithoutExtension)
                    logger.debug("target dir : $targetDir")
                    if (!targetDir.exists()) {
                        val isCreatedTargetDirectory = targetDir.mkdirs()
                        logger.debug("create target directory : $isCreatedTargetDirectory")
                    }

                    // unzip
                    onProgressMessage("Extract the initialization file")
                    logger.debug("Extract $initFile")
                    unzip(initFile, "${targetDir.toPath()}")

                    onProgressMessage("Copying content files")
                    targetDir.listFiles(FileFilter { it.isDirectory }).forEach {
                        it.copyRecursively(File("$sdMain/${Constants.DIR_HOME}/${Constants.DIR_CONTENTS}", it.name), true)
                    }

                    // unzip directory 에서 Database 초기화 파일 탐색
                    onProgressMessage("Preparing data for initialization")
                    targetDir.listFiles(FileFilter { it.isFile }).forEach { dataFile ->
                        // Database 초기화 파일 발견
                        logger.debug("find db file : ${dataFile.name}")

                        var i = 0

                        val notiInfoList = arrayListOf<NOTI_INFO>()
                        val cdList = arrayListOf<CD>()
                        val ctrList = arrayListOf<CTR>()
                        val brcList = arrayListOf<BRC>()
                        val prjList = arrayListOf<PRJ>()
                        val userinfoList = arrayListOf<USER_INFO>()
                        val vlgList = arrayListOf<VLG>()
                        val schlList = arrayListOf<SCHL>()
                        val prsnInfoList = arrayListOf<PRSN_INFO>()
                        val splyPlanList = arrayListOf<SPLY_PLAN>()
                        var chmstList = arrayListOf<CH_MST>()
                        var reportList = arrayListOf<RPT_BSC>()
                        val srvcList = arrayListOf<SRVC>()
                        val chCuslInfoList = arrayListOf<CH_CUSL_INFO>()
                        val relshList = arrayListOf<RELSH>()
                        val letrList = arrayListOf<LETR>()
                        val gmnyList = arrayListOf<GMNY>()
                        val bmiList = arrayListOf<BMI>()

                        val totalLineCount = Files.lines(dataFile.toPath()).count()

                        dataFile.forEachLine { lineString ->
                            i++

                            onProgressMessage("Reading init data : $i of $totalLineCount")

                            if (lineString.startsWith("D")) {
                                val items = lineString.split("|||")

                                if (items.size == 2) {
                                    val jsonString = items[1]

                                    val parsedJson = JsonParser().parse(jsonString).asJsonObject


                                    val entrySet = parsedJson.entrySet()
                                    val entrySetIterator = entrySet.iterator()

                                    while (entrySetIterator.hasNext()) {
                                        val set = entrySetIterator.next()


                                        try {
                                            val gson = Gson()

                                            when (set.key) {
                                                "BMI" -> {
                                                    val bmi = gson.fromJson(set.value, BMI::class.java)
                                                    bmiList.add(bmi)
                                                }
                                                "NOTI_INFO" -> {
                                                    val notiInfo = gson.fromJson(set.value, NOTI_INFO::class.java)
                                                    notiInfoList.add(notiInfo)
                                                    if (notiInfoList.size == 1000) {
                                                        logger.debug("NOTI_INFO fire ${notiInfoList.lastIndex}")
                                                        val tempList = notiInfoList.slice(0..notiInfoList.lastIndex)
                                                        commonViewModel.initNotiInfo(tempList)
                                                        notiInfoList.clear()
                                                    }
                                                }
                                                "CD" -> {
                                                    val cd = gson.fromJson(set.value, CD::class.java)
                                                    cdList.add(cd)
                                                    if (cdList.size == 1000) {
                                                        logger.debug("CD fire ${cdList.lastIndex}")
                                                        val tempList = cdList.slice(0..cdList.lastIndex)
                                                        commonViewModel.initCd(tempList)
                                                        cdList.clear()
                                                    }
                                                }
                                                "CTR" -> {
                                                    val ctr = gson.fromJson(set.value, CTR::class.java)
                                                    ctrList.add(ctr)
                                                }
                                                "BRC" -> {
                                                    val brc = gson.fromJson(set.value, BRC::class.java)
                                                    brcList.add(brc)
                                                }
                                                "PRJ" -> {
                                                    val prj = gson.fromJson(set.value, PRJ::class.java)
                                                    prjList.add(prj)
                                                }
                                                "USER_INFO" -> {
                                                    val userinfo = gson.fromJson(set.value, USER_INFO::class.java)
                                                    userinfoList.add(userinfo)
                                                }
                                                "VLG" -> {
                                                    val vlg = gson.fromJson(set.value, VLG::class.java)
                                                    vlgList.add(vlg)
                                                }
                                                "SCHL" -> {
                                                    val schl = gson.fromJson(set.value, SCHL::class.java)
                                                    schlList.add(schl)
                                                }
                                                "PRSN_INFO" -> {
                                                    val prsnInfo = gson.fromJson(set.value, PRSN_INFO::class.java)
                                                    prsnInfoList.add(prsnInfo)
                                                }
                                                "SPLY_PLAN" -> {
                                                    val splyPlan = gson.fromJson(set.value, SPLY_PLAN::class.java)
                                                    splyPlanList.add(splyPlan)
                                                }

                                                "SRVC" -> {
                                                    val srvc = gson.fromJson(set.value, SRVC::class.java)
                                                    srvcList.add(srvc)
                                                    if (srvcList.size == 3000) {
                                                        logger.debug("SRVC fire ${srvcList.lastIndex}")
                                                        val tempList = srvcList.slice(0..srvcList.lastIndex)
                                                        commonViewModel.initSrvc(tempList)
                                                        srvcList.clear()
                                                    }
                                                }
                                                "CH_CUSL_INFO" -> {
                                                    val chCuslInfo = gson.fromJson(set.value, CH_CUSL_INFO::class.java)
                                                    chCuslInfoList.add(chCuslInfo)
                                                }
                                                "RELSH" -> {
                                                    val relsh = gson.fromJson(set.value, RELSH::class.java)
                                                    relshList.add(relsh)
                                                }
                                                "LETR" -> {
                                                    val letr = gson.fromJson(set.value, LETR::class.java)
                                                    letrList.add(letr)
                                                }
                                                "GMNY" -> {
                                                    val gmny = gson.fromJson(set.value, GMNY::class.java)
                                                    gmnyList.add(gmny)
                                                }
                                                "CH_MST" -> {
                                                    val child = gson.fromJson(set.value, CH_MST::class.java)
                                                    chmstList.add(child)
                                                    if (chmstList.size == 1000) {
                                                        logger.debug("CH_MST fire ${chmstList.lastIndex}")
                                                        val tempList = chmstList.toMutableList()
                                                        reportViewModel.initAllChild(tempList)
                                                        chmstList = arrayListOf()
                                                    }
                                                }
                                                "RPT_BSC" -> {
                                                    val report = gson.fromJson(set.value, RPT_BSC::class.java)

                                                    if (report.RCP_NO.isNotEmpty() && report.CHRCP_NO.isNotEmpty()) {
                                                        reportList.add(report)

                                                        if (reportList.size == 200) {
                                                            logger.debug("RPT_BSC fire ${reportList.lastIndex}")
                                                            val tempList = reportList.toMutableList()
                                                            reportViewModel.initAll(tempList)
                                                            reportList = arrayListOf()
                                                        }
                                                    }
                                                }
                                            }

                                        } catch (e: Exception) {
                                            logger.debug("key : ${set.key} /// not found")
                                        }
                                    }
                                }
                            }
                        }

                        if (bmiList.isNotEmpty()) {
                            commonViewModel.initBmi(bmiList)
                        }

                        if (userinfoList.isNotEmpty()) {
                            userInfoViewModel.initAll(userinfoList)
                        }

                        if (notiInfoList.isNotEmpty()) {
                            commonViewModel.initNotiInfo(notiInfoList)
                        }

                        if (cdList.isNotEmpty()) {
                            commonViewModel.initCd(cdList)
                        }

                        if (ctrList.isNotEmpty()) {
                            commonViewModel.initCtr(ctrList)
                        }

                        if (brcList.isNotEmpty()) {
                            commonViewModel.initBrc(brcList)
                        }

                        if (prjList.isNotEmpty()) {
                            commonViewModel.initPrj(prjList)
                        }

                        if (vlgList.isNotEmpty()) {
                            commonViewModel.initVlg(vlgList)
                        }

                        if (schlList.isNotEmpty()) {
                            commonViewModel.initSchl(schlList)
                        }

                        if (prsnInfoList.isNotEmpty()) {
                            commonViewModel.initPrsnInfo(prsnInfoList)
                        }

                        if (splyPlanList.isNotEmpty()) {
                            commonViewModel.initSplyPlan(splyPlanList)
                        }

                        if (srvcList.isNotEmpty()) {
                            commonViewModel.initSrvc(srvcList)
                        }

                        if (chCuslInfoList.isNotEmpty()) {
                            commonViewModel.initChCuslInfo(chCuslInfoList)
                        }

                        if (relshList.isNotEmpty()) {
                            commonViewModel.initRelsh(relshList)
                        }

                        if (letrList.isNotEmpty()) {
                            commonViewModel.initLetr(letrList)
                        }

                        if (gmnyList.isNotEmpty()) {
                            commonViewModel.initGmny(gmnyList)
                        }

                        if (chmstList.isNotEmpty()) {
                            reportViewModel.initAllChild(chmstList)
                        }

                        if (reportList.isNotEmpty()) {
                            reportViewModel.initAll(reportList, true)
                        }

                        logger.debug("$i lines read...")

                        val initDateTime = dataFile.name.substringAfterLast("_").toLong()
                        val history = APP_DATA_HISTORY(fileId = dataFile.name, type = "INIT", datetime = initDateTime, registDate = Date().time)
                        reportViewModel.saveAppDataHistory(history)
                    }

                    if (targetDir.isDirectory && targetDir.exists()) targetDir.deleteRecursively()
                }
            } catch (Ex: Exception) {
                Ex.printStackTrace()
                logger.error("Error in doInBackground: $Ex")
                onProgressMessage("Initialization failed.")
                onErrorListener()
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<InitSyncFragment> {
        lateinit var menualContainer: LinearLayout
        lateinit var errorMenualContainer: LinearLayout

        lateinit var startButton: Button

        lateinit var syncProgressBar: ProgressBar

        lateinit var progressTextView: TextView

        lateinit var progressDatabaseTextView: TextView

        override fun createView(ui: AnkoContext<InitSyncFragment>) = with(ui) {
            verticalLayout {
                gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER

                padding = dimen(R.dimen.px40)

                verticalLayout {
                    gravity = Gravity.CENTER

                    textView("Files Initialization") {
                        gravity = Gravity.CENTER
                        textColorResource = R.color.colorWhite
                        textSizeDimen = R.dimen.sp50
                        typeface = Typeface.DEFAULT_BOLD
                    }.lparams(width = wrapContent, height = wrapContent)

                    space { }.lparams(height = dimen(R.dimen.px50))

                    startButton = button("Start") {
                        gravity = Gravity.CENTER
                        backgroundColorResource = R.color.colorAccent
                        textColorResource = R.color.colorWhite
//                    textSizeDimen = R.dimen.px26
                        typeface = Typeface.DEFAULT_BOLD
                        allCaps = false
                    }.lparams(width = dip(150), height = wrapContent)

                    syncProgressBar = progressBar {
                        visibility = View.GONE
                    }

                    space { }.lparams(height = dimen(R.dimen.px20))

                    progressTextView = textView {
                        gravity = Gravity.CENTER
                        textColorResource = R.color.colorWhite
                    }

                    progressDatabaseTextView = textView {
                        gravity = Gravity.CENTER
                        textColorResource = R.color.colorWhite
                    }
                }.lparams(width = matchParent, height = wrapContent) {
                    margin = dimen(R.dimen.px20)
                }

                menualContainer = verticalLayout {
                    view {
                        backgroundColorResource = R.color.colorAccent
                    }.lparams(width = matchParent, height = dip(1))

                    space { }.lparams(width = matchParent, height = dimen(R.dimen.px20))

                    textView("How to Load Initialize File") {
                        typeface = Typeface.DEFAULT_BOLD
                        textColorResource = R.color.colorWhite
                        textSize = 14f
                    }
                    space { }.lparams(width = matchParent, height = dimen(R.dimen.px20))
                    textView("""
                        |1. connect App to PC with Data Cable.
                        |2. Find 'Initial Files' with Window Explore.
                        |3. Copy the 'Initial Files' to the Directory in App.
                        |- Directory : Phone/GoodNeighbors/init
                        |4. If you copied the files, Click 'Start' button over.
                        |""".trimMargin()) {
                        textColorResource = R.color.colorWhite
                        textSize = 12f
                    }
                }.lparams {
                    margin = dimen(R.dimen.px20)
                }

                errorMenualContainer = verticalLayout {
                    visibility = View.GONE

                    view {
                        backgroundColorResource = R.color.colorAccent
                    }.lparams(width = matchParent, height = dip(1))

                    space { }.lparams(width = matchParent, height = dimen(R.dimen.px20))

                    textView("File Load Failed") {
                        typeface = Typeface.DEFAULT_BOLD
                        textColorResource = R.color.colorWhite
                        textSize = 14f
                    }
                    space { }.lparams(width = matchParent, height = dimen(R.dimen.px20))
                    textView("""
                        |1. Please Re-Check the 'Initial File' has been Copied Conrrectly
                        |2. Then Click the 'Start' button Again.
                        |""".trimMargin()) {
                        textColorResource = R.color.colorWhite
                        textSize = 12f
                    }
                }.lparams {
                    margin = dimen(R.dimen.px20)
                }
            }
        }
    }
}
