package org.rfcx.incidents.view.report.deployment.detail.edit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ActivityEditLocationBinding
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.view.guardian.checklist.site.MapPickerFragment

class EditDeploymentSiteActivity : AppCompatActivity(), EditDeploymentSiteListener {
    lateinit var binding: ActivityEditLocationBinding

    private var streamId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_location)
        binding.lifecycleOwner = this
        setContentView(binding.root)

        setupToolbar()
        initIntent()

        startFragment(
            EditDeploymentSiteFragment.newInstance(streamId ?: -1)
        )
    }

    private fun initIntent() {
        intent.extras?.let {
            streamId = it.getInt(EXTRA_STREAM_ID)
        }
    }

    override fun startMapPickerPage(
        site: Stream
    ) {
        startFragment(MapPickerFragment.newInstance(site))
    }

    override fun backToEditPage(site: Stream) {
        startFragment(
            EditDeploymentSiteFragment.newInstance(site.id, site.name, site.latitude, site.longitude)
        )
    }

    override fun finishEdit() {
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

        fun startActivity(
            context: Context,
            streamId: Int
        ) {
            val intent = Intent(context, EditDeploymentSiteActivity::class.java)
            intent.putExtra(EXTRA_STREAM_ID, streamId)
            (context as Activity).startActivity(intent)
        }
    }
}
