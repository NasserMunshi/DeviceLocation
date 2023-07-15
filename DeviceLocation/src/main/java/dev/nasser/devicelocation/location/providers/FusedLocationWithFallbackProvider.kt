package dev.nasser.devicelocation.location.providers

import android.content.Context
import android.location.Location
import android.os.Bundle
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dev.nasser.devicelocation.OnLocationUpdatedListener
import dev.nasser.devicelocation.location.LocationProvider
import dev.nasser.devicelocation.location.config.LocationParams
import dev.nasser.devicelocation.utils.GooglePlayServicesListener
import dev.nasser.devicelocation.utils.logDlDebug
import dev.nasser.devicelocation.utils.logDlInfo

/**
 * Created by Nasser Munshi on 04-Jul-2023.
 * @author Nasser Munshi
 */
class FusedLocationWithFallbackProvider(context: Context) :
    LocationProvider, GooglePlayServicesListener {
    private var listener: OnLocationUpdatedListener? = null
    private var shouldStart = false
    private var context: Context? = null
    private var params: LocationParams? = null
    private var singleUpdate = false
    private var provider: LocationProvider
    private var isCacheEnabled = false

    init {
        provider = if (
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                context
            ) == ConnectionResult.SUCCESS
        ) {
            FusedLocationProvider()
        } else {
            LocationManagerProvider()
        }
    }

    override fun init(context: Context?, isCacheEnabled: Boolean) {
        this.context = context
        this.isCacheEnabled = isCacheEnabled
        "Currently selected provider = ${provider.javaClass.simpleName}".logDlInfo()
        provider.init(context, isCacheEnabled)
    }

    override fun start(
        listener: OnLocationUpdatedListener?,
        params: LocationParams,
        singleUpdate: Boolean
    ) {
        shouldStart = true
        this.listener = listener
        this.params = params
        this.singleUpdate = singleUpdate
        provider.start(listener, params, singleUpdate)
    }

    override fun stop() {
        provider.stop()
        shouldStart = false
    }

    override fun getLastLocation(): Location? {
        return provider.getLastLocation()
    }

    override fun onConnected(bundle: Bundle?) {
        // Nothing to do here
    }

    override fun onConnectionSuspended(i: Int) {
        fallbackToLocationManager()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult?) {
        fallbackToLocationManager()
    }

    private fun fallbackToLocationManager() {
        "FusedLocationProvider not working, falling back and using LocationManager".logDlDebug()
        provider = LocationManagerProvider()
        provider.init(context, isCacheEnabled)
        if (shouldStart) {
            provider.start(listener, params ?: return, singleUpdate)
        }
    }
}