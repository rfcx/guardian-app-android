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
import kotlinx.android.synthetic.main.activity_password_change.*
import org.rfcx.ranger.R

class PasswordChangeActivity : AppCompatActivity() {
	private var menu: Menu? = null
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_password_change)
		
		setupToolbar()
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
			Toast.makeText(this, "New Password Empty", Toast.LENGTH_LONG).show()
			
		} else if (newPasswordAgainEditText.text.isNullOrEmpty()) {
			Toast.makeText(this, "Confirm new password Empty", Toast.LENGTH_LONG).show()
			
		} else if (newPasswordEditText.text.toString() == newPasswordAgainEditText.text.toString()) {
			Toast.makeText(this, "newPassword same", Toast.LENGTH_LONG).show()
			
		} else {
			Toast.makeText(this, "not same", Toast.LENGTH_LONG).show()
			
		}
	}
	
	private fun setupToolbar() {
		setSupportActionBar(toolbar)
		supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			elevation = 0f
			title = getString(R.string.profile_send_feedback)
		}
	}
	
	private fun View.hideKeyboard() = this.let {
		val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.hideSoftInputFromWindow(windowToken, 0)
	}
	
	override fun onSupportNavigateUp(): Boolean {
		onBackPressed()
		return true
	}
	
	companion object {
		fun startActivity(context: Context) {
			val intent = Intent(context, PasswordChangeActivity::class.java)
			context.startActivity(intent)
		}
	}
}
