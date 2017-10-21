package android.rfcx.org.ranger.view

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.rfcx.org.ranger.R
import android.rfcx.org.ranger.adapter.MessageAdapter
import android.rfcx.org.ranger.adapter.OnMessageItemClickListener
import android.rfcx.org.ranger.entity.message.Message
import android.rfcx.org.ranger.repo.TokenExpireException
import android.rfcx.org.ranger.repo.api.MessageApi
import android.rfcx.org.ranger.service.SendLocationLocationService
import android.rfcx.org.ranger.util.PrefKey
import android.rfcx.org.ranger.util.PreferenceHelper
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.android.synthetic.main.activity_message_list.*
import android.app.AlarmManager
import android.app.PendingIntent
import android.os.SystemClock
import android.rfcx.org.ranger.service.MessageReciver


class MessageListActivity : AppCompatActivity(), OnMessageItemClickListener, ServiceConnection {

    private val REQUEST_CODE_GOOGLE_AVAILABILITY = 100
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    lateinit var messageAdapter: MessageAdapter
    private var sendLocationService: SendLocationLocationService? = null

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, MessageListActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        checkGoogleApiAvailability()

        if (!checkPermissions()) {
            Log.w("Permission", "grant")
            requestPermissions()
        } else {
            statLocationService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)
        initToolbar()
        initAdapter()
        getMessageList()
        startAlarmForMessageNotification()

        messageSwipeRefresh.setOnRefreshListener {
            getMessageList()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unbindService(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.ranger_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_logout -> {
                logout()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initToolbar() {
        setSupportActionBar(messageToolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun initAdapter() {
        messageAdapter = MessageAdapter(this@MessageListActivity)
        messageRecyclerView.setHasFixedSize(true)
        messageRecyclerView.layoutManager = LinearLayoutManager(this@MessageListActivity)
        messageRecyclerView.adapter = messageAdapter
    }

    private fun getMessageList() {
        messageSwipeRefresh.isRefreshing = true

        MessageApi().getMessage(this@MessageListActivity, object : MessageApi.OnMessageCallBack {
            override fun onSuccess(messages: List<Message>) {
                messageSwipeRefresh.isRefreshing = false
                messageAdapter.updateMessages(messages)
            }

            override fun onFailed(t: Throwable?, message: String?) {
                messageSwipeRefresh.isRefreshing = false
                if (t is TokenExpireException) {
                    logout()
                    return
                }
                val error: String = if (message.isNullOrEmpty()) getString(R.string.error_common) else message!!
                Snackbar.make(messageParentView, error, Snackbar.LENGTH_LONG).show()
            }
        }
        )
    }

    private fun logout() {
        try {
            sendLocationService?.stopSelf()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        PreferenceHelper.getInstance(this@MessageListActivity).remove(PrefKey.LOGIN_RESPONSE)
        LoginActivity.startActivity(this@MessageListActivity)
        finish()
    }

    override fun onMessageItemClick(position: Int) {
        val message: Message? = messageAdapter.getItemAt(position)
        message?.let {
            val intent = Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?q="
                            + message.coords?.lat + ","
                            + message.coords?.lon))
            startActivity(intent)
        }
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


    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(this@MessageListActivity,
                Manifest.permission.ACCESS_FINE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this@MessageListActivity,
                Manifest.permission.ACCESS_FINE_LOCATION)

        if (!shouldProvideRationale) {
            val dialogBuilder: AlertDialog.Builder =
                    AlertDialog.Builder(this@MessageListActivity).apply {
                        setTitle(null)
                        setMessage(R.string.location_permission_msg)
                        setPositiveButton(R.string.go_to_setting) { _, _ ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:" + packageName))
                            intent.addCategory(Intent.CATEGORY_DEFAULT)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                    }
            dialogBuilder.create().show()

        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        Log.d("onRequestPermission", "onRequestPermissionsResult: " + requestCode + permissions.toString())
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                statLocationService()
            }
        }
    }

    private fun statLocationService() {

        val serviceIntent = Intent(this, SendLocationLocationService::class.java)
        // Start service
        // Bind service
        this.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE)
        this.startService(serviceIntent)
    }

    override fun onServiceDisconnected(p0: ComponentName?) {

    }

    override fun onServiceConnected(componentName: ComponentName?, service: IBinder?) {
        val binder = service as SendLocationLocationService.SendLocationLocationServiceBinder
        sendLocationService = binder.service
    }

    // start alarm for get message list and show when there have new message (repeat every 60 sec).
    private fun startAlarmForMessageNotification() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this@MessageListActivity, MessageReciver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this@MessageListActivity, 0, intent, 0)

        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                60 * 1000,
                pendingIntent)

    }

}
