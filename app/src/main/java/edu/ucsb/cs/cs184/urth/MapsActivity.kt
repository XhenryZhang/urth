package edu.ucsb.cs.cs184.urth

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.preference.PreferenceManager
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import edu.ucsb.cs.cs184.urth.FirebaseUtil.setFirebasePrefs
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

class MapsActivity : AppCompatActivity() {

    companion object {
        private val TAG = MapsActivity::class.simpleName
    }

    private lateinit var sp: SharedPreferences
    private lateinit var uid: String
    private lateinit var ref: DatabaseReference
    private lateinit var userPrefs: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        openOptionsMenu()

        sp = PreferenceManager.getDefaultSharedPreferences(this)
        uid = FirebaseAuth.getInstance().uid!!
        Log.d("debugging", uid)
        ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        if (intent.getBooleanExtra(AuthActivity.EXTRA_FETCH_PERMISSIONS, false)) {
            // Try to retrieve user preferences from Firebase
            Log.d(TAG, "Fetching user preferences for user $uid from database...")
            getFirebasePrefs(ref)
        } else {
            // Retrieve user preferences from the local preference manager
            userPrefs = sp.fetchLocalPreferences()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                return true
            }
            R.id.action_logout -> {
                val intent = Intent(this, AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                AuthUI.getInstance().delete(this)
                    .addOnSuccessListener {
                        startActivity(intent)
                        Log.d(TAG, "Logged out successfully.")
                    }
                    .addOnFailureListener {
                        intent.putExtra(AuthActivity.EXTRA_LOGOUT_ERROR, true)
                        startActivity(intent)
                        Log.d(TAG, "Error logging out: ${it.message}")
                    }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getFirebasePrefs(ref: DatabaseReference) {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dbPrefs = snapshot.getValue(UserPreferences::class.java)
                if (dbPrefs == null) {
                    // Firebase preferences not found
                    Log.d(TAG, "No existing preferences on database. Adding current preferences...")
                    userPrefs = sp.fetchLocalPreferences()
                    setFirebasePrefs(ref, userPrefs)
                } else {
                    // Update local preferences
                    Log.d(TAG, "Updating local preferences...")
                    userPrefs = dbPrefs
                    sp.edit().apply {
                        UserPreferences::class.memberProperties
                            .filterIsInstance<KMutableProperty<*>>()
                            .forEach { prop -> putValue(prop.name, prop.getter.call(userPrefs)) }
                    }.apply()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
