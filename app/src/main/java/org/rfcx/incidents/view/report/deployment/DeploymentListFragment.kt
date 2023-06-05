package org.rfcx.incidents.view.report.deployment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.databinding.FragmentDeploymentListBinding
import org.rfcx.incidents.entity.stream.Project
import org.rfcx.incidents.util.isNetworkAvailable
import org.rfcx.incidents.util.isOnAirplaneMode
import org.rfcx.incidents.view.MainActivityEventListener
import org.rfcx.incidents.view.events.adapter.ProjectAdapter
import org.rfcx.incidents.view.events.adapter.ProjectOnClickListener
import org.rfcx.incidents.view.report.deployment.detail.DeploymentDetailActivity

class DeploymentListFragment : Fragment(), DeploymentItemListener, ProjectOnClickListener {

    private lateinit var binding: FragmentDeploymentListBinding
    private val viewModel: DeploymentListViewModel by viewModel()
    private lateinit var listener: MainActivityEventListener

    private val deploymentAdapter by lazy { DeploymentListAdapter(this) }
    private val projectAdapter by lazy { ProjectAdapter(this) }

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

        setSwipe()
        setRecyclerView()
        setCollectState()

        binding.filterGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val selected = checkedIds.getOrNull(0)
            when (selected) {
                R.id.allSelectChip -> viewModel.addFilter(DeploymentListViewModel.FilterDeployment.ALL)
                R.id.unSyncedSelectChip -> viewModel.addFilter(DeploymentListViewModel.FilterDeployment.UNSYNCED)
                R.id.syncedSelectChip -> viewModel.addFilter(DeploymentListViewModel.FilterDeployment.SYNCED)
                null -> {
                    val allChip = group.findViewById<Chip>(R.id.allSelectChip)
                    allChip.isChecked = true
                }
            }
        }

        binding.toolbarLayout.projectTitleLayout.setOnClickListener {
            if (binding.projectRecyclerView.visibility == View.VISIBLE) {
                hideProjectList()
            } else {
                showProjectList()
            }
        }
    }

    private fun setRecyclerView() {
        binding.deploymentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = deploymentAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val linearLayoutManager = layoutManager!! as LinearLayoutManager
                    val visibleItemCount = linearLayoutManager.childCount
                    val total = layoutManager!!.itemCount
                    val firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition()
                    if (!binding.deploymentRefreshView.isRefreshing &&
                        (visibleItemCount + firstVisibleItemPosition) >= total &&
                        firstVisibleItemPosition >= 0 && !viewModel.isLoadingMore
                    ) {
                        viewModel.fetchStream(force = true, offset = total)
                    }
                }
            })
        }

        binding.projectRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = projectAdapter
            projectAdapter.items = emptyList()
        }
    }

    private fun setCollectState() {
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

        lifecycleScope.launch {
            viewModel.projects.collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        binding.projectSwipeRefreshView.isRefreshing = false
                        Toast.makeText(
                            context,
                            result.throwable.message
                                ?: getString(R.string.something_is_wrong),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Result.Loading -> {}
                    is Result.Success -> {
                        binding.projectSwipeRefreshView.isRefreshing = false
                        projectAdapter.items = result.data
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.streams.collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        binding.deploymentRefreshView.isRefreshing = false
                        Toast.makeText(
                            context,
                            result.throwable.message
                                ?: getString(R.string.something_is_wrong),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Result.Loading -> {}
                    is Result.Success -> {
                        binding.deploymentRefreshView.isRefreshing = false
                    }
                }
            }
        }
    }

    private fun setSwipe() {
        binding.projectSwipeRefreshView.apply {
            setOnRefreshListener {
                isRefreshing = true
                when {
                    requireContext().isOnAirplaneMode() -> {
                        isRefreshing = false
                        showSwipeAirplaneError()
                    }
                    !requireContext().isNetworkAvailable() -> {
                        isRefreshing = false
                        showSwipeNoConnectionError()
                    }
                    else -> {
                        viewModel.fetchProject(true)
                    }
                }
            }
            setColorSchemeResources(R.color.colorPrimary)
        }

        binding.deploymentRefreshView.apply {
            setOnRefreshListener {
                isRefreshing = true
                when {
                    requireContext().isOnAirplaneMode() -> {
                        isRefreshing = false
                        showSwipeAirplaneError()
                    }
                    !requireContext().isNetworkAvailable() -> {
                        isRefreshing = false
                        showSwipeNoConnectionError()
                    }
                    else -> {
                        viewModel.fetchFreshStreams(force = true)
                    }
                }
                setColorSchemeResources(R.color.colorPrimary)
            }
        }
    }

    private fun showSwipeAirplaneError() {
        Toast.makeText(
            requireContext(),
            getString(R.string.project_could_not_refreshed) + " " + getString(R.string.pls_off_air_plane_mode),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showSwipeNoConnectionError() {
        Toast.makeText(
            requireContext(),
            getString(R.string.project_could_not_refreshed) + " " + getString(R.string.no_internet_connection),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun setMap(savedInstanceState: Bundle?) {
        binding.toolbarLayout.changePageImageView.setOnClickListener {
            if (state == DeploymentListState.LIST) {
                binding.mapLayout.visibility = View.VISIBLE
                binding.listLayout.visibility = View.GONE
                binding.deploymentRefreshView.visibility = View.GONE
                binding.toolbarLayout.changePageImageView.setImageResource(R.drawable.ic_view_list)
                state = DeploymentListState.MAP
            } else {
                binding.mapLayout.visibility = View.GONE
                binding.listLayout.visibility = View.VISIBLE
                binding.deploymentRefreshView.visibility = View.VISIBLE
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

        binding.mapBoxView.setSeeDetailCallback {
            DeploymentDetailActivity.startActivity(requireContext(), it)
        }

        binding.currentLocationButton.setOnClickListener {
            binding.mapBoxView.moveCamera(viewModel.currentLocationState.value)
        }
    }

    private fun showProjectList() {
        binding.toolbarLayout.expandMoreImageView.rotation = 180F
        listener.hideBottomAppBar()
        binding.projectRecyclerView.visibility = View.VISIBLE
        binding.projectSwipeRefreshView.visibility = View.VISIBLE
    }

    private fun hideProjectList() {
        binding.toolbarLayout.expandMoreImageView.rotation = 0F
        listener.showBottomAppBar()
        binding.projectRecyclerView.visibility = View.GONE
        binding.projectSwipeRefreshView.visibility = View.GONE
    }

    override fun onProjectClicked(project: Project) {
        hideProjectList()
        viewModel.setSelectedProject(project.id)

        when {
            requireContext().isOnAirplaneMode() -> {
                Toast.makeText(requireContext(), getString(R.string.pls_off_air_plane_mode), Toast.LENGTH_LONG).show()
            }
            !requireContext().isNetworkAvailable() -> {
                Toast.makeText(requireContext(), getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onLockImageClicked() {
        Toast.makeText(context, R.string.not_have_permission, Toast.LENGTH_LONG).show()
    }

    override fun onCloudClicked(id: Int) {
        viewModel.syncDeployment(id)
    }

    override fun onItemClicked(streamId: Int) {
        DeploymentDetailActivity.startActivity(requireContext(), streamId)
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

        enum class DeploymentListState { LIST, MAP }

        fun newInstance(): DeploymentListFragment = DeploymentListFragment()
    }
}
