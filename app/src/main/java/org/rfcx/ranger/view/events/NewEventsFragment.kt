package org.rfcx.ranger.view.events

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mapbox.mapboxsdk.Mapbox
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

class NewEventsFragment : Fragment(), OnMapReadyCallback, ProjectOnClickListener, (GuardianModel) -> Unit {
	private val viewModel: NewEventsViewModel by viewModel()
	private val projectAdapter by lazy { ProjectAdapter(this) }
	private val nearbyAdapter by lazy { GuardianItemAdapter(this) }
	private val othersAdapter by lazy { GuardianItemAdapter(this) }
	
	private lateinit var mapView: MapView
	private var mapBoxMap: MapboxMap? = null
	
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
		mapboxMap.setStyle(Style.OUTDOORS) {
			mapboxMap.uiSettings.isAttributionEnabled = false
			mapboxMap.uiSettings.isLogoEnabled = false
		}
	}
	
	private fun setupToolbar() {
		(activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
		
		changePageImageView.setOnClickListener {
			if (isShowMapIcon) {
				changePageImageView.setImageResource(R.drawable.ic_view_list)
				mapView.visibility = View.VISIBLE
				guardianListScrollView.visibility = View.GONE
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
		Toast.makeText(context, guardian.name, Toast.LENGTH_SHORT).show()
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
}
