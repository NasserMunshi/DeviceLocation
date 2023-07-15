package dev.nasser.devicelocation.location.providers

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build.VERSION.SDK_INT
import android.os.Looper
import androidx.core.app.ActivityCompat
import dev.nasser.devicelocation.OnLocationUpdatedListener
import dev.nasser.devicelocation.location.LocationProvider
import dev.nasser.devicelocation.location.LocationStore
import dev.nasser.devicelocation.location.config.LocationAccuracy
import dev.nasser.devicelocation.location.config.LocationError
import dev.nasser.devicelocation.location.config.LocationParams
import dev.nasser.devicelocation.utils.logDlInfo
import java.util.function.Consumer

/**
 * Created by Nasser Munshi on 04-Jul-2023.
 * @author Nasser Munshi
 */
class LocationManagerProvider : LocationProvider, LocationListener {
    private lateinit var locationManager: LocationManager
    private var listener: OnLocationUpdatedListener? = null
    private var locationStore: LocationStore? = null
    private lateinit var mContext: Context
    private var isCacheEnabled = false

    override fun init(context: Context?, isCacheEnabled: Boolean) {
        locationManager =
            (context?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager) ?: return
        mContext = context
        this.isCacheEnabled = isCacheEnabled
        if (isCacheEnabled) locationStore = LocationStore(context)
    }

    private fun isLocationPermissionGranted(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            "Permission check failed. Please handle location permission.".logDlInfo()
            return false
        }
        return true
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = (
                mContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ) ?: return false
        return locationManager.isProviderEnabled(
            LocationManager.GPS_PROVIDER
        ) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private val locationCallback = Consumer<Location> { location ->
        listener?.onLocationUpdated(location)
    }

    @SuppressLint("MissingPermission")
    override fun start(
        listener: OnLocationUpdatedListener?,
        params: LocationParams,
        singleUpdate: Boolean
    ) {
        this.listener = listener
        if (listener == null) {
            "Listener is null, you sure about this?".logDlInfo()
        }
        val criteria = getProvider(params)
        val locationProvider = getLocationProvider()
        if (!isLocationPermissionGranted()) {
            listener?.onFailed(LocationError.NO_PERMISSION)
            return
        }

        if (!isLocationEnabled()) {
            listener?.onFailed(LocationError.NOT_ENABLED)
            return
        }

        if (singleUpdate) {
            if (SDK_INT >= android.os.Build.VERSION_CODES.R) {
                locationManager.getCurrentLocation(
                    locationProvider,
                    null,
                    mContext.mainExecutor,
                    locationCallback
                )
            } else {
                locationManager.requestSingleUpdate(
                    criteria,
                    this,
                    Looper.getMainLooper()
                )
            }
        } else {
            locationManager.requestLocationUpdates(
                locationProvider,
                params.getInterval(),
                params.getDistance(),
                this,
                Looper.getMainLooper()
            )
        }
    }

    override fun stop() {
        if (!isLocationPermissionGranted()) {
            listener?.onFailed(LocationError.NO_PERMISSION)
            return
        }

        if (!isLocationEnabled()) {
            listener?.onFailed(LocationError.NOT_ENABLED)
            return
        }
        locationManager.removeUpdates(this)
    }

    @SuppressLint("MissingPermission")
    override fun getLastLocation(): Location? {
        if (!isLocationPermissionGranted()) {
            listener?.onFailed(LocationError.NO_PERMISSION)
            return null
        }

        if (!isLocationEnabled()) {
            listener?.onFailed(LocationError.NOT_ENABLED)
            return null
        }
        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { return it }
        return locationStore?.get(LM_PROVIDER_ID)
    }

    private fun getLocationProvider(): String {
        return LocationManager.GPS_PROVIDER
    }

    private fun getProvider(params: LocationParams): Criteria {
        val criteria = Criteria()
        when (params.getAccuracy()) {
            LocationAccuracy.HIGH -> {
                criteria.accuracy = Criteria.ACCURACY_FINE
                criteria.horizontalAccuracy = Criteria.ACCURACY_HIGH
                criteria.verticalAccuracy = Criteria.ACCURACY_HIGH
                criteria.bearingAccuracy = Criteria.ACCURACY_HIGH
                criteria.speedAccuracy = Criteria.ACCURACY_HIGH
                criteria.powerRequirement = Criteria.POWER_HIGH
            }
            LocationAccuracy.MEDIUM -> {
                criteria.accuracy = Criteria.ACCURACY_COARSE
                criteria.horizontalAccuracy = Criteria.ACCURACY_MEDIUM
                criteria.verticalAccuracy = Criteria.ACCURACY_MEDIUM
                criteria.bearingAccuracy = Criteria.ACCURACY_MEDIUM
                criteria.speedAccuracy = Criteria.ACCURACY_MEDIUM
                criteria.powerRequirement = Criteria.POWER_MEDIUM
            }
            LocationAccuracy.LOW, LocationAccuracy.LOWEST -> {
                criteria.accuracy = Criteria.ACCURACY_COARSE
                criteria.horizontalAccuracy = Criteria.ACCURACY_LOW
                criteria.verticalAccuracy = Criteria.ACCURACY_LOW
                criteria.bearingAccuracy = Criteria.ACCURACY_LOW
                criteria.speedAccuracy = Criteria.ACCURACY_LOW
                criteria.powerRequirement = Criteria.POWER_LOW
            }
            else -> {
                criteria.accuracy = Criteria.ACCURACY_COARSE
                criteria.horizontalAccuracy = Criteria.ACCURACY_LOW
                criteria.verticalAccuracy = Criteria.ACCURACY_LOW
                criteria.bearingAccuracy = Criteria.ACCURACY_LOW
                criteria.speedAccuracy = Criteria.ACCURACY_LOW
                criteria.powerRequirement = Criteria.POWER_LOW
            }
        }
        return criteria
    }

    override fun onLocationChanged(location: Location) {
        listener?.onLocationUpdated(location)
        if (locationStore != null) {
            locationStore?.put(LM_PROVIDER_ID, location)
        }
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    companion object {
        private const val LM_PROVIDER_ID = "LMP"
    }
}