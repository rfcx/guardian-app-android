package org.rfcx.incidents.view.events.detail

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.data.remote.common.success
import org.rfcx.incidents.data.remote.events.toAlert
import org.rfcx.incidents.databinding.FragmentGuardianEventDetailBinding
import org.rfcx.incidents.entity.alert.Alert
import org.rfcx.incidents.entity.location.Coordinate
import org.rfcx.incidents.entity.location.Tracking
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Preferences
import org.rfcx.incidents.util.Screen
import org.rfcx.incidents.util.isNetworkAvailable
import org.rfcx.incidents.util.setFormatLabel
import org.rfcx.incidents.view.MainActivityEventListener
import org.rfcx.incidents.view.events.adapter.AlertItemAdapter

class EventDetailFragment : Fragment(), (Alert) -> Unit, SwipeRefreshLayout.OnRefreshListener {
    private var _binding: FragmentGuardianEventDetailBinding? = null
    private val binding get() = _binding!!
    private val analytics by lazy { context?.let { Analytics(it) } }
    private val viewModel: EventDetailViewModel by viewModel()
    lateinit var listener: MainActivityEventListener
    private val alertItemAdapter by lazy { AlertItemAdapter(this) }

    lateinit var streamId: String
    var distance: Double? = null
    var alerts = listOf<Alert>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arg = arguments ?: return
        streamId = arg.getString(ARG_STREAM_ID) ?: throw Error("Stream not set")
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGuardianEventDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setObserve()
        isShowProgressBar()

        binding.alertsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = alertItemAdapter
            alertItemAdapter.items = alerts

            binding.createReportButton.setOnClickListener {
                analytics?.trackCreateResponseEvent()
                listener.getCurrentLocation()?.let { loc ->
                    saveLocation(loc)
                }
                listener.openCreateReportActivity(streamId)
            }
        }

        binding.openMapsButton.setOnClickListener {
            viewModel.getStream(streamId)?.let { stream ->
                listener.openGoogleMap(stream)
            }
        }

        viewModel.getStream(streamId)?.let { stream ->
            binding.guardianNameTextView.text = stream.name
        }
        binding.distanceTextView.visibility = if (distance != null) View.VISIBLE else View.GONE
        binding.distanceTextView.text = distance?.setFormatLabel()

        streamId.let {
            if (viewModel.getEventsCount(it) != 0L) {
                alertItemAdapter.items = viewModel.getAlertsByStream(it)
                isShowProgressBar(false)
                viewModel.fetchEvents(it)
            } else {
                if (!context.isNetworkAvailable()) {
                    isShowProgressBar(false)
                } else {
                    viewModel.fetchEvents(it)
                }
            }
        }

        binding.alertsSwipeRefreshView.apply {
            setOnRefreshListener(this@EventDetailFragment)
            setColorSchemeResources(R.color.colorPrimary)
        }
    }

    private fun setObserve() {
        viewModel.getAlertsFromRemote.observe(viewLifecycleOwner) { it ->
            it.success({ list ->
                alertItemAdapter.items = list.map { a -> a.toAlert() }
                binding.notHaveEventsLayout.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                isShowProgressBar(false)
                binding.alertsSwipeRefreshView.isRefreshing = false
            }, {
                binding.alertsSwipeRefreshView.isRefreshing = false
            }, {
            })
        }
    }

    private fun setupToolbar() {
        val activity = (activity as AppCompatActivity?) ?: return
        activity.setSupportActionBar(binding.toolbarLayout)
        activity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.guardian_event_detail)
        }

        binding.toolbarLayout.setNavigationOnClickListener {
            listener.onBackPressed()
        }
    }

    override fun invoke(alert: Alert) {
        listener.openAlertDetail(alert)
    }

    private fun saveLocation(location: Location) {
        val tracking = Tracking(id = 1)
        val coordinate = Coordinate(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude
        )
        viewModel.saveLocation(tracking, coordinate)
        Preferences.getInstance(requireContext())
            .putLong(Preferences.LATEST_GET_LOCATION_TIME, System.currentTimeMillis())
    }

    override fun onRefresh() {
        streamId.let { viewModel.fetchEvents(it) }
        binding.notHaveEventsLayout.visibility = View.GONE
    }

    private fun isShowProgressBar(show: Boolean = true) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    companion object {
        const val tag = "EventDetailFragment"
        private const val ARG_DISTANCE = "ARG_DISTANCE"
        private const val ARG_STREAM_ID = "ARG_STREAM_ID"

        @JvmStatic
        fun newInstance(streamId: String, distance: Double?): EventDetailFragment {
            return EventDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_STREAM_ID, streamId)
                    if (distance != null) putDouble(ARG_DISTANCE, distance)
                }
            }
        }
    }
}