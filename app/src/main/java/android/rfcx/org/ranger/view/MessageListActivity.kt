package android.rfcx.org.ranger.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.rfcx.org.ranger.R
import android.rfcx.org.ranger.adapter.MessageAdapter
import android.rfcx.org.ranger.entity.Message
import android.rfcx.org.ranger.repo.TokenExpireException
import android.rfcx.org.ranger.repo.api.MessageApi
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.android.synthetic.main.activity_message_list.*

class MessageListActivity : AppCompatActivity() {
    private val REQUEST_CODE_GOOGLE_AVAILABILITY = 100
    lateinit var messageAdapter: MessageAdapter

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, MessageListActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        checkGoogleApiAvailability()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)
        initToolbar()
        initAdapter()
        getMessageList()

        messageSwipeRefresh.setOnRefreshListener {
            getMessageList()
        }
    }


    private fun initToolbar() {
        setSupportActionBar(messageToolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun initAdapter() {
        messageAdapter = MessageAdapter()
        messageRecyclerView.setHasFixedSize(true)
        messageRecyclerView.layoutManager = LinearLayoutManager(this@MessageListActivity)
        messageRecyclerView.adapter = messageAdapter
    }

    private fun getMessageList() {
        messageSwipeRefresh.isRefreshing = true

        MessageApi().getMessage(this@MessageListActivity, object : MessageApi.OnMessageCallBack {
            override fun onSuccess(messages : List<Message>) {
                messageSwipeRefresh.isRefreshing = false
                messageAdapter.updateMessages(messages)
            }

            override fun onFailed(t: Throwable?, message: String?) {
                messageSwipeRefresh.isRefreshing = false
                if (t is TokenExpireException) {
                    LoginActivity.startActivity(this@MessageListActivity)
                    finish()
                    return
                }
                val error: String = if (message.isNullOrEmpty()) getString(R.string.error_common) else message!!
                Snackbar.make(messageParentView, error, Snackbar.LENGTH_LONG).show()
            }
        }
        )
    }

    private fun checkGoogleApiAvailability() {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val statusCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
        val isResultSuccess = statusCode == ConnectionResult.SUCCESS
        val isErrorCanBeRecover = googleApiAvailability.isUserResolvableError(statusCode)

        if (!isResultSuccess && isErrorCanBeRecover) {
            googleApiAvailability.showErrorDialogFragment(this, statusCode, REQUEST_CODE_GOOGLE_AVAILABILITY)
        }
    }

}
