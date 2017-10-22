package android.rfcx.org.ranger.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.rfcx.org.ranger.R
import android.rfcx.org.ranger.adapter.MessageAdapter
import android.rfcx.org.ranger.adapter.OnMessageItemClickListener
import android.rfcx.org.ranger.adapter.entity.BaseItem
import android.rfcx.org.ranger.adapter.entity.EventItem
import android.rfcx.org.ranger.adapter.entity.MessageItem
import android.rfcx.org.ranger.entity.EventResponse
import android.rfcx.org.ranger.entity.ReportType
import android.rfcx.org.ranger.entity.message.Message
import android.rfcx.org.ranger.entity.report.Attributes
import android.rfcx.org.ranger.entity.report.Data
import android.rfcx.org.ranger.entity.report.Report
import android.rfcx.org.ranger.entity.report.ReportData
import android.rfcx.org.ranger.repo.TokenExpireException
import android.rfcx.org.ranger.repo.api.EventsApi
import android.rfcx.org.ranger.repo.api.MessageApi
import android.rfcx.org.ranger.repo.api.SendReportApi
import android.rfcx.org.ranger.service.SendLocationLocationService
import android.rfcx.org.ranger.util.DateHelper
import android.rfcx.org.ranger.util.PrefKey
import android.rfcx.org.ranger.util.PreferenceHelper
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_message_list.*
import kotlinx.android.synthetic.main.dialog_report.view.*
import java.util.*
import kotlin.collections.ArrayList


class MessageListActivity : AppCompatActivity(), OnMessageItemClickListener {

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

        }
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
        fab.setOnClickListener {
            if (checkPermissions()) {
                showReportDialog()
            } else {
                requestPermissions()
            }
        }

        messageRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (dy > 0)
                    fab.hide()
                else if (dy < 0)
                    fab.show()

                super.onScrolled(recyclerView, dx, dy)
            }
        })
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

    private fun getEvents(messageItems: MutableList<MessageItem>) {
        EventsApi().getEvents(this@MessageListActivity, 10, object : EventsApi.OnEventsCallBack {
            override fun onSuccess(event: EventResponse) {
                messageSwipeRefresh.isRefreshing = false
                val eventItems: ArrayList<EventItem>? = event.events?.mapTo(ArrayList()) {
                    EventItem(it, BaseItem.ITEM_EVENT_TYPE, DateHelper.getDateTime(it.beginsAt))
                }

                val baseItems: ArrayList<BaseItem> = ArrayList()
                baseItems.addAll(messageItems)
                if (eventItems != null) {
                    baseItems.addAll(eventItems)
                }
                messageAdapter.updateMessages(baseItems)
            }

            override fun onFailed(t: Throwable?, message: String?) {
                if (t is TokenExpireException) {
                    logout()
                    return
                }
                val error: String = if (message.isNullOrEmpty()) getString(R.string.error_common) else message!!
                Snackbar.make(messageParentView, error, Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun getMessageList() {
        messageSwipeRefresh.isRefreshing = true

        MessageApi().getMessage(this@MessageListActivity, object : MessageApi.OnMessageCallBack {
            override fun onSuccess(messages: List<Message>) {
                messageSwipeRefresh.isRefreshing = false
                val messageItems: MutableList<MessageItem> = messages.mapTo(ArrayList()) {
                    MessageItem(it, BaseItem.ITEM_MESSAGE_TYPE, DateHelper.getDateTime(it.time))
                }

                getEvents(messageItems)
            }

            override fun onFailed(t: Throwable?, message: String?) {
                messageSwipeRefresh.isRefreshing = false
                if (t is TokenExpireException) {
                    logout()
                    return
                }
                val error: String = if (message.isNullOrEmpty()) getString(R.string.error_common) else message!!
                Snackbar.make(rootView, error, Snackbar.LENGTH_LONG).show()
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
//                statLocationService()
            }
        }
    }

    private fun showReportDialog() {

        val builder = AlertDialog.Builder(this@MessageListActivity)
        val dialogView = layoutInflater.inflate(R.layout.dialog_report, null)
        builder.setTitle(R.string.report_title)
        builder.setView(dialogView)
        builder.setPositiveButton(R.string.report_title, null)
        builder.setNegativeButton(R.string.cancel, null)

        val dialog = builder.create()
        dialog.setOnShowListener {
            val button = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                if (dialogView.radioReportGroup.checkedRadioButtonId == -1) {
                    dialogView.reportHaveNoSelected.visibility = View.VISIBLE
                } else {
                    dialogView.reportHaveNoSelected.visibility = View.GONE
                    when {
                        dialogView.chainsawRadio.isChecked -> sendReport(ReportType.Chainsaw)
                        dialogView.gunshotRadio.isChecked -> sendReport(ReportType.Gunshot)
                        dialogView.vehicleRadio.isChecked -> sendReport(ReportType.Vehicle)
                    }
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    @SuppressLint("MissingPermission")
    private fun sendReport(reportType: ReportType) {

        if (!checkPermissions()) {
            return
        }
        val time = DateHelper.getIsoTime()

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {

                // create body
                val reportAttributes = Attributes(time, time, task.result.longitude, task.result.longitude)
                val reportData = ReportData(UUID.randomUUID().toString(), reportType.name, reportAttributes)
                val data = Data(reportData)
                val report = Report(data)

                SendReportApi().sendReport(this@MessageListActivity, report, object : SendReportApi.SendReportCallback {
                    override fun onSuccess() {
                        Snackbar.make(rootView, R.string.report_send_success, Snackbar.LENGTH_SHORT).show()
                    }

                    override fun onFailed(t: Throwable?, message: String?) {
                        Snackbar.make(rootView, if (message.isNullOrEmpty()) getString(R.string.error_common) else message!!, Snackbar.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }


}
