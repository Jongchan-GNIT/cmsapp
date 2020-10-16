package kr.goodneighbors.cms.extensions

import android.content.Context
import org.jetbrains.anko.connectivityManager

inline fun Context.isNetworkAvailable(): Boolean = connectivityManager.activeNetworkInfo?.isConnected ?: false