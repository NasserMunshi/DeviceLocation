package dev.nasser.devicelocation.location.providers

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import dev.nasser.devicelocation.OnLocationUpdatedListener
import dev.nasser.devicelocation.location.LocationStore
import dev.nasser.devicelocation.location.ServiceLocationProvider
import dev.nasser.devicelocation.location.config.LocationAccuracy
import dev.nasser.devicelocation.location.config.LocationError
import dev.nasser.devicelocation.location.config.LocationParams
import dev.nasser.devicelocation.utils.ServiceConnectionListener
import dev.nasser.devicelocation.utils.logDlError
import dev.nasser.devicelocation.utils.logDlInfo


/**
 * Created by Nasser Munshi on 12-Jul-2023.
 * @author Nasser Munshi
 */
class FusedLocationProvider : ServiceLocationProvider, LocationListener {

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var locationRequest: LocationRequest
    private var context: Context? = null
    private var locationStarted = false
    private var locationStore: LocationStore? = null
    private var listener: OnLocationUpdatedListener? = null
    private var serviceListener: ServiceConnectionListener? = null
    private var isCacheEnabled = false

    override fun fetchServiceConnectionListener(): ServiceConnectionListener? {
        return serviceListener
    }

    override fun addServiceConnectionListener(listener: ServiceConnectionListener) {
        serviceListener = listener
    }

    override fun init(context: Context?, isCacheEnabled: Boolean) {
        this.context = context
        this.isCacheEnabled = isCacheEnabled
        if (context == null) return
        if (isCacheEnabled) locationStore = LocationStore(context)
        if (!locationStarted) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        } else {
            "Location Already Started".logDlError()
        }
    }


    private fun createRequest(
        params: LocationParams, singleUpdate: Boolean
    ): LocationRequest {
        var locationAccuracy = 0
        when (params.getAccuracy()) {
            LocationAccuracy.HIGH -> locationAccuracy = Priority.PRIORITY_HIGH_ACCURACY
            LocationAccuracy.MEDIUM -> locationAccuracy = Priority.PRIORITY_BALANCED_POWER_ACCURACY
            LocationAccuracy.LOW -> locationAccuracy = Priority.PRIORITY_LOW_POWER
            LocationAccuracy.LOWEST -> locationAccuracy = Priority.PRIORITY_PASSIVE
            else -> "Invalid accuracy.".logDlError()
        }

        val locationRequest = LocationRequest.Builder(
            locationAccuracy, params.getInterval()
        ).setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(params.getInterval())
            .setMinUpdateDistanceMeters(params.getDistance())

        if (singleUpdate) {
            locationRequest.setMaxUpdates(1)
        }

        return locationRequest.build()
    }

    override fun start(
        listener: OnLocationUpdatedListener?,
        params: LocationParams,
        singleUpdate: Boolean
    ) {
        this.listener = listener
        if (listener == null) {
            "Listener is null, you sure about this?".logDlInfo()
        }
        locationRequest = createRequest(params, singleUpdate)
        startUpdating()
    }

    @SuppressLint("MissingPermission")
    private fun startUpdating() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                locationStarted = true
                fusedLocationClient?.requestLocationUpdates(
                    locationRequest, this, Looper.getMainLooper()
                )?.addOnFailureListener {
                    "Location Failed: ${it.localizedMessage}".logDlError()
                    serviceListener?.onConnectionFailed()
                }
            } else {
                "Location is not enabled".logDlError()
                listener?.onFailed(LocationError.NOT_ENABLED)
            }
        } else {
            "Permission is not granted".logDlError()
            listener?.onFailed(LocationError.NO_PERMISSION)
        }
    }

    override fun stop() {
        locationStarted = false
        fusedLocationClient?.removeLocationUpdates(this)
        fusedLocationClient = null
    }

    @SuppressLint("MissingPermission")
    override fun getLastLocation(): Location? {
        return locationStore?.get(GMS_ID)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = (
                context?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ) ?: return false
        return locationManager.isProviderEnabled(
            LocationManager.GPS_PROVIDER
        ) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        context?.let { c ->
            if (ActivityCompat.checkSelfPermission(
                    c, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    c, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                return true
            }
            return false
        } ?: kotlin.run {
            return false
        }
    }

    override fun onLocationChanged(location: Location) {
        if (listener != null) {
            listener?.onLocationUpdated(location)
        }
        if (locationStore != null) {
            "Location Stored".logDlInfo()
            locationStore?.put(GMS_ID, location)
        }
    }

    companion object {
        private const val GMS_ID = "GMS"
    }
}