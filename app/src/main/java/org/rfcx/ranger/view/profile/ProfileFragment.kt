package org.rfcx.ranger.view.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_profile.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.databinding.FragmentProfileBinding
import org.rfcx.ranger.util.Analytics
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.Screen
import org.rfcx.ranger.view.LocationTrackingViewModel
import org.rfcx.ranger.view.MainActivityEventListener
import org.rfcx.ranger.view.base.BaseFragment
import org.rfcx.ranger.view.tutorial.TutorialActivity

class ProfileFragment : BaseFragment() {
	
	private val analytics by lazy { context?.let { Analytics(it) } }
	private val profileViewModel: ProfileViewModel by viewModel()
	private val locationTrackingViewModel: LocationTrackingViewModel by sharedViewModel()
	lateinit var listener: MainActivityEventListener
	private lateinit var viewDataBinding: FragmentProfileBinding
	
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as MainActivityEventListener)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		viewDataBinding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_profile,
				container, false)
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
		}
	}
	
	@SuppressLint("DefaultLocale")
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		viewDataBinding.viewModel = profileViewModel
		setOnClickButton()
		
		val loginWith = context?.let { Preferences.getInstance(it).getString(Preferences.LOGIN_WITH) }
		if (loginWith == "email") {
			changePasswordTextView.visibility = View.VISIBLE
			
		}
		
		locationTrackingViewModel.locationTrackingState.observe(this, Observer {
			profileViewModel.onTracingStatusChange()
		})
	}
	
	private fun setOnClickButton() {
		viewDataBinding.onClickLocationTracking = View.OnClickListener {
			if (locationTrackingSwitch.isChecked) {
				// off location tracking
				locationTrackingViewModel.requireDisableLocationTracking()
			} else {
				locationTrackingViewModel.requireEnableLocationTracking()
			}
		}
		
		viewDataBinding.onClickAppIntro = View.OnClickListener {
			val preferenceHelper = context?.let { it1 -> Preferences.getInstance(it1) }
			preferenceHelper?.putBoolean(Preferences.IS_FIRST_TIME, false)
			context?.let { it1 -> TutorialActivity.startActivity(it1, null) }
		}
		
		viewDataBinding.onClickGuardingGroup = View.OnClickListener {
			analytics?.trackSetGuardianGroupStartEvent(Screen.PROFILE)
			context?.let { GuardianGroupActivity.startActivity(it) }
		}
		
		viewDataBinding.onClickRatingApp = View.OnClickListener {
			analytics?.trackRateAppEvent()
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
		
		viewDataBinding.onClickFeedback = View.OnClickListener {
			analytics?.trackFeedbackStartEvent()
			val intent = Intent(activity, FeedbackActivity::class.java)
			startActivityForResult(intent, REQUEST_CODE)
		}
		
		viewDataBinding.onClickPassword = View.OnClickListener {
			context?.let { it1 -> PasswordChangeActivity.startActivity(it1) }
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