package org.rfcx.incidents.view.guardian.checklist.checkin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentGuardianCheckinTestBinding
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener

class GuardianCheckInTestFragment : Fragment() {

    private lateinit var binding: FragmentGuardianCheckinTestBinding
    private val viewModel: GuardianCheckinTestViewModel by viewModel()

    private var mainEvent: GuardianDeploymentEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainEvent = context as GuardianDeploymentEventListener
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_guardian_checkin_test, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        mainEvent?.let {
            it.showToolbar()
            it.hideThreeDots()
            it.setToolbarTitle(getString(R.string.checkin_title))
        }

        binding.checkInFinishButton.setOnClickListener {
            mainEvent?.next()
        }
    }

    companion object {
        fun newInstance(): GuardianCheckInTestFragment = GuardianCheckInTestFragment()
    }
}
