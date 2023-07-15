package dev.nasser.devicelocation.location

import android.content.Context
import android.location.Location
import dev.nasser.devicelocation.OnLocationUpdatedListener
import dev.nasser.devicelocation.location.config.LocationParams

/**
 * Created by Nasser Munshi on 04-Jul-2023.
 * @author Nasser Munshi
 */
interface LocationProvider {
    fun init(context: Context?, isCacheEnabled: Boolean)
    fun start(listener: OnLocationUpdatedListener?, params: LocationParams, singleUpdate: Boolean)
    fun stop()
    fun getLastLocation(): Location?
}