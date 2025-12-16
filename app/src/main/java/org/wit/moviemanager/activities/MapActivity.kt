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
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
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

        // Initialize Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.MAPS_API_KEY))
        }

        location = intent.extras?.getParcelable<Location>("location")!!
        cinemaName = intent.extras?.getString("cinema") ?: ""
        cinemaAddress = intent.extras?.getString("cinemaAddress") ?: ""

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Setup autocomplete search
        setupAutocomplete()

        onBackPressedDispatcher.addCallback(this) {
            returnResult()
        }
    }

    private fun setupAutocomplete() {
        val autocompleteFragment = supportFragmentManager
            .findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        // Configure to search for establishments (businesses/venues)
        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS
            )
        )

        // Set hint and configure search
        autocompleteFragment.setHint("Search for cinema or venue")

        // Set type filter to show establishments (businesses)
        autocompleteFragment.setTypesFilter(listOf("establishment"))

        // Set country restriction (optional - Ireland only)
        autocompleteFragment.setCountries("IE")

        // Handle place selection from search
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                i("Place selected from search: ${place.name}")

                place.latLng?.let { latLng ->
                    currentMarker?.remove()

                    location.lat = latLng.latitude
                    location.lng = latLng.longitude
                    location.zoom = 15f
                    cinemaName = place.name ?: ""
                    cinemaAddress = place.address ?: getAddressFromLocation(latLng)

                    currentMarker = map.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(cinemaName)
                            .snippet(cinemaAddress)
                    )
                    currentMarker?.showInfoWindow()
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                    i("Selected from search: $cinemaName at $latLng")
                    i("Address: $cinemaAddress")
                }
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
                i("Autocomplete error: $status")
            }
        })
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

        i("Map ready - tap on a business/place to select OR use search bar")
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