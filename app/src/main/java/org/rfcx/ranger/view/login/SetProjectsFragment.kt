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
import org.rfcx.ranger.entity.OnProjectsItemClickListener
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.isNetworkAvailable
import org.rfcx.ranger.util.logout
import java.util.*

class SetProjectsFragment : Fragment(), OnProjectsItemClickListener, SwipeRefreshLayout.OnRefreshListener {
	companion object {
		@JvmStatic
		fun newInstance() = SetProjectsFragment()
	}
	
	lateinit var listener: LoginListener
	private val viewModel: SetProjectsViewModel by viewModel()
	private val projectsAdapter by lazy { ProjectsAdapter(this) }
	private var projectsItem: List<ProjectsItem>? = null
	private var subscribedProjects: ArrayList<String> = arrayListOf()
	
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
			adapter = projectsAdapter
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
			val id = viewModel.getProjectLocalId(subscribedProjects.random())
			preferences.putInt(Preferences.SELECTED_PROJECT, id)
			listener.handleOpenPage()
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
				projectsItem = viewModel.getProjectsFromLocal().map { project ->
					ProjectsItem(project, getSubscribedProject()?.contains(project.serverId)
							?: false)
				}
				projectsItem?.let { items -> projectsAdapter.items = items }
			}, {
				projectSwipeRefreshView.isRefreshing = false
				Toast.makeText(context, R.string.something_is_wrong, Toast.LENGTH_LONG).show()
			}, {
				projectSwipeRefreshView.isRefreshing = true
			})
		})
	}
	
	private fun showToast(message: String) {
		Toast.makeText(context, message, Toast.LENGTH_LONG).show()
	}
	
	override fun onRefresh() {
		viewModel.fetchProjects()
	}
	
	override fun onItemClick(item: ProjectsItem, position: Int) {
		if (item.selected) {
			viewModel.unsubscribeProject(item.project) { status ->
				if (!status) {
					projectsItem?.let { items ->
						items[position].selected = !items[position].selected
						projectsAdapter.items = items
					}
					showToast(getString(R.string.failed_unsubscribe_receive_notification, item.project.name))
				} else {
					subscribedProjects.remove(item.project.serverId ?: "")
					saveSubscribedProject(subscribedProjects)
				}
			}
		} else {
			viewModel.setProjectsAndSubscribe(item.project) { status ->
				if (!status) {
					projectsItem?.let { items ->
						items[position].selected = !items[position].selected
						projectsAdapter.items = items
					}
					showToast(getString(R.string.failed_receive_notification, item.project.name))
				} else {
					subscribedProjects.add(item.project.serverId ?: "")
					saveSubscribedProject(subscribedProjects)
				}
			}
		}
		projectsItem?.let { items ->
			items[position].selected = !items[position].selected
			projectsAdapter.items = items
		}
		selectProjectButton.isEnabled = true
	}
	
	private fun saveSubscribedProject(subscribedProjects: ArrayList<String>) {
		val preferenceHelper = Preferences.getInstance(requireContext())
		preferenceHelper.remove(Preferences.SUBSCRIBED_PROJECTS)
		preferenceHelper.putArrayList(Preferences.SUBSCRIBED_PROJECTS, subscribedProjects)
	}
	
	private fun getSubscribedProject(): ArrayList<String>? {
		val preferenceHelper = Preferences.getInstance(requireContext())
		return preferenceHelper.getArrayList(Preferences.SUBSCRIBED_PROJECTS)
	}
	
	override fun onLockImageClicked() {
		showToast(getString(R.string.not_have_permission))
	}
}
