package dev.nasser.devicelocation

import android.location.Location
import dev.nasser.devicelocation.location.config.LocationError

/**
 * Created by Nasser Munshi on 03-Jul-2023.
 * @author Nasser Munshi
 */
interface OnLocationUpdatedListener {
    fun onLocationUpdated(location: Location?)
    fun onFailed(locationError: LocationError)
}