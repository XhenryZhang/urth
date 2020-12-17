package edu.ucsb.cs.cs184.urth

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
class SearchFragment : Fragment(), BottomDrawerFragment.NavigationListener {
    // view models
    private lateinit var viewModelNews: NewsViewModel
    private lateinit var viewModelSearch: SearchViewModel

    // animation for floating action buttons
    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(this.context, R.anim.rotate_button_1) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(this.context, R.anim.unrotate_button_1) }
    private val fromTop1: Animation by lazy { AnimationUtils.loadAnimation(this.context, R.anim.from_top_level_1) }
    private val toTop1: Animation by lazy { AnimationUtils.loadAnimation(this.context, R.anim.to_top_level_1) }

    private val fromTop2: Animation by lazy { AnimationUtils.loadAnimation(this.context, R.anim.from_top_level_2) }
    private val toTop2: Animation by lazy { AnimationUtils.loadAnimation(this.context, R.anim.to_top_level_2) }
    private val fromTop3: Animation by lazy { AnimationUtils.loadAnimation(this.context, R.anim.from_top_level_3) }
    private val toTop3: Animation by lazy { AnimationUtils.loadAnimation(this.context, R.anim.to_top_level_3) }

    // floating action buttons
    private lateinit var button_open: FloatingActionButton
    private lateinit var button_general: FloatingActionButton
    private lateinit var button_entertainment: FloatingActionButton
    private lateinit var button_technology: FloatingActionButton

    private var clicked = false // flag for whether the FAB drawer is toggled on or off

    // map objects
    lateinit var mMapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var startLatLng: LatLng
    private lateinit var markers: ArrayList<Marker>

    // arguments passed to the API query
    private lateinit var location: HashSet<String>
    private lateinit var date: String
    private lateinit var searchType: String

    // preferences
    private val sp: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(context) }
    private val userPrefs: UserPreferences by lazy { sp.fetchLocalPreferences() }

    // click location
    private var city: String? = ""
    private var state: String? = ""
    private var country: String? = ""
    private lateinit var clickLatLng: LatLng

    // only gets called once the view is created
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // sets animation end behavior
        toTop1.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                clearAnim()
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationStart(animation: Animation?) {
            }
        })

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    // location information
    // genre of news -- currently defaults to 2 days ago
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // bottom drawer fragment
        val bottomDrawerFragment = BottomDrawerFragment()

        viewModelNews = ViewModelProvider(activity as ViewModelStoreOwner).get(NewsViewModel::class.java)
        viewModelSearch = ViewModelProvider(activity as ViewModelStoreOwner).get(SearchViewModel::class.java)

        // initialize map
        mMapView = view.findViewById(R.id.mapView) as MapView
        mMapView.onCreate(savedInstanceState)
        mMapView.onResume()
        try {
            MapsInitializer.initialize(activity?.applicationContext)
        } catch (e: Exception){
            e.printStackTrace()
        }

        viewModelSearch.mapLocation.observe(viewLifecycleOwner, {
            startLatLng = it
        })

        // implement button functionality
        buttonSetup(view)

        // implement map functionality
        mMapView.getMapAsync(OnMapReadyCallback { mMap ->
            googleMap = mMap

            mMap.moveCamera(CameraUpdateFactory.newLatLng(startLatLng))

            // initialize variables
            var marker: Marker? = null
            var citySet: HashSet<String>
            var countySet: HashSet<String>
            var stateSet: HashSet<String>
            var countrySet: HashSet<String>
            var locationSet: HashSet<String>

            // handle map clicks -- saves location of cities and opens drawer
            mMap.setOnMapClickListener {
                marker?.remove()
                marker = mMap.addMarker(MarkerOptions().position(it))

                city = ""
                state = ""
                country = ""

                citySet = HashSet()
                countySet = HashSet()
                stateSet = HashSet()
                countrySet = HashSet()

                viewModelNews.setNews(Array<NewsObject?>(0){_ -> null})

                clickLatLng = LatLng(it.latitude, it.longitude)

                try {
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    val locations = geocoder.getFromLocation(it.latitude, it.longitude, 1)

                    if (locations != null && locations.size > 0) {
                        city = locations[0].locality
                        val county = locations[0].subAdminArea
                        state = locations[0].adminArea
                        country = locations[0].countryName

                        if (city != null) {
                            citySet.add(city!!)
                        }
                        if (county != null) {
                            countySet.add(county)
                        }
                        if (state != null) {
                            stateSet.add(state!!)
                        }
                        if (country != null) {
                            countrySet.add(country!!)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                // request nearby cities
                val queue = Volley.newRequestQueue(requireContext())
                var url = "http://gd.geobytes.com/GetNearbyCities?"
                val radius = userPrefs.searchRadius.km

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
                        location = locationSet
                        date = getDateFromPreferences()
                        searchType = userPrefs.defaultSort.sortMethod

                        bottomDrawerFragment.show(childFragmentManager, BottomDrawerFragment.NEW_MARKER)
                    },
                    { error ->
                        Log.d("ERROR", error.toString())
                    })

                stringRequest.tag = "requestTag"
                queue.add(stringRequest)
            }

            mMap.setOnMarkerClickListener {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val locations = geocoder.getFromLocation(it.position.latitude, it.position.longitude, 1)
                if (locations != null && locations.size > 0) {
                    city = locations[0].locality
                    state = locations[0].adminArea
                    country = locations[0].countryName
                }

                val fragmentTag = if (it.tag != null) BottomDrawerFragment.BOOKMARK else BottomDrawerFragment.NEW_MARKER
                bottomDrawerFragment.show(childFragmentManager, fragmentTag)
                true
            }

            markers = ArrayList()
            viewModelSearch.bmLocation.observe(viewLifecycleOwner, { locations ->
                for (oldMarker in markers) {
                    oldMarker.remove()
                }
                markers.clear()
                for (location in locations) {
                    val latlng = LatLng(location.latitude, location.longitude)
                    val newMarker = mMap.addMarker(MarkerOptions()
                        .position(latlng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
                    newMarker.tag = BottomDrawerFragment.BOOKMARK
                    markers.add(newMarker)
                }
            })
        })
    }

    override fun onStop() {
        super.onStop()
        viewModelSearch.setLocation(googleMap.cameraPosition.target)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDateFromPreferences(): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        when (userPrefs.recencyFilter) {
            RecencyFilter.PAST_DAY, RecencyFilter.PAST_WEEK ->
                cal.add(Calendar.DATE, -userPrefs.recencyFilter.duration)
            RecencyFilter.PAST_MONTH -> cal.add(Calendar.MONTH, -userPrefs.recencyFilter.duration)
        }
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(cal.time)
    }

    // setup code for the floating action buttons accessing today's top articles
    private fun buttonSetup(view: View) {
        clicked = false
        button_open = view.findViewById(R.id.news_open_button)
        button_technology = view.findViewById(R.id.technology_news_button)
        button_entertainment = view.findViewById(R.id.entertainment_news_button)
        button_general = view.findViewById(R.id.general_news_button)

        // opens the drawer containing 3 floating action buttons for displaying
        // news from different categories
        button_open.setOnClickListener {
            setAnimation()
            setVisibility()
            setClickable()
            clicked = !clicked
        }

        button_general.setOnClickListener {
            performTop10Query(0)
        }
        button_technology.setOnClickListener {
            performTop10Query(1)
        }
        button_entertainment.setOnClickListener {
            performTop10Query(2)
        }
    }

    // toggle FAB visibility
    private fun setVisibility() {
        if (!clicked) {
            button_technology.visibility = View.VISIBLE
            button_entertainment.visibility = View.VISIBLE
            button_general.visibility = View.VISIBLE
        }else {
            button_technology.visibility = View.GONE
            button_entertainment.visibility = View.GONE
            button_general.visibility = View.GONE
        }
    }

    // open and close animation
    private fun setAnimation() {
        if (!clicked) {
            button_open.startAnimation(rotateOpen)
            button_technology.startAnimation(fromTop3)
            button_general.startAnimation(fromTop1)
            button_entertainment.startAnimation(fromTop2)
        } else {
            button_open.startAnimation(rotateClose)
            button_technology.startAnimation(toTop3)
            button_general.startAnimation(toTop1)
            button_entertainment.startAnimation(toTop2)
        }
    }

    // buttons can only be clicked when they're visible
    private fun setClickable() {
        if (!clicked) {
            button_technology.isClickable = true
            button_general.isClickable = true
            button_entertainment.isClickable = true
        } else {
            button_technology.isClickable = false
            button_general.isClickable = false
            button_entertainment.isClickable = false
        }
    }

    // clears animations associated with each button
    private fun clearAnim() {
        button_technology.clearAnimation()
        button_general.clearAnimation()
        button_entertainment.clearAnimation()
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

        viewModelNews.setNews(newsArray)
        // viewModelNews.changeTextVal("poggers")
    }

    // makes the query with the news API
    private fun makeQuery(locations: ArrayList<String>, date: String, queryType: String, top10Category: String) {
        val queue = Volley.newRequestQueue(activity) // HTTP request queue
        var url: String = "placeholder" // stores request url

        if (top10Category == "") {
            // generates query string
            var queries: String = ""
            for (location in locations) {
                if (location != locations[locations.size - 1]) {
                    queries += "\"${location}\" OR "
                }else {
                    queries += "\"${location}\""
                }
            }
            val q = if (userPrefs.searchArticleBody) "q" else "qInTitle"
            val pageSize = userPrefs.maxArticles.pageSize
            url = "http://newsapi.org/v2/everything?${q}=${queries}&from=${date}&sortBy=${queryType}&language=en&pageSize=${pageSize}&apiKey=74d85486ba4647208725db551df58782"
        }else  {
            url = "http://newsapi.org/v2/top-headlines?country=us&category=${top10Category}&apiKey=c1196b87101143c49414efbeaa14ab2b"
        }

        val titleStore = ArrayList<String>() // displays title of article
        val linkStore = ArrayList<String>() // will be used to direct the user to the browser when they click on news cardview
        val imageLinkStore = ArrayList<String>() // will be used to display image in teh cardview
        val publisherStore = ArrayList<String>() // displays publisher of article
        val dateStore = ArrayList<String>() // displays date published

        Log.d("request", url)

        // creates an anonymous object of JsonObjectRequest, and overrides its getHeader method
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
            // need to add additional things to the header, or request will be met with 403 error for an known reason
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["User-Agent"] = "Mozilla/5.0"
                return headers
            }
        }

        queue.add(stringRequest)
    }

    // gets the top headlines in the US, for user given category
    private fun performTop10Query(category: Int) {
        // general news
        if (category == 0) {
            // general
            makeQuery(object: ArrayList<String>(0) {}, "", "", "general")
        }else if (category == 1) {
            // tech
            makeQuery(object: ArrayList<String>(0) {}, "", "", "technology")
        }else {
            // entertainment
            makeQuery(object: ArrayList<String>(0) {}, "", "", "entertainment")
        }

        // switch to news fragment
        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
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

    // implements the method inherited from the interface from BottomNavigationDrawer
    override fun performNewsQuery() {
        makeQuery(ArrayList<String>(location), date, searchType, "")

        // switch to NewsFragment
        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
    }

    // implements method for getting the current city, state, and country from BottomNavigationDrawer
    override fun getLocation(): ArrayList<String> {
        var locationsArray = ArrayList<String>()
        if (!city.isNullOrBlank()) locationsArray.add(city!!)
        if (!state.isNullOrBlank()) locationsArray.add(state!!)
        if (!country.isNullOrBlank()) locationsArray.add(country!!)

        return locationsArray
    }

    // implements method for getting latitude and longitude of click from BottomNavigationDrawer
    override fun getLatLng(): LatLng {
        return clickLatLng
    }
}
