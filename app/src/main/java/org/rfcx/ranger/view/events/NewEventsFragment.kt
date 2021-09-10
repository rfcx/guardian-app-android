package org.rfcx.ranger.view.events

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.fragment_new_events.*
import kotlinx.android.synthetic.main.toolbar_project.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.data.remote.success
import org.rfcx.ranger.entity.project.Project
import org.rfcx.ranger.view.MainActivityEventListener
import org.rfcx.ranger.view.events.adapter.GuardianItemAdapter
import org.rfcx.ranger.view.events.adapter.GuardianModel
import org.rfcx.ranger.view.project.ProjectAdapter
import org.rfcx.ranger.view.project.ProjectOnClickListener

class NewEventsFragment : Fragment(), OnMapReadyCallback, PermissionsListener, ProjectOnClickListener, (GuardianModel) -> Unit {
	private val viewModel: NewEventsViewModel by viewModel()
	private val projectAdapter by lazy { ProjectAdapter(this) }
	private val nearbyAdapter by lazy { GuardianItemAdapter(this) }
	private val othersAdapter by lazy { GuardianItemAdapter(this) }
	
	private lateinit var mapView: MapView
	private var mapBoxMap: MapboxMap? = null
	private var locationManager: LocationManager? = null
	private var lastLocation: Location? = null
	private var permissionsManager: PermissionsManager = PermissionsManager(this)
	private val locationListener = object : android.location.LocationListener {
		override fun onLocationChanged(p0: Location) {
			moveCameraToCurrentLocation(p0)
			if (PermissionsManager.areLocationPermissionsGranted(context)) {
				mapBoxMap?.style?.let { style -> enableLocationComponent(style) }
			}
		}
		
		override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
		override fun onProviderEnabled(p0: String) {}
		override fun onProviderDisabled(p0: String) {}
	}
	
