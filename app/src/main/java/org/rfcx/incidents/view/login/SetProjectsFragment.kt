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
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.data.remote.success
import org.rfcx.incidents.databinding.FragmentSetProjectsBinding
import org.rfcx.incidents.entity.OnProjectsItemClickListener
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Preferences
import org.rfcx.incidents.util.Screen
import org.rfcx.incidents.util.isNetworkAvailable
import org.rfcx.incidents.util.logout

class SetProjectsFragment : Fragment(), OnProjectsItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    companion object {
        @JvmStatic
        fun newInstance() = SetProjectsFragment()
    }

    private var _binding: FragmentSetProjectsBinding? = null
    private val binding get() = _binding!!
    lateinit var listener: LoginListener
    private val viewModel: SetProjectsViewModel by viewModel()
    private val projectsAdapter by lazy { ProjectsAdapter(this) }
    private var projectsItem: List<ProjectsItem>? = null
    private var subscribedProjects: ArrayList<String> = arrayListOf()

    private val analytics by lazy { context?.let { Analytics(it) } }

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
    ): View {
        _binding = FragmentSetProjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.projectView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = projectsAdapter
        }

        binding.projectSwipeRefreshView.apply {
            setOnRefreshListener(this@SetProjectsFragment)
            setColorSchemeResources(R.color.colorPrimary)
        }

        if (requireActivity().isNetworkAvailable()) {
            setObserver()
        } else {
            showToast(getString(R.string.network_not_available))
        }

        binding.selectProjectButton.text = getString(R.string.skip)

        binding.selectProjectButton.setOnClickListener {
            val preferences = Preferences.getInstance(requireContext())
            val projectCoreId =
                if (subscribedProjects.isEmpty()) viewModel.getProjectsFromLocal().map { p -> p.serverId ?: "" }
                    .random() else subscribedProjects.random()
            val id = viewModel.getProjectLocalIdByCoreId(projectCoreId)
            preferences.putInt(Preferences.SELECTED_PROJECT, id)
            listener.handleOpenPage()
        }

        binding.logoutButton.setOnClickListener {
            requireContext().logout()
        }

        binding.refreshButton.setOnClickListener {
            binding.progressLoadProject.visibility = View.VISIBLE
            binding.noContentTextView.visibility = View.GONE
            binding.refreshButton.isEnabled = false
            viewModel.fetchProjects()
        }
    }

    private fun setObserver() {
        viewModel.projects.observe(viewLifecycleOwner) {
            it.success({
                binding.projectSwipeRefreshView.isRefreshing = false
                if (viewModel.getProjectsFromLocal().isEmpty()) {
                    binding.noContentTextView.visibility = View.VISIBLE
                    binding.refreshButton.visibility = View.VISIBLE
                    binding.logoutButton.visibility = View.VISIBLE
                    binding.selectProjectButton.visibility = View.GONE
                } else {
                    binding.noContentTextView.visibility = View.GONE
                    binding.refreshButton.visibility = View.GONE
                    binding.logoutButton.visibility = View.GONE
                    binding.selectProjectButton.visibility = View.VISIBLE
                }
                projectsItem = viewModel.getProjectsFromLocal().map { project ->
                    ProjectsItem(
                        project,
                        getSubscribedProject()?.contains(project.serverId)
                            ?: false
                    )
                }
                binding.refreshButton.isEnabled = true
                binding.progressLoadProject.visibility = View.GONE
                projectsItem?.let { items -> projectsAdapter.items = items }
            }, {
                binding.refreshButton.isEnabled = true
                binding.progressLoadProject.visibility = View.GONE
                binding.projectSwipeRefreshView.isRefreshing = false
                Toast.makeText(context, R.string.something_is_wrong, Toast.LENGTH_LONG).show()
            }, {
                binding.projectSwipeRefreshView.isRefreshing = true
            })
        }
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
        binding.selectProjectButton.isEnabled = false

        if (item.selected) {
            viewModel.unsubscribeProject(item.project) { status ->
                projectsAdapter.subscribingProject = null
                if (!status) {
                    setSelectedProject(items, position)
                    showToast(getString(R.string.failed_unsubscribe_receive_notification, item.project.name))
                } else {
                    saveSubscribedProject(subscribedProjects)
                    subscribedProjects.remove(item.project.serverId ?: "")
                    binding.selectProjectButton.isEnabled = true
                    setSelectedProject(items, position)
                }
                binding.selectProjectButton.text =
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
                    binding.selectProjectButton.isEnabled = true
                    setSelectedProject(items, position)
                }
                binding.selectProjectButton.text =
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
