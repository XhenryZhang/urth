package edu.ucsb.cs.cs184.urth

import android.content.SharedPreferences
import java.util.*
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

fun SharedPreferences.fetchLocalPreferences(): UserPreferences {
    val prefs = UserPreferences()
    UserPreferences::class.memberProperties
        .filterIsInstance<KMutableProperty<*>>()
        .forEach { prop -> prop.setter.call(prefs, getValue(prop.name)) }
    return prefs
}

fun SharedPreferences.getValue(key: String): Any? {
    val prefKey = Preference.values().find { it.key == key } ?: return null
    return when (prefKey) {
        Preference.DefaultSort -> DefaultSort.valueOf(getEnumName(key)?:"BY_DATE")
        Preference.RecencyFilter -> RecencyFilter.valueOf(getEnumName(key)?:"PAST_DAY")
        Preference.MaxArticles -> MaxArticles.valueOf(getEnumName(key)?:"FIVE")
        Preference.SearchRadius -> SearchRadius.valueOf(getEnumName(key)?:"TWENTY")
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
