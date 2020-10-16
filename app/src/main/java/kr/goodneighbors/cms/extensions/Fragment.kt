package kr.goodneighbors.cms.extensions

import android.content.Intent
import android.net.Uri
import android.support.v4.app.Fragment
import java.io.File

fun Fragment.broadcastFile(path: String) {
    val file = File(path)
    if (file.exists()) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = Uri.fromFile(file)
        activity?.sendBroadcast(mediaScanIntent)
    }
}