package edu.ucsb.cs.cs184.urth

import android.content.SharedPreferences
import java.util.Locale
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

fun SharedPreferences.fetchLocalPreferences(): UserPreferences {
    val prefs = UserPreferences()
    UserPreferences::class.memberProperties
        .filterIsInstance<KMutableProperty<*>>()
        .forEach { prop -> prop.setter.call(prefs, getValue(prop.name) ?: return@forEach) }
    return prefs
}

fun SharedPreferences.getValue(key: String): Any? {
    val prefKey = Preference.values().find { it.key == key } ?: return null
    return when (prefKey) {
        Preference.DefaultSort -> DefaultSort.valueOf(getEnumName(key) ?: return null)
        Preference.RecencyFilter -> RecencyFilter.valueOf(getEnumName(key) ?: return null)
        Preference.MaxArticles -> MaxArticles.valueOf(getEnumName(key) ?: return null)
        Preference.SearchRadius -> SearchRadius.valueOf(getEnumName(key) ?: return null)
        Preference.ExpandSearch, Preference.SearchArticleBody -> getBoolean(key, false)
    }
}

private fun SharedPreferences.getEnumName(key: String): String? {
    return getString(key, null)?.toUpperCase(Locale.getDefault())
}

fun SharedPreferences.Editor.putValue(key: String, value: Any?) {
    if (value is Boolean) {
        putBoolean(key, value)
    } else {
        val stringValue = value.toString().toLowerCase(Locale.getDefault())
        putString(key, stringValue)
    }
}
