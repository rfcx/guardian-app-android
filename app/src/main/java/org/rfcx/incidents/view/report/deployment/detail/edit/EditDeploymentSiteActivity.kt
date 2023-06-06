package org.rfcx.incidents.view.report.deployment.detail.edit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ActivityEditLocationBinding
import org.rfcx.incidents.view.guardian.checklist.site.MapPickerFragment
import org.rfcx.incidents.view.report.deployment.detail.DeploymentDetailViewModel
import org.rfcx.incidents.view.report.deployment.detail.MapPickerProtocol

class EditDeploymentSiteActivity : AppCompatActivity(), MapPickerProtocol, EditDeploymentSiteListener {
    lateinit var binding: ActivityEditLocationBinding
    private val viewModel: DeploymentDetailViewModel by viewModel()

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var altitude: Double = 0.0
    private var streamId: Int? = null
    private var deploymentId: Int? = null
    private var selectedProject: Int? = null
    private var device: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_location)
        binding.lifecycleOwner = this
        setContentView(binding.root)

        setupToolbar()
        initIntent()

        startFragment(
            MapPickerFragment.newInstance(
                latitude,
                longitude,
                altitude,
                streamId ?: -1
            )
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DEPLOYMENT_REQUEST_CODE) {
            when (resultCode) {
                ProjectActivity.RESULT_OK -> {
                    selectedProject =
                        data?.getIntExtra(EXTRA_PROJECT_ID, -1)
                }
                ProjectActivity.RESULT_DELETE -> {
                    selectedProject = data?.getIntExtra(EXTRA_PROJECT_ID, -1)
                }
            }
        }
    }

    private fun initIntent() {
        intent.extras?.let {
            streamId = it.getInt(EXTRA_STREAM_ID)
            val stream = viewModel.getStreamById(streamId ?: -1)
            latitude = stream?.latitude ?: 0.0
            longitude = stream?.longitude ?: 0.0
            altitude = stream?.altitude ?: 0.0

            deploymentId = it.getInt(EXTRA_DEPLOYMENT_ID)
            selectedProject = it.getInt(EXTRA_PROJECT_ID)
            device = it.getString(EXTRA_DEVICE)
        }
    }

    private fun setLatLng(latitude: Double, longitude: Double, altitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
        this.altitude = altitude
    }

    override fun onSelectedLocation(
        latitude: Double,
        longitude: Double,
        siteId: Int,
        name: String
    ) {
        setLatLng(latitude, longitude, altitude)
        startFragment(EditDeploymentSiteFragment.newInstance(latitude, longitude, altitude, siteId))
    }

    override fun startMapPickerPage(
        latitude: Double,
        longitude: Double,
        altitude: Double,
        streamId: Int
    ) {
        setLatLng(latitude, longitude, altitude)
        startFragment(MapPickerFragment.newInstance(latitude, longitude, altitude, streamId))
    }

    override fun updateDeploymentDetail(name: String, altitude: Double) {
        val stream = viewModel.getStreamById(streamId ?: -1)
        stream?.let { it ->
            viewModel.editStream(
                id = it.id,
                locationName = name,
                latitude = latitude,
                longitude = longitude,
                altitude = altitude,
                selectedProject ?: -1
            )
        }
        viewModel.markDeploymentNeedUpdate(deploymentId ?: -1)
        DeploymentSyncWorker.enqueue(this@EditDeploymentSiteActivity)
        finish()
    }

    private fun startFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.container.id, fragment)
            .commit()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbarDefault)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Edit location"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    companion object {
        const val EXTRA_STREAM_ID = "EXTRA_STREAM_ID"
        const val EXTRA_DEPLOYMENT_ID = "EXTRA_DEPLOYMENT_ID"
        const val EXTRA_PROJECT_ID = "EXTRA_PROJECT_ID"
        const val EXTRA_DEVICE = "EXTRA_DEVICE"

        fun startActivity(
            context: Context,
            streamId: Int,
            deploymentId: Int,
            projectId: Int,
            device: String,
            requestCode: Int
        ) {
            val intent = Intent(context, EditDeploymentSiteActivity::class.java)
            intent.putExtra(EXTRA_STREAM_ID, streamId)
            intent.putExtra(EXTRA_DEPLOYMENT_ID, deploymentId)
            intent.putExtra(EXTRA_PROJECT_ID, projectId)
            intent.putExtra(EXTRA_DEVICE, device)
            (context as Activity).startActivityForResult(intent, requestCode)
        }
    }
}
