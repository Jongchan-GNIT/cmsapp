package kr.goodneighbors.cms.extensions

import android.widget.EditText

fun EditText.getIntValue() : Int? {
    return if (text.toString().isNullOrBlank()) null
    else text.toString().toIntOrNull()
}

fun EditText.getStringValue() : String {
    return text.toString().trim()
}