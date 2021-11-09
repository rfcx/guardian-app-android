package org.rfcx.incidents.view.profile

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_profile.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentProfileBinding
import org.rfcx.incidents.util.*
import org.rfcx.incidents.view.LocationTrackingViewModel
import org.rfcx.incidents.view.MainActivityEventListener
import org.rfcx.incidents.view.base.BaseFragment
import org.rfcx.incidents.view.profile.coordinates.CoordinatesActivity
import org.rfcx.incidents.view.profile.editprofile.EditProfileActivity

class ProfileFragment : BaseFragment() {
	
	private val analytics by lazy { context?.let { Analytics(it) } }
	private val profileViewModel: ProfileViewModel by viewModel()
	private val locationTrackingViewModel: LocationTrackingViewModel by sharedViewModel()
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
			profileViewModel.resumed()
			userProfileImageView.setImageProfile(context.getUserProfile())
		}
	}
	
	override fun onResume() {
		super.onResume()
		profileViewModel.resumed()
		userProfileImageView.setImageProfile(context.getUserProfile())
	}
	
	@SuppressLint("DefaultLocale")
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		viewDataBinding.viewModel = profileViewModel
		setOnClickButton()
		
		val loginWith = context?.let { Preferences.getInstance(it).getString(Preferences.LOGIN_WITH) }
		if (loginWith == LOGIN_WITH_EMAIL) {
			changeImageProfileTextView.visibility = View.VISIBLE
			changePasswordTextView.visibility = View.VISIBLE
			userProfileImageView.visibility = View.VISIBLE
		} else {
			changeImageProfileTextView.visibility = View.GONE
			changePasswordTextView.visibility = View.GONE
			userProfileImageView.visibility = View.GONE
		}
	}
	
	private fun setOnClickButton() {
		viewDataBinding.onClickProject = View.OnClickListener {
			context?.let { SubscribeProjectsActivity.startActivity(it) }
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
		
		viewDataBinding.onClickProfilePhoto = View.OnClickListener {
			context?.let { it1 -> EditProfileActivity.startActivity(it1) }
		}
		
		viewDataBinding.onClickCoordinates = View.OnClickListener {
			context?.let { it1 -> CoordinatesActivity.startActivity(it1) }
		}
		
		viewDataBinding.onClickStartDemo = View.OnClickListener {
			val builder = context?.let { androidx.appcompat.app.AlertDialog.Builder(it) }
			
			if (builder != null) {
				builder.setTitle(null)
				builder.setMessage(R.string.notification_will_sent)
				builder.setCancelable(false)
				
				builder.setPositiveButton(getString(R.string.perform_test)) { _, _ ->
					context?.let { it1 -> NotificationDemo(profileViewModel.randomGuidOfAlert()).startDemo(it1) }
				}
				
				builder.setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
					dialog.dismiss()
				}
				
				val alertDialog = builder.create()
				alertDialog.setOnShowListener {
					alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(resources.getColor(R.color.text_secondary))
					alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextSize(TypedValue.COMPLEX_UNIT_PX, 40.0F)
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(TypedValue.COMPLEX_UNIT_PX, 40.0F)
				}
				alertDialog.show()
			}
		}
	}
	
	override fun onStart() {
		super.onStart()
		profileViewModel.updateSiteName()
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
		const val LOGIN_WITH_EMAIL = "auth0"
	}
}