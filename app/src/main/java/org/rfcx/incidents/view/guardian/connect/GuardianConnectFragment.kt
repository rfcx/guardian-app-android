package org.rfcx.incidents.view.guardian.connect

import android.net.wifi.ScanResult
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.databinding.FragmentGuardianConnectBinding
import org.rfcx.incidents.service.wifi.WifiHotspotManager

class GuardianConnectFragment : Fragment(), (ScanResult) -> Unit {

    lateinit var binding: FragmentGuardianConnectBinding
    private lateinit var hotspotManager: WifiHotspotManager
    private val viewModel: GuardianConnectViewModel by viewModel()
    private val hotspotAdapter by lazy { GuardianHotspotAdapter(this) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGuardianConnectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.guardianHotspotRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = hotspotAdapter
        }

        lifecycleScope.launchWhenStarted {
            viewModel.nearbyHotspots()
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
                            binding.notFoundTextView.visibility = View.VISIBLE
                        } else {
                            binding.guardianHotspotRecyclerView.visibility = View.VISIBLE
                            binding.notFoundTextView.visibility = View.GONE
                            hotspotAdapter.items = result.data
                        }
                        binding.connectGuardianLoading.visibility = View.GONE
                    }
                }
            }
        }

        binding.connectGuardianButton.setOnClickListener {
            lifecycleScope.launchWhenStarted {
                launch {
                    viewModel.connect()
                    viewModel.connectionState.collect { result ->
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
                            is Result.Success -> launch { viewModel.initSocket() }
                        }
                    }
                }

                launch {
                    viewModel.initSocketState.collectLatest { result ->
                        when (result) {
                            is Result.Success -> launch { viewModel.readSocket() }
                            else -> {}
                        }
                    }
                }

                launch {
                    viewModel.socketMessageState.collect { result ->
                        Log.d("Comp3", result.toString())
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
                                binding.guardianHotspotRecyclerView.visibility = View.VISIBLE
                                binding.connectGuardianLoading.visibility = View.GONE
                                binding.connectGuardianButton.isEnabled = true
                            }
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
