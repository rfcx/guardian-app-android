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
import org.rfcx.ranger.util.logout
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
	private var project: Project? = null
	
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
		
		projectView.apply {
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
		
		selectProjectButton.setOnClickListener {
			val preferences = Preferences.getInstance(requireContext())
			preferences.putInt(Preferences.SELECTED_PROJECT, selectedProject)
			project?.let { it1 ->
				viewModel.setProjects(it1) {
					if (it) {
						Toast.makeText(context, "Subscribe Successful", Toast.LENGTH_LONG).show()
						
					} else {
						Toast.makeText(context, "Subscribe Failed", Toast.LENGTH_LONG).show()
					}
					listener.handleOpenPage()
				}
			}
		}
		
		logoutButton.setOnClickListener {
			requireContext().logout()
		}
	}
	
	private fun setObserver() {
		viewModel.projects.observe(viewLifecycleOwner, {
			it.success({
				projectSwipeRefreshView.isRefreshing = false
				if (viewModel.getProjectsFromLocal().isEmpty()) {
					noContentTextView.visibility = View.VISIBLE
				} else {
					noContentTextView.visibility = View.GONE
				}
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
		this.project = project
		selectedProject = project.id
		selectProjectButton.isEnabled = true
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
