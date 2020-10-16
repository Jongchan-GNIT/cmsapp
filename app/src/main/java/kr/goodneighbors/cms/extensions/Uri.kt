package kr.goodneighbors.cms.extensions

import android.content.Context
import android.net.Uri
import android.provider.MediaStore


fun Uri.getRealPath(context:Context) : String {
    val result: String
    val cursor = context.applicationContext.contentResolver.query(this, null, null, null, null)
    if (cursor == null) { // Source is Dropbox or other similar local file path
        result = this.getPath()
    } else {
        cursor.moveToFirst()
        val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        result = cursor.getString(idx)
        cursor.close()
    }
    return result
}