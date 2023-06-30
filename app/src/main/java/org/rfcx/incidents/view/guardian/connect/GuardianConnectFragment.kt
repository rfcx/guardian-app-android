package org.rfcx.incidents.view.guardian.connect

import android.net.wifi.ScanResult
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.databinding.FragmentGuardianConnectBinding
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener
import org.rfcx.incidents.view.guardian.GuardianScreen

class GuardianConnectFragment : Fragment(), (ScanResult) -> Unit {

    private lateinit var binding: FragmentGuardianConnectBinding
    private val viewModel: GuardianConnectViewModel by viewModel()
    private val hotspotAdapter by lazy { GuardianHotspotAdapter(this) }

    private var mainEvent: GuardianDeploymentEventListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainEvent = context as GuardianDeploymentEventListener
        binding = FragmentGuardianConnectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainEvent?.hideToolbar()

        collectStates()

        binding.guardianHotspotRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = hotspotAdapter
        }

        binding.connectGuardianButton.setOnClickListener {
            mainEvent?.connectHotspot(viewModel.getSelectedHotspot())
            lifecycleScope.launch {
                launch { collectHotspotConnect() }
            }
        }

        binding.retryGuardianButton.setOnClickListener {
            viewModel.nearbyHotspots()
        }
    }

    // Observe all UI StateFlow
    private fun collectStates() {
        lifecycleScope.launch {
            launch { collectNearbyHotspot() }
        }
    }

    private fun collectNearbyHotspot() {
        lifecycleScope.launch {
            viewModel.hotspotsState.collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        Toast.makeText(requireContext(), "from nearby 1", Toast.LENGTH_SHORT).show()
                        binding.connectGuardianLoading.visibility = View.GONE
                    }
                    Result.Loading -> {
                        binding.connectGuardianLoading.visibility = View.VISIBLE
                        binding.notFoundTextView.visibility = View.GONE
                    }
                    is Result.Success -> {
                        if (result.data.isEmpty()) {
                            binding.guardianHotspotRecyclerView.visibility = View.GONE
                            binding.retryGuardianButton.visibility = View.VISIBLE
                            binding.notFoundTextView.visibility = View.VISIBLE
                        } else {
                            binding.guardianHotspotRecyclerView.visibility = View.VISIBLE
                            binding.notFoundTextView.visibility = View.GONE
                            binding.retryGuardianButton.visibility = View.GONE
                            hotspotAdapter.items = result.data
                        }
                        Toast.makeText(requireContext(), "from nearby 2", Toast.LENGTH_SHORT).show()
                        binding.connectGuardianLoading.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun collectHotspotConnect() {
        lifecycleScope.launch {
            mainEvent?.getHotspotConnectionState()?.collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        binding.guardianHotspotRecyclerView.visibility = View.VISIBLE
                        binding.connectGuardianLoading.visibility = View.GONE
                        binding.connectGuardianButton.isEnabled = true
                    }
                    Result.Loading -> {
                        binding.guardianHotspotRecyclerView.visibility = View.GONE
                        binding.connectGuardianLoading.visibility = View.VISIBLE
                        binding.connectGuardianButton.isEnabled = false
                    }
                    is Result.Success -> {
                        if (result.data) {
                            launch { mainEvent?.initSocket() }
                            launch { collectSocketInitial() }
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun collectSocketInitial() {
        lifecycleScope.launch {
            mainEvent?.getInitSocketState()?.collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        if (result.data) {
                            mainEvent?.sendHeartBeatSocket()
                            collectSocketRead()
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun collectSocketRead() {
        lifecycleScope.launch {
            mainEvent?.getSocketMessageState()?.collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        binding.guardianHotspotRecyclerView.visibility = View.VISIBLE
                        binding.connectGuardianLoading.visibility = View.GONE
                        binding.connectGuardianButton.isEnabled = true
                    }
                    Result.Loading -> {
                        binding.guardianHotspotRecyclerView.visibility = View.GONE
                        binding.connectGuardianLoading.visibility = View.VISIBLE
                        binding.connectGuardianButton.isEnabled = false
                    }
                    is Result.Success -> {
                        if (result.data.isNotEmpty()) {
                            mainEvent?.changeScreen(GuardianScreen.CHECKLIST)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    override fun invoke(hotspot: ScanResult) {
        viewModel.setSelectedHotspot(hotspot)
        binding.connectGuardianButton.isEnabled = true
    }

    companion object {
        fun newInstance(): GuardianConnectFragment {
            return GuardianConnectFragment()
        }
    }
}
