package org.rfcx.incidents.view.report.deployment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.databinding.FragmentDeploymentListBinding
import org.rfcx.incidents.entity.guardian.registration.GuardianRegistration
import org.rfcx.incidents.entity.stream.Project
import org.rfcx.incidents.util.isNetworkAvailable
import org.rfcx.incidents.util.isOnAirplaneMode
import org.rfcx.incidents.view.MainActivityEventListener
import org.rfcx.incidents.view.base.BaseMapFragment
import org.rfcx.incidents.view.events.adapter.ProjectAdapter
import org.rfcx.incidents.view.events.adapter.ProjectOnClickListener
import org.rfcx.incidents.view.guardian.GuardianDeploymentActivity
import org.rfcx.incidents.view.report.deployment.detail.DeploymentDetailActivity

class DeploymentListFragment : BaseMapFragment(), DeploymentItemListener, ProjectOnClickListener {

    private lateinit var binding: FragmentDeploymentListBinding
    private val viewModel: DeploymentListViewModel by viewModel()
    private lateinit var listener: MainActivityEventListener

    private val deploymentAdapter by lazy { DeploymentListAdapter(this) }
    private val projectAdapter by lazy { ProjectAdapter(this) }

    private lateinit var unsyncedAlert: AlertDialog
    private var state = DeploymentListState.LIST
    private var currentFilter = DeploymentListViewModel.FilterDeployment.UNSYNCED

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        listener = context as MainActivityEventListener
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_deployment_list, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapView!!.getMapAsync(this)
        binding.viewModel = viewModel

        setSwipe()
        setRecyclerView()
        setCollectState()

