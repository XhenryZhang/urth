package edu.ucsb.cs.cs184.urth

import android.util.Log
import com.google.firebase.database.DatabaseReference

object FirebaseUtil {
    private val TAG = FirebaseUtil::class.simpleName

    fun setFirebasePrefs(ref: DatabaseReference, prefs: UserPreferences) {
        ref.setValue(prefs)
            .addOnSuccessListener {
                Log.d(TAG, "Saved local preferences to Firebase")
            }
            .addOnFailureListener {
                Log.e(TAG, "Error saving preferences to Firebase: ${it.message}")
            }
    }

    fun addBookmark(ref: DatabaseReference, locationString: String, location: Location) {
        val newRef = ref.child(locationString)
        newRef.setValue(location)
            .addOnSuccessListener {
                Log.d(TAG, "Saved bookmark to Firebase")
            }
            .addOnFailureListener {
                Log.e(TAG, "Error saving bookmark to Firebase: ${it.message}")
            }
    }

    fun removeBookmark(ref: DatabaseReference, locationString: String) {
        val bmRef = ref.child(locationString)
        bmRef.removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "Removed bookmark from Firebase")
            }
            .addOnFailureListener {
                Log.e(TAG, "Error removing bookmark from Firebase: ${it.message}")
            }
    }
}