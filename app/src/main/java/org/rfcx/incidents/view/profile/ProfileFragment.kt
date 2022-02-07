package org.rfcx.incidents.view.profile

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentProfileBinding
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Screen
import org.rfcx.incidents.view.MainActivityEventListener
import org.rfcx.incidents.view.base.BaseFragment

class ProfileFragment : BaseFragment() {

    private val analytics by lazy { context?.let { Analytics(it) } }
    private val profileViewModel: ProfileViewModel by viewModel()
    lateinit var listener: MainActivityEventListener
    private lateinit var viewDataBinding: FragmentProfileBinding

    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(context)
            .setView(layoutInflater.inflate(R.layout.custom_loading_alert_dialog, null))
            .setCancelable(false)
            .create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = (context as MainActivityEventListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewDataBinding = DataBindingUtil.inflate(
            layoutInflater, R.layout.fragment_profile,
            container, false
        )
        viewDataBinding.lifecycleOwner = this
        return viewDataBinding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        handleShowSnackbarResult(requestCode, resultCode, data)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            analytics?.trackScreen(Screen.PROFILE)
            profileViewModel.resumed()
        }
    }

    override fun onResume() {
        super.onResume()
        profileViewModel.resumed()
    }

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDataBinding.viewModel = profileViewModel
        setOnClickButton()
    }

    private fun setOnClickButton() {
        viewDataBinding.onClickProject = View.OnClickListener {
            context?.let { SubscribeProjectsActivity.startActivity(it) }
        }

        viewDataBinding.onClickSystemOptions = View.OnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = getString(R.string.app_name)
                val descriptionText = getString(R.string.app_name)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val mChannel = NotificationChannel(BuildConfig.APPLICATION_ID, name, importance)
                mChannel.description = descriptionText
                val notificationManager = context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(mChannel)

                val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, BuildConfig.APPLICATION_ID)
                    putExtra(Settings.EXTRA_CHANNEL_ID, BuildConfig.APPLICATION_ID)
                }
                startActivity(intent)
            }
        }

        viewDataBinding.onClickFeedback = View.OnClickListener {
            analytics?.trackFeedbackStartEvent()
            val intent = Intent(activity, FeedbackActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE)
        }

        viewDataBinding.onClickLogout = View.OnClickListener {
            profileViewModel.onLogout()
        }
    }

    override fun onPause() {
        super.onPause()
        dialog.dismiss()
    }

    private fun handleShowSnackbarResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        if (requestCode != REQUEST_CODE || resultCode != RESULT_CODE || intentData == null) return

        view?.let {
            Snackbar.make(it, R.string.feedback_submitted, Snackbar.LENGTH_SHORT)
                .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                .setAnchorView(R.id.bottomBar).show() // TODO :: Check it work or not?
        }
    }

    companion object {
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }

        const val tag = "ProfileFragment"
        const val RESULT_CODE = 12
        const val REQUEST_CODE = 11
    }
}
