package org.rfcx.incidents.view.guardian.checklist.site

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentGuardianSiteSetBinding
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.setFormatLabel
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener

class GuardianSiteSetFragment : Fragment() {

    private lateinit var binding: FragmentGuardianSiteSetBinding
    private val viewModel: GuardianSiteSetViewModel by viewModel()
    private var mainEvent: GuardianDeploymentEventListener? = null

    // Arguments
    private lateinit var site: Stream
    private var isNewSite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initIntent()
    }

    private fun initIntent() {
        arguments?.let {
            site = it.getSerializable(ARG_SITE) as Stream
            isNewSite = it.getBoolean(ARG_IS_NEW_SITE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainEvent = context as GuardianDeploymentEventListener
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_guardian_site_set, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        mainEvent?.let {
            it.showToolbar()
            it.setToolbarTitle(getString(R.string.guardian_site_title))
        }

        binding.mapBoxView.onCreate(savedInstanceState)
        binding.mapBoxView.setParam(canMove = false, createPinAtCurrentLoc = true)

        binding.nextButton.setOnClickListener {
            mainEvent?.nextWithStream(site)
        }

        binding.currentLocate.setOnClickListener {
            viewModel.updateSiteToCurrentLocation()
            val currentLoc = LatLng(viewModel.currentLocationState.value?.latitude ?: 0.0, viewModel.currentLocationState.value?.longitude ?: 0.0)
            val siteLoc = LatLng(site.latitude, site.longitude)
            binding.mapBoxView.setPinOnMap(siteLoc, currentLoc)
        }

        binding.viewMapBox.setOnClickListener {
            mainEvent?.goToMapPickerScreen(site)
        }

        viewModel.setSite(site)
        viewModel.setIsNewSite(isNewSite)
        collectCurrentLoc()
    }

    private fun collectCurrentLoc() {
        lifecycleScope.launch {
            viewModel.currentLocationState.collectLatest { currentLoc ->
                currentLoc?.let {
                    val curLoc = LatLng(it.latitude, it.longitude)
                    val siteLoc = LatLng(site.latitude, site.longitude)
                    setWithInText(curLoc, siteLoc)

                    binding.mapBoxView.setCurrentLocation(curLoc)
                    binding.mapBoxView.setSiteLocation(siteLoc)
                }
            }
        }
    }

    private fun setWithInText(curLoc: LatLng, target: LatLng) {
        val distance = curLoc.distanceTo(target)
        if (distance <= 20) {
            setWithinText()
        } else {
            setNotWithinText(distance.setFormatLabel())
        }
    }

    private fun setWithinText() {
        binding.withinTextView.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_checklist_passed,
            0,
            0,
            0
        )
        binding.withinTextView.text = getString(R.string.within)

    }

    private fun setNotWithinText(distance: String) {
        binding.withinTextView.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_checklist_cross,
            0,
            0,
            0
        )
        binding.withinTextView.text = getString(R.string.more_than, distance)
    }

    override fun onStart() {
        super.onStart()
        binding.mapBoxView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapBoxView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapBoxView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapBoxView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapBoxView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapBoxView.onDestroy()
    }

    companion object {
        private const val ARG_SITE = "ARG_SITE"
        private const val ARG_IS_NEW_SITE = "ARG_IS_NEW_SITE"

        @JvmStatic
        fun newInstance() = GuardianSiteSetFragment()

        fun newInstance(stream: Stream, isNewSite: Boolean = false) =
            GuardianSiteSetFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_SITE, stream)
                    putBoolean(ARG_IS_NEW_SITE, isNewSite)
                }
            }
    }
}
