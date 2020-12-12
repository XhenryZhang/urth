package edu.ucsb.cs.cs184.urth

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import java.util.Locale
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

class SettingsActivity : AppCompatActivity() {

    companion object {
        private val TAG = SettingsActivity::class.simpleName
    }

    private val sp: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    private val userPrefs: UserPreferences by lazy { fetchLocalPreferences() }

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

    private fun fetchLocalPreferences(): UserPreferences {
        val prefs = UserPreferences()
        UserPreferences::class.memberProperties
            .filterIsInstance<KMutableProperty<*>>()
            .forEach { prop -> prop.setter.call(prefs, sp.getValue(prop.name)) }
        return prefs
    }

    private fun listenForPreferenceChanges() {
        sp.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            // TODO: save new preference value to Firebase
            val prop = UserPreferences::class.memberProperties.find { it.name == key }
            if (prop != null && prop is KMutableProperty<*>) {
                prop.setter.call(userPrefs, sharedPreferences.getValue(key))
                Log.d(TAG, "Updated $key preference to ${sharedPreferences.getValue(key)}")
                Log.d(TAG, "New user preferences: $userPrefs")
            } else {
                Log.w(TAG, "Invalid preference key: $key")
            }
        }
    }

    private fun SharedPreferences.getValue(key: String): Any {
        val prefKey = Preference.values().find { it.key == key } ?: return Unit
        return when (prefKey) {
            Preference.DefaultSort -> DefaultSort.valueOf(getEnumName(key))
            Preference.RecencyFilter -> RecencyFilter.valueOf(getEnumName(key))
            Preference.MaxArticles -> MaxArticles.valueOf(getEnumName(key))
            Preference.SearchRadius -> SearchRadius.valueOf(getEnumName(key))
            Preference.ExpandSearch, Preference.SearchArticleBody -> getBoolean(key, false)
        }
    }

    private fun SharedPreferences.getEnumName(key: String): String {
        return getString(key, null)!!.toUpperCase(Locale.getDefault())
    }
}
