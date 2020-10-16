@file:Suppress("LocalVariableName")

package kr.goodneighbors.cms.service.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.os.Environment
import kr.goodneighbors.cms.common.Constants
import kr.goodneighbors.cms.service.db.SettingDao
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingRepository @Inject constructor(
        private val dao: SettingDao
) {
    private lateinit var deletePastContentFileResult:MutableLiveData<Boolean>
    fun deletePastContentFile(): LiveData<Boolean> {
        deletePastContentFileResult = MutableLiveData()
        Thread(Runnable {
            val sdMain = Environment.getExternalStorageDirectory()
            val contentsRootDir = File("$sdMain/${Constants.DIR_HOME}/${Constants.DIR_CONTENTS}")

            try {
                dao.findAllPastReportContentFile()?.forEach {
                    val f = File(contentsRootDir, "${it.FILE_PATH}/${it.FILE_NM}")
                    if (f.exists()) {
                        f.delete()
                    }
                }

                dao.findAllPastCouseingContentFile()?.forEach {
                    val f = File(contentsRootDir, "${it.IMG_FP}/${it.IMG_NM}")
                    if (f.exists()) {
                        f.delete()
                    }
                }

                deletePastContentFileResult.postValue(true)
            }
            catch(e: Exception) {
                e.printStackTrace()
                deletePastContentFileResult.postValue(false)
            }
        }).start()
        return deletePastContentFileResult
    }
}