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
import org.rfcx.incidents.data.remote.guardian.software.SoftwareResponse
import org.rfcx.incidents.databinding.ActivitySoftwareDownloadBinding
import org.rfcx.incidents.view.guardian.checklist.CheckListAdapter

class SoftwareDownloadActivity : AppCompatActivity(), SoftwareEventListener {

    private lateinit var binding: ActivitySoftwareDownloadBinding
    private val viewModel: SoftwareDownloadViewModel by viewModel()
    private val softwareAdapter by lazy { SoftwareDownloadAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySoftwareDownloadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setToolbarTitle("Software Download")

        viewModel.getSoftwareFromRemote()

        binding.softwareRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = softwareAdapter
        }

        lifecycleScope.launch {
            viewModel.softwareItemState.collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        Toast.makeText(this@SoftwareDownloadActivity, result.throwable.message, Toast.LENGTH_LONG).show()
                    }
                    Result.Loading -> {
                        binding.softwareLoading.visibility = View.VISIBLE
                        binding.softwareRecyclerView.visibility = View.GONE
                    }
                    is Result.Success -> {
                        binding.softwareLoading.visibility = View.GONE
                        binding.softwareRecyclerView.visibility = View.VISIBLE
                        softwareAdapter.availableSoftwares = result.data
                    }
                }
            }
        }
    }

    fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbarDefault)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    fun setToolbarTitle(title: String) {
        supportActionBar?.apply {
            this.title = title
        }
    }

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, SoftwareDownloadActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onDownloadClicked(software: SoftwareResponse) {
        // TODO
    }

    override fun onDeleteClicked(software: SoftwareResponse) {
        // TODO
    }
}
