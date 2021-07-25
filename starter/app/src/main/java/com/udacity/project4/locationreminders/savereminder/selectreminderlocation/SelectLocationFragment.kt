package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    companion object {
        private const val TAG = "SelectLocationFragment"
        private const val DEFAULT_ZOOM = 15f
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // LocationRequest - Requirements for the location updates, i.e., how often you
    // should receive updates, the priority, etc.
    private lateinit var locationRequest: LocationRequest

    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
    private lateinit var locationCallback: LocationCallback
    private lateinit var lastLocation: Location
    private lateinit var selectedPoi: PointOfInterest
    private lateinit var selectedMarker: Marker

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        // Setup map
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.location_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val locationClient = context?.let {
            LocationServices.getFusedLocationProviderClient(
                it
            )
        }
        if (locationClient != null) {
            fusedLocationProviderClient = locationClient
        }
        else {
            Log.e(TAG, "Unable to get LocationService")
        }

        locationRequest = LocationRequest.create().apply {
            // Sets the desired interval for active location updates. This interval is inexact. You
            // may not receive updates at all if no location sources are available, or you may
            // receive them less frequently than requested. You may also receive updates more
            // frequently than requested if other applications are requesting location at a more
            // frequent interval.
            //
            // IMPORTANT NOTE: Apps running on Android 8.0 and higher devices (regardless of
            // targetSdkVersion) may receive updates less frequently than this interval when the app
            // is no longer in the foreground.
            interval = TimeUnit.SECONDS.toMillis(60)

            // Sets the fastest rate for active location updates. This interval is exact, and your
            // application will never receive updates more frequently than this value.
            fastestInterval = TimeUnit.SECONDS.toMillis(30)

            // Sets the maximum time when batched location updates are delivered. Updates may be
            // delivered sooner than this interval.
            maxWaitTime = TimeUnit.MINUTES.toMillis(2)

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                // Set the map's camera position to the current location of the device
                lastLocation = locationResult.lastLocation
                Log.d(TAG, "Latitude: " + lastLocation.latitude)
                Log.d(TAG, "Longitude: " + lastLocation.longitude)
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            lastLocation.latitude,
                            lastLocation.longitude
                        ), DEFAULT_ZOOM
                    )
                )
            }
        }

        binding.saveLocationButton
            .setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setMapStyle(map)
        enableMyLocation()
        setPoiClick(map)
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }

    }

    // Request enable user location. Get device location when permission is granted.
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.setMyLocationEnabled(true)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            getDeviceLocation()
        }
        else {
            requestPermissions(
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            if (this::selectedMarker.isInitialized) {
                selectedMarker.remove()
            }
            selectedMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            selectedMarker.showInfoWindow()
            selectedPoi = poi
        }
    }

    private fun onLocationSelected() {
        if (this::selectedPoi.isInitialized) {
            _viewModel.selectedPOI.value = selectedPoi
            _viewModel.latitude.value = selectedPoi.latLng.latitude
            _viewModel.longitude.value = selectedPoi.latLng.longitude
            _viewModel.reminderSelectedLocationStr.value = selectedPoi.name
        }
        findNavController().popBackStack()
    }

    private fun isPermissionGranted(): Boolean {
        val context = context ?: return false

        return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    /*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     * Zoom user location.
     */
    private fun getDeviceLocation() {
        try {
            if (isPermissionGranted()) {
                val locationResult: Task<Location> = fusedLocationProviderClient.lastLocation
                activity?.let { it ->
                    locationResult.addOnCompleteListener(
                        it,
                        OnCompleteListener<Location?> { task ->
                            if (task.isSuccessful && (task.result != null)) {
                                // Set the map's camera position to the current location of the device
                                    lastLocation = task.result as Location
                                Log.d(TAG, "Latitude: " + lastLocation.getLatitude())
                                Log.d(TAG, "Longitude: " + lastLocation.getLongitude())
                                map.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(
                                            lastLocation.getLatitude(),
                                            lastLocation.getLongitude()
                                        ), DEFAULT_ZOOM
                                    )
                                )
                            } else {
                                Log.d(TAG, "Current location is null. Using defaults.")
                                Log.e(TAG, "Exception: %s", task.exception)
                            }
                        })
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message!!)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            }
            else {
                map.setMyLocationEnabled(false)
                Snackbar.make(binding.root,
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                    _viewModel.navigationCommand.value = NavigationCommand.Back
                }.show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

}
