package org.rfcx.incidents.view.report.deployment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentDeploymentListBinding
import org.rfcx.incidents.view.MainActivityEventListener
import java.util.Date

class DeploymentListFragment : Fragment(), CloudListener {

    private lateinit var binding: FragmentDeploymentListBinding
    private val viewModel: DeploymentListViewModel by viewModel()
    private lateinit var listener: MainActivityEventListener

    private val deploymentAdapter by lazy { DeploymentListAdapter(this) }

    private var state = DeploymentListState.LIST

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        listener = context as MainActivityEventListener
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_deployment_list, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setMap(savedInstanceState)
        binding.viewModel = viewModel

        binding.deploymentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = deploymentAdapter
        }

        lifecycleScope.launch {
            viewModel.deployments.collectLatest {
                if (it.isEmpty()) {
                    binding.noDeploymentLayout.visibility = View.VISIBLE
                } else {
                    binding.noDeploymentLayout.visibility = View.GONE
                }
                deploymentAdapter.items = it
            }
        }

        lifecycleScope.launch {
            viewModel.selectedProject.collectLatest {
                binding.toolbarLayout.projectTitleTextView.text = it
            }
        }

        binding.filterGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val selected = checkedIds.getOrNull(0)
            when(selected) {
                R.id.allSelectChip -> viewModel.addFilter(DeploymentListViewModel.FilterDeployment.ALL)
                R.id.unSyncedSelectChip -> viewModel.addFilter(DeploymentListViewModel.FilterDeployment.UNSYNCED)
                R.id.syncedSelectChip -> viewModel.addFilter(DeploymentListViewModel.FilterDeployment.SYNCED)
                null -> {
                    val allChip = group.findViewById<Chip>(R.id.allSelectChip)
                    allChip.isChecked = true
                }
            }
        }
    }

    private fun setMap(savedInstanceState: Bundle?) {
        binding.toolbarLayout.changePageImageView.setOnClickListener {
            if (state == DeploymentListState.LIST) {
                binding.mapLayout.visibility = View.VISIBLE
                binding.listLayout.visibility = View.GONE
                binding.toolbarLayout.changePageImageView.setImageResource(R.drawable.ic_view_list)
                state = DeploymentListState.MAP
            } else {
                binding.mapLayout.visibility = View.GONE
                binding.listLayout.visibility = View.VISIBLE
                binding.toolbarLayout.changePageImageView.setImageResource(R.drawable.ic_map)
                state = DeploymentListState.LIST
            }
        }

        binding.mapBoxView.onCreate(savedInstanceState)
        binding.mapBoxView.setParam(canMove = true, fromDeploymentList = true)
        lifecycleScope.launch {
            viewModel.currentLocationState.collectLatest { currentLoc ->
                currentLoc?.let {
                    val curLoc = LatLng(it.latitude, it.longitude)
                    binding.mapBoxView.setCurrentLocation(curLoc)
                }
            }
        }

        binding.mapBoxView.setMapReadyCallback {
            if (it) {
                lifecycleScope.launch {
                    viewModel.markers.collectLatest { markers ->
                        binding.mapBoxView.addSiteAndDeploymentToMarker(markers)
                    }
                }
            }
        }

        binding.currentLocationButton.setOnClickListener {
            binding.mapBoxView.moveCamera(viewModel.currentLocationState.value)
        }
    }

    override fun onClicked(id: Int) {
        viewModel.syncDeployment(id)
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
        const val tag = "DeploymentListFragment"
        enum class DeploymentListState { LIST, MAP}
        fun newInstance(): DeploymentListFragment = DeploymentListFragment()
    }
}
