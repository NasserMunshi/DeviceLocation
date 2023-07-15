package dev.nasser.devicelocation.location.providers

import android.content.Context
import android.location.Location
import dev.nasser.devicelocation.OnLocationUpdatedListener
import dev.nasser.devicelocation.location.LocationProvider
import dev.nasser.devicelocation.location.ServiceLocationProvider
import dev.nasser.devicelocation.location.config.LocationParams
import java.util.*

/**
 * Created by Nasser Munshi on 04-Jul-2023.
 * @author Nasser Munshi
 */
/**
 * A [LocationProvider] that allows multiple location services to be used. <br></br><br></br> New
 * instances of `MultiFallbackProvider` must be initialized via the Builder class:
 * <pre>
 * LocationProvider provider = new MultiLocationProvider.Builder()
 * .withGooglePlayServicesProvider()
 * .withDefaultProvider()
 * .build();
</pre> *
 * `MultiFallbackProvider` will attempt to use the location services in the order they
 * were added to the builder.  If the provider fails to connect to the underlying service, the next
 * provider in the list is used. <br></br><br></br> If no providers are added to the builder, the [ ] is used by default.
 *
 */
class MultiFallbackProvider internal constructor() : LocationProvider {
    private val providers: Queue<LocationProvider>
    private var currentProvider: LocationProvider? = null
    private var context: Context? = null
    private var locationListener: OnLocationUpdatedListener? = null
    private var locationParams: LocationParams? = null
    private var singleUpdate = false
    private var shouldStart = false
    private var isCacheEnabled = false

    init {
        providers = LinkedList<LocationProvider>()
    }

    override fun init(context: Context?, isCacheEnabled: Boolean) {
        this.context = context
        this.isCacheEnabled = isCacheEnabled
        getCurrentProvider()?.init(context, isCacheEnabled)
    }

    override fun start(
        listener: OnLocationUpdatedListener?,
        params: LocationParams,
        singleUpdate: Boolean
    ) {
        shouldStart = true
        locationListener = listener
        locationParams = params
        this.singleUpdate = singleUpdate
        getCurrentProvider()?.start(listener, params, singleUpdate)
    }

    override fun stop() {
        getCurrentProvider()?.stop()
    }

    override fun getLastLocation(): Location? {
        val current: LocationProvider = getCurrentProvider() ?: return null
        return current.getLastLocation()
    }

    fun addProvider(provider: LocationProvider): Boolean {
        return providers.add(provider)
    }

    fun getProviders(): Collection<LocationProvider> {
        return providers
    }

    /**
     * Gets the current `LocationProvider` instance in use.
     *
     * @return the underlying `LocationProvider` used for location services.
     */
    fun getCurrentProvider(): LocationProvider? {
        if (currentProvider == null && !providers.isEmpty()) {
            currentProvider = providers.poll()
        }
        return currentProvider
    }

    /**
     * Fetches the next location provider in the fallback list, and initializes it. If location
     * updates have already been started, this restarts location updates.<br></br><br></br>If there are no
     * location providers left, no action occurs.
     */
    fun fallbackProvider() {
        if (!providers.isEmpty()) {
            // Stop the current provider if it is running
            currentProvider?.stop()
            // Fetch the next provider in the list.
            currentProvider = providers.poll()
            currentProvider?.init(context, isCacheEnabled)
            if (shouldStart) {
                currentProvider?.start(locationListener, locationParams ?: return, singleUpdate)
            }
        }
    }

    /**
     * Builder class for the [MultiFallbackProvider].
     */
    class Builder {
        private val builtProvider: MultiFallbackProvider = MultiFallbackProvider()

        /**
         * Adds Google Location Services as a provider.
         */
        fun withGooglePlayServicesProvider(): Builder {
            return withServiceProvider(FusedLocationProvider())
        }

        /**
         * Adds the built-in Android Location Manager as a provider.
         */
        fun withDefaultProvider(): Builder {
            return withProvider(LocationManagerProvider())
        }

        /**
         * Adds the given [ServiceLocationProvider] as a location provider. If the given
         * location provider detects that its underlying service is not available, the built
         * `MultiFallbackProvider` will fall back to the next location provider in the
         * list.
         *
         * @param provider a `ServiceLocationProvider` that can detect if the underlying
         * location service is not available.
         */
        fun withServiceProvider(provider: ServiceLocationProvider): Builder {
            val fallbackListener = FallbackListenerWrapper(
                builtProvider,
                provider
            )
            provider.addServiceConnectionListener(fallbackListener)
            return withProvider(provider)
        }

        /**
         * Adds the given [LocationProvider] as a provider. Note that these providers
         * **DO NOT** support fallback behavior.
         *
         * @param provider a `LocationProvider` instance.
         */
        fun withProvider(provider: LocationProvider): Builder {
            builtProvider.addProvider(provider)
            return this
        }

        /**
         * Builds a [MultiFallbackProvider] instance. If no providers were added to the
         * builder, the built-in Android Location Manager is used.
         */
        fun build(): MultiFallbackProvider {
            // Always ensure we have the default provider
            if (builtProvider.providers.isEmpty()) {
                withDefaultProvider()
            }
            return builtProvider
        }
    }
}