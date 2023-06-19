package org.rfcx.incidents.view.profile.guardian

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ActivityUnsentRegistrationBinding
import org.rfcx.incidents.entity.guardian.registration.GuardianRegistration

class UnsentGuardianRegistrationActivity : AppCompatActivity(), RegisterButtonListener {
    private lateinit var binding: ActivityUnsentRegistrationBinding
    private val viewModel: UnsentRegistrationViewModel by viewModel()
    private val registrationAdapter by lazy { UnsentGuardianRegistrationAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_unsent_registration)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        setContentView(binding.root)

        setupToolbar()

        binding.registrationView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = registrationAdapter
        }

        lifecycleScope.launch {
            viewModel.registrations.collectLatest { result ->
                registrationAdapter.registrations = result
            }
        }

        lifecycleScope.launch {
            viewModel.registrationError.collectLatest { result ->
                Toast.makeText(this@UnsentGuardianRegistrationActivity, result, Toast.LENGTH_LONG).show()
            }
        }
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

    override fun onClicked(registration: GuardianRegistration) {
        viewModel.register(registration)
    }

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, UnsentGuardianRegistrationActivity::class.java)
            context.startActivity(intent)
        }
    }
}
