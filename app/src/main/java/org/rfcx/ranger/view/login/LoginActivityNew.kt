package org.rfcx.ranger.view.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_login_new.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.util.CredentialKeeper
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.getUserNickname
import org.rfcx.ranger.view.MainActivity
import org.rfcx.ranger.view.base.BaseActivity

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
		
		val preferenceHelper = Preferences.getInstance(this)
		val isConsentGiven = preferenceHelper.getBoolean(Preferences.CONSENT_GIVEN, false)
		val selectedProject = preferenceHelper.getInt(Preferences.SELECTED_PROJECT, -1)
		
		if (CredentialKeeper(this).hasValidCredentials() && isConsentGiven && selectedProject != -1 && getUserNickname().substring(0, 1) != "+") {
			openMain()
		} else {
			openLoginFragment()
		}
	}
	
	override fun handleOpenPage() {
		val preferenceHelper = Preferences.getInstance(this)
		val isConsentGiven = preferenceHelper.getBoolean(Preferences.CONSENT_GIVEN, false)
		val selectedProject = preferenceHelper.getInt(Preferences.SELECTED_PROJECT, -1)
		
		when {
			getUserNickname().substring(0, 1) == "+" -> {
				openSetUserNameFragmentFragment()
			}
			(!isConsentGiven) -> {
				openTermsAndServiceFragment()
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
	
	override fun openTermsAndServiceFragment() {
		supportFragmentManager.beginTransaction()
				.replace(loginContainer.id, TermsAndServiceFragment(),
						"TermsAndServiceFragment").commit()
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
		if (intent?.hasExtra("event_guid") == true) {
			return intent.getStringExtra("event_guid")
		}
		return null
	}
}

interface LoginListener {
	fun openMain()
	fun openSetUserNameFragmentFragment()
	fun openTermsAndServiceFragment()
	fun openSetProjectsFragment()
	fun openLoginFragment()
	fun handleOpenPage()
}
