package org.rfcx.ranger.view.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.crashlytics.android.core.CrashlyticsCore
import kotlinx.android.synthetic.main.activity_login_new.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.util.CredentialKeeper
import org.rfcx.ranger.util.getSiteName
import org.rfcx.ranger.util.getUserNickname
import org.rfcx.ranger.view.MainActivityNew
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
		val eventGuId = getEventFromIntentIfHave(intent)
		if (CredentialKeeper(this).hasValidCredentials() && getSiteName().isNotEmpty() && getUserNickname().substring(0, 1) != "+") {
			MainActivityNew.startActivity(this@LoginActivityNew, eventGuId)
			finish()
		} else {
			supportFragmentManager.beginTransaction()
					.add(loginContainer.id, LoginFragment(),
							"LoginFragment").commit()
		}
	}
	
	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)
		getEventFromIntentIfHave(intent)
	}
	
	override fun openMain() {
		MainActivityNew.startActivity(this@LoginActivityNew, getEventFromIntentIfHave(intent))
		finish()
	}
	
	override fun openInvitationCodeFragment() {
		supportFragmentManager.beginTransaction()
				.replace(loginContainer.id, InvitationCodeFragment(),
						"InvitationCodeFragment").commit()
	}
	
	
	override fun openSetUserNameFragmentFragment() {
		supportFragmentManager.beginTransaction()
				.replace(loginContainer.id, SetUserNameFragment(),
						"SetUserNameFragment").commit()
		
	}
	
	private fun getEventFromIntentIfHave(intent: Intent?) :String? {
		if (intent?.hasExtra("event_guid") == true) {
			return intent.getStringExtra("event_guid")
		}
		return null
	}
}

interface LoginListener {
	fun openMain()
	fun openInvitationCodeFragment()
	fun openSetUserNameFragmentFragment()
}
