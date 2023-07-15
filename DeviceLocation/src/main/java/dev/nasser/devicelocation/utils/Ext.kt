package dev.nasser.devicelocation.utils

import android.util.Log
import dev.nasser.devicelocation.BuildConfig
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Nasser Munshi on 05-Mar-2023.
 * @author Nasser Munshi
 */

fun Long.toDlTimeDateString(): String {
    val dateTime = Date(this)
    val format = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.US)
    return format.format(dateTime)
}

//------------------------------------------
// Log
//------------------------------------------
const val APP_TAG = "DeviceLocation"

fun String.logDlVerbose(tag: String = APP_TAG) {
    if (BuildConfig.DEBUG)
        Log.v(tag, this)
}

fun String.logDlDebug(tag: String = APP_TAG) {
    if (BuildConfig.DEBUG)
        Log.d(tag, this)
}

fun String.logDlInfo(tag: String = APP_TAG) {
    if (BuildConfig.DEBUG)
        Log.i(tag, this)
}

fun String.logDlWarn(tag: String = APP_TAG) {
    if (BuildConfig.DEBUG)
        Log.w(tag, this)

}

fun String.logDlError(tag: String = APP_TAG) {
    if (BuildConfig.DEBUG)
        Log.e(tag, this)
}
