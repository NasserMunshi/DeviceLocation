package dev.nasser.devicelocation.location.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils

/**
 * Created by Nasser Munshi on 04-Jul-2023.
 * @author Nasser Munshi
 */
class LocationState private constructor(context: Context) {
    private val context: Context
    private val locationManager: LocationManager

    init {
        this.context = context
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    /**
     * Indicates if location services are enabled for the device.
     *
     * @return `true` if the user has turned on location services.
     */
    fun locationServicesEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            var locationMode = Settings.Secure.LOCATION_MODE_OFF
            try {
                locationMode =
                    Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
            } catch (ignored: SettingNotFoundException) {
                // This is ignored
            }
            locationMode != Settings.Secure.LOCATION_MODE_OFF
        } else {
            val locationProviders = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED
            )
            !TextUtils.isEmpty(locationProviders)
        }
    }

    /**
     * Indicates if any *active* location provider is enabled.
     *
     * @return `true` if an active location provider (network, GPS) is enabled.
     */
    fun isAnyProviderAvailable(): Boolean {
        return isGpsAvailable() || isNetworkAvailable()
    }

    /**
     * Indicates if GPS location updates are enabled.
     *
     * @return `true` if GPS location updates are enabled.
     */
    fun isGpsAvailable(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /**
     * Indicates if location updates from mobile network signals are enabled.
     *
     * @return `true` if location can be determined from mobile network signals.
     */
    fun isNetworkAvailable(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Indicates if the "passive" location provider is enabled.
     *
     * @return `true` if location updates from other applications are enabled.
     */
    fun isPassiveAvailable(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)
    }

    /**
     * Indicates if the device allows mock locations.
     *
     * @return `true` if mock locations are enabled for the entire device.
     */
    @Deprecated(
        """use {@link android.location.Location#isFromMockProvider()} instead for Android
      KitKat devices and higher."""
    )
    fun isMockSettingEnabled(): Boolean {
        return "0" != Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ALLOW_MOCK_LOCATION
        )
    }

    companion object {
        // Safe to suppress because this is always an application context
        @SuppressLint("StaticFieldLeak")
        private var instance: LocationState? = null
        fun with(context: Context): LocationState? {
            if (instance == null) {
                instance = LocationState(context.applicationContext)
            }
            return instance
        }
    }
}