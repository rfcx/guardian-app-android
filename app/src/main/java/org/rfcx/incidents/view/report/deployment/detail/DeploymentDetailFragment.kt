package org.rfcx.incidents.view.report.deployment.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentDeploymentDetailBinding
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.latitudeCoordinates
import org.rfcx.incidents.util.longitudeCoordinates
import org.rfcx.incidents.util.setFormatLabel
import org.rfcx.incidents.view.base.BaseMapFragment
import org.rfcx.incidents.view.report.deployment.detail.display.DisplayImageActivity
import org.rfcx.incidents.view.report.deployment.detail.edit.EditDeploymentSiteActivity
import org.rfcx.incidents.view.report.deployment.detail.image.AddImageActivity

class DeploymentDetailFragment : BaseMapFragment() {

    private val deploymentImageAdapter by lazy { DeploymentImageAdapter() }
    lateinit var binding: FragmentDeploymentDetailBinding
    private val viewModel: DeploymentDetailViewModel by viewModel()

    private var streamId = -1
    private var toAddImage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initIntent()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_deployment_detail, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapView!!.getMapAsync(this)
        setupImageRecycler()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.stream.collectLatest {
                updateDeploymentDetailView(it)
                if (it != null) {
                    val siteLoc = LatLng(it.latitude, it.longitude)
                    setSiteLocation(siteLoc)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.images.collectLatest {
                deploymentImageAdapter.setImages(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorFetching.collectLatest {
                if (it != null) {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.editButton.setOnClickListener {
            EditDeploymentSiteActivity.startActivity(requireContext(), streamId)
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        setGoogleMap(p0, false)
    }

    private fun updateDeploymentDetailView(stream: Stream?) {
        binding.latitudeValue.text = stream?.latitude.latitudeCoordinates()
        binding.longitudeValue.text = stream?.longitude.longitudeCoordinates()
        binding.altitudeValue.text = stream?.altitude?.setFormatLabel()
        stream?.deployment?.let { dp ->
            binding.deploymentIdTextView.text = dp.deploymentKey
        }
    }

    private fun initIntent() {
        arguments?.let {
            streamId = it.getInt(ARG_STREAM_ID)
        }
        if (streamId != -1) {
            viewModel.setStreamId(streamId)
        }
    }

    private fun setupImageRecycler() {
        binding.deploymentImageRecycler.apply {
            adapter = deploymentImageAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
        }

        deploymentImageAdapter.onImageAdapterClickListener = object : OnImageAdapterClickListener {
            override fun onAddImageClick() {
                AddImageActivity.startActivity(
                    requireContext(),
                    streamId
                )
                toAddImage = true
            }

            override fun onImageClick(position: Int) {
                val pair = viewModel.getListOfPathForDisplay(position)
                DisplayImageActivity.startActivity(
                    requireContext(),
                    pair.first.toTypedArray(),
                    pair.second.toTypedArray()
                )
            }

            override fun onDeleteImageClick(position: Int, imagePath: String) {
                deploymentImageAdapter.removeAt(position)
            }
        }

        deploymentImageAdapter.setImages(arrayListOf())
    }

    companion object {
        private const val ARG_STREAM_ID = "ARG_STREAM_ID"

        @JvmStatic
        fun newInstance(streamId: Int) = DeploymentDetailFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_STREAM_ID, streamId)
            }
        }
    }
}
