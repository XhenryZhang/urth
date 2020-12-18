package edu.ucsb.cs.cs184.urth

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import edu.ucsb.cs.cs184.urth.FirebaseUtil.setFirebasePrefs
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

class SettingsActivity : AppCompatActivity() {

    companion object {
        private val TAG = SettingsActivity::class.simpleName
    }

    private val sp: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    private val userPrefs: UserPreferences by lazy { sp.fetchLocalPreferences() }
    private var settingsChanged = false

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
        listenForPreferenceChanges()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (settingsChanged) {
            val uid = FirebaseAuth.getInstance().uid
            val prefRef = FirebaseDatabase.getInstance().getReference("/users/$uid/preferences")
            setFirebasePrefs(prefRef, userPrefs)
        }
    }

    private fun listenForPreferenceChanges() {
        sp.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            val prop = UserPreferences::class.memberProperties.find { it.name == key }
            if (prop != null && prop is KMutableProperty<*>) {
                val newValue = sharedPreferences.getValue(key)
                if (newValue != null) {
                    settingsChanged = true
                    prop.setter.call(userPrefs, newValue)
                    Log.d(TAG, "Updated $key preference to $newValue")
                    Log.d(TAG, "New user preferences: $userPrefs")
                }
            } else {
                Log.w(TAG, "Invalid preference key: $key")
            }
        }
    }
}
