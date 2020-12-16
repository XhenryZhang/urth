package edu.ucsb.cs.cs184.urth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class SearchViewModel: ViewModel() {
    private val _mapLocation = MutableLiveData<LatLng>(). apply {
        value = LatLng(34.4208, -119.6982)
    }

    fun setLocation(newLoc: LatLng) {
        _mapLocation.value = newLoc
    }

    val mapLocation: LiveData<LatLng> = _mapLocation
}