package org.rfcx.incidents.view.profile

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.data.remote.success
import org.rfcx.incidents.databinding.ActivitySubscribeProjectsBinding
import org.rfcx.incidents.entity.OnProjectsItemClickListener
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Preferences
import org.rfcx.incidents.util.Screen
import org.rfcx.incidents.util.handleError
import org.rfcx.incidents.util.isNetworkAvailable
import org.rfcx.incidents.util.isOnAirplaneMode
import org.rfcx.incidents.view.base.BaseActivity
import org.rfcx.incidents.view.login.ProjectsAdapter
import org.rfcx.incidents.view.login.ProjectsItem

class SubscribeProjectsActivity : BaseActivity(), OnProjectsItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    lateinit var binding: ActivitySubscribeProjectsBinding
    private val viewModel: SubscribeProjectsViewModel by viewModel()
    private val projectsAdapter by lazy { ProjectsAdapter(this) }
    private var projectsItem: List<ProjectsItem>? = null
    private var subscribedProjects: ArrayList<String> = arrayListOf()
    private val analytics by lazy { Analytics(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscribeProjectsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()

        // setup list
        binding.projectsRecycler.apply {
            layoutManager = LinearLayoutManager(this@SubscribeProjectsActivity)
            adapter = projectsAdapter
        }

        getSubscribedProject()?.let { projects -> subscribedProjects.addAll(projects) }

        binding.projectSwipeRefreshView.apply {
            setOnRefreshListener(this@SubscribeProjectsActivity)
            setColorSchemeResources(R.color.colorPrimary)
        }

        viewModel.projects.observe(this) { result ->
            result.success({ projects ->
                binding.projectSwipeRefreshView.isRefreshing = false
                projectsItem = projects.map { project ->
                    ProjectsItem(
                        project,
                        getSubscribedProject()?.contains(project.serverId)
                            ?: false
                    )
                }
                projectsAdapter.items = projectsItem as List<ProjectsItem>
            }, {
                binding.projectSwipeRefreshView.isRefreshing = false
                this@SubscribeProjectsActivity.handleError(it)
            }, {
                binding.projectSwipeRefreshView.isRefreshing = true
            })
        }

        checkStateBeforeFetchProjects()
    }

    private fun getSubscribedProject(): ArrayList<String>? {
        val preferenceHelper = Preferences.getInstance(this)
        return preferenceHelper.getArrayList(Preferences.SUBSCRIBED_PROJECTS)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            elevation = 0f
            title = getString(R.string.receive_alert_notification)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, SubscribeProjectsActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onItemClick(item: ProjectsItem, position: Int) {
        when {
            this.isOnAirplaneMode() -> {
                this.showToast(getString(R.string.pls_off_air_plane_mode))
            }
            !this.isNetworkAvailable() -> {
                this.showToast(getString(R.string.no_internet_connection))
            }
            else -> {
                val dialog = Dialog(this)
                dialog.setContentView(R.layout.fragment_loading)
                dialog.setCancelable(false)
                dialog.show()

                if (item.selected) {
                    viewModel.unsubscribeProject(item.project) { status ->
                        dialog.dismiss()
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
                        dialog.dismiss()
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
            }
        }
    }

    private fun saveSubscribedProject(subscribedProjects: ArrayList<String>) {
        val preferenceHelper = Preferences.getInstance(this)
        preferenceHelper.remove(Preferences.SUBSCRIBED_PROJECTS)
        preferenceHelper.putArrayList(Preferences.SUBSCRIBED_PROJECTS, subscribedProjects)
    }

    override fun onLockImageClicked() {
        showToast(getString(R.string.not_have_permission))
    }

    override fun onRefresh() {
        checkStateBeforeFetchProjects()
    }

    private fun checkStateBeforeFetchProjects() {
        when {
            this.isOnAirplaneMode() -> {
                binding.projectSwipeRefreshView.isRefreshing = false
                this.showToast(getString(R.string.project_could_not_refreshed) + " " + getString(R.string.pls_off_air_plane_mode))
            }
            !this.isNetworkAvailable() -> {
                binding.projectSwipeRefreshView.isRefreshing = false
                this.showToast(getString(R.string.project_could_not_refreshed) + " " + getString(R.string.no_internet_connection))
            }
            else -> {
                viewModel.fetchProjects()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        analytics.trackScreen(Screen.SUBSCRIBE_PROJECTS)
    }
}
