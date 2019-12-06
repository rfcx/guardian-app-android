package org.rfcx.ranger.view.alerts.GuardianListDetail.AlertDetailByType

import android.os.Bundle
import android.util.Log
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
import org.rfcx.ranger.view.alerts.GuardianListDetail.AlertDetailByType.AlertDetailByTypeActivity.Companion.ALERT_VALUE
import org.rfcx.ranger.view.alerts.adapter.AlertClickListener
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
		if (value != null) {
			viewModel.getEventFromDatabase(value)
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
	}
	
	override fun onClickedAlert(event: Event) {
		showDetail(event)
	}
	
	override fun showDetail(event: Event) {
		val currentShowing =
				childFragmentManager.findFragmentByTag(AlertBottomDialogFragment.tag)
		if (currentShowing != null && currentShowing is AlertBottomDialogFragment) {
			currentShowing.dismissAllowingStateLoss()
		}
		AlertBottomDialogFragment.newInstance(event).show(childFragmentManager,
				AlertBottomDialogFragment.tag)	}
	
	override fun onReviewed(eventGuID: String, reviewValue: String) {
		viewModel.onEventReviewed(eventGuID, reviewValue)
	}
	
	companion object {
		const val tag = "AlertDetailByTypeFragment"
		fun newInstance(value: String): AlertDetailByTypeFragment {
			return AlertDetailByTypeFragment().apply {
				arguments = Bundle().apply {
					putString(ALERT_VALUE, value)
				}
			}
		}
	}
}