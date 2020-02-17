package org.rfcx.ranger.view.alerts.guardian

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_guardian_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.data.remote.success
import org.rfcx.ranger.util.handleError
import org.rfcx.ranger.view.alerts.guardian.alertType.AlertValueActivity
import org.rfcx.ranger.view.base.BaseFragment

class GuardianDetailFragment : BaseFragment() {
	private val viewModel: GuardianViewModel by viewModel()
	private val guardianAdapter by lazy {
		GuardianDetailAdapter { item ->
			context?.let {
				AlertValueActivity.startActivity(it,
						item.value, item.displayName, viewModel.guardianName)
			}
		}
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_guardian_detail, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupAlertGroupRecycler()
		
		val guardianName = arguments?.getString(ARG_GUARDIAN_NAME, null)
		if (guardianName != null) {
			viewModel.guardianName = guardianName // setGuardianName
			viewModel.fetchEventsByGuardianName()
		}
		
		viewModel.eventGroups.observe(this, Observer { it ->
			it.success({ items ->
				loadingProgress.visibility = View.INVISIBLE
				guardianAdapter.submitList(items)
			}, {
				loadingProgress.visibility = View.INVISIBLE
				context.handleError(it)
			}, {
				loadingProgress.visibility = View.VISIBLE
			})
		})
	}
	
	private fun setupAlertGroupRecycler() {
		val alertsLayoutManager = LinearLayoutManager(context)
		eventsInGuardianRecycler.apply {
			layoutManager = alertsLayoutManager
			adapter = guardianAdapter
		}
	}
	
	companion object {
		const val tag = "GuardianListDetailFragment"
		private const val ARG_GUARDIAN_NAME = "ARG_GUARDIAN_NAME"
		
		fun newInstance(guardianName: String): GuardianDetailFragment {
			return GuardianDetailFragment().apply {
				arguments = Bundle().apply {
					putString(ARG_GUARDIAN_NAME, guardianName)
				}
			}
		}
	}
}