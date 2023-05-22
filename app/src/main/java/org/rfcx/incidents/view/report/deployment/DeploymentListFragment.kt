package org.rfcx.incidents.view.report.deployment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentDeploymentListBinding
import org.rfcx.incidents.entity.guardian.deployment.Deployment
import org.rfcx.incidents.view.MainActivityEventListener
import org.rfcx.incidents.view.report.draft.ReportsAdapter

class DeploymentListFragment : Fragment(), CloudListener {

    private lateinit var binding: FragmentDeploymentListBinding
    private val viewModel: DeploymentListViewModel by viewModel()
    private lateinit var listener: MainActivityEventListener

    private val deploymentAdapter by lazy { DeploymentListAdapter(this) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        listener = context as MainActivityEventListener
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_deployment_list, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        binding.deploymentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = deploymentAdapter
        }

        lifecycleScope.launch {
            viewModel.deployments.collectLatest {
                deploymentAdapter.items = it
            }
        }

        lifecycleScope.launch {
            viewModel.selectedProject.collectLatest {
                binding.toolbarLayout.projectTitleTextView.text = it
            }
        }
    }


    override fun onClicked(id: Int) {
        viewModel.syncDeployment(id)
    }

    companion object {
        const val tag = "DeploymentListFragment"
        fun newInstance(): DeploymentListFragment = DeploymentListFragment()
    }
}