        binding.filterGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val selected = checkedIds.getOrNull(0)
            when (selected) {
                R.id.allSelectChip -> {
                    currentFilter = DeploymentListViewModel.FilterDeployment.ALL
                    viewModel.addFilter(currentFilter)
                }

                R.id.unSyncedSelectChip -> {
                    currentFilter = DeploymentListViewModel.FilterDeployment.UNSYNCED
                    viewModel.addFilter(currentFilter)
                }

                R.id.syncedSelectChip -> {
                    currentFilter = DeploymentListViewModel.FilterDeployment.SYNCED
                    viewModel.addFilter(currentFilter)
                }

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

        binding.deployGuardianButton.setOnClickListener {
            GuardianDeploymentActivity.startActivity(requireContext())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun setRecyclerView() {
        binding.deploymentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = deploymentAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    // super.onScrolled(recyclerView, dx, dy)
                    val linearLayoutManager = layoutManager!! as LinearLayoutManager
                    val visibleItemCount = linearLayoutManager.childCount
                    val total = layoutManager!!.itemCount
                    val firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition()
                    if ((visibleItemCount + firstVisibleItemPosition) >= total &&
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
                            getString(R.string.something_is_wrong),
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
                            getString(R.string.something_is_wrong),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    Result.Loading -> binding.deploymentRefreshView.isRefreshing = false
                    is Result.Success -> {
                        binding.deploymentRefreshView.isRefreshing = false
                        binding.deploymentsRecyclerView.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.uploadImageState.collectLatest { result ->
                if (result.isNotEmpty()) {
                    Toast.makeText(
                        context,
                        result,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.registerState.collectLatest { result ->
                if (result.isNotEmpty()) {
                    Toast.makeText(
                        context,
                        result,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.alertUnsynced.collectLatest {
                if (it) {
                    showUnsyncedAlert()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.unsyncedAlertState.collectLatest {
                if (it == 0) {
                    binding.toolbarLayout.unsyncedCountText.visibility = View.GONE
                } else {
                    binding.toolbarLayout.unsyncedCountText.visibility = View.VISIBLE
                }
                binding.toolbarLayout.unsyncedCountText.text = it.toString()
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
                binding.progressBar.visibility = View.VISIBLE
                binding.deploymentsRecyclerView.visibility = View.GONE
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

    private fun showUnsyncedAlert() {
        unsyncedAlert =
            MaterialAlertDialogBuilder(requireContext(), R.style.BaseAlertDialog).apply {
                setTitle(getString(R.string.refresh_title))
                setMessage(getString(R.string.refresh_message))
                setPositiveButton(getString(R.string.continue_name)) { _, _ ->
                    viewModel.fetchFreshStreams(force = true, fromAlertUnsynced = true)
                }
                setNegativeButton(R.string.back) { _, _ ->
                    unsyncedAlert.dismiss()
                }
            }.create()
        unsyncedAlert.show()
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

    private fun showProjectList() {
        binding.toolbarLayout.expandMoreImageView.rotation = 180F
        listener.hideBottomAppBar()
        binding.projectRecyclerView.visibility = View.VISIBLE
        binding.projectSwipeRefreshView.visibility = View.VISIBLE
        if (state == DeploymentListState.LIST) {
            binding.deployGuardianButton.visibility = View.GONE
        }
    }

    private fun hideProjectList() {
        binding.toolbarLayout.expandMoreImageView.rotation = 0F
        listener.showBottomAppBar()
        binding.projectRecyclerView.visibility = View.GONE
        binding.projectSwipeRefreshView.visibility = View.GONE
        if (state == DeploymentListState.LIST) {
            binding.deployGuardianButton.visibility = View.VISIBLE
        }
    }

    override fun onProjectClicked(project: Project) {
        map?.clear()
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

    override fun onImageIconClicked(deploymentId: String) {
        viewModel.uploadImages(deploymentId)
    }

    override fun onRegisterClicked(registration: GuardianRegistration) {
        viewModel.register(registration)
    }

    override fun onItemClicked(streamId: Int) {
        DeploymentDetailActivity.startActivity(requireContext(), streamId)
    }

    override fun onMapReady(p0: GoogleMap) {
        setGoogleMap(p0, true)

        lifecycleScope.launch {
            viewModel.markers.collectLatest { markers ->
                map?.clear()
                setMarker(markers)
            }
        }

        p0.uiSettings.isZoomControlsEnabled = false
        fusedLocationClient()

        binding.toolbarLayout.changePageButton.setOnClickListener {
            if (state == DeploymentListState.LIST) {
                binding.mapLayout.visibility = View.VISIBLE
                binding.listLayout.visibility = View.GONE
                binding.deploymentRefreshView.visibility = View.GONE
                binding.deployGuardianButton.visibility = View.GONE
                binding.toolbarLayout.screenName.text = getString(R.string.deployments)
                state = DeploymentListState.MAP
                viewModel.setScreen(true)
                viewModel.addFilter(DeploymentListViewModel.FilterDeployment.ALL)
            } else {
                binding.mapLayout.visibility = View.GONE
                binding.listLayout.visibility = View.VISIBLE
                binding.deploymentRefreshView.visibility = View.VISIBLE
                binding.deployGuardianButton.visibility = View.VISIBLE
                binding.toolbarLayout.screenName.text = getString(R.string.map)
                state = DeploymentListState.LIST
                viewModel.setScreen(false)
                viewModel.addFilter(currentFilter)
            }
        }

        binding.toolbarLayout.changePageButton.performClick()

        lifecycleScope.launch {
            viewModel.currentLocationState.collectLatest { currentLoc ->
                currentLoc?.let {
                    val curLoc = LatLng(it.latitude, it.longitude)
                    setCurrentLocation(curLoc)
                }
            }
        }

        setSeeDetailCallback {
            DeploymentDetailActivity.startActivity(requireContext(), it)
        }

        binding.currentLocationButton.setOnClickListener {
            moveCamera(viewModel.currentLocationState.value)
        }
    }

    companion object {
        const val tag = "DeploymentListFragment"

        enum class DeploymentListState { LIST, MAP }

        fun newInstance(): DeploymentListFragment = DeploymentListFragment()
    }
}
