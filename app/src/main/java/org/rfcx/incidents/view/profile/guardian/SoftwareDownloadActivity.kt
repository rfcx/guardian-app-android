package org.rfcx.incidents.view.profile.guardian

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.databinding.ActivitySoftwareDownloadBinding
import org.rfcx.incidents.entity.guardian.GuardianFile

class SoftwareDownloadActivity : AppCompatActivity(), GuardianFileEventListener {

    private lateinit var binding: ActivitySoftwareDownloadBinding
    private val viewModel: GuardianFileDownloadViewModel by viewModel()
    private val softwareAdapter by lazy { GuardianFileDownloadAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySoftwareDownloadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar("Software Download")

        viewModel.getSoftwareItem()

        binding.softwareRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = softwareAdapter
        }

        collectStates()
    }

    fun setupToolbar(title: String) {
        setSupportActionBar(binding.toolbarLayout.toolbarDefault)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            this.title = title
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun collectStates() {
        lifecycleScope.launch {
            launch { getSoftwareState() }
            launch { downloadSoftwareState() }
            launch { deleteSoftwareState() }
        }
    }

    private fun getSoftwareState() {
        lifecycleScope.launch {
            viewModel.guardianFileItemState.collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        Toast.makeText(this@SoftwareDownloadActivity, result.throwable.message, Toast.LENGTH_LONG).show()
                    }
                    Result.Loading -> {
                        binding.softwareLoading.visibility = View.VISIBLE
                        binding.softwareRecyclerView.visibility = View.GONE
                    }
                    is Result.Success -> {
                        if (result.data.isEmpty()) {
                            binding.noSoftwareItem.visibility = View.VISIBLE
                        } else {
                            binding.softwareRecyclerView.visibility = View.VISIBLE
                            softwareAdapter.availableFiles = result.data
                        }
                        binding.softwareLoading.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun downloadSoftwareState() {
        lifecycleScope.launch {
            viewModel.downloadGuardianFileState.collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        softwareAdapter.hideLoading()
                        Toast.makeText(this@SoftwareDownloadActivity, result.throwable.message, Toast.LENGTH_LONG).show()
                    }
                    Result.Loading -> softwareAdapter.showLoading()
                    is Result.Success -> {
                        softwareAdapter.hideLoading()
                    }
                }
            }
        }
    }

    private fun deleteSoftwareState() {
        lifecycleScope.launch {
            viewModel.deleteGuardianFileState.collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        softwareAdapter.hideLoading()
                        Toast.makeText(this@SoftwareDownloadActivity, result.throwable.message, Toast.LENGTH_LONG).show()
                    }
                    Result.Loading -> softwareAdapter.showLoading()
                    is Result.Success -> {
                        softwareAdapter.hideLoading()
                    }
                }
            }
        }
    }

    override fun onDownloadClicked(file: GuardianFile) {
        viewModel.download(file)
    }

    override fun onDeleteClicked(file: GuardianFile) {
        viewModel.delete(file)
    }

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, SoftwareDownloadActivity::class.java)
            context.startActivity(intent)
        }
    }
}
