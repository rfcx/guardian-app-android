package org.rfcx.ranger.view.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_profile.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.util.CloudMessaging
import org.rfcx.ranger.util.logout
import org.rfcx.ranger.view.LocationTrackingViewModel
import org.rfcx.ranger.view.MainActivityEventListener
import org.rfcx.ranger.view.base.BaseFragment

class ProfileFragment : BaseFragment() {
	
	private val profileViewModel: ProfileViewModel by viewModel()
	private val locationTrackingViewModel: LocationTrackingViewModel by sharedViewModel()
	lateinit var listener: MainActivityEventListener
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as MainActivityEventListener)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_profile, container, false)
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		handleShowSnackbarResult(requestCode, resultCode, data)
	}
	
	@SuppressLint("DefaultLocale")
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		setupButtonListeners()
		
		profileViewModel.locationTracking.observe(this, Observer {
			Log.e("BaseActivity", "$it")
			locationTrackingSwitch.isChecked = it
		})
		
		profileViewModel.notificationReceiving.observe(this, Observer { on ->
			notificationReceiveSwitch.isChecked = on
			// TODO this should be in the VM
			// TODO need to protect again continuous presses
			context?.let {
				if (on) {
					CloudMessaging.subscribeIfRequired(it)
				} else {
					CloudMessaging.unsubscribe(it)
				}
			}
		})
		
		profileViewModel.userSite.observe(this, Observer {
			userLocationTextView.text = it
		})
		
		profileViewModel.appVersion.observe(this, Observer {
			versionTextView.text = it
		})
		
		profileViewModel.userName.observe(this, Observer {
			userNameTextView.text = it.capitalize()
		})
		
		profileViewModel.guardianGroup.observe(this, Observer {
			siteNameTextView.text = it
		})
		
		locationTrackingViewModel.locationTrackingState.observe(this, Observer {
			profileViewModel.onTracingStatusChange()
		})
	}
	
	private fun setupButtonListeners() {
		locationTrackingSwitchLayout.setOnClickListener {
			if (locationTrackingSwitch.isChecked) {
				// off location tracking
				locationTrackingViewModel.requireDisableLocationTracking()
			} else {
				locationTrackingViewModel.requireEnableLocationTracking()
			}
		}
		
		notificationReceiveSwitch.setOnCheckedChangeListener { _, isChecked ->
			profileViewModel.onReceiving(isChecked)
		}
		
		guardianGroupLayout.setOnClickListener {
			context?.let { GuardianGroupActivity.startActivity(it) }
		}
		
		logoutTextView.setOnClickListener {
			context.logout()
		}
		
		rateAppTextView.setOnClickListener {
			val appPackageName = activity?.packageName
			try {
				val playStoreUri: Uri = Uri.parse("market://details?id=$appPackageName")
				val playStoreIntent = Intent(Intent.ACTION_VIEW, playStoreUri)
				startActivity(playStoreIntent)
			} catch (exp: Exception) {
				val exceptionUri: Uri = Uri.parse("http://play.google.com/store/apps/details?id=$appPackageName")
				val exceptionIntent = Intent(Intent.ACTION_VIEW, exceptionUri)
				startActivity(exceptionIntent)
			}
		}
		
		feedbackTextView.setOnClickListener {
			val intent = Intent(activity, FeedbackActivity::class.java)
			startActivityForResult(intent, REQUEST_CODE)
		}
	}
	
	override fun onStart() {
		super.onStart()
		profileViewModel.updateSiteName()
	}
	
	private fun handleShowSnackbarResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
		if (requestCode != REQUEST_CODE || resultCode != RESULT_CODE || intentData == null) return
		
		view?.let {
			Snackbar.make(it, R.string.feedback_submitted, Snackbar.LENGTH_SHORT)
					.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
					.setAnchorView(R.id.newReportFabButton).show()
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