package org.rfcx.ranger.view.events

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_new_events.*
import kotlinx.android.synthetic.main.toolbar_project.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.data.remote.success
import org.rfcx.ranger.entity.project.Project
import org.rfcx.ranger.view.MainActivityEventListener
import org.rfcx.ranger.view.project.ProjectAdapter
import org.rfcx.ranger.view.project.ProjectOnClickListener

class NewEventsFragment : Fragment(), ProjectOnClickListener {
	private val viewModel: NewEventsViewModel by viewModel()
	private val projectAdapter by lazy { ProjectAdapter(this) }
	lateinit var listener: MainActivityEventListener
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as MainActivityEventListener)
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setHasOptionsMenu(true)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_new_events, container, false)
	}
	
	@SuppressLint("ResourceAsColor")
	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		inflater.inflate(R.menu.map_menu, menu)
		super.onCreateOptionsMenu(menu, inflater)
	}
	
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.mapView -> openMap()
		}
		return super.onOptionsItemSelected(item)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupToolbar()
		viewModel.fetchProjects()
		setOnClickListener()
		setRecyclerView()
		setObserver()
	}
	
	private fun setupToolbar() {
		(activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
	}
	
	private fun openMap() {
		Log.d("openMap", "openMap")
	}
	
	private fun setRecyclerView() {
		projectRecyclerView.apply {
			layoutManager = LinearLayoutManager(context)
			adapter = projectAdapter
			projectAdapter.items = viewModel.getProjectsFromLocal()
		}
		setProjectTitle(viewModel.getProjectName())
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
	
	companion object {
		const val tag = "NewEventsFragment"
		
		@JvmStatic
		fun newInstance() = NewEventsFragment()
	}
}
