package org.rfcx.incidents.view.guardian.checklist.classifierupload

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.databinding.FragmentClassifierUploadBinding
import org.rfcx.incidents.entity.guardian.GuardianFile
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener
import org.rfcx.incidents.view.guardian.checklist.softwareupdate.ChildrenClickedListener
import org.rfcx.incidents.view.guardian.checklist.softwareupdate.GuardianFileUpdateAdapter
import org.rfcx.incidents.view.guardian.checklist.softwareupdate.SoftwareUpdateFragment
import org.rfcx.incidents.view.guardian.checklist.softwareupdate.SoftwareUpdateViewModel

class ClassifierUploadFragment : Fragment(), ChildrenClickedListener {
    lateinit var binding: FragmentClassifierUploadBinding
    private val viewModel: ClassifierUploadViewModel by viewModel()
    private val classifierUploadAdapter by lazy { GuardianFileUpdateAdapter(this) }
    private var mainEvent: GuardianDeploymentEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainEvent = context as GuardianDeploymentEventListener
        binding = FragmentClassifierUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainEvent?.let {
            it.showToolbar()
            it.setToolbarTitle("Classifier Upload")
        }

        binding.classifierRecyclerView.apply {
            adapter = classifierUploadAdapter
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

        viewModel.getGuardianClassifier()
        lifecycleScope.launch {
            viewModel.guardianClassifierState.collectLatest {
                classifierUploadAdapter.files = it
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ClassifierUploadFragment()
    }

    override fun onItemClick(selectedFile: GuardianFile) {
        viewModel.updateOrInstallGuardianFile(selectedFile)
    }
}
