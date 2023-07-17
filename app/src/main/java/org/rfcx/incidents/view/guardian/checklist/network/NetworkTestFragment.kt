package org.rfcx.incidents.view.guardian.checklist.network

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentGuardianNetworkBinding
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener

class NetworkTestFragment : Fragment() {

    private lateinit var binding: FragmentGuardianNetworkBinding
    private val viewModel: NetworkTestViewModel by viewModel()

    private var mainEvent: GuardianDeploymentEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainEvent = context as GuardianDeploymentEventListener
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_guardian_network, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        mainEvent?.let {
            it.showToolbar()
            it.hideThreeDots()
            it.setToolbarTitle(getString(R.string.network_test))
        }

        binding.cellDataTransferButton.setOnClickListener {
            viewModel.sendSpeedTestCommand()
        }

        binding.nextButton.setOnClickListener {
            mainEvent?.next()
        }
    }

    companion object {
        fun newInstance(): NetworkTestFragment = NetworkTestFragment()
    }
}
