package org.rfcx.ranger.view.events.detail

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_guardian_event_detail.*
import kotlinx.android.synthetic.main.toolbar_default.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.util.setFormatLabel
import org.rfcx.ranger.view.MainActivityEventListener
import org.rfcx.ranger.view.events.adapter.EventItemAdapter

class GuardianEventDetailFragment : Fragment() {
	private val viewModel: GuardianEventDetailViewModel by viewModel()
	lateinit var listener: MainActivityEventListener
	private val eventItemAdapter by lazy { EventItemAdapter() }
	
	var name: String? = null
	var guardianId: String? = null
	var distance: Double? = null
	var number: Int? = null
	var list = listOf<Event>()
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val arg = arguments ?: return
		name = arg.getString(ARG_NAME)
		guardianId = arg.getString(ARG_GUARDIAN_ID)
		number = arg.getInt(ARG_NUMBER)
	}
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as MainActivityEventListener)
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
			adapter = eventItemAdapter
			eventItemAdapter.items = list.take(number ?: 0)
			
			createReportButton.setOnClickListener {
				name?.let { name ->
					guardianId?.let { id ->
						listener.openCreateReportActivity(name, id)
					}
				}
			}
		}
		
		guardianNameTextView.text = name
		distanceTextView.text = distance?.setFormatLabel()
	}
	
	private fun setObserve() {
		viewModel.getEvents().observe(viewLifecycleOwner, { events ->
			list = events.filter { e -> e.guardianId == guardianId }
			eventItemAdapter.items = list.take(number ?: 0)
		})
	}
	
	private fun setupToolbar() {
		val activity = (activity as AppCompatActivity?) ?: return
		activity.setSupportActionBar(toolbarDefault)
		activity.supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			title = getString(R.string.guardian_event_detail)
		}
		
		toolbarDefault.setNavigationOnClickListener {
			listener.onBackPressed()
		}
	}
	
	companion object {
		const val tag = "GuardianEventDetailFragment"
		private const val ARG_NAME = "ARG_NAME"
		private const val ARG_DISTANCE = "ARG_DISTANCE"
		private const val ARG_GUARDIAN_ID = "ARG_GUARDIAN_ID"
		private const val ARG_NUMBER = "ARG_NUMBER"
		
		@JvmStatic
		fun newInstance(name: String, distance: Double, eventSize: Int, guardianId: String): GuardianEventDetailFragment {
			return GuardianEventDetailFragment().apply {
				arguments = Bundle().apply {
					putString(ARG_NAME, name)
					putString(ARG_GUARDIAN_ID, guardianId)
					putDouble(ARG_DISTANCE, distance)
					putInt(ARG_NUMBER, eventSize)
				}
			}
		}
	}
}
