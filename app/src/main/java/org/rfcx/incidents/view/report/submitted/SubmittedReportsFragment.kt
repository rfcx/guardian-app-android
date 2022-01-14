package org.rfcx.incidents.view.report.submitted

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_submitted_reports.*
import kotlinx.android.synthetic.main.toolbar_project.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.data.remote.success
import org.rfcx.incidents.entity.project.Project
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.response.SyncState
import org.rfcx.incidents.util.*
import org.rfcx.incidents.view.MainActivityEventListener
import org.rfcx.incidents.view.MainActivityViewModel
import org.rfcx.incidents.view.project.ProjectAdapter
import org.rfcx.incidents.view.project.ProjectOnClickListener

class SubmittedReportsFragment : Fragment(), SubmittedReportsOnClickListener, ProjectOnClickListener {
	private val analytics by lazy { context?.let { Analytics(it) } }
	private val viewModel: MainActivityViewModel by viewModel()
	private val reportsAdapter by lazy { SubmittedReportsAdapter(this) }
	private val projectAdapter by lazy { ProjectAdapter(this) }
	lateinit var listener: MainActivityEventListener
	lateinit var preferences: Preferences
	private var streams = listOf<String>()
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as MainActivityEventListener)
	}
	
	override fun onHiddenChanged(hidden: Boolean) {
		super.onHiddenChanged(hidden)
		if (!hidden) {
			val projectId = preferences.getInt(Preferences.SELECTED_PROJECT, -1)
			setProjectTitle(viewModel.getProjectName(projectId))
			
			streamsByProject()
			reportsAdapter.items = viewModel.getResponsesFromLocal().sortedByDescending { r -> r.submittedAt }.filter { r -> r.syncState == SyncState.SENT.value && streams.contains(r.streamId) }
		}
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_submitted_reports, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		preferences = Preferences.getInstance(requireContext())
		setRecyclerView()
		setObserve()
		setOnClickListener()
		changePageImageView.visibility = View.GONE
		
		val projectId = preferences.getInt(Preferences.SELECTED_PROJECT, -1)
		setProjectTitle(viewModel.getProjectName(projectId))
	}
	
	private fun setRecyclerView() {
		projectRecyclerView.apply {
			layoutManager = LinearLayoutManager(context)
			adapter = projectAdapter
			projectAdapter.items = viewModel.getProjectsFromLocal()
		}
		
		submittedReportsRecyclerView.apply {
			layoutManager = LinearLayoutManager(context)
			adapter = reportsAdapter
		}
	}
	
	private fun setOnClickListener() {
		projectTitleLayout.setOnClickListener {
			setOnClickProjectName()
		}
		
		projectSwipeRefreshView.apply {
			setOnRefreshListener {
				isRefreshing = true
				when {
					requireContext().isOnAirplaneMode() -> {
						isRefreshing = false
						requireContext().showToast(getString(R.string.project_could_not_refreshed) + " " + getString(R.string.pls_off_air_plane_mode))
					}
					!requireContext().isNetworkAvailable() -> {
						isRefreshing = false
						requireContext().showToast(getString(R.string.project_could_not_refreshed) + " " + getString(R.string.no_internet_connection))
					}
					else -> {
						viewModel.fetchProjects()
					}
				}
			}
			setColorSchemeResources(R.color.colorPrimary)
		}
	}
	
	private fun setOnClickProjectName() {
		if (projectRecyclerView.visibility == View.VISIBLE) {
			expandMoreImageView.rotation = 0F
			listener.showBottomAppBar()
			projectRecyclerView.visibility = View.GONE
			projectSwipeRefreshView.visibility = View.GONE
		} else {
			expandMoreImageView.rotation = 180F
			listener.hideBottomAppBar()
			projectRecyclerView.visibility = View.VISIBLE
			projectSwipeRefreshView.visibility = View.VISIBLE
		}
	}
	
	override fun onResume() {
		super.onResume()
		analytics?.trackScreen(Screen.SUBMITTED_REPORTS)
	}
	
	private fun streamsByProject() {
		val projectId = preferences.getInt(Preferences.SELECTED_PROJECT, -1)
		val projectCoreId = viewModel.getProjectById(projectId)?.serverId
		projectCoreId?.let {
			streams = viewModel.getStreamsByProjectCoreId(it).map { s -> s.serverId }
		}
	}
	
	@SuppressLint("NotifyDataSetChanged")
	private fun setObserve() {
		viewModel.getResponses().observe(viewLifecycleOwner, { res ->
			notHaveSubmittedReportsGroupView.visibility = if (res.isEmpty()) View.VISIBLE else View.GONE
			streamsByProject()
			reportsAdapter.items = res.sortedByDescending { r -> r.submittedAt }.filter { r -> r.syncState == SyncState.SENT.value && streams.contains(r.streamId) }
			reportsAdapter.notifyDataSetChanged()
		})
		
		viewModel.getProjectsFromRemote.observe(viewLifecycleOwner, { it ->
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
	
	override fun onClicked(project: Project) {
		expandMoreImageView.rotation = 0F
		
		listener.showBottomAppBar()
		projectRecyclerView.visibility = View.GONE
		projectSwipeRefreshView.visibility = View.GONE
		viewModel.setProjectSelected(project.id)
		
		when {
			requireContext().isOnAirplaneMode() -> {
				requireContext().showToast(getString(R.string.pls_off_air_plane_mode))
			}
			!requireContext().isNetworkAvailable() -> {
				requireContext().showToast(getString(R.string.no_internet_connection))
			}
			else -> {
				streamsByProject()
				reportsAdapter.items = viewModel.getResponsesFromLocal().sortedByDescending { r -> r.submittedAt }.filter { r -> r.syncState == SyncState.SENT.value && streams.contains(r.streamId) }
			}
		}
		setProjectTitle(project.name)
	}
	
	private fun setProjectTitle(str: String) {
		projectTitleTextView.text = str
	}
	
	override fun onLockImageClicked() {
		Toast.makeText(context, R.string.not_have_permission, Toast.LENGTH_LONG).show()
	}
	
	override fun onClickedItem(response: Response) {
		if (response.syncState == SyncState.SENT.value) {
			response.guid?.let { listener.openDetailResponse(it) }
		}
	}
	
	companion object {
		const val tag = "SubmittedReportsFragment"
		
		@JvmStatic
		fun newInstance() = SubmittedReportsFragment()
	}
}
