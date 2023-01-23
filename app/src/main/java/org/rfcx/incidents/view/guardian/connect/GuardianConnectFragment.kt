package org.rfcx.incidents.view.guardian.connect

import android.net.wifi.ScanResult
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.shareIn
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.databinding.FragmentGuardianConnectBinding
import org.rfcx.incidents.service.wifi.WifiHotspotManager
import org.rfcx.incidents.view.base.BaseFragment
import org.rfcx.incidents.view.events.adapter.EventItemAdapter

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

        viewModel.nearbyHotspots()
        lifecycleScope.launchWhenStarted {
            viewModel.hotspotsState.collectLatest { result ->
                when (result) {
                    is Result.Error -> {}
                    Result.Loading -> {
                        binding.connectGuardianLoading.visibility = View.VISIBLE
                    }
                    is Result.Success -> {
                        hotspotAdapter.items = result.data
                        binding.connectGuardianLoading.visibility = View.GONE
                    }
                }
            }
        }
    }
    override fun invoke(hotspot: ScanResult) {
        TODO("Not yet implemented")
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
