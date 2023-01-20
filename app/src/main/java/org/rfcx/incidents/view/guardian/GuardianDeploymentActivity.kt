package org.rfcx.incidents.view.guardian

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.rfcx.incidents.databinding.ActivityGuardianDeploymentBinding
import org.rfcx.incidents.view.guardian.connect.GuardianConnectFragment
import org.rfcx.incidents.view.report.create.CreateReportActivity

class GuardianDeploymentActivity : AppCompatActivity() {

    lateinit var binding: ActivityGuardianDeploymentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Binding view
        binding = ActivityGuardianDeploymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show guardian connect screen first
        showScreen(GuardianScreen.CONNECT)
    }

    private fun showScreen(screen: GuardianScreen) {
        when (screen) {
            GuardianScreen.CONNECT -> startFragment(GuardianConnectFragment.newInstance())
        }
    }

    private fun startFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.contentContainer.id, fragment)
            .commit()
    }
    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, GuardianDeploymentActivity::class.java)
            context.startActivity(intent)
        }
    }

}