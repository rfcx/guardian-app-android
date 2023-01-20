package org.rfcx.incidents.view.guardian.connect

import android.net.wifi.ScanResult
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.rfcx.incidents.databinding.FragmentGuardianConnectBinding
import org.rfcx.incidents.service.wifi.NearbyHotspotListener
import org.rfcx.incidents.service.wifi.WifiHotspotManager

class GuardianConnectFragment : Fragment(), NearbyHotspotListener, (ScanResult) -> Unit {

    lateinit var binding: FragmentGuardianConnectBinding
    private lateinit var hotspotManager: WifiHotspotManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGuardianConnectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hotspotManager = WifiHotspotManager(requireContext())
        hotspotManager.nearbyHotspot(this)
    }

    override fun onScanReceive(result: List<ScanResult>) {
        Log.d(GuardianConnectFragment.javaClass.name, result.toString())
    }

    override fun onWifiConnected() {
    }

    override fun invoke(p1: ScanResult) {
    }

    override fun onDestroy() {
        super.onDestroy()

        hotspotManager.unRegisterReceiver()
    }

    companion object {
        fun newInstance(): GuardianConnectFragment {
            return GuardianConnectFragment()
        }
    }
}
