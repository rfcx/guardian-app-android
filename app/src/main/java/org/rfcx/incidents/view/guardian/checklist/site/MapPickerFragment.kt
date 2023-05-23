package org.rfcx.incidents.view.guardian.checklist.site

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.mapbox.mapboxsdk.geometry.LatLng
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentMapPickerBinding
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.latitudeCoordinates
import org.rfcx.incidents.util.longitudeCoordinates
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener

class MapPickerFragment :
    Fragment() {
    private lateinit var site: Stream

    private lateinit var binding: FragmentMapPickerBinding
    private val viewModel: GuardianSiteSetViewModel by viewModel()
    private var mainEvent: GuardianDeploymentEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainEvent = context as GuardianDeploymentEventListener
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map_picker, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        initIntent()

        binding.mapBoxPickerView.onCreate(savedInstanceState)
        binding.mapBoxPickerView.setParam(canMove = true, fromDeploymentList = false)
        viewModel.currentLocationState.value.let {
            binding.mapBoxPickerView.setCurrentLocation(LatLng(it?.latitude ?: 0.0, it?.longitude ?: 0.0))
            binding.mapBoxPickerView.setSiteLocation(LatLng(site.latitude, site.longitude))
        }
        binding.locationTextView.text = "${site.latitude.latitudeCoordinates()}, ${site.longitude.longitudeCoordinates()}"
        binding.mapBoxPickerView.setCameraMoveCallback {
            binding.locationTextView.text = "${it.latitude.latitudeCoordinates()}, ${it.longitude.longitudeCoordinates()}"
        }

        binding.selectButton.setOnClickListener {
            val currentCameraPosition = binding.mapBoxPickerView.getCurrentPosition()
            site.latitude = currentCameraPosition.latitude
            site.longitude = currentCameraPosition.longitude
            mainEvent?.goToSiteSetScreen(site, isNewSite = false)
        }

        binding.currentLocationButton.setOnClickListener {
            val latLng = viewModel.currentLocationState.value
            latLng?.let {
                binding.mapBoxPickerView.moveCamera(LatLng(it.latitude, it.longitude))
                binding.locationTextView.text = "${it.latitude.latitudeCoordinates()}, ${it.longitude.longitudeCoordinates()}"
            }
        }
    }

    private fun initIntent() {
        arguments?.let {
            site = it.getSerializable(ARG_SITE) as Stream
        }
    }

    override fun onStart() {
        super.onStart()
        binding.mapBoxPickerView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapBoxPickerView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapBoxPickerView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapBoxPickerView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapBoxPickerView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapBoxPickerView.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapBoxPickerView.onDestroy()
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
