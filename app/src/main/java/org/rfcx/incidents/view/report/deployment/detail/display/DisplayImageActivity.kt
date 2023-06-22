package org.rfcx.incidents.view.report.deployment.detail.display

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ActivityDisplayImageBinding

class DisplayImageActivity : AppCompatActivity() {

    lateinit var binding: ActivityDisplayImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_display_image)
        binding.lifecycleOwner = this
        setContentView(binding.root)

        val paths = intent.extras?.getStringArray(PATH_IMAGE) ?: arrayOf()
        val labels = intent.extras?.getStringArray(LABEL_IMAGE) ?: arrayOf()
        setupToolbar()

        val adapter = DisplayImageAdapter(paths.toList(), this)
        binding.imageViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.imageViewPager.adapter = adapter

        binding.imageViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setupToolbarTitle(labels[position])
            }
        })
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbarDefault)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    private fun setupToolbarTitle(name: String) {
        supportActionBar?.apply {
            title = name
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        finish()
    }

    companion object {
        const val PATH_IMAGE = "PATH_IMAGE"
        const val LABEL_IMAGE = "LABEL_IMAGE"

        fun startActivity(context: Context, paths: Array<String>, label: Array<String>) {
            val intent = Intent(context, DisplayImageActivity::class.java)
            intent.putExtra(PATH_IMAGE, paths)
            intent.putExtra(LABEL_IMAGE, label)
            context.startActivity(intent)
        }
    }
}
