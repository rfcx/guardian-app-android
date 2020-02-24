package org.rfcx.ranger.view.alerts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_group_alerts.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.data.remote.success
import org.rfcx.ranger.util.handleError
import org.rfcx.ranger.view.alerts.guardian.GuardianDetailActivity
import org.rfcx.ranger.view.alerts.adapter.GroupByGuardianAdapter
import org.rfcx.ranger.view.base.BaseFragment

class GroupAlertsFragment : BaseFragment(), OnItemClickListener {
	
	private val viewModel: GroupAlertsViewModel by viewModel()
	private val groupByGuardianAdapter by lazy { GroupByGuardianAdapter(this) }
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_group_alerts, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupSwipeRefresh()
		groupAlertsRecyclerView.apply {
			val alertsLayoutManager = LinearLayoutManager(context)
			layoutManager = alertsLayoutManager
			adapter = groupByGuardianAdapter
		}
		
		viewModel.groups.observe(this, Observer { it ->
			it.success({ items ->
				swipeRefresh.isRefreshing = false
				groupByGuardianAdapter.items = items
			}, {
				swipeRefresh.isRefreshing = false
				context.handleError(it)
			}, {
				swipeRefresh.isRefreshing = !viewModel.isRefreshing
			})
		})
	}
	
	override fun onResume() {
		super.onResume()
		groupByGuardianAdapter.notifyDataSetChanged()
	}
	
	override fun onItemClick(eventGroup: EventGroup) {
		context?.let { GuardianDetailActivity.startActivity(it,
				eventGroup.guardianName, eventGroup.events > 0) }
	}
	
	private fun setupSwipeRefresh() {
		context?.let {
			swipeRefresh.setColorSchemeColors(ContextCompat.getColor(it, R.color.colorPrimary))
		}
		swipeRefresh.setOnRefreshListener {
			viewModel.refresh()
		}
	}
	companion object {
		const val tag = "GroupAlertsFragment"
		fun newInstance(): GroupAlertsFragment {
			return GroupAlertsFragment()
		}
	}
}

interface OnItemClickListener {
	fun onItemClick(eventGroup: EventGroup)
}