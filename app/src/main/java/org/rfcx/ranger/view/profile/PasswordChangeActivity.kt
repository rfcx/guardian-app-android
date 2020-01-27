package org.rfcx.ranger.view.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
		sendFeedbackView.hideKeyboard()
		
		if (newPasswordEditText.text.isNullOrEmpty()) {
			Toast.makeText(this, getString(R.string.please_enter_your_new_password), Toast.LENGTH_SHORT).show()
			
		} else if (newPasswordAgainEditText.text.isNullOrEmpty()) {
			Toast.makeText(this, getString(R.string.please_confirm_your_new_password), Toast.LENGTH_SHORT).show()
			
		} else if (newPasswordEditText.text!!.length < 6 || newPasswordAgainEditText.text!!.length < 6) {
			Toast.makeText(this, getString(R.string.password_must_have_at_least_6_characters), Toast.LENGTH_SHORT).show()
			
		} else if (newPasswordEditText.text.toString() == newPasswordAgainEditText.text.toString()) {
			passwordChangeViewModel.changeUserPassword(newPasswordEditText.text.toString())
		} else {
			Toast.makeText(this, getString(R.string.confirm_password_does_not_match), Toast.LENGTH_SHORT).show()
		}
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
