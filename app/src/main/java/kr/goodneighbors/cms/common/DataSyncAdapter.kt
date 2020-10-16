package kr.goodneighbors.cms.common

import android.Manifest
import android.arch.lifecycle.Observer
import android.content.pm.PackageManager
import android.os.Environment
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobile.auth.core.IdentityHandler
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.extensions.broadcastFile
import kr.goodneighbors.cms.extensions.fileId
import kr.goodneighbors.cms.extensions.observeOnce
import kr.goodneighbors.cms.extensions.renameWithHash
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
import kr.goodneighbors.cms.service.viewmodel.SyncViewModel
import org.jetbrains.anko.support.v4.runOnUiThread
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.zip.*

class DataSyncAdapter(val fragment: Fragment) {
    init {
        AWSMobileClient.getInstance().initialize(fragment.requireContext()) {
            credentialsProvider = AWSMobileClient.getInstance().credentialsProvider
            awsConfiguration = AWSMobileClient.getInstance().configuration

            IdentityManager.getDefaultIdentityManager().getUserID(object : IdentityHandler {
                override fun handleError(exception: Exception?) {
                    logger.debug("----------Retrieving identity: ${exception?.message}")
                }

                override fun onIdentityId(identityId: String?) {
                    val cachedIdentityId = IdentityManager.getDefaultIdentityManager().cachedUserID
                    logger.debug("----------Identity = $identityId, cachedIdentityId = $cachedIdentityId")
                }
            })
        }.execute()
    }

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(DataSyncAdapter::class.java)
    }

    private val viewModel: SyncViewModel by lazy {
        SyncViewModel()
    }

    private var credentialsProvider: AWSCredentialsProvider? = null
    private var awsConfiguration: AWSConfiguration? = null

    fun import(filePath: String,
               onSuccessListener: (fileId: String) -> Unit,
               onErrorListener: (message: String) -> Unit,
               onPublishProgress: (message: String) -> Unit) {
        try {
            val sdMain = Environment.getExternalStorageDirectory()

            // 초기화 디렉토리 탐색
            val initZipFile = File(filePath)
            if (!initZipFile.exists()) {
                onErrorListener("${initZipFile.fileId()} : File Not Found!")
                return
            }

            val fileId = initZipFile.fileId()

            onPublishProgress("Start Updating...")

            val targetDirPath = "$sdMain/${Constants.DIR_HOME}/${Constants.DIR_TEMP}/$fileId"
            val tempTargetDir = File(targetDirPath)
            if (tempTargetDir.exists() && tempTargetDir.isDirectory) {
                logger.debug("디렉토리 발견..제거..$tempTargetDir")

                tempTargetDir.deleteRecursively()
            }

            // unzip 대상 디렉토리 생성
            val targetDir = File(targetDirPath)
            logger.debug("target dir : $targetDir")
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            // unzip
            onPublishProgress("Extract the synchronization file")
            unzip(initZipFile, "${targetDir.toPath()}")

            val initDataFile = File(targetDir.path, fileId)
            if (!initDataFile.exists()) {
                onErrorListener("Data file not found!")
                return
            }

            onPublishProgress("Preparing data for initialization")

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
            val chmstList = arrayListOf<CH_MST>()
            val reportList = arrayListOf<RPT_BSC>()
            val srvcList = arrayListOf<SRVC>()
            val chCuslInfoList = arrayListOf<CH_CUSL_INFO>()
            val relshList = arrayListOf<RELSH>()
            val letrList = arrayListOf<LETR>()
            val gmnyList = arrayListOf<GMNY>()
            val bmiList = arrayListOf<BMI>()

            val totalLineCount = Files.lines(initDataFile.toPath()).count()

            initDataFile.forEachLine { lineString ->
                i++

                onPublishProgress("Reading sync data : $i of $totalLineCount")

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
                                    "NOTI_INFO" -> {
                                        val notiInfo = gson.fromJson(set.value, NOTI_INFO::class.java)
                                        notiInfoList.add(notiInfo)
                                    }
                                    "CD" -> {
                                        val cd = gson.fromJson(set.value, CD::class.java)
                                        cdList.add(cd)
                                    }

                                    "CTR" -> {
                                        val ctr = gson.fromJson(set.value, CTR::class.java)
                                        ctrList.add(ctr)
                                    }
                                    "BRC" -> {
                                        val brc = gson.fromJson(set.value, BRC::class.java)
                                        brcList.add(brc)
                                    }
                                    "BMI" -> {
                                        val bmi = gson.fromJson(set.value, BMI::class.java)
                                        bmiList.add(bmi)
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
                                    "CH_MST" -> {
                                        val child = gson.fromJson(set.value, CH_MST::class.java)
                                        chmstList.add(child)
                                    }
                                    "RPT_BSC" -> {
                                        val report = gson.fromJson(set.value, RPT_BSC::class.java)

                                        if (report.RCP_NO.isNotEmpty() && report.CHRCP_NO.isNotEmpty()) {
//                                                        reportViewModel.saveRPT_BSC(report)
                                            reportList.add(report)
                                        }
                                    }
                                    "SRVC" -> {
                                        val srvc = gson.fromJson(set.value, SRVC::class.java)
                                        srvcList.add(srvc)
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
                                }

                            } catch (e: Exception) {
                                logger.error("key : ${set.key} /// not found : ", e)
                            }
                        }
                    }
                }
            }

            onPublishProgress("Start database update!")

            if (notiInfoList.isNotEmpty()) {
                viewModel.saveAllNOTI_INFO(notiInfoList)
            }

            if (cdList.isNotEmpty()) {
                viewModel.saveAllCD(cdList)
            }

            if (ctrList.isNotEmpty()) {
                viewModel.saveAllCTR(ctrList)
            }

            if (brcList.isNotEmpty()) {
                viewModel.saveAllBRC(brcList)
            }

            if (bmiList.isNotEmpty()) {
                viewModel.saveAllBMI(bmiList)
            }

            if (prjList.isNotEmpty()) {
                viewModel.saveAllPRJ(prjList)
            }

            if (userinfoList.isNotEmpty()) {
                viewModel.saveAllUSER_INFO(userinfoList)
            }

            if (vlgList.isNotEmpty()) {
                viewModel.saveAllVLG(vlgList)
            }

            if (schlList.isNotEmpty()) {
                viewModel.saveAllSCHL(schlList)
            }

            if (prsnInfoList.isNotEmpty()) {
                viewModel.saveAllPRSN_INFO(prsnInfoList)
            }

            if (splyPlanList.isNotEmpty()) {
                viewModel.saveAllSPLY_PLAN(splyPlanList)
            }

            if (srvcList.isNotEmpty()) {
                viewModel.saveAllSRVC(srvcList)
            }

            val destPath = "$sdMain/${Constants.DIR_HOME}/${Constants.DIR_CONTENTS}"
            if (chCuslInfoList.isNotEmpty()) {
                viewModel.saveAllCH_CUSL_INFO(chCuslInfoList, targetDir.path, destPath)
            }

            if (relshList.isNotEmpty()) {
                viewModel.saveAllRELSH(relshList)
            }

            if (letrList.isNotEmpty()) {
                viewModel.saveAllLETR(letrList)
            }

            if (gmnyList.isNotEmpty()) {
                viewModel.saveAllGMNY(gmnyList)
            }

            if (chmstList.isNotEmpty()) {
                viewModel.saveAllCH_MST(chmstList)
            }

            if (reportList.isNotEmpty()) {
                fragment.runOnUiThread {
                    onPublishProgress("Report data updating...")
                    viewModel.saveAllRPT_BSC(reportList, targetDir.path, destPath).observeOnce(fragment, Observer {processState->
                        processState?.apply {
                            when(this) {
                                ProcessState.DONE -> onSuccessListener(fileId)
                                ProcessState.ERROR -> onErrorListener("There was an error updating your data.")
                                else -> {}
                            }
                        }
                    })
                }
            }
            else {
                onSuccessListener(fileId)
            }

            val initDateTime = fileId.substringAfterLast("_").toLong()
            val history = APP_DATA_HISTORY(fileId = fileId, type = "IMPORT", datetime = initDateTime, registDate = Date().time)
            viewModel.saveAppDataHistory(history)
        } catch (Ex: Exception) {
            Ex.printStackTrace()
            logger.error("Error in doInBackground: $Ex")
            onErrorListener("Sync failed")
        }
    }

    fun export(imei: String,
               isOffline: Boolean = false,
               onSuccessListener: (filePath: String) -> Unit,
               onErrorListener: (message: String) -> Unit,
               onPublishProgress: (message: String) -> Unit) {
        logger.debug("export($imei)")
        var returnFilePath: String?

        try {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(fragment.requireContext())
            val ctrCd = sharedPref.getString("user_ctr_cd", "")
            val brcCd = sharedPref.getString("user_brc_cd", "")
            val prjCd = sharedPref.getString("user_prj_cd", "")

            val cdp = arrayOf(ctrCd, brcCd, prjCd).joinToString("")

            val sdMain = Environment.getExternalStorageDirectory()
            val contentsRootDir = File("$sdMain/${Constants.DIR_HOME}/${Constants.DIR_CONTENTS}")
            val fileName = generateFileName(cdp)

            if (ActivityCompat.checkSelfPermission(fragment.requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                onErrorListener("Do not have write permission!")
                return
            }

            // 임시 디렉토리 생성
            onPublishProgress("Create temporary directory...")
            val tempDir = File("$sdMain/${Constants.DIR_HOME}/${Constants.DIR_TEMP}", fileName)
//            val tempContentsDir = File(tempDir, "sw/dev")

            if (!tempDir.exists()) {
                logger.debug("임시 디렉토리 생성 : ${tempDir.path}")
                tempDir.mkdirs()
            }

            // 파일 생성
            onPublishProgress("Create data file...")
            val dataFile = File(tempDir, fileName)

            dataFile.bufferedWriter().use { out ->
                out.write("S|||$cdp|||$imei\n")
            }

            // 데이터 조회
            onPublishProgress("Preparing data...")

            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().create()
            viewModel.findAllExportData().observeOnce(fragment, android.arch.lifecycle.Observer { exportData ->
                exportData?.apply {
                    ////////// CH_MST //////////
                    onPublishProgress("Converting Children Data : ${children?.size ?: 0}")
                    logger.debug("Children 데이터 변환 : ${children?.size ?: 0}")

                    children?.forEachIndexed { index, child ->
                        val json = gson.toJson(child)
                        logger.debug("child : $json")
                        dataFile.appendText("D|||{\"CH_MST\":$json}\n")
                    }

                    ////////// RPT_BSC //////////
                    onPublishProgress("Converting Report data : ${reports?.size ?: 0}")
                    logger.debug("Reports 데이터 변환 : ${reports?.size ?: 0}")

                    reports?.forEachIndexed { _, report ->
                        val json = gson.toJson(report)
                        logger.debug("report : $json")
                        dataFile.appendText("D|||{\"RPT_BSC\":$json}\n")

                        val files = report.ATCH_FILE
                        if (files != null && files.size > 0) {
                            files.forEach { file ->
                                logger.debug("ATCH_FILE : $file")
                                val contentsfile = File(contentsRootDir, "${file.FILE_PATH}/${file.FILE_NM}")
                                if (contentsfile.exists()) {
                                    val contentsTargetDir = File(tempDir, file.FILE_PATH)
                                    if (!contentsTargetDir.exists()) {
                                        contentsTargetDir.mkdirs()
                                    }
                                    contentsfile.copyTo(File(contentsTargetDir, file.FILE_NM), true)
                                }
                            }
                        }
                    }
                    ////////// SRVC //////////
                    onPublishProgress("Converting Proviced Service data : ${services?.size ?: 0}")
                    logger.debug("Proviced Service 데이터 변환 : ${services?.size ?: 0}")
                    services?.forEachIndexed { index, item ->
                        val json = gson.toJson(item)
                        logger.debug("service : $json")
                        dataFile.appendText("D|||{\"SRVC\":$json}\n")
                    }

                    ////////// CH_CUSL_INFO //////////
                    onPublishProgress("Converting Counseling data : ${counseling?.size ?: 0}")
                    logger.debug("Counseling 데이터 변환 : ${counseling?.size ?: 0}")
                    counseling?.forEachIndexed { index, item ->
                        val json = gson.toJson(item)
                        logger.debug("couseling : $json")
                        dataFile.appendText("D|||{\"CH_CUSL_INFO\":$json}\n")

                        val contentsfile = File(contentsRootDir, "${item.IMG_FP}/${item.IMG_NM}")
                        if (contentsfile.exists()) {
                            val contentsTargetDir = File(tempDir, item.IMG_FP)
                            if (!contentsTargetDir.exists()) {
                                contentsTargetDir.mkdirs()
                            }
                            contentsfile.copyTo(File(contentsTargetDir, item.IMG_NM), true)
                        }
                    }

                    ////////// MOD_HIS_INFO //////////
                    onPublishProgress("Converting History data : ${history?.size ?: 0}")
                    logger.debug("History 데이터 변환 : ${history?.size ?: 0}")
                    history?.forEachIndexed { index, item ->
                        val json = gson.toJson(item)
                        logger.debug("history : $json")
                        dataFile.appendText("D|||{\"MOD_HIS_INFO\":$json}\n")
                    }

                    dataFile.appendText("E|||")

                    onPublishProgress("Compressing files...")
                    val zipFileName = "$sdMain/${Constants.DIR_HOME}/${if (isOffline) Constants.DIR_EXPORT_OFFLINE else Constants.DIR_EXPORT}/$fileName.zip"
                    zip(tempDir.path, zipFileName)
                    val hashedFile = File(zipFileName).renameWithHash()
                    returnFilePath = hashedFile.path
                    logger.debug("file path : $returnFilePath")
                    onPublishProgress("File Compression Complete.")

                    val history = APP_DATA_HISTORY(fileId = dataFile.name, type = "EXPORT", datetime = dataFile.name.substringAfterLast("_").toLong(), registDate = Date().time)
                    logger.debug("history : $history")
                    viewModel.saveAppDataHistory(history)

                    fragment.broadcastFile(returnFilePath!!)
                    onSuccessListener(returnFilePath!!)
                }

            })
        } catch (e: Exception) {
            e.printStackTrace()
            logger.error("Error in doInBackground", e)

            onErrorListener("Synchronization file creation failed.")
        }
    }

    fun upload(imei: String,
               onSuccessListener: (zippedFilePath: String) -> Unit,
               onErrorListener: (message: String) -> Unit,
               onPublishProgress: (message: String) -> Unit,
               onUploadProgress: (progress: Int) -> Unit) {
        fun copyToFail(zippedFilePath: String) {
            val f = File(zippedFilePath)

            if (f.exists()) {
                val sdMain = Environment.getExternalStorageDirectory()
                val failDir = File("$sdMain/${Constants.DIR_HOME}/${Constants.DIR_FAIL}")
                f.copyTo(File(failDir, f.name))
            }
        }
        export(imei = imei,
                onSuccessListener = {zippedFilePath->
                    try {
                        logger.debug(zippedFilePath)

                        onPublishProgress("Prepare to upload.")

                        val sharedPref = PreferenceManager.getDefaultSharedPreferences(fragment.requireContext())
                        val userid = sharedPref.getString("userid", "")
                        val bucket = sharedPref.getString("ref_1", "")
                        val bucketRootPath = sharedPref.getString("ref_2", "sw")

                        val sdMain = Environment.getExternalStorageDirectory()

                        val targetDirPath = "$sdMain/${Constants.DIR_HOME}/${Constants.DIR_CONTENTS}"
                        val targetFile = File(zippedFilePath)

                        logger.debug("targetDirPath = $targetDirPath")
                        logger.debug("targetFile = ${targetFile.length()}, $targetFile")

                        var userAwsConfig = AWSConfiguration(fragment.requireContext(), R.raw.awsconfiguration_ap)

                        when (bucketRootPath) {
                            "vg" -> {
                                userAwsConfig = AWSConfiguration(fragment.requireContext(), R.raw.awsconfiguration_us)
                            }
                            "re" -> {
                                userAwsConfig = AWSConfiguration(fragment.requireContext(), R.raw.awsconfiguration_eu)
                            }
                        }
                        val userAwsConfigJsonObject = userAwsConfig.optJsonObject("CredentialsProvider").getJSONObject("CognitoIdentity").getJSONObject("Default")
                        logger.debug("userAwsConfigJsonObject : $userAwsConfigJsonObject, ${userAwsConfigJsonObject.getString("Region")}, ${userAwsConfigJsonObject.getString("PoolId")}")

                        val userCredentialsProvider = CognitoCachingCredentialsProvider(
                                fragment.requireContext(), // Context
                                userAwsConfigJsonObject.getString("PoolId"), // Identity Pool rcp_no
                                Regions.fromName(userAwsConfigJsonObject.getString("Region")) // Region
                        )

                        val scClientEndPoint = "http://s3-${userAwsConfigJsonObject.getString("Region")}.amazonaws.com"
                        logger.debug("scClientEndPoint : $scClientEndPoint // Regions : ${userAwsConfigJsonObject.getString("Region")} /// PoolId : ${userAwsConfigJsonObject.getString("PoolId")}")

                        val scClient = AmazonS3Client(userCredentialsProvider)

                        val txUtil = TransferUtility.builder()
                                .context(fragment.requireContext())
                                .awsConfiguration(userAwsConfig)
                                .s3Client(scClient)
                                .build()

                        val uploadkey = "$bucketRootPath/${Constants.BUILD}/sync/up/$userid/${targetFile.name}"
                        logger.debug("bucket : $bucket /// upload key : $uploadkey /// file : ${targetFile.path}")

                        onPublishProgress("Start uploading...")

                        val txObserver = txUtil.upload(bucket, uploadkey, targetFile, CannedAccessControlList.PublicRead)
                        txObserver.setTransferListener(object : TransferListener {
                            override fun onStateChanged(id: Int, state: TransferState) {
                                logger.debug("onStateChanged($id, $state)")
                                if (state == TransferState.COMPLETED) {
                                    logger.debug("onStateChanged completed upload111")
                                    onUploadProgress(100)

                                    onPublishProgress("Calling server API...")

                                    viewModel.callUploadAPI(uploadkey).observeOnce(fragment, Observer {
                                        it?.apply {
                                            if (this == "SUCCESS") {
                                                onSuccessListener(zippedFilePath)
                                            }
                                            else {
                                                onErrorListener(this)
                                            }
                                        }
                                    })
                                }

                                if (state == TransferState.CANCELED) {
                                    logger.debug("onStateChanged TransferState.CANCELED")
                                    onErrorListener("AWS SDK State : TransferState.CANCELED\nMaking sync data is failed check your network is available or generate sync data through offline")
                                    copyToFail(zippedFilePath)
                                }

                                if (state == TransferState.FAILED) {
                                    logger.debug("onStateChanged TransferState.FAILED")
                                    onErrorListener("AWS SDK State : TransferState.FAILED\nMaking sync data is failed check your network is available or generate sync data through offline")
                                    copyToFail(zippedFilePath)
                                }
                            }

                            override fun onProgressChanged(id: Int, current: Long, total: Long) {
                                val done: Int = ((current / total.toDouble()) * 100).toInt()
                                logger.debug("onProgressChanged rcp_no: $id, percent done = $done, total : $total, current: $current")
                                onUploadProgress(done)
                            }

                            override fun onError(id: Int, ex: Exception) {
                                logger.error("onError : $id - ", ex)
                                ex.printStackTrace()
                            }
                        })

                        if (txObserver.state == TransferState.COMPLETED) {
                            logger.debug("onStateChanged completed upload")
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        logger.error("Error: ", e)
                    }
                },
                onErrorListener = {m ->
                    onErrorListener(m)

                },
                onPublishProgress = { m ->
                    onPublishProgress(m)
                })
    }


    private fun generateFileName(code: String): String {
        return "${code}_U_${Date().time}"
    }

    private fun zip(sourceDirPath: String, zipFilePath: String) {
        File(zipFilePath).let { if (it.exists()) it.delete() }

        val zipFile = Files.createFile(Paths.get(zipFilePath))

        ZipOutputStream(Files.newOutputStream(zipFile)).use { stream ->
            val sourceDir = Paths.get(sourceDirPath)
            Files.walk(sourceDir).filter { path -> !Files.isDirectory(path) }.forEach { path ->
                val zipEntry = ZipEntry(path.toString().substring(sourceDir.toString().length + 1))

                stream.putNextEntry(zipEntry)
                stream.write(Files.readAllBytes(path))
                stream.closeEntry()
            }
        }
    }

    private fun unzip(zipedFile: File, targetPath: String) {
        val zipFile = net.lingala.zip4j.core.ZipFile(zipedFile)
        zipFile.extractAll(targetPath)
    }
}