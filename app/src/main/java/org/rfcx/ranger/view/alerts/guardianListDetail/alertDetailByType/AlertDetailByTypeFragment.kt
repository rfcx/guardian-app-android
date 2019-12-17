package org.rfcx.ranger.view.alerts.guardianListDetail.alertDetailByType

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_alert_detail_by_type.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.data.remote.success
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.view.alert.AlertBottomDialogFragment
import org.rfcx.ranger.view.alert.AlertListener
import org.rfcx.ranger.view.alerts.guardianListDetail.alertDetailByType.AlertDetailByTypeActivity.Companion.ALERT_VALUE
import org.rfcx.ranger.view.alerts.adapter.AlertClickListener
import org.rfcx.ranger.view.alerts.guardianListDetail.alertDetailByType.AlertDetailByTypeActivity.Companion.GUARDIAN_NAME
import org.rfcx.ranger.view.base.BaseFragment

class AlertDetailByTypeFragment : BaseFragment(), AlertClickListener, AlertListener {
	private val viewModel: AlertDetailByTypeViewModel by viewModel()
	private val alertDetailByTypeAdapter by lazy { AlertDetailByTypeAdapter(this) }
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_alert_detail_by_type, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		val value = arguments?.getString(ALERT_VALUE)
		val guardianName = arguments?.getString(GUARDIAN_NAME)
		if (value != null && guardianName != null) {
			viewModel.getEventFromDatabase(value, guardianName)
		}
		
		alertDetailByTypeRecycler.apply {
			layoutManager = LinearLayoutManager(context)
			adapter = alertDetailByTypeAdapter
		}
		
		viewModel.arrayEvent.observe(this, Observer {
			it.success({ items ->
				alertDetailByTypeAdapter.items = items
			})
		})
		
		alertDetailByTypeAdapter.mOnSeeOlderClickListener = object : OnSeeOlderClickListener {
			override fun onSeeOlderClick() {
				viewModel.loadMoreEvents()
			}
		}
	}
	
	override fun onClickedAlert(event: Event) {
		showDetail(event.id)
	}
	
	override fun showDetail(eventGuID: String) {
		val currentShowing =
				childFragmentManager.findFragmentByTag(AlertBottomDialogFragment.tag)
		if (currentShowing != null && currentShowing is AlertBottomDialogFragment) {
			currentShowing.dismissAllowingStateLoss()
		}
		AlertBottomDialogFragment.newInstance(eventGuID).show(childFragmentManager,
				AlertBottomDialogFragment.tag)
	}
	
	override fun onReviewed(eventGuID: String, reviewValue: String) {
		viewModel.onEventReviewed(eventGuID, reviewValue)
	}
	
	companion object {
		const val tag = "AlertDetailByTypeFragment"
		fun newInstance(value: String, guardianName: String): AlertDetailByTypeFragment {
			return AlertDetailByTypeFragment().apply {
				arguments = Bundle().apply {
					putString(ALERT_VALUE, value)
					putString(GUARDIAN_NAME, guardianName)
				}
			}
		}
	}
}

interface OnSeeOlderClickListener {
	fun onSeeOlderClick()
}