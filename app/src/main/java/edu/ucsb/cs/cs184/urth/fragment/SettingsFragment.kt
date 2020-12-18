package edu.ucsb.cs.cs184.urth.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import edu.ucsb.cs.cs184.urth.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}
