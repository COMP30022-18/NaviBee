package au.edu.unimelb.eng.navibee

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }

    /**
     * Set up the [android.app.ActionBar], if the API is available.
     */
    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}

class SettingsFragment: PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var sharedPref: SharedPreferences

    private val ignoreSummary = HashSet<String>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preference)
    }

    override fun onResume() {
        super.onResume()

        sharedPref = preferenceManager.sharedPreferences

        sharedPref.registerOnSharedPreferenceChangeListener(this)

        recursiveIteratePreference(preferenceScreen)
    }

    private fun recursiveIteratePreference(p: PreferenceGroup) {
        for (i in  0 until p.preferenceCount){
            p.getPreference(i).apply {
                if (this is PreferenceGroup) {
                    recursiveIteratePreference(this)
                } else if (!this.summary.isNullOrBlank()) {
                    ignoreSummary.add(this.key)
                } else {
                    updateSummary(this)
                }
            }
        }
    }

    override fun onPause() {
        sharedPref.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key !in ignoreSummary)
            updateSummary(preferenceScreen.findPreference(key))
    }

    private fun updateSummary(pref: Preference) {
        pref.summary = when (pref) {
            is ListPreference -> pref.entry
            is EditTextPreference -> pref.text
            else -> pref.summary
        }
    }

}