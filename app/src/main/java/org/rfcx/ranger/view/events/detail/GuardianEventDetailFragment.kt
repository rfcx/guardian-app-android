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
import org.rfcx.ranger.R
import org.rfcx.ranger.util.setFormatLabel
import org.rfcx.ranger.view.MainActivityEventListener
import org.rfcx.ranger.view.events.adapter.EventItemAdapter
import org.rfcx.ranger.view.events.adapter.EventModel
import org.rfcx.ranger.view.report.create.CreateReportActivity
import java.util.*

class GuardianEventDetailFragment : Fragment() {
	lateinit var listener: MainActivityEventListener
	private val eventItemAdapter by lazy { EventItemAdapter() }
	var name: String? = null
	var distance: Float? = null
	var number: Int? = null
	
	val list = listOf(
			EventModel("Chainsaw", Date(121, 7, 25, 22, 19)),
			EventModel("Chainsaw", Date(121, 7, 25, 5, 38)),
			EventModel("Chainsaw", Date(121, 7, 19, 17, 29)),
			EventModel("Chainsaw", Date(121, 8, 15, 9, 36)),
			EventModel("Chainsaw", Date(121, 8, 8, 15, 32)),
			EventModel("Chainsaw", Date(121, 8, 13, 19, 25)),
			EventModel("Chainsaw", Date(121, 8, 14, 16, 23))
	).sortedByDescending { item -> item.date }
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val arg = arguments ?: return
		name = arg.getString(ARG_NAME)
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
		
		alertsRecyclerView.apply {
			layoutManager = LinearLayoutManager(context)
			adapter = eventItemAdapter
			eventItemAdapter.items = list.take(number ?: 0)
			
			createReportButton.setOnClickListener {
				val name = name ?: return@setOnClickListener
				CreateReportActivity.startActivity(context, name)
			}
		}
		
		guardianNameTextView.text = name
		distanceTextView.text = distance?.setFormatLabel()
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
		private const val ARG_NUMBER = "ARG_NUMBER"
		
		@JvmStatic
		fun newInstance(name: String, distance: Float, num: Int): GuardianEventDetailFragment {
			return GuardianEventDetailFragment().apply {
				arguments = Bundle().apply {
					putString(ARG_NAME, name)
					putFloat(ARG_DISTANCE, distance)
					putInt(ARG_NUMBER, num)
				}
			}
		}
	}
}