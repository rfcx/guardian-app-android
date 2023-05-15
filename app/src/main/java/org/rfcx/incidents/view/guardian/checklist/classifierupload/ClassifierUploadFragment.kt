package org.rfcx.incidents.view.guardian.checklist.classifierupload

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.data.remote.common.GuardianModeNotCompatibleException
import org.rfcx.incidents.data.remote.common.NoActiveClassifierException
import org.rfcx.incidents.data.remote.common.OperationTimeoutException
import org.rfcx.incidents.data.remote.common.SoftwareNotCompatibleException
import org.rfcx.incidents.databinding.FragmentClassifierUploadBinding
import org.rfcx.incidents.entity.guardian.file.GuardianFile
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener

class ClassifierUploadFragment : Fragment(), ChildrenClickedListener {
    lateinit var binding: FragmentClassifierUploadBinding
    private val viewModel: ClassifierUploadViewModel by viewModel()
    private val classifierUploadAdapter by lazy { ClassifierUploadAdapter(this) }
    private var mainEvent: GuardianDeploymentEventListener? = null

    private lateinit var dialogBuilder: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainEvent = context as GuardianDeploymentEventListener
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_classifier_upload, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        mainEvent?.let {
            it.showToolbar()
            it.hideThreeDots()
            it.setToolbarTitle(getString(R.string.classifier_title))
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

        lifecycleScope.launch {
            viewModel.errorClassifierState.collectLatest {
                when (it) {
                    is OperationTimeoutException -> {
                        dialogBuilder = MaterialAlertDialogBuilder(requireContext(), R.style.BaseAlertDialog).apply {
                            setTitle(null)
                            setMessage(R.string.classifier_service_reboot_message)
                            setPositiveButton(R.string.restart) { _, _ ->
                                viewModel.restartService()
                            }
                            setNegativeButton(R.string.no) { _, _ ->
                                dialogBuilder.dismiss()
                            }
                        }.create()
                        dialogBuilder.show()
                    }
                    is SoftwareNotCompatibleException -> {
                        showAlert(it.message)
                    }
                    is GuardianModeNotCompatibleException -> {
                        showAlert(it.message)
                    }
                    is NoActiveClassifierException -> {
                        binding.noActiveWarnTextView.visibility = View.VISIBLE
                        binding.nextButton.isEnabled = false
                    }
                    else -> {
                        if (::dialogBuilder.isInitialized) {
                            dialogBuilder.dismiss()
                        }
                        binding.noActiveWarnTextView.visibility = View.GONE
                        binding.nextButton.isEnabled = true
                    }
                }
            }
        }
    }

    private fun showAlert(text: String) {
        if (::dialogBuilder.isInitialized && dialogBuilder.isShowing) {
            return
        }
        dialogBuilder =
            MaterialAlertDialogBuilder(requireContext(), R.style.BaseAlertDialog).apply {
                setTitle(null)
                setMessage(text)
                setPositiveButton(R.string.go_back) { _, _ ->
                    dialogBuilder.dismiss()
                    mainEvent?.back()
                }
            }.create()
        dialogBuilder.show()
    }

    companion object {
        @JvmStatic
        fun newInstance() = ClassifierUploadFragment()
    }

    override fun onUploadClick(selectedFile: GuardianFile) {
        viewModel.updateOrUploadClassifier(selectedFile)
    }

    override fun onActivateClick(selectedFile: GuardianFile) {
        viewModel.activateClassifier(selectedFile)
    }

    override fun onDeActivateClick(selectedFile: GuardianFile) {
        viewModel.deActivateClassifier(selectedFile)
    }
}
