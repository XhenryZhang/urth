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
import android.widget.TextView
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

    companion object {
        private val TAG = SearchFragment::class.simpleName
    }

    // view models
    private lateinit var viewModelNews: NewsViewModel
    private lateinit var viewModelSearch: SearchViewModel
    private lateinit var bottomDrawerFragment: BottomDrawerFragment

    // animation for floating action buttons
    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(this.context, R.anim.rotate_button_1)
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(this.context, R.anim.unrotate_button_1)
    }
    private val fromTop1: Animation by lazy {
        AnimationUtils.loadAnimation(this.context, R.anim.from_top_level_1)
    }
    private val toTop1: Animation by lazy {
        AnimationUtils.loadAnimation(this.context, R.anim.to_top_level_1)
    }
    private val fromTop2: Animation by lazy {
        AnimationUtils.loadAnimation(this.context, R.anim.from_top_level_2)
    }
    private val toTop2: Animation by lazy {
        AnimationUtils.loadAnimation(this.context, R.anim.to_top_level_2)
    }
    private val fromTop3: Animation by lazy {
        AnimationUtils.loadAnimation(this.context, R.anim.from_top_level_3)
    }
    private val toTop3: Animation by lazy {
        AnimationUtils.loadAnimation(this.context, R.anim.to_top_level_3)
    }

    // floating action buttons
    private lateinit var buttonOpen: FloatingActionButton
    private lateinit var buttonGeneral: FloatingActionButton
    private lateinit var buttonEntertainment: FloatingActionButton
    private lateinit var buttonTechnology: FloatingActionButton
    private lateinit var buttonBookmarks: FloatingActionButton

    // UI FAB text
    private lateinit var textGeneral: TextView
    private lateinit var textEntertainment: TextView
    private lateinit var textTechnology: TextView

    private var clicked = false // flag for whether the FAB drawer is toggled on or off

    // map objects
    lateinit var mMapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var startLatLng: LatLng
    private lateinit var markers: ArrayList<Marker>
    private var marker: Marker? = null

    // arguments passed to the API query
    private lateinit var location: HashSet<String>
    private lateinit var date: String
    private lateinit var searchType: String
    private lateinit var bmLocation: HashSet<String>

    // preferences
    private val sp: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }
    private val userPrefs: UserPreferences by lazy { sp.fetchLocalPreferences() }

    // click location
    private var city: String? = ""
    private var state: String? = ""
    private var country: String? = ""
    private lateinit var clickLatLng: LatLng

    var locationSet: HashSet<String> = HashSet()

    // only gets called once the view is created
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // sets animation end behavior -- whenever one animation ends, clear all the other animations on the screen
        // this is to prevent animations from becoming visible upon the view losing focus when the bottom drawer is opened
        // or when the appbar menu is selected
        toTop1.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                clearAnim()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationStart(animation: Animation?) {}
        })

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // bottom drawer fragment
        bottomDrawerFragment = BottomDrawerFragment()

        viewModelNews =
            ViewModelProvider(activity as ViewModelStoreOwner).get(NewsViewModel::class.java)
        viewModelSearch =
            ViewModelProvider(activity as ViewModelStoreOwner).get(SearchViewModel::class.java)

        // initialize map
        mMapView = view.findViewById(R.id.mapView) as MapView
        mMapView.onCreate(savedInstanceState)
        mMapView.onResume()
        try {
            MapsInitializer.initialize(activity?.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        viewModelSearch.mapLocation.observe(viewLifecycleOwner, {
            startLatLng = it
        })

        // implement button functionality
        buttonSetup(view)

        // implement map functionality
        mMapView.getMapAsync { mMap ->
            googleMap = mMap

            mMap.moveCamera(CameraUpdateFactory.newLatLng(startLatLng))

            // handle map clicks -- saves location of cities and opens drawer
            mMap.setOnMapClickListener {
                marker?.remove()
                marker = mMap.addMarker(MarkerOptions().position(it))

                city = ""
                state = ""
                country = ""

                viewModelNews.setNews(Array(0) { null })
                clickLatLng = LatLng(it.latitude, it.longitude)
                getNearbyLocations(it.latitude, it.longitude)

                bottomDrawerFragment.show(childFragmentManager, BottomDrawerFragment.NEW_MARKER)
            }

            mMap.setOnMarkerClickListener {
                getNearbyLocations(it.position.latitude, it.position.longitude)

                // option to remove or add the bookmark
                val fragmentTag = if (it.tag != null) {
                    BottomDrawerFragment.BOOKMARK
                } else {
                    BottomDrawerFragment.NEW_MARKER
                }
                bottomDrawerFragment.show(childFragmentManager, fragmentTag)
                true
            }

            markers = ArrayList()

            // refresh the bookmark array once database is updated
            viewModelSearch.bmLocation.observe(viewLifecycleOwner, { locations ->
                for (oldMarker in markers) {
                    oldMarker.remove()
                }
                markers.clear()
                for (location in locations) {
                    val latlng = LatLng(location.latitude, location.longitude)
                    val newMarker = mMap.addMarker(
                        MarkerOptions()
                            .position(latlng)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    )
                    newMarker.tag = BottomDrawerFragment.BOOKMARK
                    markers.add(newMarker)
                }
            })
        }
    }

    private fun getNearbyLocations(
        lat: Double,
        lon: Double,
        cb: ((HashSet<String>, Boolean) -> Unit)? = null,
        flag: Boolean = false
    ) {
        val locSet = HashSet<String>()
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val locations = geocoder.getFromLocation(lat, lon, 1)

            if (locations != null && locations.size > 0) {
                city = locations[0].locality
                val county = locations[0].subAdminArea
                state = locations[0].adminArea
                country = locations[0].countryName

                if (city != null) {
                    locSet.add(city!!)
                }
                if (county != null) {
                    locSet.add(county)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // request nearby cities
        val queue = Volley.newRequestQueue(requireContext())
        var url = "http://gd.geobytes.com/GetNearbyCities?"
        val radius = userPrefs.searchRadius.km

        url += "radius=$radius&Latitude=${lat}&Longitude=${lon}"

        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                val responseArray = JSONArray(response)

                // add returned cities to citySet
                for (i in 0 until responseArray.length()) {
                    if (responseArray.getJSONArray(i).length() >= 1) {
                        if (responseArray.getJSONArray(i).getString(1) != null) {
                            locSet.add(responseArray.getJSONArray(i).getString(1))
                        }
                    }
                }

                // define headers and make request
                date = getDateFromPreferences()
                searchType = userPrefs.defaultSort.sortMethod
                if (cb != null) {
                    cb(locSet, flag)
                } else {
                    locationSet = locSet
                    location = locationSet
                }
            },
            { error ->
                Log.d(TAG, "Error: ${error.message}")
            })

        stringRequest.tag = "requestTag"
        queue.add(stringRequest)
    }

    override fun onStop() {
        super.onStop()
        // when the view changes, remember the current camera location
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
        buttonOpen = view.findViewById(R.id.news_open_button)
        buttonTechnology = view.findViewById(R.id.technology_news_button)
        buttonEntertainment = view.findViewById(R.id.entertainment_news_button)
        buttonGeneral = view.findViewById(R.id.general_news_button)
        buttonBookmarks = view.findViewById(R.id.bookmark_news_button)

        textGeneral = view.findViewById(R.id.text_general)
        textTechnology = view.findViewById(R.id.text_technology)
        textEntertainment = view.findViewById(R.id.text_entertainment)

        // opens the drawer containing 3 floating action buttons for displaying
        // news from different categories
        buttonOpen.setOnClickListener {
            setAnimation()
            setVisibility()
            setClickable()
            clicked = !clicked
        }

        buttonGeneral.setOnClickListener {
            performTop10Query(0)
        }
        buttonTechnology.setOnClickListener {
            performTop10Query(1)
        }
        buttonEntertainment.setOnClickListener {
            performTop10Query(2)
        }
        buttonBookmarks.setOnClickListener {
            getBookmarkArticles()
        }
    }

    // toggle FAB visibility
    private fun setVisibility() {
        if (!clicked) {
            buttonTechnology.visibility = View.VISIBLE
            buttonEntertainment.visibility = View.VISIBLE
            buttonGeneral.visibility = View.VISIBLE
            textTechnology.visibility = View.VISIBLE
            textEntertainment.visibility = View.VISIBLE
            textGeneral.visibility = View.VISIBLE
        } else {
            buttonTechnology.visibility = View.GONE
            buttonEntertainment.visibility = View.GONE
            buttonGeneral.visibility = View.GONE
            textTechnology.visibility = View.GONE
            textEntertainment.visibility = View.GONE
            textGeneral.visibility = View.GONE
        }
    }

    // open and close animation
    private fun setAnimation() {
        if (!clicked) {
            buttonOpen.startAnimation(rotateOpen)
            buttonTechnology.startAnimation(fromTop3)
            buttonGeneral.startAnimation(fromTop1)
            buttonEntertainment.startAnimation(fromTop2)
            textTechnology.startAnimation(fromTop3)
            textEntertainment.startAnimation(fromTop2)
            textGeneral.startAnimation(fromTop1)
        } else {
            buttonOpen.startAnimation(rotateClose)
            buttonTechnology.startAnimation(toTop3)
            buttonGeneral.startAnimation(toTop1)
            buttonEntertainment.startAnimation(toTop2)
            textTechnology.startAnimation(toTop3)
            textEntertainment.startAnimation(toTop2)
            textGeneral.startAnimation(toTop1)
        }
    }

    // buttons can only be clicked when they're visible
    private fun setClickable() {
        if (!clicked) {
            buttonTechnology.isClickable = true
            buttonGeneral.isClickable = true
            buttonEntertainment.isClickable = true
        } else {
            buttonTechnology.isClickable = false
            buttonGeneral.isClickable = false
            buttonEntertainment.isClickable = false
        }
    }

    // clears animations associated with each button
    private fun clearAnim() {
        buttonTechnology.clearAnimation()
        buttonGeneral.clearAnimation()
        buttonEntertainment.clearAnimation()
        textTechnology.clearAnimation()
        textEntertainment.clearAnimation()
        textGeneral.clearAnimation()
    }

    // creates a list of news objects and passes it to the NewsViewModel
    private fun createNewsObject(
        titleStore: ArrayList<String>,
        linkStore: ArrayList<String>,
        imageLinkStore: ArrayList<String>,
        publisherStore: ArrayList<String>,
        dateStore: ArrayList<String>
    ) {
        val newsArray: Array<NewsObject?> = Array(titleStore.size) { _ -> null }
        for (i in newsArray.indices) {
            newsArray[i] = NewsObject(
                titleStore[i],
                publisherStore[i],
                dateStore[i],
                linkStore[i],
                imageLinkStore[i]
            )
        }

        viewModelNews.setNews(newsArray)
    }

    // makes the query with the news API
    private fun makeQuery(
        locations: ArrayList<String>,
        date: String,
        queryType: String,
        top10Category: String
    ) {
        val queue = Volley.newRequestQueue(activity) // HTTP request queue
        val url: String // stores request url

        if (top10Category == "") {
            // generates query string
            var queries = ""
            for (location in locations) {
                queries += if (location != locations[locations.size - 1]) {
                    "\"${location}\" OR "
                } else {
                    "\"${location}\""
                }
            }
            val q = if (userPrefs.searchArticleBody) "q" else "qInTitle"
            val pageSize = userPrefs.maxArticles.pageSize
            url =
                "http://newsapi.org/v2/everything?${q}=${queries}&from=${date}&sortBy=${queryType}&language=en&pageSize=${pageSize}&apiKey=fd89a7c8fd914e2fb8144c15790cacbb"
        } else {
            url =
                "http://newsapi.org/v2/top-headlines?country=us&category=${top10Category}&apiKey=c1196b87101143c49414efbeaa14ab2b"
        }

        val titleStore = ArrayList<String>() // displays title of article
        val linkStore =
            ArrayList<String>() // will be used to direct the user to the browser when they click on news cardview
        val imageLinkStore = ArrayList<String>() // will be used to display image in the cardview
        val publisherStore = ArrayList<String>() // displays publisher of article
        val dateStore = ArrayList<String>() // displays date published

        Log.d(TAG, "Query URL: $url")

        // creates an anonymous object of JsonObjectRequest, and overrides its getHeader method
        val stringRequest: JsonObjectRequest = object : JsonObjectRequest(Method.GET,
            url,
            null,
            { response ->
                val responseObject: JSONArray = response.getJSONArray("articles")

                // stores response in a series of array lists, to be passed to the NewsViewModel
                if (response.getString("status") == "error") {
                    Log.e(TAG, "Error in response: ${response.getString("message")}")
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
                Log.d(TAG, "JSON error: ${error.message}")
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
        when (category) {
            0 -> makeQuery(object : ArrayList<String>(0) {}, "", "", "general")
            1 -> makeQuery(object : ArrayList<String>(0) {}, "", "", "technology")
            else -> makeQuery(object : ArrayList<String>(0) {}, "", "", "entertainment")
        }

        // switch to news fragment
        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
    }

    // gets news articles for bookmarked locations
    private fun getBookmarkArticles() {
        if (this::bmLocation.isInitialized) {
            bmLocation.clear()
        } else {
            bmLocation = HashSet()
        }

        val bmLoc = viewModelSearch.bmLocation.value!!
        if (bmLoc.isEmpty()) {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
        for (loc in bmLoc) {
            val flag = bmLoc.isNotEmpty() && loc == bmLoc.last()
            getNearbyLocations(loc.latitude, loc.longitude, ::addNearbyLocations, flag)
        }
    }

    // add locations that are near a bookmarked location
    private fun addNearbyLocations(nearbyLocations: HashSet<String>, shouldTransition: Boolean) {
        bmLocation.addAll(nearbyLocations)
        if (shouldTransition) {
            val bmLocationList = ArrayList(bmLocation)
            if (bmLocationList.isEmpty()) {
                Toast.makeText(context, "No cities bookmarked!", Toast.LENGTH_SHORT).show()
            } else {
                makeQuery(bmLocationList, date, searchType, "")
                findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            }
        }
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

        if (requestCode == 100) {
            Toast.makeText(
                requireContext(),
                "Location permissions granted",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Log.e(TAG, "Location permissions denied")
            Toast.makeText(
                requireContext(),
                "Error: Location permissions denied",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // implements the method inherited from the interface from BottomNavigationDrawer
    override fun performNewsQuery() {
        makeQuery(ArrayList(location), date, searchType, "")

        // switch to NewsFragment
        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
    }

    // implements method for getting the current city, state, and country from BottomNavigationDrawer
    override fun getLocation(): ArrayList<String> {
        val locationsArray = ArrayList<String>()
        if (!city.isNullOrBlank()) locationsArray.add(city!!)
        if (!state.isNullOrBlank()) locationsArray.add(state!!)
        if (!country.isNullOrBlank()) locationsArray.add(country!!)

        return locationsArray
    }

    // implements method for getting latitude and longitude of click from BottomNavigationDrawer
    override fun getLatLng(): LatLng {
        return clickLatLng
    }

    fun removeNewMarker() = marker?.remove()
}
