package org.rfcx.incidents.view.guardian

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.coroutines.flow.SharedFlow
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.databinding.ActivityGuardianDeploymentBinding
import org.rfcx.incidents.view.guardian.checklist.GuardianCheckListFragment
import org.rfcx.incidents.view.guardian.checklist.classifierupload.ClassifierUploadFragment
import org.rfcx.incidents.view.guardian.checklist.powerdiagnostic.PowerDiagnosticFragment
import org.rfcx.incidents.view.guardian.checklist.softwareupdate.SoftwareUpdateFragment
import org.rfcx.incidents.view.guardian.connect.GuardianConnectFragment

class GuardianDeploymentActivity : AppCompatActivity(), GuardianDeploymentEventListener {

    lateinit var binding: ActivityGuardianDeploymentBinding
    private val viewModel: GuardianDeploymentViewModel by viewModel()

    private var currentScreen = GuardianScreen.CHECKLIST
    private val passedScreen = arrayListOf<GuardianScreen>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Binding view
        binding = ActivityGuardianDeploymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        // Show guardian connect screen first
        showScreen(GuardianScreen.CONNECT)
    }

    private fun showScreen(screen: GuardianScreen) {
        when (screen) {
            GuardianScreen.CONNECT -> startFragment(GuardianConnectFragment.newInstance())
            GuardianScreen.CHECKLIST -> startFragment(GuardianCheckListFragment.newInstance())
            GuardianScreen.SOFTWARE_UPDATE -> startFragment(SoftwareUpdateFragment.newInstance())
            GuardianScreen.CLASSIFIER_UPLOAD -> startFragment(ClassifierUploadFragment.newInstance())
            GuardianScreen.POWER_DIAGNOSTIC -> startFragment(PowerDiagnosticFragment.newInstance())
        }
    }

    private fun startFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.contentContainer.id, fragment)
            .commit()
    }

    override fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbarDefault)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    override fun showToolbar() {
        binding.toolbarLayout.toolbarDefault.visibility = View.VISIBLE
    }

    override fun hideToolbar() {
        binding.toolbarLayout.toolbarDefault.visibility = View.GONE
    }

    override fun setToolbarTitle(title: String) {
        supportActionBar?.apply {
            this.title = title
        }
    }

    override fun changeScreen(screen: GuardianScreen) {
        currentScreen = screen
        showScreen(screen)
    }

    override fun back() {
        when (currentScreen) {
            GuardianScreen.CONNECT -> onBackPressedDispatcher.onBackPressed()
            GuardianScreen.CHECKLIST -> changeScreen(GuardianScreen.CONNECT)
            GuardianScreen.SOFTWARE_UPDATE -> changeScreen(GuardianScreen.CHECKLIST)
            GuardianScreen.CLASSIFIER_UPLOAD -> changeScreen(GuardianScreen.CHECKLIST)
            GuardianScreen.POWER_DIAGNOSTIC -> changeScreen(GuardianScreen.POWER_DIAGNOSTIC)
        }
    }

    override fun next() {
        passedScreen.add(currentScreen)
        changeScreen(GuardianScreen.CHECKLIST)
    }

    override fun getPassedScreen(): List<GuardianScreen> = passedScreen

    override fun initSocket() {
        viewModel.initSocket()
    }

    override fun sendHeartBeatSocket() {
        viewModel.sendHeartbeatSignalPeriodic()
    }

    override fun getInitSocketState(): SharedFlow<Result<Boolean>> {
        return viewModel.initSocketState
    }

    override fun getSocketMessageState(): SharedFlow<Result<List<String>>> {
        return viewModel.socketMessageState
    }

    override fun closeSocket() {
        viewModel.onDestroy()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onDestroy()
    }

    override fun onBackPressed() {
        back()
    }

    override fun onSupportNavigateUp(): Boolean {
        back()
        return true
    }

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, GuardianDeploymentActivity::class.java)
            context.startActivity(intent)
        }
    }
}