	private var isShowMapIcon = true
	lateinit var listener: MainActivityEventListener
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as MainActivityEventListener)
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		context?.let { Mapbox.getInstance(it, getString(R.string.mapbox_token)) }
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_new_events, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		mapView = view.findViewById(R.id.mapView)
		mapView.onCreate(savedInstanceState)
		mapView.getMapAsync(this)
		
		setupToolbar()
		viewModel.fetchProjects()
		setOnClickListener()
		setRecyclerView()
		setObserver()
	}
	
	override fun onMapReady(mapboxMap: MapboxMap) {
		mapBoxMap = mapboxMap
		mapboxMap.setStyle(Style.OUTDOORS) { style ->
			mapboxMap.uiSettings.isAttributionEnabled = false
			mapboxMap.uiSettings.isLogoEnabled = false
			getLocation()
		}
	}
	
	private fun enableLocationComponent(style: Style) {
		val context = context ?: return
		val mapboxMap = mapBoxMap ?: return
		
		// Check if permissions are enabled and if not request
		if (PermissionsManager.areLocationPermissionsGranted(context)) {
			
			// Create and customize the LocationComponent's options
			val customLocationComponentOptions = LocationComponentOptions.builder(context)
					.trackingGesturesManagement(true)
					.accuracyColor(ContextCompat.getColor(context, R.color.colorPrimary))
					.build()
			
			val locationComponentActivationOptions = LocationComponentActivationOptions.builder(context, style)
					.locationComponentOptions(customLocationComponentOptions)
					.build()
			
			// Get an instance of the LocationComponent and then adjust its settings
			mapboxMap.locationComponent.apply {
				// Activate the LocationComponent with options
				activateLocationComponent(locationComponentActivationOptions)
				// Enable to make the LocationComponent visible
				if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
						&& ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					return
				}
				isLocationComponentEnabled = true
				
				// Set the LocationComponent's camera mode
				cameraMode = CameraMode.TRACKING
				// Set the LocationComponent's render mode
				renderMode = RenderMode.COMPASS
			}
		} else {
			permissionsManager = PermissionsManager(this)
			permissionsManager.requestLocationPermissions(activity)
		}
	}
	
	private fun getLocation() {
		if (!isAdded || isDetached) return
		val style = mapBoxMap?.style ?: return
		
		// Check if permissions are enabled and if not request
		if (PermissionsManager.areLocationPermissionsGranted(context)) {
			locationManager?.removeUpdates(locationListener)
			locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
			try {
				lastLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
				lastLocation?.let { moveCameraToCurrentLocation(it) }
				enableLocationComponent(style)
			} catch (ex: SecurityException) {
				ex.printStackTrace()
			} catch (ex: IllegalArgumentException) {
				ex.printStackTrace()
			}
		} else {
			permissionsManager = PermissionsManager(this)
			permissionsManager.requestLocationPermissions(activity)
		}
	}
	
	private fun moveCameraToCurrentLocation(location: Location) {
		val latLng = LatLng(location.latitude, location.longitude)
		mapBoxMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0))
	}
	
	private fun setupToolbar() {
		(activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
		
		changePageImageView.setOnClickListener {
			if (isShowMapIcon) {
				changePageImageView.setImageResource(R.drawable.ic_view_list)
				mapView.visibility = View.VISIBLE
				guardianListScrollView.visibility = View.GONE
				mapBoxMap?.style?.let { style -> enableLocationComponent(style) }
			} else {
				changePageImageView.setImageResource(R.drawable.ic_map)
				mapView.visibility = View.GONE
				guardianListScrollView.visibility = View.VISIBLE
			}
			isShowMapIcon = !isShowMapIcon
		}
	}
	
	private fun setRecyclerView() {
		projectRecyclerView.apply {
			layoutManager = LinearLayoutManager(context)
			adapter = projectAdapter
			projectAdapter.items = viewModel.getProjectsFromLocal()
		}
		setProjectTitle(viewModel.getProjectName())
		
		nearbyRecyclerView.apply {
			layoutManager = LinearLayoutManager(context)
			adapter = nearbyAdapter
			nearbyAdapter.items = viewModel.nearbyGuardians
		}
		
		othersRecyclerView.apply {
			layoutManager = LinearLayoutManager(context)
			adapter = othersAdapter
			othersAdapter.items = viewModel.othersGuardians
		}
	}
	
	private fun setOnClickListener() {
		projectTitleLayout.setOnClickListener {
			setOnClickProjectName()
		}
		
		projectSwipeRefreshView.apply {
			setOnRefreshListener {
				viewModel.fetchProjects()
				isRefreshing = true
			}
			setColorSchemeResources(R.color.colorPrimary)
		}
	}
	
	private fun setOnClickProjectName() {
		listener.hideBottomAppBar()
		projectRecyclerView.visibility = View.VISIBLE
		projectSwipeRefreshView.visibility = View.VISIBLE
	}
	
	override fun onClicked(project: Project) {
		listener.showBottomAppBar()
		projectRecyclerView.visibility = View.GONE
		projectSwipeRefreshView.visibility = View.GONE
		viewModel.setProjectSelected(project.id)
		setProjectTitle(project.name)
	}
	
	@SuppressLint("NotifyDataSetChanged")
	private fun setObserver() {
		viewModel.projects.observe(viewLifecycleOwner, { it ->
			it.success({
				projectSwipeRefreshView.isRefreshing = false
				projectAdapter.items = listOf()
				projectAdapter.items = viewModel.getProjectsFromLocal()
				projectAdapter.notifyDataSetChanged()
			}, {
				projectSwipeRefreshView.isRefreshing = false
				Toast.makeText(context, it.message
						?: getString(R.string.something_is_wrong), Toast.LENGTH_LONG).show()
			}, {
			})
		})
	}
	
	private fun setProjectTitle(str: String) {
		projectTitleTextView.text = str
	}
	
	override fun onLockImageClicked() {
		Toast.makeText(context, R.string.not_have_permission, Toast.LENGTH_LONG).show()
	}
	
	override fun invoke(guardian: GuardianModel) {
		listener.openGuardianEventDetail()
	}
	
	override fun onResume() {
		super.onResume()
		mapView.onResume()
	}
	
	override fun onStart() {
		super.onStart()
		mapView.onStart()
	}
	
	override fun onStop() {
		super.onStop()
		mapView.onStop()
	}
	
	override fun onLowMemory() {
		super.onLowMemory()
		mapView.onLowMemory()
	}
	
	override fun onPause() {
		super.onPause()
		mapView.onPause()
	}
	
	companion object {
		const val tag = "NewEventsFragment"
		
		@JvmStatic
		fun newInstance() = NewEventsFragment()
	}
	
	override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {}
	
	override fun onPermissionResult(granted: Boolean) {
		val style = mapBoxMap?.style ?: return
		val context = context ?: return
		if (granted) {
			enableLocationComponent(style)
		} else {
			Toast.makeText(context, R.string.location_permission_msg, Toast.LENGTH_LONG).show()
		}
	}
}
