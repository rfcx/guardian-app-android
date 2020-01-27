package org.rfcx.ranger.view.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_password_change.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.data.remote.success

class PasswordChangeActivity : AppCompatActivity() {
	private var menu: Menu? = null
	private val passwordChangeViewModel: PasswordChangeViewModel by viewModel()
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_password_change)
		
		setupToolbar()
		newPasswordEditText.showKeyboard()
		
		newPasswordEditText.addTextChangedListener(object : TextWatcher {
			override fun afterTextChanged(p0: Editable?) {
				if (p0 != null) {
					if (p0.isNotEmpty() && p0.length < 6) {
						errorNewPasswordTextView.text = getString(R.string.password_must_have_at_least_6_characters)
						errorNewPasswordTextView.visibility = View.VISIBLE
						DrawableCompat.setTint(newPasswordEditText.background, ContextCompat.getColor(this@PasswordChangeActivity, R.color.text_error))
					}
					
					if (p0.isNotEmpty() && p0.length >= 6) {
						errorNewPasswordTextView.visibility = View.GONE
						DrawableCompat.setTint(newPasswordEditText.background, ContextCompat.getColor(this@PasswordChangeActivity, R.color.gray_30))
					}
				}
			}
			
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
				Log.d("addTextChangedListener", "beforeTextChanged")
			}
			
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
				Log.d("addTextChangedListener","onTextChanged")
			}
		})
		
		newPasswordAgainEditText.addTextChangedListener(object : TextWatcher {
			override fun afterTextChanged(p0: Editable?) {
				if (p0 != null) {
					if (p0.isNotEmpty() && newPasswordEditText.text !== p0) {
						errorNewPasswordAgainTextView.text = getString(R.string.confirm_password_does_not_match)
						errorNewPasswordAgainTextView.visibility = View.VISIBLE
						DrawableCompat.setTint(newPasswordAgainEditText.background, ContextCompat.getColor(this@PasswordChangeActivity, R.color.text_error))
					}
					if (newPasswordEditText.text.toString() == newPasswordAgainEditText.text.toString()) {
						errorNewPasswordAgainTextView.visibility = View.GONE
						DrawableCompat.setTint(newPasswordAgainEditText.background, ContextCompat.getColor(this@PasswordChangeActivity, R.color.gray_30))
					}
				}
			}
			
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
				Log.d("addTextChangedListener", "beforeTextChanged")
			}
			
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
				Log.d("addTextChangedListener","onTextChanged")
			}
		})
		
		passwordChangeViewModel.status.observe(this, Observer { it ->
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
		})
	}
	
	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		this.menu = menu
		val inflater = menuInflater
		inflater.inflate(R.menu.password_change, menu)
		return super.onCreateOptionsMenu(menu)
	}
	
	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		when (item?.itemId) {
			android.R.id.home -> finish()
			R.id.passwordChangeView -> passwordChange()
		}
		return super.onOptionsItemSelected(item)
	}
	
	private fun passwordChange() {
		val sendFeedbackView = findViewById<View>(R.id.passwordChangeView)
		
		errorNewPasswordTextView.visibility = View.GONE
		DrawableCompat.setTint(newPasswordEditText.background, ContextCompat.getColor(this, R.color.gray_30))
		
		errorNewPasswordAgainTextView.visibility = View.GONE
		DrawableCompat.setTint(newPasswordAgainEditText.background, ContextCompat.getColor(this, R.color.gray_30))
		
		if (newPasswordEditText.text.isNullOrEmpty()) {
			errorNewPasswordTextView.text = getString(R.string.please_enter_your_new_password)
			errorNewPasswordTextView.visibility = View.VISIBLE
			DrawableCompat.setTint(newPasswordEditText.background, ContextCompat.getColor(this, R.color.text_error))
			
		} else if (newPasswordEditText.text!!.length < 6) {
			errorNewPasswordTextView.text = getString(R.string.password_must_have_at_least_6_characters)
			errorNewPasswordTextView.visibility = View.VISIBLE
			DrawableCompat.setTint(newPasswordEditText.background, ContextCompat.getColor(this, R.color.text_error))
			
		}  else if (newPasswordAgainEditText.text.isNullOrEmpty()) {
			errorNewPasswordAgainTextView.text = getString(R.string.please_confirm_your_new_password)
			errorNewPasswordAgainTextView.visibility = View.VISIBLE
			DrawableCompat.setTint(newPasswordAgainEditText.background, ContextCompat.getColor(this, R.color.text_error))
			
		}  else if (newPasswordAgainEditText.text!!.length < 6 ) {
			errorNewPasswordAgainTextView.text = getString(R.string.password_must_have_at_least_6_characters)
			errorNewPasswordAgainTextView.visibility = View.VISIBLE
			DrawableCompat.setTint(newPasswordAgainEditText.background, ContextCompat.getColor(this, R.color.text_error))
			
		} else if (newPasswordEditText.text.toString() == newPasswordAgainEditText.text.toString()) {
			passwordChangeViewModel.changeUserPassword(newPasswordEditText.text.toString())
			
		} else {
			errorNewPasswordAgainTextView.text = getString(R.string.confirm_password_does_not_match)
			errorNewPasswordAgainTextView.visibility = View.VISIBLE
			DrawableCompat.setTint(newPasswordAgainEditText.background, ContextCompat.getColor(this, R.color.text_error))		}
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
