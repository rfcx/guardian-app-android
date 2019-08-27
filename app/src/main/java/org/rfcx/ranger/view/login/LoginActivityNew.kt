package org.rfcx.ranger.view.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_login_new.*
import org.rfcx.ranger.R
import org.rfcx.ranger.util.CredentialKeeper
import org.rfcx.ranger.util.getSiteName
import org.rfcx.ranger.view.MainActivityNew
import org.rfcx.ranger.view.base.BaseActivity


// TODO change class name
class LoginActivityNew : BaseActivity(), LoginListener {
	
	companion object {
		fun startActivity(context: Context) {
			val intent = Intent(context, LoginActivityNew::class.java)
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			context.startActivity(intent)
		}
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_login_new)
		
		if (CredentialKeeper(this).hasValidCredentials() && getSiteName().isNotEmpty()) {
			MainActivityNew.startActivity(this@LoginActivityNew)
			finish()
		} else {
			supportFragmentManager.beginTransaction()
					.add(loginContainer.id, LoginFragment(),
							"LoginFragment").commit()
		}
	}
	
	override fun openMain() {
		MainActivityNew.startActivity(this@LoginActivityNew)
		finish()
	}
	
	override fun openInvitationCodeFragment() {
		supportFragmentManager.beginTransaction()
				.replace(loginContainer.id, InvitationCodeFragment(),
						"InvitationCodeFragment").commit()
	}
}

interface LoginListener {
	fun openMain()
	fun openInvitationCodeFragment()
}
