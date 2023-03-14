package org.rfcx.incidents.view.guardian.checklist.network

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentGuardianNetworkBinding
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener
import org.koin.androidx.viewmodel.ext.android.viewModel

class NetworkTestFragment : Fragment() {

    private lateinit var binding: FragmentGuardianNetworkBinding
    private val viewModel: NetworkTestViewModel by viewModel()

    private val listOfSignal by lazy {
        listOf(binding.signalStrength1, binding.signalStrength2, binding.signalStrength3, binding.signalStrength4)
    }

    private val listOfTag by lazy {
        listOf(
            binding.satBadSignalQuality,
            binding.satOKSignalQuality,
            binding.satGoodSignalQuality,
            binding.satPerfectSignalQuality,
            binding.satErrorSignalQuality
        )
    }

    private var mainEvent: GuardianDeploymentEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainEvent = context as GuardianDeploymentEventListener
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_guardian_network, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        mainEvent?.let {
            it.showToolbar()
            it.setToolbarTitle("Network test")
        }

        retrieveSimModule()
        retrieveSatModule()

        binding.nextButton.setOnClickListener {
            mainEvent?.next()
        }
    }

    private fun retrieveSimModule() {
        // AdminSocketManager.pingBlob.observe(
        //     viewLifecycleOwner,
        //     Observer {
        //         val hasSim = deploymentProtocol?.getSimDetected()
        //         if (hasSim != null && hasSim) {
        //             simDetectionCheckbox.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checklist_passed, 0, 0, 0)
        //             cellSignalLayout.visibility = View.VISIBLE
        //             cellDataTransferLayout.visibility = View.VISIBLE
        //         } else {
        //             simDetectionCheckbox.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_red_error, 0, 0, 0)
        //             cellSignalLayout.visibility = View.GONE
        //             cellDataTransferLayout.visibility = View.GONE
        //         }
        //
        //         val cellStrength = deploymentProtocol?.getNetwork()
        //         if (cellStrength == null) {
        //             showCellSignalStrength(SignalState.NONE)
        //             signalValue.text = getString(R.string.speed_test_failed)
        //         } else {
        //             when {
        //                 cellStrength > -70 -> showCellSignalStrength(SignalState.MAX)
        //                 cellStrength > -90 -> showCellSignalStrength(SignalState.HIGH)
        //                 cellStrength > -110 -> showCellSignalStrength(SignalState.NORMAL)
        //                 cellStrength > -130 -> showCellSignalStrength(SignalState.LOW)
        //                 else -> showCellSignalStrength(SignalState.NONE)
        //             }
        //             signalValue.text = getString(R.string.signal_value, cellStrength)
        //         }
        //
        //         val speedTest = deploymentProtocol?.getSpeedTest()
        //         val tempDownload = speedTest?.downloadSpeed
        //         val tempUpload = speedTest?.uploadSpeed
        //         val hasConnection = speedTest?.hasConnection
        //         val isFailed = speedTest?.isFailed
        //         val isWaitingSpeedTest = speedTest?.isTesting
        //
        //         if (hasConnection != null && hasConnection) {
        //             showSpeedTest()
        //         } else {
        //             hideHideSpeedTest()
        //         }
        //
        //         when {
        //             isFailed != null && isFailed -> {
        //                 cellDownloadDataTransferValues.text = getString(R.string.speed_test_failed)
        //                 cellUploadDataTransferValues.text = getString(R.string.speed_test_failed)
        //             }
        //             isWaitingSpeedTest == true -> {
        //                 cellDownloadDataTransferValues.text = getString(R.string.speed_test_wait)
        //                 cellUploadDataTransferValues.text = getString(R.string.speed_test_wait)
        //             }
        //             isWaitingSpeedTest == false -> {
        //                 if (tempDownload == null || tempDownload == -1.0) {
        //                     cellDownloadDataTransferValues.text = getString(R.string.speed_test_run)
        //                 } else {
        //                     cellDownloadDataTransferValues.text =
        //                         getString(R.string.speed_test_kbps, tempDownload)
        //                 }
        //                 if (tempUpload == null || tempUpload == -1.0) {
        //                     cellUploadDataTransferValues.text = getString(R.string.speed_test_run)
        //                 } else {
        //                     cellUploadDataTransferValues.text =
        //                         getString(R.string.speed_test_kbps_upload, tempUpload)
        //                 }
        //             }
        //         }
        //     }
        // )
        //
        // cellDataTransferButton.setOnClickListener {
        //     GuardianSocketManager.runSpeedTest()
        // }
    }

    private fun showSpeedTest() {
        // noCellConnectionText.visibility = View.GONE
        // cellDownloadDataTransferValues.visibility = View.VISIBLE
        // cellUploadDataTransferValues.visibility = View.VISIBLE
        // cellDataTransferButton.visibility = View.VISIBLE
    }

    private fun hideHideSpeedTest() {
        // noCellConnectionText.visibility = View.VISIBLE
        // cellDownloadDataTransferValues.visibility = View.GONE
        // cellUploadDataTransferValues.visibility = View.GONE
        // cellDataTransferButton.visibility = View.GONE
    }

    private fun retrieveSatModule() {
        // AdminSocketManager.pingBlob.observe(
        //     viewLifecycleOwner,
        //     Observer {
        //         val hasSatModule = deploymentProtocol?.getSatId()
        //         if (hasSatModule != null) {
        //             satDetectionCheckbox.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checklist_passed, 0, 0, 0)
        //             satSignalLayout.visibility = View.VISIBLE
        //         } else {
        //             satDetectionCheckbox.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_red_error, 0, 0, 0)
        //             satSignalLayout.visibility = View.GONE
        //         }
        //
        //         val swmStrength = deploymentProtocol?.getSwmNetwork()
        //         if (swmStrength == null) {
        //             showSatSignalTagStrength(SignalState.NONE)
        //             satSignalValues.text = getString(R.string.speed_test_failed)
        //         } else {
        //             when {
        //                 swmStrength <= -110 -> {
        //                     showSatSignalTagStrength(SignalState.ERROR)
        //                     satSignalErrorAlert.visibility = View.VISIBLE
        //                 }
        //                 swmStrength < -104 -> {
        //                     showSatSignalTagStrength(SignalState.MAX)
        //                     satSignalErrorAlert.visibility = View.GONE
        //                 }
        //                 swmStrength < -100 -> {
        //                     showSatSignalTagStrength(SignalState.HIGH)
        //                     satSignalErrorAlert.visibility = View.GONE
        //                 }
        //                 swmStrength < -97 -> {
        //                     showSatSignalTagStrength(SignalState.NORMAL)
        //                     satSignalErrorAlert.visibility = View.GONE
        //                 }
        //                 swmStrength < -93 -> {
        //                     showSatSignalTagStrength(SignalState.LOW)
        //                     satSignalErrorAlert.visibility = View.GONE
        //                 }
        //             }
        //             satSignalValues.text = getString(R.string.signal_value, swmStrength)
        //         }
        //     }
        // )
    }

    private fun showCellSignalStrength(state: SignalState) {
        // listOfSignal.forEachIndexed { index, view ->
        //     if (index < state.value) {
        //         (view.background as GradientDrawable).setBackground(
        //             requireContext(),
        //             R.color.signal_filled
        //         )
        //     } else {
        //         (view.background as GradientDrawable).setBackground(requireContext(), R.color.white)
        //     }
        // }
    }

    private fun showSatSignalTagStrength(state: SignalState) {
        listOfTag.forEachIndexed { index, view ->
            if ((index + 1) == state.value) {
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    companion object {

        private enum class SignalState(val value: Int) {
            NONE(0), LOW(1), NORMAL(2), HIGH(3), MAX(4), ERROR(5)
        }

        private fun GradientDrawable.setBackground(context: Context, color: Int) {
            this.setColor(ContextCompat.getColor(context, color))
        }

        fun newInstance(): NetworkTestFragment = NetworkTestFragment()
    }
}
