package edu.ucsb.cs.cs184.urth

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONArray
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
        mMap = googleMap

        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // ensure location permissions are enabled
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show()
        }

        var location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        var startLatLng: LatLng
        // center map on current location
        startLatLng = if (location != null){
            LatLng(location.latitude, location.longitude)
        } else {
            LatLng(-34.0, 151.0)
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(startLatLng))

        // initialize variables
        var marker: Marker? = null
        var citySet: HashSet<String>
        var countySet: HashSet<String>
        var stateSet: HashSet<String>
        var countrySet: HashSet<String>
        var locationSet: HashSet<String>
        val TAG = "requestTag"

        // handle map clicks
        mMap.setOnMapClickListener {
            Log.d("CLICKED", "$it")
            marker?.remove()
            marker = mMap.addMarker(MarkerOptions().position(it))

            citySet = HashSet()
            countySet = HashSet()
            stateSet = HashSet()
            countrySet = HashSet()

            try {
                val geocoder = Geocoder(this, Locale.getDefault())
                val locations = geocoder.getFromLocation(it.latitude, it.longitude, 1)

                if (locations != null && locations.size > 0){
                    val city = locations[0].locality
                    val county = locations[0].subAdminArea
                    val state = locations[0].adminArea
                    val country = locations[0].countryName

                    Log.d("WHAT", "City: $city")
                    Log.d("WHAT", "County: $county")
                    Log.d("WHAT", "State: $state")
                    Log.d("WHAT", "country: $country")

                    citySet.add(city)
                    countySet.add(county)
                    stateSet.add(state)
                    countrySet.add(country)
                }
            } catch (e: IOException){
                e.printStackTrace()
            }

            val queue = Volley.newRequestQueue(this)
            var url = "http://gd.geobytes.com/GetNearbyCities?"
            val radius = 50

            url += "radius=$radius&Latitude=${it.latitude}&Longitude=${it.longitude}"



            Log.d(TAG, url)
            val stringRequest = StringRequest(Request.Method.GET, url,
                { response ->
                    Log.d(TAG, response)

                    val responseArray = JSONArray(response)
                    for (i in 0 until responseArray.length()){
                        if (responseArray.getJSONArray(i).length() >= 1) {
                            citySet.add(responseArray.getJSONArray(i).getString(1))
                        }
                    }
                    locationSet = (citySet + countySet + stateSet + countrySet) as HashSet<String>
                    Log.d(TAG, citySet.toString())
                    Toast.makeText(this, locationSet.toString(), Toast.LENGTH_LONG).show()
                },
                { error ->
                    Log.d("ERROR", error.toString())
                })

            stringRequest.tag = TAG
            queue.add(stringRequest)

        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
