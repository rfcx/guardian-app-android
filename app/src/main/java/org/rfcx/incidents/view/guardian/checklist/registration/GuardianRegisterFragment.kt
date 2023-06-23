package org.rfcx.incidents.view.guardian.checklist.registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentGuardianRegisterBinding
import org.rfcx.incidents.util.isNetworkAvailable
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener

class GuardianRegisterFragment : Fragment() {

    private lateinit var binding: FragmentGuardianRegisterBinding
    private val viewModel: GuardianRegisterViewModel by viewModel()

    private var mainEvent: GuardianDeploymentEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainEvent = context as GuardianDeploymentEventListener
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_guardian_register, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        mainEvent?.let {
            it.showToolbar()
            it.hideThreeDots()
            it.setToolbarTitle(getString(R.string.register_guardian))
        }

        binding.registerGuardianButton.setOnClickListener {
            if (requireContext().isNetworkAvailable()) {
                viewModel.sendRegistrationOnline(binding.productionRadioButton.isChecked)
            } else {
                viewModel.sendRegistrationOffline(binding.productionRadioButton.isChecked)
            }
        }

        binding.registerFinishButton.setOnClickListener {
            mainEvent?.next()
        }
    }

    companion object {
        fun newInstance(): GuardianRegisterFragment = GuardianRegisterFragment()
    }
}
