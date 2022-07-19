package org.rfcx.incidents.view.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.rfcx.incidents.data.preferences.Preferences
import org.rfcx.incidents.databinding.ActivityLoginNewBinding
import org.rfcx.incidents.util.getTokenID
import org.rfcx.incidents.util.getUserNickname
import org.rfcx.incidents.util.setupDisplayTheme
import org.rfcx.incidents.view.MainActivity
import org.rfcx.incidents.view.base.BaseActivity

class LoginActivity : BaseActivity(), LoginListener {

    private lateinit var binding: ActivityLoginNewBinding

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginNewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupDisplayTheme()

        val preferenceHelper = Preferences.getInstance(this)
        val selectedProject = preferenceHelper.getString(Preferences.SELECTED_PROJECT, "")

        if (this.getTokenID() != null && selectedProject != "" && getUserNickname().substring(0, 1) != "+") {
            openMain()
        } else {
            openLoginFragment()
        }
    }

    override fun handleOpenPage() {
        val preferenceHelper = Preferences.getInstance(this)
        val selectedProject = preferenceHelper.getString(Preferences.SELECTED_PROJECT, "")

        when {
            getUserNickname().substring(0, 1) == "+" -> {
                openSetUserNameFragmentFragment()
            }
            selectedProject == "" -> {
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
        MainActivity.startActivity(this@LoginActivity, getEventFromIntentIfHave(intent))
        finish()
    }

    override fun openSetUserNameFragmentFragment() {
        supportFragmentManager.beginTransaction()
            .replace(
                binding.loginContainer.id, SetUserNameFragment(),
                "SetUserNameFragment"
            ).commit()
    }

    override fun openSetProjectsFragment() {
        supportFragmentManager.beginTransaction()
            .replace(
                binding.loginContainer.id, SetProjectsFragment(),
                "SetProjectsFragment"
            ).commit()
    }

    override fun openLoginFragment() {
        supportFragmentManager.beginTransaction()
            .replace(
                binding.loginContainer.id, LoginFragment(),
                "LoginFragment"
            ).commit()
    }

    private fun getEventFromIntentIfHave(intent: Intent?): String? {
        if (intent?.hasExtra("streamId") == true) {
            return intent.getStringExtra("streamId")
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
