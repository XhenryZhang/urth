package edu.ucsb.cs.cs184.urth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class BottomDrawerFragment : BottomSheetDialogFragment() {
    var activityCallback: NavigationListener? = null
    var locationString: String = ""

    private val uid: String by lazy { FirebaseAuth.getInstance().uid!! }
    private val bmRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference("/users/$uid/bookmarks")
    }

    companion object {
        const val BOOKMARK = "BOOKMARK"
        const val NEW_MARKER = "NEW_MARKER"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_drawer, container, false)
    }

    interface NavigationListener {
        fun performNewsQuery()
        fun getLocation(): ArrayList<String>
        fun getLatLng(): LatLng
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // access interface methods defined in SearchFragment
        activityCallback = parentFragment as NavigationListener

        // button to perform news query
        val newsButton: Button = view.findViewById(R.id.drawer_news_button)

        newsButton.setOnClickListener {
            Toast.makeText(context, "Finding news articles...", Toast.LENGTH_SHORT).show()
            activityCallback?.performNewsQuery()
        }

        // set location as header
        val textView: TextView = view.findViewById(R.id.drawer_text)
        locationString = ""
        val locationList = activityCallback?.getLocation()
        if (locationList != null) {
            if (locationList.isNotEmpty()) {
                locationString = locationList[0]
            }
            var i = 1
            while (i <= locationList.lastIndex) {
                locationString += ", ${locationList[i++]}"
            }
        }
        textView.text = locationString

        // button for bookmarks
        val bookmarkButton: Button = view.findViewById(R.id.drawer_bookmark_button)
        val unbookmarkButton: Button = view.findViewById(R.id.drawer_unbookmark_button)

        if (tag == BOOKMARK) {
            unbookmarkButton.visibility = View.VISIBLE
        } else {
            bookmarkButton.visibility = View.VISIBLE
        }

        bookmarkButton.setOnClickListener {
            if (locationString.isNotBlank()) {
                while (locationList!!.size < 3) locationList.add(0, "")
                val city = if (locationList[0].isNotEmpty()) locationList[0] else null
                val state = if (locationList[1].isNotEmpty()) locationList[1] else null
                val country = locationList[2]
                val latlng = activityCallback!!.getLatLng()
                val latitude = latlng.latitude
                val longitude = latlng.longitude
                val location = Location(city, state, country, latitude, longitude)

                (parentFragment as SearchFragment).removeNewMarker()
                Toast.makeText(context, "Added to bookmarks!", Toast.LENGTH_SHORT).show()
                FirebaseUtil.addBookmark(bmRef, locationString, location)
                dismiss()
            } else {
                Toast.makeText(context, "Unable to add location", Toast.LENGTH_SHORT).show()
            }
        }

        unbookmarkButton.setOnClickListener {
            FirebaseUtil.removeBookmark(bmRef, locationString)
            dismiss()
        }
    }
}
