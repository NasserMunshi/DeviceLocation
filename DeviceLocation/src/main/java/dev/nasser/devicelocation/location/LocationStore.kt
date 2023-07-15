package dev.nasser.devicelocation.location

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import androidx.annotation.VisibleForTesting
import dev.nasser.devicelocation.common.Store

/**
 * Created by Nasser Munshi on 04-Jul-2023.
 * @author Nasser Munshi
 */

class LocationStore(context: Context) : Store<Location?> {
    private var preferences: SharedPreferences?

    init {
        preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
    }

    @VisibleForTesting
    fun setPreferences(preferences: SharedPreferences?) {
        this.preferences = preferences
    }

    override fun put(id: String?, location: Location?) {
        if (id == null) return
        if (location == null) return
        val editor = preferences!!.edit()
        editor.putString(getFieldKey(id, PROVIDER_ID), location.provider)
        editor.putLong(
            getFieldKey(id, LATITUDE_ID),
            java.lang.Double.doubleToLongBits(location.latitude)
        )
        editor.putLong(
            getFieldKey(id, LONGITUDE_ID),
            java.lang.Double.doubleToLongBits(location.longitude)
        )
        editor.putFloat(getFieldKey(id, ACCURACY_ID), location.accuracy)
        editor.putLong(
            getFieldKey(id, ALTITUDE_ID),
            java.lang.Double.doubleToLongBits(location.altitude)
        )
        editor.putFloat(getFieldKey(id, SPEED_ID), location.speed)
        editor.putLong(getFieldKey(id, TIME_ID), location.time)
        editor.putFloat(getFieldKey(id, BEARING_ID), location.bearing)
        editor.apply()
    }

    override fun get(id: String?): Location? {
        if (id == null) return null
        return if (preferences != null && preferences!!.contains(
                getFieldKey(
                    id,
                    LATITUDE_ID
                )
            ) && preferences!!.contains(
                getFieldKey(id, LONGITUDE_ID)
            )
        ) {
            val location = Location(preferences!!.getString(PROVIDER_ID, PROVIDER))
            location.latitude = java.lang.Double.longBitsToDouble(
                preferences!!.getLong(
                    getFieldKey(
                        id,
                        LATITUDE_ID
                    ), 0
                )
            )
            location.longitude = java.lang.Double.longBitsToDouble(
                preferences!!.getLong(
                    getFieldKey(
                        id,
                        LONGITUDE_ID
                    ), 0
                )
            )
            location.accuracy =
                preferences!!.getFloat(getFieldKey(id, ACCURACY_ID), 0f)
            location.altitude = java.lang.Double.longBitsToDouble(
                preferences!!.getLong(
                    getFieldKey(
                        id,
                        ALTITUDE_ID
                    ), 0
                )
            )
            location.speed =
                preferences!!.getFloat(getFieldKey(id, SPEED_ID), 0f)
            location.time =
                preferences!!.getLong(getFieldKey(id, TIME_ID), 0)
            location.bearing =
                preferences!!.getFloat(getFieldKey(id, BEARING_ID), 0f)
            location
        } else {
            null
        }
    }

    override fun remove(id: String?) {
        if (id == null) return
        val editor = preferences!!.edit()
        editor.remove(getFieldKey(id, PROVIDER_ID))
        editor.remove(getFieldKey(id, LATITUDE_ID))
        editor.remove(getFieldKey(id, LONGITUDE_ID))
        editor.remove(getFieldKey(id, ACCURACY_ID))
        editor.remove(getFieldKey(id, ALTITUDE_ID))
        editor.remove(getFieldKey(id, SPEED_ID))
        editor.remove(getFieldKey(id, TIME_ID))
        editor.remove(getFieldKey(id, BEARING_ID))
        editor.apply()
    }

    private fun getFieldKey(id: String, field: String): String {
        return PREFIX_ID + "_" + id + "_" + field
    }

    companion object {
        const val PROVIDER = "LocationStore"
        private const val PREFERENCES_FILE = "LOCATION_STORE"
        private val PREFIX_ID = LocationStore::class.java.canonicalName + ".KEY"
        private const val PROVIDER_ID = "PROVIDER"
        private const val LATITUDE_ID = "LATITUDE"
        private const val LONGITUDE_ID = "LONGITUDE"
        private const val ACCURACY_ID = "ACCURACY"
        private const val ALTITUDE_ID = "ALTITUDE"
        private const val SPEED_ID = "SPEED"
        private const val TIME_ID = "TIME"
        private const val BEARING_ID = "BEARING"
    }
}