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
import org.rfcx.incidents.databinding.FragmentGuardianChecklistBinding
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
        mainEvent?.setToolbarTitle("Checklist")

        binding.guardianCheckListRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = checkListAdapter
        }
        collectStates()
        viewModel.getAllCheckList(mainEvent?.getPassedScreen())
    }

    private fun collectStates() {
        lifecycleScope.launchWhenStarted {
            launch { collectCheckListItem() }
        }
    }

    private fun collectCheckListItem() {
        lifecycleScope.launch {
            viewModel.checklistItemState.collectLatest {
                checkListAdapter.setCheckList(it)
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
        }
    }
}
