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
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener

class GuardianPrefsFragment :
    PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val viewModel: GuardianPreferenceViewModel by viewModel()
    private var mainEvent: GuardianDeploymentEventListener? = null

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        mainEvent = context as GuardianDeploymentEventListener
        addPreferencesFromResource(R.xml.prefs)
        preferenceScreen.removeAll()
        val preferenceCategory = PreferenceCategory(preferenceScreen.context)
        preferenceCategory.title = ""
        preferenceScreen.addPreference(preferenceCategory)
        mainEvent?.setEditor(preferenceScreen.sharedPreferences?.edit())

        lifecycleScope.launch {
            viewModel.preferenceState.collectLatest { prefs ->
                mainEvent?.setGuardianPrefs(prefs)
                prefs.forEach {
                    if (it.parent != null) {
                        it.parent?.removeAll()
                    }
                    preferenceCategory.addPreference(it)
                }
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val value = sharedPreferences?.getString(key, "")
        if (value != null && key != null) {
            viewModel.setPreferencesChanged(key, value)
            mainEvent?.setChangedPrefs(viewModel.getPreferencesChanged())
        }
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }
}
