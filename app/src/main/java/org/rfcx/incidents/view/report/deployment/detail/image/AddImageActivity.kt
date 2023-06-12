package org.rfcx.incidents.view.report.deployment.detail.image

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ActivityAddPhotosBinding
import org.rfcx.incidents.view.guardian.checklist.photos.AddPhotosFragment
import org.rfcx.incidents.view.report.deployment.detail.edit.EditDeploymentSiteActivity

class AddImageActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddPhotosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_photos)
        binding.lifecycleOwner = this
        setContentView(binding.root)

        setupToolbar()

        startFragment(
            AddPhotosFragment.newInstance()
        )
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

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, AddImageActivity::class.java)
            (context as Activity).startActivity(intent)
        }
    }
}
