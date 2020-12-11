package edu.ucsb.cs.cs184.urth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.firebase.ui.auth.AuthUI

class MapsActivity : AppCompatActivity() {

    companion object {
        private val TAG = MapsActivity::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        openOptionsMenu()
        if (intent.getBooleanExtra(AuthActivity.FETCH_PERMISSIONS, false)) {
            Log.d(TAG, "Fetching user preferences from database...")
            // TODO: get user preferences from Firebase database
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
