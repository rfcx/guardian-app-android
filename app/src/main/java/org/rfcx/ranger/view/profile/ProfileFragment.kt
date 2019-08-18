package org.rfcx.ranger.view.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_profile.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.view.MainActivityNew
import org.rfcx.ranger.view.base.BaseFragment

class ProfileFragment : BaseFragment() {
	
	private val profileViewModel: ProfileViewModel by viewModel()
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_profile, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		setEventClick()
		
		profileViewModel.locationTracking.observe(this, Observer {
			Log.e("BaseActivity", "$it")
			locationTrackingSwitch.isChecked = it
		})
		
		profileViewModel.notificationReceiving.observe(this, Observer {
			notificationReceiveSwitch.isChecked = it
		})
		
		profileViewModel.userSite.observe(this, Observer {
			userLocationTextView.text = it
		})
		
		profileViewModel.appVersion.observe(this, Observer {
			versionTextView.text = it
		})
		
		profileViewModel.userName.observe(this, Observer {
			userNameTextView.text = it
		})
	}
	
	private fun setEventClick() {
		locationTrackingSwitchLayout.setOnClickListener {
			if (locationTrackingSwitch.isChecked) {
				// off location tracking
				(activity as MainActivityNew).disableLocationTracking()
				profileViewModel.onTracingStatusChange()
				
			} else {
				// on location tracking
				(activity as MainActivityNew).enableLocationTracking {
					profileViewModel.onTracingStatusChange()
				}
			}
		}
		
		notificationReceiveSwitch.setOnCheckedChangeListener { _, isChecked ->
			profileViewModel.onReceiving(isChecked)
		}
		
		guardianGroupLayout.setOnClickListener {
			//TODO: move to select guardian site page
			context?.let { it1 -> GuardianGroupActivity.startActivity(it1) }
		}
		
		logoutTextView.setOnClickListener {
			//TODO: delete token
		}
		
		rateAppTextView.setOnClickListener {
			//TODO: move to rate app page
		}
		
		feedbackTextView.setOnClickListener {
			//TODO: move to feedback page
		}
		
	}
	
	companion object {
		fun newInstance(): ProfileFragment {
			return ProfileFragment()
		}
		
		const val tag = "ProfileFragment"
	}
}