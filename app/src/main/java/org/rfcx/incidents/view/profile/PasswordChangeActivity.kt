package org.rfcx.incidents.view.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_password_change.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.data.remote.success

class PasswordChangeActivity : AppCompatActivity() {
    private val passwordChangeViewModel: PasswordChangeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_change)

        setupToolbar()
        newPasswordEditText.showKeyboard()

        newPasswordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                errorNewPasswordTextView.visibility = View.VISIBLE
                when {
                    newPasswordEditText.text.isNullOrEmpty() ->
                        errorNewPasswordTextView.text =
                            getString(R.string.please_enter_your_new_password)
                    newPasswordEditText.text!!.length < 6 ->
                        errorNewPasswordTextView.text =
                            getString(R.string.password_must_have_at_least_6_characters)
                    newPasswordEditText.text!!.length >= 6 -> errorNewPasswordTextView.visibility = View.GONE
                }
            }
        }

        newPasswordAgainEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                errorNewPasswordAgainTextView.visibility = View.VISIBLE
                when {
                    newPasswordAgainEditText.text.isNullOrEmpty() ->
                        errorNewPasswordAgainTextView.text =
                            getString(R.string.please_confirm_your_new_password)
                    newPasswordAgainEditText.text!!.length < 6 ->
                        errorNewPasswordAgainTextView.text =
                            getString(R.string.password_must_have_at_least_6_characters)
                    newPasswordAgainEditText.text!!.length >= 6 -> {
                        errorNewPasswordAgainTextView.visibility = View.GONE
                        updatePasswordButton.isEnabled = true
                    }
                }
            }
        }

        newPasswordAgainEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    if (!newPasswordAgainEditText.text.isNullOrEmpty()) {
                        if (newPasswordAgainEditText.text!!.length >= 6) {
                            updatePasswordButton.isEnabled = true
                            errorNewPasswordAgainTextView.visibility = View.GONE
                        } else {
                            updatePasswordButton.isEnabled = false
                        }
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d("addTextChangedListener", "beforeTextChanged")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d("addTextChangedListener", "onTextChanged")
            }
        })

        updatePasswordButton.setOnClickListener {
            if (newPasswordEditText.text.toString() == newPasswordAgainEditText.text.toString()) {
                errorNewPasswordAgainTextView.visibility = View.GONE
                passwordChangeViewModel.changeUserPassword(newPasswordEditText.text.toString())
                updatePasswordButton.hideKeyboard()
            } else {
                errorNewPasswordAgainTextView.visibility = View.VISIBLE
                errorNewPasswordAgainTextView.text = getString(R.string.confirm_password_does_not_match)
                updatePasswordButton.isEnabled = false
            }
        }

        passwordChangeViewModel.status.observe(
            this,
            Observer { it ->
                it.success({
                    if (it == "true") {
                        loadingProgress.visibility = View.INVISIBLE
                        Toast.makeText(this, getString(R.string.password_changed_successfully), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }, {
                    loadingProgress.visibility = View.INVISIBLE
                    Toast.makeText(this, getString(R.string.something_is_wrong), Toast.LENGTH_SHORT).show()
                }, {
                    // Loading block
                    loadingProgress.visibility = View.VISIBLE
                })
            }
        )
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            elevation = 0f
            title = getString(R.string.password)
        }
    }

    private fun View.hideKeyboard() = this.let {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun View.showKeyboard() = this.let {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        newPasswordEditText.hideKeyboard()
        newPasswordAgainEditText.hideKeyboard()
        return true
    }

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, PasswordChangeActivity::class.java)
            context.startActivity(intent)
        }
    }
}
