package org.rfcx.incidents.view.login

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
import org.rfcx.incidents.R
import org.rfcx.incidents.data.remote.success
import org.rfcx.incidents.entity.OnProjectsItemClickListener
import org.rfcx.incidents.util.*

class SetProjectsFragment : Fragment(), OnProjectsItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    companion object {
        @JvmStatic
        fun newInstance() = SetProjectsFragment()
    }

    private val analytics by lazy { context?.let { Analytics(it) } }

    lateinit var listener: LoginListener
    private val viewModel: SetProjectsViewModel by viewModel()
    private val projectsAdapter by lazy { ProjectsAdapter(this) }
    private var projectsItem: List<ProjectsItem>? = null
    private var subscribedProjects: ArrayList<String> = arrayListOf()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = (context as LoginListener)
    }

    override fun onResume() {
        super.onResume()
        analytics?.trackScreen(Screen.SUBSCRIBE_PROJECTS)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        selectProjectButton.text = getString(R.string.skip)

        selectProjectButton.setOnClickListener {
            val preferences = Preferences.getInstance(requireContext())
            val projectCoreId =
                if (subscribedProjects.isEmpty()) viewModel.getProjectsFromLocal().map { p -> p.serverId ?: "" }
                    .random() else subscribedProjects.random()
            val id = viewModel.getProjectLocalIdByCoreId(projectCoreId)
            preferences.putInt(Preferences.SELECTED_PROJECT, id)
            listener.handleOpenPage()
        }

        logoutButton.setOnClickListener {
            requireContext().logout()
        }

        refreshButton.setOnClickListener {
            progressLoadProject.visibility = View.VISIBLE
            noContentTextView.visibility = View.GONE
            refreshButton.isEnabled = false
            viewModel.fetchProjects()
        }
    }

    private fun setObserver() {
        viewModel.projects.observe(viewLifecycleOwner, {
            it.success({
                projectSwipeRefreshView.isRefreshing = false
                if (viewModel.getProjectsFromLocal().isEmpty()) {
                    noContentTextView.visibility = View.VISIBLE
                    refreshButton.visibility = View.VISIBLE
                    logoutButton.visibility = View.VISIBLE
                    selectProjectButton.visibility = View.GONE
                } else {
                    noContentTextView.visibility = View.GONE
                    refreshButton.visibility = View.GONE
                    logoutButton.visibility = View.GONE
                    selectProjectButton.visibility = View.VISIBLE
                }
                projectsItem = viewModel.getProjectsFromLocal().map { project ->
                    ProjectsItem(
                        project,
                        getSubscribedProject()?.contains(project.serverId)
                            ?: false
                    )
                }
                refreshButton.isEnabled = true
                progressLoadProject.visibility = View.GONE
                projectsItem?.let { items -> projectsAdapter.items = items }
            }, {
                refreshButton.isEnabled = true
                progressLoadProject.visibility = View.GONE
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
        val items = projectsItem ?: return
        projectsAdapter.subscribingProject = item.project.name
        projectsAdapter.items = items
        selectProjectButton.isEnabled = false

        if (item.selected) {
            viewModel.unsubscribeProject(item.project) { status ->
                projectsAdapter.subscribingProject = null
                if (!status) {
                    setSelectedProject(items, position)
                    showToast(getString(R.string.failed_unsubscribe_receive_notification, item.project.name))
                } else {
                    saveSubscribedProject(subscribedProjects)
                    subscribedProjects.remove(item.project.serverId ?: "")
                    selectProjectButton.isEnabled = true
                    setSelectedProject(items, position)
                }
                selectProjectButton.text =
                    if (subscribedProjects.isNotEmpty()) getString(R.string.continue_text) else getString(R.string.skip)
            }
        } else {
            viewModel.setProjectsAndSubscribe(item.project) { status ->
                projectsAdapter.subscribingProject = null
                if (!status) {
                    setSelectedProject(items, position)
                    showToast(getString(R.string.failed_receive_notification, item.project.name))
                } else {
                    subscribedProjects.add(item.project.serverId ?: "")
                    saveSubscribedProject(subscribedProjects)
                    selectProjectButton.isEnabled = true
                    setSelectedProject(items, position)
                }
                selectProjectButton.text =
                    if (subscribedProjects.isNotEmpty()) getString(R.string.continue_text) else getString(R.string.skip)
            }
        }
    }

    private fun setSelectedProject(items: List<ProjectsItem>, position: Int) {
        items[position].selected = !items[position].selected
        projectsAdapter.items = items
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
