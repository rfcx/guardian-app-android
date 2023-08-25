package org.rfcx.incidents.view.guardian.checklist.site

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentGuardianSiteSetBinding
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.setFormatLabel
import org.rfcx.incidents.view.base.BaseMapFragment
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener

class GuardianSiteSetFragment : BaseMapFragment() {

    private lateinit var binding: FragmentGuardianSiteSetBinding
    private val viewModel: GuardianSiteSetViewModel by viewModel()
    private var mainEvent: GuardianDeploymentEventListener? = null

    // Arguments
    private lateinit var site: Stream
    private var isNewSite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
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
        mapView = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapView!!.getMapAsync(this)

        binding.viewModel = viewModel

        mainEvent?.let {
            it.showToolbar()
            it.setToolbarTitle(getString(R.string.guardian_site_title))
        }

        binding.nextButton.setOnClickListener {
            mainEvent?.nextWithStream(site)
        }

        binding.currentLocate.setOnClickListener {
            viewModel.updateSiteToCurrentLocation()
            val currentLoc = LatLng(viewModel.currentLocationState.value?.latitude ?: 0.0, viewModel.currentLocationState.value?.longitude ?: 0.0)
            addMarker(currentLoc)
            moveCamera(currentLoc)
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

                    setCurrentLocation(curLoc)
                    setSiteLocation(siteLoc)
                }
            }
        }
    }

    private fun setWithInText(curLoc: LatLng, target: LatLng) {
        val distance = SphericalUtil.computeDistanceBetween(curLoc, target)
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

    override fun onMapReady(p0: GoogleMap) {
        setGoogleMap(p0, false)

        val latLng = LatLng(site.latitude, site.longitude)
        addMarker(latLng)
        moveCamera(latLng)
        fusedLocationClient()
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
