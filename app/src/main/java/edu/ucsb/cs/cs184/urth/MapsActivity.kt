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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MapsActivity : AppCompatActivity() {

    companion object {
        private val TAG = MapsActivity::class.simpleName
    }

    private lateinit var sp: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference
    private lateinit var userPrefs: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        openOptionsMenu()

        sp = PreferenceManager.getDefaultSharedPreferences(this)
        auth = FirebaseAuth.getInstance()
        ref = FirebaseDatabase.getInstance().getReference("/users/${auth.uid}")
        if (intent.getBooleanExtra(AuthActivity.FETCH_PERMISSIONS, false)) {
            Log.d(TAG, "Fetching user preferences for user ${auth.uid} from database...")
            // TODO: try to get user preferences from Firebase database and update pref manager permissions
            // TODO: if DB preferences not found, get from pref manager and upload
        } else {
            // Otherwise, retrieve user preferences from the local preference manager
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
                        intent.putExtra(AuthActivity.LOGOUT_ERROR, true)
                        startActivity(intent)
                        Log.d(TAG, "Error logging out: ${it.message}")
                    }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
