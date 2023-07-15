package dev.nasser.devicelocation.location.providers

import dev.nasser.devicelocation.location.LocationProvider
import dev.nasser.devicelocation.location.ServiceLocationProvider
import dev.nasser.devicelocation.utils.ServiceConnectionListener

/**
 * Created by Nasser Munshi on 04-Jul-2023.
 * @author Nasser Munshi
 */
/**
 * A decorator for a [ServiceConnectionListener] used to execute the [ ]'s fail over.
 *
 */
internal class FallbackListenerWrapper(
    parentProvider: MultiFallbackProvider,
    childProvider: ServiceLocationProvider
) : ServiceConnectionListener {
    private val listener: ServiceConnectionListener?
    private val fallbackProvider: MultiFallbackProvider
    private val childProvider: ServiceLocationProvider

    init {
        fallbackProvider = parentProvider
        this.childProvider = childProvider
        listener = childProvider.fetchServiceConnectionListener()
    }

    override fun onConnected() {
        listener?.onConnected()
    }

    override fun onConnectionSuspended() {
        listener?.onConnectionSuspended()
        runFallback()
    }

    override fun onConnectionFailed() {
        listener?.onConnectionFailed()
        runFallback()
    }

    private fun runFallback() {
        val current: LocationProvider? = fallbackProvider.getCurrentProvider()
        if (current != null && current == childProvider) {
            fallbackProvider.fallbackProvider()
        }
    }
}