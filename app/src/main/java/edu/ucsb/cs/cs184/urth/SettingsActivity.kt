package edu.ucsb.cs.cs184.urth

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class SettingsActivity : AppCompatActivity() {

    companion object {
        private val TAG = SettingsActivity::class.simpleName
        private val STRING_KEYS = listOf("default_sort", "recency_filter", "search_radius")
        private val BOOLEAN_KEYS = listOf("expand_search")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener { sharedPreferences, s ->
                // TODO: save new preference value to Firebase
                when (s) {
                    in STRING_KEYS -> {
                        Log.d(TAG, "Updated $s to ${sharedPreferences.getString(s, "")}")
                        Toast.makeText(
                            this,
                            "Updated $s to ${sharedPreferences.getString(s, "")}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    in BOOLEAN_KEYS -> {
                        Log.d(TAG, "Updated $s to ${sharedPreferences.getBoolean(s, false)}")
                        Toast.makeText(
                            this,
                            "Updated $s to ${sharedPreferences.getBoolean(s, false)}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> Log.w(TAG, "Invalid preference key: $s")
                }
            }
    }

    // This can be moved into its own file later if we start adding a bunch of code to it
    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}