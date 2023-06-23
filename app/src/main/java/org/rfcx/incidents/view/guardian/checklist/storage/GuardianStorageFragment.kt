package org.rfcx.incidents.view.guardian.checklist.storage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentGuardianStorageBinding
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener

class GuardianStorageFragment : Fragment() {

    private lateinit var binding: FragmentGuardianStorageBinding
    private val viewModel: GuardianStorageViewModel by viewModel()

    private var mainEvent: GuardianDeploymentEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainEvent = context as GuardianDeploymentEventListener
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_guardian_storage, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        mainEvent?.let {
            it.showToolbar()
            it.hideThreeDots()
            it.setToolbarTitle(getString(R.string.storage_title))
        }

        binding.audioCoverageButton.setOnClickListener {
            HeatmapAudioCoverageActivity.startActivity(requireContext(), viewModel.archived)
        }

        binding.internalFinishButton.setOnClickListener {
            mainEvent?.next()
        }
    }

    companion object {
        fun newInstance() = GuardianStorageFragment()
    }
}
