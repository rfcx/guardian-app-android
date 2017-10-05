package android.rfcx.org.ranger.view

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.rfcx.org.ranger.R
import android.rfcx.org.ranger.adapter.MessageAdapter
import android.rfcx.org.ranger.entity.Message
import android.rfcx.org.ranger.repo.api.MessageApi
import android.rfcx.org.ranger.repo.api.OnMessageCallBack
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_message_list.*

class MessageListActivity : AppCompatActivity() {

    lateinit var messageAdapter: MessageAdapter

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, MessageListActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)
        initToolbar()
        initAdapter()
        getMessageList()
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
        MessageApi().getMessage(this@MessageListActivity, object : OnMessageCallBack {
            override fun onSuccess(messages: List<Message>) {
                messageAdapter.updateMessages(messages)
            }

            override fun onFailed(t: Throwable?, message: String?) {
                Log.w("onFailed", "-- $message")
            }
        }
        )
    }

}
