package kr.goodneighbors.cms.extensions

import java.io.File
import java.security.MessageDigest

fun File.directory(): String {
    return path.substringBeforeLast("/")
}

fun File.fullName(): String {
    return path.substringAfterLast("/")
}

fun File.fileName(): String {
    return fullName().substringBeforeLast(".")
}

fun File.extension(): String {
    return fullName().substringAfterLast(".")
}

fun File.fileId(): String {
    return fileName().substringBeforeLast("_")
}

fun File.renameWithHash(): File {

    val bytes = readBytes()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    val hash = digest.fold("") { str, it -> str + "%02x".format(it) }
    val target = File(path.split(".zip").joinToString("_$hash.zip"))
    renameTo(target)
    return target
}