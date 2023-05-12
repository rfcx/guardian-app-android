package org.rfcx.incidents.view.guardian.checklist.preference

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentGuardianPreferenceBinding
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener

class GuardianPreferenceFragment : Fragment() {

    private lateinit var binding: FragmentGuardianPreferenceBinding
    private val viewModel: GuardianPreferenceViewModel by viewModel()

    private var mainEvent: GuardianDeploymentEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainEvent = context as GuardianDeploymentEventListener
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_guardian_preference, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        mainEvent?.let {
            it.showToolbar()
            it.setToolbarTitle("Preference Settings")
        }

        // start guardian prefs fragment once view created
        parentFragmentManager.beginTransaction()
            .replace(binding.advancedContainer.id, GuardianPrefsFragment())
            .commit()

        binding.advancedFinishButton.setOnClickListener {
            syncConfig()
        }

        lifecycleScope.launch {
            viewModel.syncState.collectLatest {
                if (it) {
                    mainEvent?.next()
                }
            }
        }
    }

    private fun syncConfig() {
        viewModel.sync()
    }

    companion object {
        fun newInstance(): GuardianPreferenceFragment {
            return GuardianPreferenceFragment()
        }
    }
}
