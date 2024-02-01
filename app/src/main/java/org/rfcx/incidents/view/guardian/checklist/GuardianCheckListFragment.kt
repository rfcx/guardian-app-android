package org.rfcx.incidents.view.guardian.checklist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentGuardianChecklistBinding
import org.rfcx.incidents.service.deploy.DeploymentSyncWorker
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener
import org.rfcx.incidents.view.guardian.GuardianScreen

class GuardianCheckListFragment : Fragment(), (Int, String) -> Unit {

    lateinit var binding: FragmentGuardianChecklistBinding
    private val viewModel: GuardianCheckListViewModel by viewModel()
    private val checkListAdapter by lazy { CheckListAdapter(this) }
    private var mainEvent: GuardianDeploymentEventListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainEvent = context as GuardianDeploymentEventListener
        binding = FragmentGuardianChecklistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainEvent?.showToolbar()
        mainEvent?.setToolbarTitle(getString(R.string.checklist))
        mainEvent?.showThreeDots()

        binding.guardianCheckListRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = checkListAdapter
        }
        collectStates()
        viewModel.getAllCheckList(mainEvent?.getPassedScreen())

        binding.checklistDeployButton.setOnClickListener {
            mainEvent?.let {
                viewModel.deploy(it.getSavedStream(), it.getSavedImages())
                DeploymentSyncWorker.enqueue()
                it.finishDeploy()
            }
        }

        if (mainEvent?.isAbleToDeploy() == true) {
            binding.checklistDeployButton.isEnabled = true
        }
    }

    private fun collectStates() {
        lifecycleScope.launchWhenStarted {
            launch { collectCheckListItem() }
            launch { collectRegistration() }
            launch { collectGuardianId() }
        }
    }

    private fun collectCheckListItem() {
        lifecycleScope.launch {
            viewModel.checklistItemState.collectLatest {
                checkListAdapter.setCheckList(it)
            }
        }
    }

    private fun collectRegistration() {
        lifecycleScope.launch {
            viewModel.registrationState.collectLatest {
                if (it) {
                    mainEvent?.setPassedScreen(GuardianScreen.REGISTER)
                    viewModel.getAllCheckList(mainEvent?.getPassedScreen())
                }
            }
        }
    }

    private fun collectGuardianId() {
        lifecycleScope.launch {
            viewModel.guardianIdState.collectLatest {
                if (it.isNotEmpty()) {
                    mainEvent?.setToolbarSubTitle(it)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainEvent?.closeSocket()
    }

    companion object {
        fun newInstance(): GuardianCheckListFragment {
            return GuardianCheckListFragment()
        }
    }

    override fun invoke(number: Int, name: String) {
        when (number) {
            0 -> mainEvent?.changeScreen(GuardianScreen.SOFTWARE_UPDATE)
            1 -> mainEvent?.changeScreen(GuardianScreen.CLASSIFIER_UPLOAD)
            2 -> mainEvent?.changeScreen(GuardianScreen.POWER_DIAGNOSTIC)
            3 -> mainEvent?.changeScreen(GuardianScreen.COMMUNICATION)
            4 -> mainEvent?.changeScreen(GuardianScreen.REGISTER)
            5 -> mainEvent?.changeScreen(GuardianScreen.NETWORK_TEST)
            6 -> mainEvent?.changeScreen(GuardianScreen.AUDIO_PARAMETER)
            7 -> mainEvent?.changeScreen(GuardianScreen.MICROPHONE)
            8 -> mainEvent?.changeScreen(GuardianScreen.STORAGE)
            9 -> mainEvent?.changeScreen(GuardianScreen.SITE)
            10 -> mainEvent?.changeScreen(GuardianScreen.PHOTO)
            11 -> mainEvent?.changeScreen(GuardianScreen.CHECKIN)
        }
    }
}
