package kr.goodneighbors.cms.extensions

import java.text.SimpleDateFormat
import java.util.*

fun Date.timeToString(format: String = "yyyyMMddHHmmss"): String = SimpleDateFormat(format, Locale.getDefault()).format(this)