package org.rfcx.ranger.view.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.fragment_set_projects.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.data.remote.success
import org.rfcx.ranger.entity.project.Project
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.isNetworkAvailable
import org.rfcx.ranger.view.project.ProjectAdapter
import org.rfcx.ranger.view.project.ProjectOnClickListener

class SetProjectsFragment : Fragment(), ProjectOnClickListener, SwipeRefreshLayout.OnRefreshListener {
	companion object {
		@JvmStatic
		fun newInstance() = SetProjectsFragment()
	}
	
	lateinit var listener: LoginListener
	private val viewModel: SetProjectsViewModel by viewModel()
	private val projectAdapter by lazy { ProjectAdapter(this) }
	private var selectedProject = -1
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as LoginListener)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_set_projects, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		projectsRecycler.apply {
			layoutManager = LinearLayoutManager(context)
			adapter = projectAdapter
		}
		
		projectSwipeRefreshView.apply {
			setOnRefreshListener(this@SetProjectsFragment)
			setColorSchemeResources(R.color.colorPrimary)
		}
		
		if (requireActivity().isNetworkAvailable()) {
			setObserver()
		} else {
			showToast(getString(R.string.network_not_available))
		}
		
		submitProjectsButton.setOnClickListener {
			val preferences = Preferences.getInstance(requireContext())
			preferences.putInt(Preferences.SELECTED_PROJECT, selectedProject)
			listener.handleOpenPage()
			// viewModel.setProjects(project)  for subscribe cloud messaging but now the notification not yet available
		}
	}
	
	private fun setObserver() {
		viewModel.projects.observe(viewLifecycleOwner, {
			it.success({
				projectSwipeRefreshView.isRefreshing = false
				projectAdapter.items = viewModel.getProjectsFromLocal()
			}, {
				projectSwipeRefreshView.isRefreshing = false
				Toast.makeText(context, R.string.something_is_wrong, Toast.LENGTH_LONG).show()
			}, {
				projectSwipeRefreshView.isRefreshing = true
			})
		})
	}
	
	override fun onClicked(project: Project) {
		selectedProject = project.id
		submitProjectsButton.isEnabled = true
	}
	
	override fun onLockImageClicked() {
		showToast(getString(R.string.not_have_permission))
	}
	
	private fun showToast(message: String) {
		Toast.makeText(context, message, Toast.LENGTH_LONG).show()
	}
	
	override fun onRefresh() {
		viewModel.fetchProjects()
	}
}
