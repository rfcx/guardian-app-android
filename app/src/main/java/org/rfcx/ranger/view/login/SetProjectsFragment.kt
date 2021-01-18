package org.rfcx.ranger.view.login

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_set_projects.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.data.remote.success
import org.rfcx.ranger.entity.OnProjectsItemClickListener
import org.rfcx.ranger.entity.guardian.GuardianGroup

class SetProjectsFragment : Fragment(), OnProjectsItemClickListener {
	lateinit var listener: LoginListener
	private val viewModel: SetProjectsViewModel by viewModel()
	private val projectsAdapter by lazy { ProjectsAdapter(this) }
	private var projectsState = ArrayList<ProjectsItem>()
	private var projects = listOf<GuardianGroup>()
	private var project: GuardianGroup? = null
	
	private val dialog: AlertDialog by lazy {
		AlertDialog.Builder(context)
				.setView(layoutInflater.inflate(R.layout.custom_loading_alert_dialog, null))
				.setCancelable(false)
				.create()
	}
	
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
			adapter = projectsAdapter
		}
		
		viewModel.items.observe(this, Observer { it ->
			it.success({
				projects = it
				it.map { project -> projectsState.add(ProjectsItem(project, false)) }
				
				projectsProgressBar.visibility = View.INVISIBLE
				projectsAdapter.items = projectsState
			}, {
				projectsProgressBar.visibility = View.INVISIBLE
				Toast.makeText(context, R.string.something_is_wrong, Toast.LENGTH_LONG).show()
			}, {
				projectsProgressBar.visibility = View.VISIBLE
			})
		})
		
		submitProjectsButton.setOnClickListener {
			dialog.show()
			
			project?.let { it1 ->
				viewModel.setProjects(it1) {
					dialog.dismiss()
					listener.handleOpenPage()
				}
			}
		}
	}
	
	override fun onItemClick(item: ProjectsItem, position: Int) {
		submitProjectsButton.isEnabled = true
		projects.forEachIndexed { index, _ ->
			projectsState[index] = ProjectsItem(projects[index], position == index)
		}
		projectsAdapter.items = projectsState
		
		this.project = item.project
	}
	
	companion object {
		@JvmStatic
		fun newInstance() = SetProjectsFragment()
	}
}
