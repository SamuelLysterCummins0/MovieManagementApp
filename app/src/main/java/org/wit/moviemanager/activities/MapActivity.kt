package org.wit.moviemanager.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import org.wit.moviemanager.databinding.ActivityMapBinding
import org.wit.moviemanager.R
import org.wit.moviemanager.models.Location
import androidx.activity.addCallback
import timber.log.Timber.i
import java.util.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnPoiClickListener {

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapBinding
    private var location = Location()
    private var currentMarker: Marker? = null
    private var cinemaName: String = ""
    private var cinemaAddress: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = "Select Cinema Location"
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        location = intent.extras?.getParcelable<Location>("location")!!
        cinemaName = intent.extras?.getString("cinema") ?: ""
        cinemaAddress = intent.extras?.getString("cinemaAddress") ?: ""

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        onBackPressedDispatcher.addCallback(this) {
            returnResult()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        returnResult()
        return true
    }

    private fun returnResult() {
        val resultIntent = Intent()
        resultIntent.putExtra("location", location)
        resultIntent.putExtra("cinema", cinemaName)
        resultIntent.putExtra("cinemaAddress", cinemaAddress)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true

        // Enable My Location button if permission granted
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = true
        }

        val loc = LatLng(location.lat, location.lng)
        map.setOnPoiClickListener(this)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, location.zoom))

        // Restore previous marker if exists
        if (cinemaName.isNotEmpty() && location.lat != 0.0 && location.lng != 0.0) {
            val markerOptions = MarkerOptions()
                .position(loc)
                .title(cinemaName)
                .snippet(cinemaAddress.ifEmpty { "Selected Cinema" })
            currentMarker = map.addMarker(markerOptions)
            currentMarker?.showInfoWindow()
            i("Restored marker: $cinemaName at $loc")
        }

        i("Map ready - tap on a business/place to select")
    }

    override fun onPoiClick(poi: PointOfInterest) {
        currentMarker?.remove()

        location.lat = poi.latLng.latitude
        location.lng = poi.latLng.longitude
        location.zoom = map.cameraPosition.zoom

        cinemaName = poi.name
        cinemaAddress = getAddressFromLocation(poi.latLng)

        val marker = map.addMarker(
            MarkerOptions()
                .position(poi.latLng)
                .title(poi.name)
                .snippet(cinemaAddress)
        )
        currentMarker = marker
        marker?.showInfoWindow()

        i("Selected POI: ${poi.name} at ${poi.latLng}")
        i("Address: $cinemaAddress")
    }

    private fun getAddressFromLocation(latLng: LatLng): String {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val addressLine = address.getAddressLine(0)
                addressLine ?: "Address not available"
            } else {
                "Address not available"
            }
        } catch (e: Exception) {
            i("Geocoder error: ${e.message}")
            "Address not available"
        }
    }
}