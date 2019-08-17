package org.rfcx.ranger.view.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_profile.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.view.base.BaseFragment
import org.rfcx.ranger.view.profile.GuardianGroupList.GuardianGroupListActivity

class ProfileFragment : BaseFragment(){

    private val profileViewModel: ProfileViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val trackingSwitch = view.findViewById<SwitchCompat>(R.id.locationTrackingSwitch)
        val notificationSwitch = view.findViewById<SwitchCompat>(R.id.notificationReceiveSwitch)
        val guardianGroupLayout = view.findViewById<LinearLayout>(R.id.guardianGroupLayout)
        val logoutButton = view.findViewById<TextView>(R.id.logoutTextView)
        val rateAppButton = view.findViewById<TextView>(R.id.rateAppTextView)
        val feedbackButton = view.findViewById<TextView>(R.id.feedbackTextView)

        trackingSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            profileViewModel.onTracking(isChecked)
        }

        notificationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            profileViewModel.onReceiving(isChecked)
        }

        guardianGroupLayout.setOnClickListener {
            //TODO: move to select guardian site page
            val intent = Intent(activity, GuardianGroupListActivity::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            //TODO: delete token
        }

        rateAppButton.setOnClickListener {
            //TODO: move to rate app page
        }

        feedbackButton.setOnClickListener {
            //TODO: move to feedback page
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileViewModel.locationTracking.observe(this, Observer {
            locationTrackingSwitch.isChecked = it
        })

        profileViewModel.notificationReceiving.observe(this, Observer {
            notificationReceiveSwitch.isChecked = it
        })

        profileViewModel.userSite.observe(this, Observer {
            userLocationTextView.text = it
        })

        profileViewModel.appVersion.observe(this, Observer{
            versionTextView.text = it
        })

        profileViewModel.userName.observe(this, Observer{
            userNameTextView.text = it
        })
    }

    companion object {
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }

        const val tag = "ProfileFragment"
    }
}