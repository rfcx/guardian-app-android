package org.rfcx.incidents.view.guardian

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.wifi.ScanResult
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.databinding.ActivityGuardianDeploymentBinding
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.view.guardian.checklist.GuardianCheckListFragment
import org.rfcx.incidents.view.guardian.checklist.audio.GuardianAudioParameterFragment
import org.rfcx.incidents.view.guardian.checklist.checkin.GuardianCheckInTestFragment
import org.rfcx.incidents.view.guardian.checklist.classifierupload.ClassifierUploadFragment
import org.rfcx.incidents.view.guardian.checklist.communication.CommunicationFragment
import org.rfcx.incidents.view.guardian.checklist.microphone.GuardianMicrophoneFragment
import org.rfcx.incidents.view.guardian.checklist.network.NetworkTestFragment
import org.rfcx.incidents.view.guardian.checklist.photos.AddPhotosFragment
import org.rfcx.incidents.view.guardian.checklist.photos.Image
import org.rfcx.incidents.view.guardian.checklist.powerdiagnostic.PowerDiagnosticFragment
import org.rfcx.incidents.view.guardian.checklist.preference.GuardianPreferenceFragment
import org.rfcx.incidents.view.guardian.checklist.registration.GuardianRegisterFragment
import org.rfcx.incidents.view.guardian.checklist.site.GuardianSiteSelectFragment
import org.rfcx.incidents.view.guardian.checklist.site.GuardianSiteSetFragment
import org.rfcx.incidents.view.guardian.checklist.site.MapPickerFragment
import org.rfcx.incidents.view.guardian.checklist.softwareupdate.SoftwareUpdateFragment
import org.rfcx.incidents.view.guardian.checklist.storage.GuardianStorageFragment
import org.rfcx.incidents.view.guardian.connect.GuardianConnectFragment

class GuardianDeploymentActivity : AppCompatActivity(), GuardianDeploymentEventListener {

    lateinit var binding: ActivityGuardianDeploymentBinding
    private val viewModel: GuardianDeploymentViewModel by viewModel()

    private var currentScreen = GuardianScreen.CHECKLIST
    private val passedScreen = arrayListOf<GuardianScreen>()

    private lateinit var stream: Stream
    private var isNewSite = false

    private var _savedImages = listOf<Image>()

    private var menu: Menu? = null

