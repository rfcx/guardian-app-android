package org.rfcx.incidents.view.report.deployment.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ActivityDeploymentDetailBinding

class DeploymentDetailActivity : AppCompatActivity() {

    private val deploymentImageAdapter by lazy { DeploymentImageAdapter() }
    lateinit var binding: ActivityDeploymentDetailBinding
    private val viewModel: DeploymentDetailViewModel by viewModel()

    private var streamId = -1
    private var toAddImage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_deployment_detail)
        binding.lifecycleOwner = this
        setContentView(binding.root)

        binding.viewModel = viewModel

        getExtra()
        setupToolbar()

        startFragment(DeploymentDetailFragment.newInstance(streamId))

        lifecycleScope.launch {
            viewModel.stream.collectLatest {
                supportActionBar?.title = it?.name
            }
        }
    }

    private fun getExtra() {
        intent.extras?.getInt(EXTRA_STREAM_ID)?.let {
            streamId = it
            viewModel.setStreamId(it)
        }
    }

    private fun startFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.deploymentDetailContainer.id, fragment)
            .commit()
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

    companion object {
        private const val EXTRA_STREAM_ID = "EXTRA_STREAM_ID"
        fun startActivity(context: Context, streamId: Int) {
            val intent = Intent(context, DeploymentDetailActivity::class.java)
            intent.putExtra(EXTRA_STREAM_ID, streamId)
            context.startActivity(intent)
        }
    }
}
