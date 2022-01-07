package org.rfcx.incidents.view.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_login_new.*
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.event.Event
import org.rfcx.incidents.util.CredentialKeeper
import org.rfcx.incidents.util.Preferences
import org.rfcx.incidents.util.getUserNickname
import org.rfcx.incidents.util.setupDisplayTheme
import org.rfcx.incidents.view.MainActivity
import org.rfcx.incidents.view.base.BaseActivity

// TODO change class name
class LoginActivityNew : BaseActivity(), LoginListener {
	
	private var eventFromNotification: Event? = null
	
	companion object {
		fun startActivity(context: Context) {
			val intent = Intent(context, LoginActivityNew::class.java)
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
			context.startActivity(intent)
		}
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_login_new)
		setupDisplayTheme()
		
		val preferenceHelper = Preferences.getInstance(this)
		val selectedProject = preferenceHelper.getInt(Preferences.SELECTED_PROJECT, -1)
		
		if (CredentialKeeper(this).hasValidCredentials() && selectedProject != -1 && getUserNickname().substring(0, 1) != "+") {
			openMain()
		} else {
			openLoginFragment()
		}
	}
	
	override fun handleOpenPage() {
		val preferenceHelper = Preferences.getInstance(this)
		val selectedProject = preferenceHelper.getInt(Preferences.SELECTED_PROJECT, -1)
		
		when {
			getUserNickname().substring(0, 1) == "+" -> {
				openSetUserNameFragmentFragment()
			}
			selectedProject == -1 -> {
				openSetProjectsFragment()
			}
			else -> {
				openMain()
			}
		}
	}
	
	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)
		getEventFromIntentIfHave(intent)
	}
	
	override fun openMain() {
		MainActivity.startActivity(this@LoginActivityNew, getEventFromIntentIfHave(intent))
		finish()
	}
	
	override fun openSetUserNameFragmentFragment() {
		supportFragmentManager.beginTransaction()
				.replace(loginContainer.id, SetUserNameFragment(),
						"SetUserNameFragment").commit()
		
	}
	
	override fun openSetProjectsFragment() {
		supportFragmentManager.beginTransaction()
				.replace(loginContainer.id, SetProjectsFragment(),
						"SetProjectsFragment").commit()
	}
	
	override fun openLoginFragment() {
		supportFragmentManager.beginTransaction()
				.replace(loginContainer.id, LoginFragment(),
						"LoginFragment").commit()
		
	}
	
	private fun getEventFromIntentIfHave(intent: Intent?): String? {
		if (intent?.hasExtra("streamName") == true) {
			return intent.getStringExtra("streamName")
		}
		return null
	}
}

interface LoginListener {
	fun openMain()
	fun openSetUserNameFragmentFragment()
	fun openSetProjectsFragment()
	fun openLoginFragment()
	fun handleOpenPage()
}
