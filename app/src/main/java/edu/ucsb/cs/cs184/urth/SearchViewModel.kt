package edu.ucsb.cs.cs184.urth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class SearchViewModel : ViewModel() {
    // where the view of the map is centered
    private val _mapLocation = MutableLiveData<LatLng>().apply {
        value = LatLng(34.4208, -119.6982)
    }

    private val _bmLocations = MutableLiveData<HashSet<Location>>()
    val bmLocation = _bmLocations

    fun setLocation(newLoc: LatLng) {
        _mapLocation.value = newLoc
    }

    val mapLocation: LiveData<LatLng> = _mapLocation

    init {
        _bmLocations.value = HashSet()
        val uid = FirebaseAuth.getInstance().uid!!
        val bmRef = FirebaseDatabase.getInstance().getReference("/users/$uid/bookmarks")

        // updates the MutableLiveData to mirror changes in location data in Firebase
        bmRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val location = snapshot.getValue(Location::class.java)
                _bmLocations.value?.add(location!!)
                _bmLocations.value = _bmLocations.value
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val location = snapshot.getValue(Location::class.java)
                _bmLocations.value?.remove(location)
                _bmLocations.value = _bmLocations.value
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val location = snapshot.getValue(Location::class.java)
                _bmLocations.value?.remove(location!!)
                _bmLocations.value?.add(location!!)
                _bmLocations.value = _bmLocations.value
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
