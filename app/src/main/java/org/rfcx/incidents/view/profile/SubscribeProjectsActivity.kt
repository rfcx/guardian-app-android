package org.rfcx.incidents.view.profile

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.activity_subscribe_projects.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.data.remote.success
import org.rfcx.incidents.entity.OnProjectsItemClickListener
import org.rfcx.incidents.entity.project.Project
import org.rfcx.incidents.util.*
import org.rfcx.incidents.view.base.BaseActivity
import org.rfcx.incidents.view.login.ProjectsAdapter
import org.rfcx.incidents.view.login.ProjectsItem


class SubscribeProjectsActivity : BaseActivity(), OnProjectsItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private val analytics by lazy { Analytics(this) }
    private val viewModel: GuardianGroupViewModel by viewModel()
    private val projectsAdapter by lazy { ProjectsAdapter(this) }
    private var projectsItem: List<ProjectsItem>? = null
    private var subscribedProjects: ArrayList<String> = arrayListOf()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscribe_projects)
        setupToolbar()
        setList()
        
        // setup list
        projectsRecycler.apply {
            layoutManager = LinearLayoutManager(this@SubscribeProjectsActivity)
            adapter = projectsAdapter
        }
        
        getSubscribedProject()?.let { projects -> subscribedProjects.addAll(projects) }
        
        projectSwipeRefreshView.apply {
            setOnRefreshListener(this@SubscribeProjectsActivity)
            setColorSchemeResources(R.color.colorPrimary)
        }
        
        viewModel.getProjectsFromRemote.observe(this, Observer { it ->
            it.success({
                projectSwipeRefreshView.isRefreshing = false
                setList()
            }, {
                projectSwipeRefreshView.isRefreshing = false
                this@SubscribeProjectsActivity.handleError(it)
            }, {
                projectSwipeRefreshView.isRefreshing = true
            })
        })
        
        checkStateBeforeFetchProjects()
    }
    
    private fun setList() {
        projectsItem = viewModel.getProjectsFromLocal().map { project ->
            ProjectsItem(
                project, getSubscribedProject()?.contains(project.serverId)
                    ?: false
            )
        }
        projectsAdapter.items = projectsItem as List<ProjectsItem>
    }
    
    private fun getSubscribedProject(): ArrayList<String>? {
        val preferenceHelper = Preferences.getInstance(this)
        return preferenceHelper.getArrayList(Preferences.SUBSCRIBED_PROJECTS)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
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
                projectSwipeRefreshView.isRefreshing = false
                this.showToast(getString(R.string.project_could_not_refreshed) + " " + getString(R.string.pls_off_air_plane_mode))
            }
            !this.isNetworkAvailable() -> {
                projectSwipeRefreshView.isRefreshing = false
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

interface OnItemClickListener {
    fun onItemClick(project: Project)
}
