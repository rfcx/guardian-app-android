package org.rfcx.incidents.view.guardian.checklist.preference

import android.content.SharedPreferences
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R

class GuardianPrefsFragment :
    PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val viewModel: GuardianPreferenceViewModel by viewModel()

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs)
        val preferenceCategory = PreferenceCategory(preferenceScreen.context)
        preferenceCategory.title = ""
        preferenceScreen.addPreference(preferenceCategory)

        lifecycleScope.launch {
            viewModel.preferenceState.collectLatest { prefs ->
                prefs.forEach {
                    preferenceCategory.addPreference(it)
                }
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val value = sharedPreferences.getString(key, "")
        viewModel.setPreferencesChanged(key, value!!)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceScreen.sharedPreferences?.edit()?.clear()?.apply()
    }
}
