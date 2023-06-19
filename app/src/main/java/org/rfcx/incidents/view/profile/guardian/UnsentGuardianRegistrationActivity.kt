package org.rfcx.incidents.view.profile.guardian

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ActivityUnsentRegistrationBinding

class UnsentGuardianRegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUnsentRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_unsent_registration)
        binding.lifecycleOwner = this
        setContentView(binding.root)

        setupToolbar()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbarDefault)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            this.title = "Unsent Guardian registration"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, UnsentGuardianRegistrationActivity::class.java)
            context.startActivity(intent)
        }
    }
}
