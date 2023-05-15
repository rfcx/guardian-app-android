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
import org.rfcx.incidents.R
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.databinding.ActivityClassifierDownloadBinding
import org.rfcx.incidents.entity.guardian.file.GuardianFile

class ClassifierDownloadActivity : AppCompatActivity(), GuardianFileEventListener {

    private lateinit var binding: ActivityClassifierDownloadBinding
    private val viewModel: GuardianFileDownloadViewModel by viewModel()
    private val classifierAdapter by lazy { GuardianFileDownloadAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityClassifierDownloadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(getString(R.string.classifier_download_title))

        viewModel.getClassifierItem()

        binding.classifierRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = classifierAdapter
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
        onBackPressed()
        return true
    }

    private fun collectStates() {
        lifecycleScope.launch {
            launch { getClassifierState() }
            launch { downloadClassifierState() }
            launch { deleteClassifierState() }
        }
    }

    private fun getClassifierState() {
        lifecycleScope.launch {
            viewModel.guardianFileItemState.collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        Toast.makeText(this@ClassifierDownloadActivity, result.throwable.message, Toast.LENGTH_LONG).show()
                    }
                    Result.Loading -> {
                        binding.classifierLoading.visibility = View.VISIBLE
                        binding.classifierRecyclerView.visibility = View.GONE
                    }
                    is Result.Success -> {
                        if (result.data.isEmpty()) {
                            binding.noClassifierItem.visibility = View.VISIBLE
                        } else {
                            binding.classifierRecyclerView.visibility = View.VISIBLE
                            classifierAdapter.availableFiles = result.data
                        }
                        binding.classifierLoading.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun downloadClassifierState() {
        lifecycleScope.launch {
            viewModel.downloadGuardianFileState.collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        classifierAdapter.hideLoading()
                        Toast.makeText(this@ClassifierDownloadActivity, result.throwable.message, Toast.LENGTH_LONG).show()
                    }
                    Result.Loading -> classifierAdapter.showLoading()
                    is Result.Success -> {
                        classifierAdapter.hideLoading()
                    }
                }
            }
        }
    }

    private fun deleteClassifierState() {
        lifecycleScope.launch {
            viewModel.deleteGuardianFileState.collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        classifierAdapter.hideLoading()
                        Toast.makeText(this@ClassifierDownloadActivity, result.throwable.message, Toast.LENGTH_LONG).show()
                    }
                    Result.Loading -> classifierAdapter.showLoading()
                    is Result.Success -> {
                        classifierAdapter.hideLoading()
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
            val intent = Intent(context, ClassifierDownloadActivity::class.java)
            context.startActivity(intent)
        }
    }
}
