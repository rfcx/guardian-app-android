package org.rfcx.incidents.view.report.deployment.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ActivityDeploymentDetailBinding
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.latitudeCoordinates
import org.rfcx.incidents.util.longitudeCoordinates
import org.rfcx.incidents.util.setFormatLabel
import org.rfcx.incidents.view.report.deployment.detail.edit.EditDeploymentSiteActivity

class DeploymentDetailActivity : AppCompatActivity() {

    private val deploymentImageAdapter by lazy { DeploymentImageAdapter() }
    lateinit var binding: ActivityDeploymentDetailBinding
    private val viewModel: DeploymentDetailViewModel by viewModel()

    private var streamId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_deployment_detail)
        binding.lifecycleOwner = this
        setContentView(binding.root)

        binding.viewModel = viewModel

        setMap(savedInstanceState)
        getExtra()
        setupToolbar()
        setupImageRecycler()

        lifecycleScope.launch {
            viewModel.stream.collectLatest {
                updateDeploymentDetailView(it)
                if (it != null) {
                    val siteLoc = LatLng(it.latitude, it.longitude)
                    binding.mapBoxView.setSiteLocation(siteLoc)
                }
            }
        }

        binding.editButton.setOnClickListener {
            EditDeploymentSiteActivity.startActivity(this, streamId)
        }
    }

    private fun getExtra() {
        intent.extras?.getInt(EXTRA_STREAM_ID)?.let {
            streamId = it
            viewModel.setStreamId(it)
        }
    }

    private fun setMap(savedInstanceState: Bundle?) {
        binding.mapBoxView.onCreate(savedInstanceState)
        binding.mapBoxView.setParam(canMove = false, fromDeploymentList = false, showPin = false)
    }

    private fun updateDeploymentDetailView(stream: Stream?) {
        supportActionBar?.title = stream?.name
        binding.latitudeValue.text = stream?.latitude.latitudeCoordinates()
        binding.longitudeValue.text = stream?.longitude.longitudeCoordinates()
        binding.altitudeValue.text = stream?.altitude?.setFormatLabel()
        stream?.deployment?.let { dp ->
            binding.deploymentIdTextView.text = dp.deploymentKey
        }
    }

    private fun setupImageRecycler() {
        binding.deploymentImageRecycler.apply {
            adapter = deploymentImageAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
        }
        deploymentImageAdapter.setImages(arrayListOf())
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbarDefault)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onStart() {
        super.onStart()
        binding.mapBoxView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapBoxView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapBoxView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapBoxView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapBoxView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        binding.mapBoxView.onSaveInstanceState(outState)
    }

    companion object {
        private const val EXTRA_STREAM_ID = "EXTRA_STREAM_ID"
        fun startActivity(context: Context, streamId: Int) {
            val intent = Intent(context, DeploymentDetailActivity::class.java)
            intent.putExtra(EXTRA_STREAM_ID, streamId)
            context.startActivity(intent)
        }
    }
}
