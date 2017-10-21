package android.rfcx.org.ranger.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.rfcx.org.ranger.entity.message.Message
import android.rfcx.org.ranger.repo.api.MessageApi
import android.util.Log

/**
 * Created by Jingjoeh on 10/21/2017 AD.
 */
class MessageService : BroadcastReceiver() {
    private val TAG = MessageService::class.java.simpleName
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive")

        MessageApi().getMessage(context!!,object : MessageApi.OnMessageCallBack{
            override fun onFailed(t: Throwable?, message: String?) {
                Log.e(TAG, message)
            }

            override fun onSuccess(messages: List<Message>) {
               Log.i(TAG, messages.size.toString())
            }

        })

    }
}