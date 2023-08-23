package org.rfcx.incidents.view.report.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.databinding.ActivityResponseDetailBinding
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Screen

class ResponseDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_RESPONSE_CORE_ID = "EXTRA_RESPONSE_CORE_ID"

        fun startActivity(context: Context, responseCoreId: String) {
            val intent = Intent(context, ResponseDetailActivity::class.java)
            intent.putExtra(EXTRA_RESPONSE_CORE_ID, responseCoreId)
            context.startActivity(intent)
        }
    }

    lateinit var binding: ActivityResponseDetailBinding

    private val analytics by lazy { Analytics(this) }
    private val viewModel: ResponseDetailViewModel by viewModel()

    private var responseCoreId: String? = null
    private var response: Response? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResponseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        responseCoreId = intent?.getStringExtra(EXTRA_RESPONSE_CORE_ID)
        response = responseCoreId?.let { viewModel.getResponseByCoreId(it) }
        setupToolbar()
        startFragment(ResponseDetailFragment.newInstance(responseCoreId ?: ""))
    }

    private fun startFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.responseDetailContainer.id, fragment)
            .commit()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            elevation = 0f
            title = "#" + response?.incidentRef + " " + response?.streamName
        }
    }

    override fun onResume() {
        super.onResume()
        analytics.trackScreen(Screen.RESPONSE_DETAIL)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