    private var prefs: List<Preference> = listOf()
    private var prefsChanges = ""
    private var prefsEditor: SharedPreferences.Editor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Binding view
        binding = ActivityGuardianDeploymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        // Show guardian connect screen first
        changeScreen(GuardianScreen.CONNECT)
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        val inflater = menuInflater
        inflater.inflate(R.menu.preference_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.preference -> onThreeDotsClicked()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onThreeDotsClicked() {
        changeScreen(GuardianScreen.PREFERENCE)
    }

    private fun showScreen(screen: GuardianScreen) {
        when (screen) {
            GuardianScreen.CONNECT -> startFragment(GuardianConnectFragment.newInstance())
            GuardianScreen.CHECKLIST -> startFragment(GuardianCheckListFragment.newInstance())
            GuardianScreen.SOFTWARE_UPDATE -> startFragment(SoftwareUpdateFragment.newInstance())
            GuardianScreen.CLASSIFIER_UPLOAD -> startFragment(ClassifierUploadFragment.newInstance())
            GuardianScreen.POWER_DIAGNOSTIC -> startFragment(PowerDiagnosticFragment.newInstance())
            GuardianScreen.COMMUNICATION -> startFragment(CommunicationFragment.newInstance())
            GuardianScreen.REGISTER -> startFragment(GuardianRegisterFragment.newInstance())
            GuardianScreen.NETWORK_TEST -> startFragment(NetworkTestFragment.newInstance())
            GuardianScreen.AUDIO_PARAMETER -> startFragment(GuardianAudioParameterFragment.newInstance())
            GuardianScreen.MICROPHONE -> startFragment(GuardianMicrophoneFragment.newInstance())
            GuardianScreen.STORAGE -> startFragment(GuardianStorageFragment.newInstance())
            GuardianScreen.SITE -> startFragment(GuardianSiteSelectFragment.newInstance())
            GuardianScreen.SITE_SET -> startFragment(GuardianSiteSetFragment.newInstance())
            GuardianScreen.MAP_PICKER -> startFragment(GuardianSiteSetFragment.newInstance())
            GuardianScreen.PHOTO -> startFragment(AddPhotosFragment.newInstance())
            GuardianScreen.CHECKIN -> startFragment(GuardianCheckInTestFragment.newInstance())
            GuardianScreen.PREFERENCE -> startFragment(GuardianPreferenceFragment.newInstance())
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

    override fun showThreeDots() {
        this.menu?.findItem(R.id.preference)?.isVisible = true
    }
    override fun hideThreeDots() {
        this.menu?.findItem(R.id.preference)?.isVisible = false
    }

    override fun setToolbarTitle(title: String) {
        supportActionBar?.apply {
            this.title = title
        }
    }

    override fun isAbleToDeploy(): Boolean {
        if (!passedScreen.contains(GuardianScreen.REGISTER)) return false
        if (!passedScreen.contains(GuardianScreen.NETWORK_TEST)) return false
        if (!passedScreen.contains(GuardianScreen.AUDIO_PARAMETER)) return false
        if (!passedScreen.contains(GuardianScreen.MICROPHONE)) return false
        if (!passedScreen.contains(GuardianScreen.STORAGE)) return false
        if (!passedScreen.contains(GuardianScreen.SITE)) return false
        return true
    }

    override fun changeScreen(screen: GuardianScreen) {
        currentScreen = screen
        showScreen(screen)
    }

    override fun setPassedScreen(screen: GuardianScreen) {
        passedScreen.add(screen)
    }

    override fun back() {
        when (currentScreen) {
            GuardianScreen.CONNECT -> finish()
            GuardianScreen.CHECKLIST -> {
                viewModel.disconnectWifi()
                viewModel.onDestroy()
                changeScreen(GuardianScreen.CONNECT)
            }
            GuardianScreen.SOFTWARE_UPDATE -> changeScreen(GuardianScreen.CHECKLIST)
            GuardianScreen.CLASSIFIER_UPLOAD -> changeScreen(GuardianScreen.CHECKLIST)
            GuardianScreen.POWER_DIAGNOSTIC -> changeScreen(GuardianScreen.CHECKLIST)
            GuardianScreen.COMMUNICATION -> changeScreen(GuardianScreen.CHECKLIST)
            GuardianScreen.REGISTER -> changeScreen(GuardianScreen.CHECKLIST)
            GuardianScreen.NETWORK_TEST -> changeScreen(GuardianScreen.CHECKLIST)
            GuardianScreen.AUDIO_PARAMETER -> changeScreen(GuardianScreen.CHECKLIST)
            GuardianScreen.MICROPHONE -> changeScreen(GuardianScreen.CHECKLIST)
            GuardianScreen.STORAGE -> changeScreen(GuardianScreen.CHECKLIST)
            GuardianScreen.SITE -> changeScreen(GuardianScreen.CHECKLIST)
            GuardianScreen.SITE_SET -> changeScreen(GuardianScreen.CHECKLIST)
            GuardianScreen.PHOTO -> changeScreen(GuardianScreen.CHECKLIST)
            GuardianScreen.CHECKIN -> changeScreen(GuardianScreen.CHECKLIST)
            GuardianScreen.MAP_PICKER -> {
                currentScreen = GuardianScreen.SITE_SET
                startFragment(GuardianSiteSetFragment.newInstance(this.stream, this.isNewSite))
            }
            GuardianScreen.PREFERENCE -> {
                // Need to remove guardian prefs before adding news
                removeGuardianPrefs()
                changeScreen(GuardianScreen.CHECKLIST)
            }
        }
    }

    override fun next() {
        passedScreen.add(currentScreen)
        // Need to remove guardian prefs before adding news
        if (currentScreen == GuardianScreen.PREFERENCE) {
            removeGuardianPrefs()
        }
        changeScreen(GuardianScreen.CHECKLIST)
    }

    override fun finishDeploy() {
        super.finish()
    }

    override fun goToSiteSetScreen(stream: Stream, isNewSite: Boolean) {
        currentScreen = GuardianScreen.SITE_SET
        this.stream = stream
        this.isNewSite = isNewSite
        startFragment(GuardianSiteSetFragment.newInstance(stream, isNewSite))
    }

    override fun goToMapPickerScreen(stream: Stream) {
        this.stream = stream
        startFragment(MapPickerFragment.newInstance(stream))
    }

    override fun getPassedScreen(): List<GuardianScreen> = passedScreen
    override fun nextWithStream(stream: Stream) {
        currentScreen = GuardianScreen.SITE
        this.stream = stream
        passedScreen.add(currentScreen)
        changeScreen(GuardianScreen.CHECKLIST)
    }

    override fun connectHotspot(hotspot: ScanResult?) {
        lifecycleScope.launch {
            viewModel.connectWifi(hotspot)
        }
    }
    override fun getHotspotConnectionState(): StateFlow<Result<Boolean>?> {
        return viewModel.connectionState
    }
    override fun initSocket() {
        viewModel.initSocket()
    }

    override fun sendHeartBeatSocket() {
        viewModel.sendHeartbeatSignalPeriodic()
    }

    override fun getInitSocketState(): StateFlow<Result<Boolean>?> {
        return viewModel.initSocketState
    }

    override fun getSocketMessageState(): StateFlow<Result<List<String>>?> {
        return viewModel.socketMessageState
    }

    override fun closeSocket() {
        viewModel.onDestroy()
    }

    override fun getSavedStream(): Stream {
        return stream
    }

    override fun getSavedImages(): List<Image> {
        return _savedImages
    }

    override fun setSavedImages(images: List<Image>) {
        _savedImages = images
    }

    override fun setGuardianPrefs(prefs: List<Preference>) {
        this.prefs = prefs
    }

    override fun setChangedPrefs(prefs: String) {
        this.prefsChanges = prefs
    }

    override fun getChangedPrefs(): String {
        return this.prefsChanges
    }

    private fun removeGuardianPrefs() {
        this.prefs.forEach {
            this.prefsEditor!!.remove(it.key)!!.apply()
        }
    }

    override fun setEditor(editor: SharedPreferences.Editor?) {
        this.prefsEditor = editor
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onDestroy()
        this.prefsEditor?.clear()?.apply()
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
