package org.rfcx.ranger.view.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_set_projects.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.data.remote.success
import org.rfcx.ranger.util.handleError

class SetProjectsFragment : Fragment() {
	lateinit var listener: LoginListener
	private val viewModel: SetProjectsViewModel by viewModel()
	private val projectsAdapter by lazy { ProjectsAdapter() }
	
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
				projectsProgressBar.visibility = View.INVISIBLE
				projectsAdapter.items = it
				
			}, {
				projectsProgressBar.visibility = View.INVISIBLE
				context.handleError(it)
			}, {
				projectsProgressBar.visibility = View.VISIBLE
			})
		})
		
		submitProjectsButton.setOnClickListener {
			listener.openMain()
		}
	}
	
	companion object {
		@JvmStatic
		fun newInstance() = SetProjectsFragment()
	}
}
