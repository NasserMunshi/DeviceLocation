package dev.nasser.devicelocation

import android.content.Context
import android.location.Location
import dev.nasser.devicelocation.location.LocationProvider
import dev.nasser.devicelocation.location.config.LocationParams
import dev.nasser.devicelocation.location.providers.FusedLocationWithFallbackProvider
import dev.nasser.devicelocation.location.utils.LocationState
import java.util.*

/**
 * Created by Nasser Munshi on 04-Jul-2023.
 * @author Nasser Munshi
 */

/**
 * Creates the DeviceLocation basic instance.
 *
 * @param context       execution context
 * @param logger        logger interface
 * @param defaultInitialize TRUE (default) if we want to instantiate directly the default providers. FALSE if we want to initialize them ourselves.
 */
class DeviceLocation constructor(
    private val context: Context,
    private val isCacheEnabled: Boolean = false,
    private var defaultInitialize: Boolean = true
) {

    /**
     * @param provider location provider we want to use
     * @return request handler for location operations
     */
    /**
     * @return request handler for location operations
     */
    @JvmOverloads
    fun location(
        provider: LocationProvider = FusedLocationWithFallbackProvider(context)
    ): LocationControl {
        return LocationControl(this, provider)
    }

    class LocationControl(
        private val deviceLocation: DeviceLocation,
        locationProvider: LocationProvider
    ) {
        private var params: LocationParams
        private val provider: LocationProvider?
        private var oneFix: Boolean

        init {
            params = LocationParams.BEST_EFFORT
            oneFix = false
            if (!MAPPING.containsKey(deviceLocation.context)) {
                MAPPING[deviceLocation.context] = locationProvider
            }
            provider = MAPPING[deviceLocation.context]
            if (deviceLocation.defaultInitialize) {
                provider?.init(deviceLocation.context, deviceLocation.isCacheEnabled)
            }
        }

        fun config(params: LocationParams): LocationControl {
            this.params = params
            return this
        }

        fun oneFix(): LocationControl {
            oneFix = true
            return this
        }

        fun continuous(): LocationControl {
            oneFix = false
            return this
        }

        fun state(): LocationState? {
            return LocationState.with(deviceLocation.context)
        }

        val lastLocation: Location?
            get() = provider?.getLastLocation()

        fun get(): LocationControl {
            return this
        }

        fun start(listener: OnLocationUpdatedListener?) {
            if (provider == null) {
                throw RuntimeException("A provider must be initialized")
            }
            provider.start(listener, params, oneFix)
        }

        fun stop() {
            provider?.stop()
            MAPPING.remove(deviceLocation.context)
        }

        companion object {
            private val MAPPING: MutableMap<Context, LocationProvider> = WeakHashMap()
        }
    }

    companion object {
        fun with(context: Context): DeviceLocation {
            return DeviceLocation(context, true)
        }
    }
}