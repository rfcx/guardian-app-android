package org.rfcx.incidents.view.events

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.data.preferences.Preferences
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.common.success
import org.rfcx.incidents.databinding.FragmentStreamsBinding
import org.rfcx.incidents.entity.CrashlyticsKey
import org.rfcx.incidents.entity.stream.MarkerDetail
import org.rfcx.incidents.entity.stream.MarkerItem
import org.rfcx.incidents.entity.stream.Project
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.service.EventNotification
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Crashlytics
import org.rfcx.incidents.util.LocationPermissions
import org.rfcx.incidents.util.Screen
import org.rfcx.incidents.util.isNetworkAvailable
import org.rfcx.incidents.util.isOnAirplaneMode
import org.rfcx.incidents.view.MainActivityEventListener
import org.rfcx.incidents.view.base.BaseMapFragment
import org.rfcx.incidents.view.events.adapter.ProjectAdapter
import org.rfcx.incidents.view.events.adapter.ProjectOnClickListener
import org.rfcx.incidents.view.events.adapter.StreamAdapter
import java.util.Date

class StreamsFragment :
    BaseMapFragment(),
    ProjectOnClickListener,
    SwipeRefreshLayout.OnRefreshListener,
        (Stream) -> Unit {

    companion object {
        const val tag = "EventsFragment"

        @JvmStatic
        fun newInstance() = StreamsFragment()
    }

    private var _binding: FragmentStreamsBinding? = null
    private val binding get() = _binding!!

    private val analytics by lazy { context?.let { Analytics(it) } }
    private val firebaseCrashlytics by lazy { Crashlytics() }

    private val viewModel: StreamsViewModel by viewModel()
    private val projectAdapter by lazy { ProjectAdapter(this) }
    private val streamAdapter by lazy { StreamAdapter(this) }
    lateinit var preferences: Preferences
    private val locationPermissions by lazy { LocationPermissions(requireActivity()) }

    private var locationManager: LocationManager? = null
    private var lastLocation: Location? = null
    private val locationListener = object : android.location.LocationListener {
        override fun onLocationChanged(p0: Location) {
            viewModel.saveLastTimeToKnowTheCurrentLocation(Date().time)

            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient()
            }
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
        override fun onProviderEnabled(p0: String) {}
        override fun onProviderDisabled(p0: String) {}
    }

    private var isShowMapIcon = true
    lateinit var listener: MainActivityEventListener
    private lateinit var localBroadcastManager: LocalBroadcastManager

    private lateinit var unsyncedAlert: AlertDialog

    private val streamIdReceived = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val streamId = intent.getStringExtra(EventNotification.INTENT_KEY_STREAM_ID)
            if (streamId != null) {
                viewModel.refreshStreams(force = true)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = (context as MainActivityEventListener)
        localBroadcastManager = LocalBroadcastManager.getInstance(context)
        val actionReceiver = IntentFilter()
        actionReceiver.addAction("haveNewEvent")
        localBroadcastManager.registerReceiver(streamIdReceived, actionReceiver)
    }

    override fun onDetach() {
        super.onDetach()
        localBroadcastManager.unregisterReceiver(streamIdReceived)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStreamsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapView!!.getMapAsync(this)
        preferences = Preferences.getInstance(requireContext())

        // Show loading indicator for first time
        isShowProgressBar()

        setupToolbar()
        setOnClickListener()
        setObserver()
        setRecyclerView()
        onClickCurrentLocationButton()

        binding.refreshView.apply {
            setOnRefreshListener(this@StreamsFragment)
            setColorSchemeResources(R.color.colorPrimary)
        }

        lifecycleScope.launch {
            viewModel.alertUnsynced.collectLatest {
                if (it) {
                    showUnsyncedAlert()
                }
            }
        }

        viewModel.refreshProjects()
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

    private fun onClickCurrentLocationButton() {
        binding.currentLocationButton.setOnClickListener {
            locationPermissions.check { allow ->
                if (allow) {
                    fusedLocationClient()
                    moveCamera(getLastLocation())
                } else {
                    getLocation()
                }
            }
        }
    }

    private fun setRecyclerView() {
        binding.projectRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = projectAdapter
        }

        val streamsLayoutManager = LinearLayoutManager(context)
        binding.streamRecyclerView.apply {
            layoutManager = streamsLayoutManager
            adapter = streamAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val visibleItemCount = streamsLayoutManager.childCount
                    val total = streamsLayoutManager.itemCount
                    val firstVisibleItemPosition = streamsLayoutManager.findFirstVisibleItemPosition()
                    if (!binding.refreshView.isRefreshing &&
                        (visibleItemCount + firstVisibleItemPosition) >= total &&
                        firstVisibleItemPosition >= 0 && !viewModel.isLoadingMore
                    ) {
                        viewModel.refreshStreams(force = true, total)
                    }
                }
            })
        }
    }

    private fun setOnClickListener() {
        binding.toolbarLayout.projectTitleLayout.setOnClickListener {
            projectNameSelected()
        }

        binding.projectSwipeRefreshView.apply {
            setOnRefreshListener {
                isRefreshing = true
                when {
                    requireContext().isOnAirplaneMode() || !requireContext().isNetworkAvailable() -> {
                        isRefreshing = false
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.project_could_not_refreshed) + " " + getString(R.string.pls_off_air_plane_mode),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    else -> {
                        viewModel.refreshProjects(true)
                    }
                }
            }
            setColorSchemeResources(R.color.colorPrimary)
        }
    }

    private fun projectNameSelected() {
        if (binding.projectRecyclerView.visibility == View.VISIBLE) {
            binding.toolbarLayout.expandMoreImageView.rotation = 0F
            listener.showBottomAppBar()
            binding.projectRecyclerView.visibility = View.GONE
            binding.projectSwipeRefreshView.visibility = View.GONE
        } else {
            binding.toolbarLayout.expandMoreImageView.rotation = 180F
            listener.hideBottomAppBar()
            binding.projectRecyclerView.visibility = View.VISIBLE
            binding.projectSwipeRefreshView.visibility = View.VISIBLE
        }
    }

    override fun onProjectClicked(project: Project) {
        viewModel.selectProject(project.id)
        firebaseCrashlytics.setCustomKey(CrashlyticsKey.OnSelectedProject.key, project.id + " : " + project.name)

        isShowNotHaveStreams(false)
        isShowNotHaveIncident(false)
        binding.streamLayout.visibility = View.GONE
        binding.toolbarLayout.expandMoreImageView.rotation = 0F

        isShowProgressBar()

        listener.showBottomAppBar()
        binding.projectRecyclerView.visibility = View.GONE
        binding.projectSwipeRefreshView.visibility = View.GONE

        when {
            requireContext().isOnAirplaneMode() -> {
                Toast.makeText(requireContext(), getString(R.string.pls_off_air_plane_mode), Toast.LENGTH_LONG).show()
            }

            !requireContext().isNetworkAvailable() -> {
                Toast.makeText(requireContext(), getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setObserver() {

        viewModel.selectedProject.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> binding.toolbarLayout.projectTitleTextView.text = result.data.name
                else -> binding.toolbarLayout.projectTitleTextView.text = ""
            }
        }

        viewModel.projects.observe(viewLifecycleOwner) { result ->
            result.success({ projects ->
                binding.projectSwipeRefreshView.isRefreshing = false
                projectAdapter.items = projects
                projectAdapter.notifyDataSetChanged()
            }, {
                binding.projectSwipeRefreshView.isRefreshing = false
                Toast.makeText(
                    context,
                    it.message
                        ?: getString(R.string.something_is_wrong),
                    Toast.LENGTH_LONG
                ).show()
            }, {
            })
        }

        viewModel.streams.observe(viewLifecycleOwner) { it ->
            it.success({ streams ->
                streamAdapter.items = streams.filter { it.lastIncident != null }
                streamAdapter.notifyDataSetChanged()
                binding.streamLayout.visibility = View.VISIBLE
                binding.refreshView.isRefreshing = false
                isShowProgressBar(false)

                val list = mutableListOf<MarkerItem>()
                streams.map { stream ->
                    val data =
                        MarkerDetail(
                            stream.id,
                            stream.name,
                            stream.externalId ?: "",
                            distanceLabel(lastLocation, stream),
                            stream.lastIncident?.events?.size ?: 0,
                            false,
                            null
                        )
                    val item = MarkerItem(
                        stream.latitude,
                        stream.longitude,
                        stream.name,
                        Gson().toJson(data)
                    )
                    list.add(item)
                }
                setMarker(list)

                if (streams.isEmpty()) {
                    isShowNotHaveIncident(false)
                    isShowNotHaveStreams(binding.mapView.visibility == View.GONE && binding.progressBar.visibility == View.GONE)
                } else if (streams.none { it.lastIncident != null }) {
                    isShowNotHaveStreams(false)
                    isShowNotHaveIncident(binding.mapView.visibility == View.GONE && binding.progressBar.visibility == View.GONE)
                } else {
                    isShowNotHaveStreams(false)
                    isShowNotHaveIncident(false)
                }
            }, {
                binding.refreshView.isRefreshing = false
                isShowProgressBar(false)
            }, {
                binding.refreshView.isRefreshing = false
                binding.streamLayout.visibility = View.GONE
                isShowProgressBar()
            })
        }
    }

    override fun onLockImageClicked() {
        Toast.makeText(context, R.string.not_have_permission, Toast.LENGTH_LONG).show()
    }

    override fun invoke(stream: Stream) {
        firebaseCrashlytics.setCustomKey(CrashlyticsKey.OnClickStreamNewEventPage.key, "Stream name: " + stream.name + "/ Project id: " + stream.projectId)
        listener.openStreamDetail(stream.externalId ?: "", null)
    }

    private fun setupToolbar() {
        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.toolbarLayout.toolbar)

        binding.toolbarLayout.changePageImageView.setOnClickListener {
            if (isShowMapIcon) {
                analytics?.trackScreen(Screen.MAP)
                isShowNotHaveStreams(false)
                isShowNotHaveIncident(false)
                binding.toolbarLayout.changePageImageView.setImageResource(R.drawable.ic_view_list)
                binding.mapView.visibility = View.VISIBLE
                binding.refreshView.visibility = View.GONE
                binding.currentLocationButton.visibility = View.VISIBLE
                fusedLocationClient()
            } else {
                binding.toolbarLayout.changePageImageView.setImageResource(R.drawable.ic_map)
                binding.mapView.visibility = View.GONE
                binding.refreshView.visibility = View.VISIBLE
                binding.currentLocationButton.visibility = View.GONE
            }
            isShowMapIcon = !isShowMapIcon
        }
    }

    private fun isShowProgressBar(show: Boolean = true) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun isShowNotHaveStreams(show: Boolean) {
        binding.notHaveStreamsGroupView.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun isShowNotHaveIncident(show: Boolean) {
        binding.notHaveIncidentGroupView.visibility = if (show) View.VISIBLE else View.GONE
    }

    /* ------------------- vv Setup Map vv ------------------- */

    override fun onMapReady(mMap: GoogleMap) {
        map = mMap
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.uiSettings.isZoomControlsEnabled = false
        setGoogleMap(mMap, true)
        fusedLocationClient()

        setOpenStreamDetailCallback { name, serverId, distance ->
            firebaseCrashlytics.setCustomKey(CrashlyticsKey.OnClickStreamMapPage.key, name)
            listener.openStreamDetail(serverId, distance)
        }
    }

    private fun getLocation() {
        if (!isAdded || isDetached) return

        // Check if permissions are enabled and if not request
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager?.removeUpdates(locationListener)
            locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
            try {
                lastLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                lastLocation?.let {
                    listener.setCurrentLocation(it)
                    viewModel.saveLastTimeToKnowTheCurrentLocation(Date().time)
                    moveCamera(it)
                }
                fusedLocationClient()
            } catch (ex: SecurityException) {
                ex.printStackTrace()
            } catch (ex: IllegalArgumentException) {
                ex.printStackTrace()
            }
        } else {
            locationPermissions.check { }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.refreshStreams()

            val projectId = preferences.getString(Preferences.SELECTED_PROJECT)
            projectId?.let { viewModel.selectProject(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        analytics?.trackScreen(Screen.NEW_EVENTS)
    }

    override fun onRefresh() {
        if (context.isNetworkAvailable()) {
            viewModel.fetchFreshStreams(force = true)
        } else {
            binding.refreshView.isRefreshing = false
            Toast.makeText(requireContext(), getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show()
        }
    }

    private fun distanceLabel(origin: Location?, destination: Stream): Double {
        if (origin == null) return 0.0
        return SphericalUtil.computeDistanceBetween(LatLng(origin.latitude, origin.longitude), LatLng(destination.latitude, destination.longitude))
    }
}
