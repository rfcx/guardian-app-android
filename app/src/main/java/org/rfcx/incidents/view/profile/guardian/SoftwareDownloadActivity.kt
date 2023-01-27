package org.rfcx.incidents.view.profile.guardian

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.databinding.ActivitySoftwareDownloadBinding

class SoftwareDownloadActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySoftwareDownloadBinding
    private val viewModel: SoftwareDownloadViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySoftwareDownloadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.getSoftwareFromRemote()
        setupToolbar()
        setToolbarTitle("Software Download")
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
}
