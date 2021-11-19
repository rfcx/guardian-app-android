package org.rfcx.incidents.view.events.detail

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_guardian_event_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.alert.Alert
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Screen
import org.rfcx.incidents.util.setFormatLabel
import org.rfcx.incidents.view.MainActivityEventListener
import org.rfcx.incidents.view.events.adapter.AlertItemAdapter


class GuardianEventDetailFragment : Fragment() {
	private val analytics by lazy { context?.let { Analytics(it) } }
	private val viewModel: GuardianEventDetailViewModel by viewModel()
	lateinit var listener: MainActivityEventListener
	private val alertItemAdapter by lazy { AlertItemAdapter() }
	
	var name: String? = null
	var guardianId: String? = null
	var distance: Double? = null
	var alerts = listOf<Alert>()
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val arg = arguments ?: return
		name = arg.getString(ARG_NAME)
		guardianId = arg.getString(ARG_GUARDIAN_ID)
		if (arg.get(ARG_DISTANCE) != null) {
			distance = arg.getDouble(ARG_DISTANCE)
		}
	}
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as MainActivityEventListener)
	}
	
	override fun onResume() {
		super.onResume()
		analytics?.trackScreen(Screen.GUARDIAN_EVENT_DETAIL)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_guardian_event_detail, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupToolbar()
		setObserve()
		
		alertsRecyclerView.apply {
			layoutManager = LinearLayoutManager(context)
			adapter = alertItemAdapter
			alertItemAdapter.items = alerts
			
			createReportButton.setOnClickListener {
				name?.let { name ->
					guardianId?.let { id ->
						listener.openCreateReportActivity(name, id)
					}
				}
			}
		}
		
		openMapsButton.setOnClickListener {
			guardianId?.let { id ->
				val stream = viewModel.getStream(id)
				if (stream != null) {
					listener.openGoogleMap(stream)
				}
			}
		}
		
		guardianNameTextView.text = name
		distanceTextView.visibility = if (distance != null) View.VISIBLE else View.GONE
		distanceTextView.text = distance?.setFormatLabel()
	}
	
	private fun setObserve() {
		viewModel.getAlerts().observe(viewLifecycleOwner, { events ->
			alerts = events.filter { e -> e.streamId == guardianId }
			alertItemAdapter.items = alerts
		})
	}
	
	private fun setupToolbar() {
		val activity = (activity as AppCompatActivity?) ?: return
		activity.setSupportActionBar(toolbarLayout)
		activity.supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			title = getString(R.string.guardian_event_detail)
		}
		
		toolbarLayout.setNavigationOnClickListener {
			listener.onBackPressed()
		}
	}
	
	companion object {
		const val tag = "GuardianEventDetailFragment"
		private const val ARG_NAME = "ARG_NAME"
		private const val ARG_DISTANCE = "ARG_DISTANCE"
		private const val ARG_GUARDIAN_ID = "ARG_GUARDIAN_ID"
		
		@JvmStatic
		fun newInstance(name: String, distance: Double?, guardianId: String): GuardianEventDetailFragment {
			return GuardianEventDetailFragment().apply {
				arguments = Bundle().apply {
					putString(ARG_NAME, name)
					putString(ARG_GUARDIAN_ID, guardianId)
					if (distance != null) putDouble(ARG_DISTANCE, distance)
				}
			}
		}
	}
}
