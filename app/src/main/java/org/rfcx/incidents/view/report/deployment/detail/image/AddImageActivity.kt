package org.rfcx.incidents.view.report.deployment.detail.image

import android.app.Activity
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
import org.rfcx.incidents.databinding.ActivityAddPhotosBinding
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.view.guardian.checklist.photos.AddPhotosFragment
import org.rfcx.incidents.view.guardian.checklist.photos.Image

class AddImageActivity : AppCompatActivity(), AddImageListener {
    lateinit var binding: ActivityAddPhotosBinding
    private val viewModel: AddImageViewModel by viewModel()

    private var streamId = -1
    private var stream: Stream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_photos)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        setContentView(binding.root)

        getExtra()
        setupToolbar()

        startFragment(
            AddPhotosFragment.newInstance()
        )

        lifecycleScope.launch {
            viewModel.stream.collectLatest {
                if (it != null) {
                    stream = it
                }
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
            .replace(binding.container.id, fragment)
            .commit()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbarDefault)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Add photos"
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

    override fun saveImages(images: List<Image>) {
        saveImages(images)
    }

    override fun getImages(): List<Image> {
        return viewModel.getImages() ?: return listOf()
    }

    override fun openDetailScreen() {
        finish()
    }

    companion object {
        private const val EXTRA_STREAM_ID = "EXTRA_STREAM_ID"
        fun startActivity(context: Context, streamId: Int) {
            val intent = Intent(context, AddImageActivity::class.java)
            intent.putExtra(EXTRA_STREAM_ID, streamId)
            (context as Activity).startActivity(intent)
        }
    }
}
