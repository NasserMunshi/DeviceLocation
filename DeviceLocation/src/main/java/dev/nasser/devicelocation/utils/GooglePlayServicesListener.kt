package dev.nasser.devicelocation.utils

import android.os.Bundle
import com.google.android.gms.common.ConnectionResult

/**
 * Created by Nasser Munshi on 03-Jul-2023.
 * @author Nasser Munshi
 */
interface GooglePlayServicesListener {
    fun onConnected(bundle: Bundle?)
    fun onConnectionSuspended(i: Int)
    fun onConnectionFailed(connectionResult: ConnectionResult?)
}