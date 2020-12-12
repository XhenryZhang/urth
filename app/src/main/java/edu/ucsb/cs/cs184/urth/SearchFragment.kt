package edu.ucsb.cs.cs184.urth

import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONArray
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SearchFragment : Fragment() {
    private lateinit var viewModel: NewsViewModel

    // map objects
    lateinit var mMapView: MapView
    private lateinit var googleMap: GoogleMap

    // arguments passed to the API query
    private lateinit var location: Array<String>
    private lateinit var date: String
    private var searchType: Boolean = false

    // only gets called once the view is created
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    // location information
    // genre of news -- currently defaults to 2 days ago
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(activity as ViewModelStoreOwner).get(NewsViewModel::class.java)

        // initialize map
        mMapView = view.findViewById(R.id.mapView) as MapView
        mMapView.onCreate(savedInstanceState)
        mMapView.onResume()
        try {
            MapsInitializer.initialize(activity?.applicationContext)
        } catch (e: Exception){
            e.printStackTrace()
        }

        // implement map functionality
        mMapView.getMapAsync(OnMapReadyCallback { mMap ->
            googleMap = mMap

            // request and wait for location permissions to be granted
//            if (ActivityCompat.checkSelfPermission(
//                    requireContext(),
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                    requireContext(),
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                ActivityCompat.requestPermissions(
//                    requireActivity(),
//                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100
//                )
//
//                // pause app until permissions are added
//                while (true) {
//                    if (!(ActivityCompat.checkSelfPermission(
//                            requireContext(),
//                            Manifest.permission.ACCESS_FINE_LOCATION
//                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                            requireContext(),
//                            Manifest.permission.ACCESS_COARSE_LOCATION
//                        ) != PackageManager.PERMISSION_GRANTED
//                                )
//                    ) {
//                        break
//                    }
//                }
//            }

//            googleMap.isMyLocationEnabled = true

            val locationManager =
                requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
//            var location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val startLatLng: LatLng = LatLng(-34.0, 151.0)

            // center map on current location
//            startLatLng = if (location != null) {
//                LatLng(location.latitude, location.longitude)
//            } else {
//                LatLng(-34.0, 151.0)
//            }

            mMap.moveCamera(CameraUpdateFactory.newLatLng(startLatLng))

            // initialize variables
            var marker: Marker? = null
            var citySet: HashSet<String>
            var countySet: HashSet<String>
            var stateSet: HashSet<String>
            var countrySet: HashSet<String>
            var locationSet: HashSet<String>

            // handle map clicks
            mMap.setOnMapClickListener {
                marker?.remove()
                marker = mMap.addMarker(MarkerOptions().position(it))

                citySet = HashSet()
                countySet = HashSet()
                stateSet = HashSet()
                countrySet = HashSet()

                try {
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    val locations = geocoder.getFromLocation(it.latitude, it.longitude, 1)

                    if (locations != null && locations.size > 0) {
                        val city = locations[0].locality
                        val county = locations[0].subAdminArea
                        val state = locations[0].adminArea
                        val country = locations[0].countryName

                        if (city != null) {
                            citySet.add(city)
                        }
                        if (county != null) {
                            countySet.add(county)
                        }
                        if (state != null) {
                            stateSet.add(state)
                        }
                        if (country != null) {
                            countrySet.add(country)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                // request nearby cities
                val queue = Volley.newRequestQueue(requireContext())
                var url = "http://gd.geobytes.com/GetNearbyCities?"
                val radius = 20

                url += "radius=$radius&Latitude=${it.latitude}&Longitude=${it.longitude}"

                val stringRequest = StringRequest(Request.Method.GET, url,
                    { response ->
                        val responseArray = JSONArray(response)

                        // add returned cities to citySet
                        for (i in 0 until responseArray.length()) {
                            if (responseArray.getJSONArray(i).length() >= 1) {
                                if (responseArray.getJSONArray(i).getString(1) != null) {
                                    citySet.add(responseArray.getJSONArray(i).getString(1))
                                }
                            }
                        }

                        // define headers and make request
                        locationSet = (citySet + countySet) as HashSet<String>
                        val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val queryType = "relevance"
                        makeQuery(ArrayList<String>(locationSet), date, queryType)

                        // switch to NewsFragment
                        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
                    },
                    { error ->
                        Log.d("ERROR", error.toString())
                    })

                stringRequest.tag = "requestTag"
                queue.add(stringRequest)
            }
        })
    }

    // creates a list of news objects and passes it to the NewsViewModel
    private fun createNewsObject(
        titleStore: ArrayList<String>,
        linkStore: ArrayList<String>,
        imageLinkStore: ArrayList<String>,
        publisherStore: ArrayList<String>,
        dateStore: ArrayList<String>
    ) {
        val newsArray: Array<NewsObject?> = Array<NewsObject?>(titleStore.size) { _ -> null}
        for (i in 0 until newsArray.size) {
            newsArray[i] = NewsObject(
                titleStore[i],
                publisherStore[i],
                dateStore[i],
                linkStore[i],
                imageLinkStore[i]
            )
        }

        viewModel.setNews(newsArray)
        viewModel.changeTextVal("poggers")
    }

    // makes the query with the news API
    private fun makeQuery(locations: ArrayList<String>, date: String, queryType: String) {
        val queue = Volley.newRequestQueue(activity)

        // generates query string
        var queries: String = ""
        for (location in locations) {
            if (location != locations[locations.size - 1]) {
                queries += "\"${location}\" OR "
            }else {
                queries += "\"${location}\""
            }
        }

        // TODO: replace api key with your own
        var url = "http://newsapi.org/v2/everything?qInTitle=${queries}&from=${date}&sortBy=${queryType}&apiKey=84f2017538e54767a2557129ec44f823"
        val titleStore = ArrayList<String>() // displays title of article
        val linkStore = ArrayList<String>() // will be used to direct the user to the browser when they click on news cardview
        val imageLinkStore = ArrayList<String>() // will be used to display image in teh cardview
        val publisherStore = ArrayList<String>() // displays publisher of article
        val dateStore = ArrayList<String>() // displays date published

        Log.d("request", url)

        // creates an anonymous object of JsonObjectRequest, and overrides it getHeader method
        val stringRequest:JsonObjectRequest = object: JsonObjectRequest(Request.Method.GET,
            url,
            null,
            { response ->
                var responseObject: org.json.JSONArray = response.getJSONArray("articles")

                // stores response in a series of array lists, to be passed to the NewsViewModel
                if (response.getString("status") == "error") {
                    Log.d("request", response.getString("message"))
                } else {
                    for (i in 0 until responseObject.length()) {
                        val articleInformation: org.json.JSONObject =
                            responseObject.getJSONObject(i)
                        titleStore.add(articleInformation.getString("title"))
                        publisherStore.add(
                            articleInformation.getJSONObject("source").getString("name")
                        )
                        imageLinkStore.add(articleInformation.getString("urlToImage"))
                        linkStore.add(articleInformation.getString("url"))
                        dateStore.add(articleInformation.getString("publishedAt"))
                    }
                    for (i in titleStore) {
                        Log.d("request", i)
                    }
                    createNewsObject(
                        titleStore,
                        linkStore,
                        imageLinkStore,
                        publisherStore,
                        dateStore
                    )
                }
            },
            { error ->
                Log.d("ERROR", error.toString())
            }
        ) {
            // need to add addition things to the header, or request will be met with 403 error for an known reason
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["User-Agent"] = "Mozilla/5.0"
                return headers
            }
        }

        queue.add(stringRequest)
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100){
            Toast.makeText(requireContext(), "Location permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Log.e("PERResult", "Denied")
            Toast.makeText(requireContext(), "PERMISSION_DENIED", Toast.LENGTH_SHORT).show()
        }
    }
}