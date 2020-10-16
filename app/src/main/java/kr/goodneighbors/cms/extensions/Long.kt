package kr.goodneighbors.cms.extensions

import java.text.SimpleDateFormat
import java.util.*

fun Long.toDateFormat(_pattern: String = "MM-dd-yyyy") : String = SimpleDateFormat(_pattern, Locale.getDefault()).format(Date(this))
