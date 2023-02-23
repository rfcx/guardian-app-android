package org.rfcx.incidents.view.guardian.checklist.softwareupdate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentSoftwareUpdateBinding
import org.rfcx.incidents.entity.guardian.GuardianFile
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener

class SoftwareUpdateFragment : Fragment(), ChildrenClickedListener {
    lateinit var binding: FragmentSoftwareUpdateBinding
    private val viewModel: SoftwareUpdateViewModel by viewModel()
    private val softwareUpdateAdapter by lazy { SoftwareUpdateAdapter(this) }
    private var mainEvent: GuardianDeploymentEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainEvent = context as GuardianDeploymentEventListener
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_software_update, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        mainEvent?.let {
            it.showToolbar()
            it.setToolbarTitle(getString(R.string.software_title))
        }

        binding.apkRecyclerView.apply {
            adapter = softwareUpdateAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        binding.nextButton.setOnClickListener {
            mainEvent?.next()
        }

        viewModel.getGuardianSoftware()
        lifecycleScope.launch {
            viewModel.guardianSoftwareState.collectLatest {
                softwareUpdateAdapter.files = it
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = SoftwareUpdateFragment()
    }

    override fun onItemClick(selectedFile: GuardianFile) {
        viewModel.updateOrInstallSoftware(selectedFile)
    }
}
