package org.rfcx.incidents.view.guardian.checklist

import android.net.wifi.ScanResult
import android.os.Bundle
import android.util.Log
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
import org.rfcx.incidents.databinding.FragmentGuardianChecklistBinding
import org.rfcx.incidents.databinding.FragmentGuardianConnectBinding
import org.rfcx.incidents.service.wifi.WifiHotspotManager
import org.rfcx.incidents.view.base.BaseFragment
import org.rfcx.incidents.view.events.adapter.EventItemAdapter
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener

class GuardianChecklistFragment : Fragment() {

    lateinit var binding: FragmentGuardianChecklistBinding

    private var mainEvent: GuardianDeploymentEventListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainEvent = context as GuardianDeploymentEventListener
        binding = FragmentGuardianChecklistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainEvent?.showToolbar()
        mainEvent?.setToolbarTitle("Checklist")

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        fun newInstance(): GuardianChecklistFragment {
            return GuardianChecklistFragment()
        }
    }
}
