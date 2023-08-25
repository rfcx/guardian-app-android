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
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentMapPickerBinding
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.latitudeCoordinates
import org.rfcx.incidents.util.longitudeCoordinates
import org.rfcx.incidents.view.base.BaseMapFragment
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener
import org.rfcx.incidents.view.report.deployment.detail.edit.EditDeploymentSiteListener

class MapPickerFragment :
    BaseMapFragment() {
    private lateinit var site: Stream

    private lateinit var binding: FragmentMapPickerBinding
    private val viewModel: GuardianSiteSetViewModel by viewModel()
    private var mainEvent: GuardianDeploymentEventListener? = null
    private var detailEvent: EditDeploymentSiteListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        when (context) {
            is GuardianDeploymentEventListener -> mainEvent = context as GuardianDeploymentEventListener
            is EditDeploymentSiteListener -> detailEvent = context as EditDeploymentSiteListener
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map_picker, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = childFragmentManager.findFragmentById(R.id.mapPickerView) as SupportMapFragment
        mapView!!.getMapAsync(this)
        binding.viewModel = viewModel
        initIntent()

        lifecycleScope.launch {
            viewModel.currentLocationState.value.let {
                setCurrentLocation(LatLng(it?.latitude ?: 0.0, it?.longitude ?: 0.0))
                setSiteLocation(LatLng(16.00, 100.453))
            }
        }

        binding.locationTextView.text = "${site.latitude.latitudeCoordinates()}, ${site.longitude.longitudeCoordinates()}"

        setCameraMoveCallback {
            binding.locationTextView.text = "${it.latitude.latitudeCoordinates()}, ${it.longitude.longitudeCoordinates()}"
        }

        binding.selectButton.setOnClickListener {
            if (getCurrentPosition() != null) {
                val currentCameraPosition = getCurrentPosition()
                site.latitude = currentCameraPosition!!.latitude
                site.longitude = currentCameraPosition.longitude
                mainEvent?.goToSiteSetScreen(site, isNewSite = false)
                detailEvent?.backToEditPage(site)
            }
        }

        binding.currentLocationButton.setOnClickListener {
            val latLng = viewModel.currentLocationState.value
            latLng?.let {
                moveCamera(LatLng(it.latitude, it.longitude))
                binding.locationTextView.text = "${it.latitude.latitudeCoordinates()}, ${it.longitude.longitudeCoordinates()}"
            }
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        setGoogleMap(p0, true)
        setCallback(p0)
        moveCamera(LatLng(site.latitude, site.longitude))
        fusedLocationClient()
    }

    private fun initIntent() {
        arguments?.let {
            site = it.getSerializable(ARG_SITE) as Stream
        }
    }

    companion object {
        private const val ARG_SITE = "ARG_SITE"

        @JvmStatic
        fun newInstance(site: Stream) =
            MapPickerFragment()
                .apply {
                    arguments = Bundle().apply {
                        putSerializable(ARG_SITE, site)
                    }
                }
    }
}
