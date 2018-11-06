package org.rfcx.ranger.service

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.rfcx.ranger.view.MessageListActivity

class MyFireBaseMessagingService : FirebaseMessagingService() {
	
	override fun onMessageReceived(remoteMessage: RemoteMessage?) {
		super.onMessageReceived(remoteMessage)
		// getting new message
		// send Broadcast to @MessageListActivity to reload list
		val intent = Intent(MessageListActivity.INTENT_FILTER_MESSAGE_BROADCAST)
		applicationContext.sendBroadcast(intent)
	}
}