package org.rfcx.incidents.view.guardian.connect

import android.net.wifi.ScanResult
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.bind
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.databinding.FragmentGuardianConnectBinding
import org.rfcx.incidents.service.wifi.WifiHotspotManager
import org.rfcx.incidents.view.guardian.GuardianDeploymentViewModel
import org.rfcx.incidents.view.base.BaseFragment
import org.rfcx.incidents.view.events.adapter.EventItemAdapter
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener

class GuardianConnectFragment : Fragment(), (ScanResult) -> Unit {

    private lateinit var binding: FragmentGuardianConnectBinding
    private lateinit var hotspotManager: WifiHotspotManager
    private val viewModel: GuardianConnectViewModel by viewModel()
    private val mainViewModel: GuardianDeploymentViewModel by viewModel()
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

        lifecycleScope.launchWhenStarted { viewModel.nearbyHotspots() }

        binding.connectGuardianButton.setOnClickListener{
            lifecycleScope.launchWhenStarted {
                launch {
                    viewModel.connect()
                }
            }
        }

        binding.retryGuardianButton.setOnClickListener {
            lifecycleScope.launchWhenStarted { viewModel.nearbyHotspots() }
        }
    }

    // Observe all UI StateFlow
    private fun collectStates() {
        lifecycleScope.launchWhenStarted {
            launch { collectNearbyHotspot() }
            launch { collectHotspotConnect() }
            launch { collectSocketInitial() }
            launch { collectSocketRead() }
        }
    }

    private fun collectNearbyHotspot() {
        lifecycleScope.launch {
            viewModel.hotspotsState.collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        binding.connectGuardianLoading.visibility = View.GONE
                    }
                    Result.Loading -> {
                        binding.connectGuardianLoading.visibility = View.VISIBLE
                    }
                    is Result.Success -> {
                        if (result.data.isNullOrEmpty()) {
                            binding.guardianHotspotRecyclerView.visibility = View.GONE
                            binding.retryGuardianButton.visibility = View.VISIBLE
                            binding.notFoundTextView.visibility = View.VISIBLE
                        } else {
                            binding.guardianHotspotRecyclerView.visibility = View.VISIBLE
                            binding.notFoundTextView.visibility = View.GONE
                            binding.retryGuardianButton.visibility = View.GONE
                            hotspotAdapter.items = result.data
                        }
                        binding.connectGuardianLoading.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun collectHotspotConnect() {
        lifecycleScope.launch {
            viewModel.connectionState.collectLatest { result ->
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
                            launch { mainViewModel.initSocket() }
                        }
                    }
                }
            }
        }
    }

    private fun collectSocketInitial() {
        lifecycleScope.launch {
            mainViewModel.initSocketState.collectLatest { result ->
                Log.d("Comp3", result.toString())
                when (result) {
                    is Result.Success -> {
                        if (result.data) {
                            launch { mainViewModel.readSocket() }
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun collectSocketRead() {
        lifecycleScope.launch {
            mainViewModel.socketMessageState.collectLatest { result ->
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
                            Log.d("Comp4", result.toString())
                            binding.guardianHotspotRecyclerView.visibility = View.VISIBLE
                            binding.connectGuardianLoading.visibility = View.GONE
                            binding.connectGuardianButton.isEnabled = true
                        }
                    }
                }
            }
        }
    }

    override fun invoke(hotspot: ScanResult) {
        viewModel.setSelectedHotspot(hotspot)
        binding.connectGuardianButton.isEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        fun newInstance(): GuardianConnectFragment {
            return GuardianConnectFragment()
        }
    }
}
