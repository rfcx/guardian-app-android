package org.rfcx.incidents.view.report.draft

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_draft_reports.*
import kotlinx.android.synthetic.main.toolbar_project.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.project.Project
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.response.SyncState
import org.rfcx.incidents.util.*
import org.rfcx.incidents.view.MainActivityEventListener
import org.rfcx.incidents.view.MainActivityViewModel
import org.rfcx.incidents.view.project.ProjectAdapter
import org.rfcx.incidents.view.project.ProjectOnClickListener

class DraftReportsFragment : Fragment(), ReportOnClickListener, ProjectOnClickListener {
	private val analytics by lazy { context?.let { Analytics(it) } }
	private val viewModel: MainActivityViewModel by viewModel()
	private val reportsAdapter by lazy { DraftReportsAdapter(this) }
	private val projectAdapter by lazy { ProjectAdapter(this) }
	
	lateinit var listener: MainActivityEventListener
	lateinit var preferences: Preferences
	private var streams = listOf<String>()
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as MainActivityEventListener)
	}
	
	override fun onResume() {
		super.onResume()
		analytics?.trackScreen(Screen.DRAFT_REPORTS)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_draft_reports, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		preferences = Preferences.getInstance(requireContext())
		val projectId = preferences.getInt(Preferences.SELECTED_PROJECT, -1)
		setProjectTitle(viewModel.getProjectName(projectId))
		changePageImageView.visibility = View.GONE
		
		setObserve()
		setRecyclerView()
		setOnClickListener()
	}
	
	private fun setRecyclerView() {
		projectRecyclerView.apply {
			layoutManager = LinearLayoutManager(context)
			adapter = projectAdapter
			projectAdapter.items = viewModel.getProjectsFromLocal()
		}
		
		draftReportsRecyclerView.apply {
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
	
	private fun setObserve() {
		viewModel.getResponses().observe(viewLifecycleOwner, { responses ->
			notHaveDraftReportsGroupView.visibility = if (responses.isEmpty()) View.VISIBLE else View.GONE
			setStreamsOfProject()
			reportsAdapter.items = responses.sortedByDescending { r -> r.startedAt }.filter { r -> r.syncState == SyncState.UNSENT.value && streams.contains(r.streamId) }
		})
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
	
	override fun onClickedDelete(response: Response) {
		Toast.makeText(context, "On click delete ${response.streamName}", Toast.LENGTH_SHORT).show()
	}
	
	override fun onClickedItem(response: Response) {
		if (response.syncState == SyncState.SENT.value) {
			Toast.makeText(context, getString(R.string.can_not_open_the_report), Toast.LENGTH_SHORT).show()
		} else {
			listener.openCreateResponse(response)
		}
	}
	
	private fun setStreamsOfProject() {
		val projectId = preferences.getInt(Preferences.SELECTED_PROJECT, -1)
		val projectCoreId = viewModel.getProjectById(projectId)?.serverId
		projectCoreId?.let {
			streams = viewModel.getStreamsByProjectCoreId(it).map { s -> s.serverId }
		}
	}
	
	override fun onHiddenChanged(hidden: Boolean) {
		super.onHiddenChanged(hidden)
		if (!hidden) {
			val projectId = preferences.getInt(Preferences.SELECTED_PROJECT, -1)
			setProjectTitle(viewModel.getProjectName(projectId))
			
			setStreamsOfProject()
			reportsAdapter.items = viewModel.getResponsesFromLocal().sortedByDescending { r -> r.startedAt }.filter { r -> r.syncState == SyncState.UNSENT.value && streams.contains(r.streamId) }
			
		}
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
				setStreamsOfProject()
				reportsAdapter.items = viewModel.getResponsesFromLocal().sortedByDescending { r -> r.startedAt }.filter { r -> r.syncState == SyncState.UNSENT.value && streams.contains(r.streamId) }
			}
		}
		setProjectTitle(project.name)
	}
	
	override fun onLockImageClicked() {
		Toast.makeText(context, R.string.not_have_permission, Toast.LENGTH_LONG).show()
	}
	
	private fun setProjectTitle(str: String) {
		projectTitleTextView.text = str
	}
	
	companion object {
		const val tag = "DraftReportsFragment"
		
		@JvmStatic
		fun newInstance() = DraftReportsFragment()
	}
}
