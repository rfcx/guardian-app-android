package org.rfcx.incidents.view.guardian.checklist.communication

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
import org.rfcx.incidents.databinding.FragmentGuardianCommunicationConfigurationBinding
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener

class CommunicationFragment : Fragment() {

    private lateinit var binding: FragmentGuardianCommunicationConfigurationBinding
    private val viewModel: CommunicationViewModel by viewModel()

    private var mainEvent: GuardianDeploymentEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainEvent = context as GuardianDeploymentEventListener
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_guardian_communication_configuration, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        mainEvent?.let {
            it.showToolbar()
            it.setToolbarTitle("Communication Configuration")
        }

        binding.guardianPlanGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.satOnlyRadioButton) {
                binding.passTimesTextView.visibility = View.VISIBLE
                binding.timeOffRadioGroup.visibility = View.VISIBLE
                binding.offTimeChipGroup.visibility = View.VISIBLE
            } else {
                binding.passTimesTextView.visibility = View.GONE
                binding.timeOffRadioGroup.visibility = View.GONE
                binding.offTimeChipGroup.visibility = View.GONE
            }
        }

        binding.offTimeChipGroup.fragmentManager = parentFragmentManager
        binding.timeOffRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.manualRadioButton) {
                binding.offTimeChipGroup.allowAdd = true
                viewModel.onManualClicked()
                hideEmptyOffTimeText()
            } else {
                binding.offTimeChipGroup.allowAdd = false
                viewModel.onAutoClicked()
                showEmptyOffTimeText()
            }
        }

        lifecycleScope.launch {
            viewModel.guardianSatTimeOffState.collectLatest {
                binding.offTimeChipGroup.setTimes(it)
            }
        }
    }

    private fun showEmptyOffTimeText() {
        binding.emptyOffTimeTextView.visibility = View.VISIBLE
    }

    private fun hideEmptyOffTimeText() {
        binding.emptyOffTimeTextView.visibility = View.GONE
    }

    companion object {
        fun newInstance(): CommunicationFragment = CommunicationFragment()
    }
}
