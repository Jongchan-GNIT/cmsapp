package kr.goodneighbors.cms.extensions

import java.security.MessageDigest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

fun String.convertDateFormat(before: String = "yyyyMMdd", after: String = "MM-dd-yyyy") : String {
    if (isNullOrBlank()) return ""

    return try {
        val formatter = DateTimeFormatter.ofPattern(before, Locale.ENGLISH)
        val date = LocalDate.parse(trim(), formatter)
        date.format(DateTimeFormatter.ofPattern(after))
    } catch(e: Exception) {
        this
    }
}

fun String.isNumber(): Boolean {
    if (isNullOrBlank()) return false
    return this.matches("-?\\d+(\\.\\d+)?".toRegex())
}

fun String.toSHA(): String {
    val bytes = toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}