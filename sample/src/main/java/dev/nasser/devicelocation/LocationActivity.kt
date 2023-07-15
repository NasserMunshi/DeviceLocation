package dev.nasser.devicelocation

import android.Manifest
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import dev.nasser.devicelocation.location.config.LocationError
import dev.nasser.devicelocation.location.config.LocationError.*
import dev.nasser.devicelocation.location.config.LocationParams
import dev.nasser.devicelocation.location.providers.LocationManagerProvider
import dev.nasser.devicelocation.utils.toDlTimeDateString

class LocationActivity : AppCompatActivity() {

    private val askSinglePermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { result ->
            if (result) {
                setResult(1)
                startFetchLocation()
            } else {

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        findViewById<Button>(R.id.btnStart).setOnClickListener {
            onLocationStartClicked()
        }

        findViewById<Button>(R.id.btnEnd).setOnClickListener {
            onLocationStopClicked()
        }
    }

    private fun startFetchLocation() {

        // Single location with default initialization

        DeviceLocation.with(this).location().oneFix().start(
            object : OnLocationUpdatedListener {
                override fun onLocationUpdated(location: Location?) {
                    showLocation(location)
                }

                override fun onFailed(locationError: LocationError) {
                    when (locationError) {
                        NOT_ENABLED -> {}
                        NO_PERMISSION -> {}
                    }
                }
            })



        // Location with provider
        /*
        val provider = LocationManagerProvider()
        provider.init(context = this, isCacheEnabled = false)

        DeviceLocation(
            context = this,
            isCacheEnabled = false,
            defaultInitialize = false
        ).location(provider).config(
            LocationParams.NAVIGATION
        ).start(
            object : OnLocationUpdatedListener {
                override fun onLocationUpdated(location: Location?) {
                    showLocation(location)
                }

                override fun onFailed(locationError: LocationError) {
                    when (locationError) {
                        NOT_ENABLED -> {}
                        NO_PERMISSION -> {}
                    }
                }
            })
         */
    }

    private fun showLocation(location: Location?) {
        location?.let {
            val isMocLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                location.isMock
            } else {
                location.isFromMockProvider
            }
            val locationText = "Latitude: ${location.latitude} | Longitude: ${location.longitude}" +
                    " | Is Moc: $isMocLocation | Time: ${location.time.toDlTimeDateString()}"
            findViewById<TextView>(R.id.tvInfo).text = locationText

        } ?: kotlin.run {
            findViewById<TextView>(R.id.tvInfo).text = "Null location"
        }
    }

    private fun onLocationStartClicked() {
        findViewById<TextView>(R.id.tvInfo).text = "Updating Location"
        askSinglePermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun onLocationStopClicked() {
        findViewById<TextView>(R.id.tvInfo).text = "Location Update Stopped!"
        DeviceLocation.with(this).location().stop()
    }
}