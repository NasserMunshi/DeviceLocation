package dev.nasser.devicelocation.location

import dev.nasser.devicelocation.utils.ServiceConnectionListener

/**
 * Created by Nasser Munshi on 04-Jul-2023.
 * @author Nasser Munshi
 */
/**
 * An extension of the [LocationProvider] interface for location providers that utilize 3rd
 * party services. Implementations must invoke the appropriate [ServiceConnectionListener]
 * events when the connection to the 3rd party service succeeds, fails, or is suspended.
 */
interface ServiceLocationProvider : LocationProvider {
    /**
     * Gets the [ServiceConnectionListener] callback for this location provider.
     */

    fun fetchServiceConnectionListener(): ServiceConnectionListener?

    /**
     * Set the [ServiceConnectionListener] used for callbacks from the 3rd party service.
     *
     * @param listener a `ServiceConnectionListener` to respond to connection events from
     * the underlying 3rd party location service.
     */

    fun addServiceConnectionListener(listener: ServiceConnectionListener)
}