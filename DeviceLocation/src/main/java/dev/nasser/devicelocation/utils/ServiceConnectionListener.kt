package dev.nasser.devicelocation.utils

/**
 * Created by Nasser Munshi on 03-Jul-2023.
 * @author Nasser Munshi
 */

interface ServiceConnectionListener {
    /**
     * Callback when a successful connection to a 3rd party service is made
     */
    fun onConnected()

    /**
     * Callback when the connection to a 3rd party service is interrupted (network failure,
     * temporary outage, etc.)
     */
    fun onConnectionSuspended()

    /**
     * Callback when the connection to a 3rd party service fails (missing libraries, bad API key,
     * etc.)
     */
    fun onConnectionFailed()
}