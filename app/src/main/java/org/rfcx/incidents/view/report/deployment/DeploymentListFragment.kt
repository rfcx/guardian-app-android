package org.rfcx.incidents.view.report.deployment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentDeploymentListBinding
import org.rfcx.incidents.view.MainActivityEventListener

class DeploymentListFragment : Fragment() {

    private lateinit var binding: FragmentDeploymentListBinding
    private val viewModel: DeploymentListViewModel by viewModel()
    private lateinit var listener: MainActivityEventListener

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

        lifecycleScope.launch {
            viewModel.deployments.collectLatest {
                //TODO update deployment list
            }
        }
    }

    companion object {
        const val tag = "DeploymentListFragment"
        fun newInstance(): DeploymentListFragment = DeploymentListFragment()
    }
}
