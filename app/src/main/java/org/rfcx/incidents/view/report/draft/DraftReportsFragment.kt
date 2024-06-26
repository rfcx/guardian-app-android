package org.rfcx.incidents.view.report.draft

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.data.preferences.Preferences
import org.rfcx.incidents.databinding.FragmentDraftReportsBinding
import org.rfcx.incidents.entity.CrashlyticsKey
import org.rfcx.incidents.entity.location.Coordinate
import org.rfcx.incidents.entity.location.Tracking
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.response.SyncState
import org.rfcx.incidents.entity.stream.Project
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Crashlytics
import org.rfcx.incidents.util.LocationPermissions
import org.rfcx.incidents.util.Screen
import org.rfcx.incidents.util.isNetworkAvailable
import org.rfcx.incidents.util.isOnAirplaneMode
import org.rfcx.incidents.view.MainActivityEventListener
import org.rfcx.incidents.view.MainActivityViewModel
import org.rfcx.incidents.view.events.adapter.ProjectAdapter
import org.rfcx.incidents.view.events.adapter.ProjectOnClickListener

class DraftReportsFragment : Fragment(), ReportOnClickListener, ProjectOnClickListener, SelectSiteListener {
    private var _binding: FragmentDraftReportsBinding? = null
    private val binding get() = _binding!!

    private val analytics by lazy { context?.let { Analytics(it) } }
    private val firebaseCrashlytics by lazy { Crashlytics() }

    private val viewModel: MainActivityViewModel by viewModel() // TODO should have its own view model
    private val reportsAdapter by lazy { ReportsAdapter(this) }
    private val projectAdapter by lazy { ProjectAdapter(this) }
    private val locationPermissions by lazy { LocationPermissions(requireActivity()) }

    lateinit var listener: MainActivityEventListener
    lateinit var preferences: Preferences
    private var streams = listOf<String>()
    private lateinit var dialog: SelectSiteDialog

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = (context as MainActivityEventListener)
    }

    override fun onResume() {
        super.onResume()
        analytics?.trackScreen(Screen.DRAFT_REPORTS)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDraftReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences = Preferences.getInstance(requireContext())
        val projectId = preferences.getString(Preferences.SELECTED_PROJECT, "")
        setProjectTitle(viewModel.getProjectName(projectId))
        binding.toolbarLayout.changePageImageView.visibility = View.GONE

        setRecyclerView()
        setObserve()
        setOnClickListener()
    }

    private fun setRecyclerView() {
        binding.projectRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = projectAdapter
            projectAdapter.items = viewModel.getProjectsFromLocal()
        }

        binding.draftReportsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reportsAdapter
        }
    }

    private fun setOnClickListener() {
        binding.toolbarLayout.projectTitleLayout.setOnClickListener {
            setOnClickProjectName()
        }

        binding.projectSwipeRefreshView.apply {
            setOnRefreshListener {
                isRefreshing = true
                when {
                    requireContext().isOnAirplaneMode() -> {
                        isRefreshing = false
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.project_could_not_refreshed) + " " + getString(R.string.pls_off_air_plane_mode),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    !requireContext().isNetworkAvailable() -> {
                        isRefreshing = false
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.project_could_not_refreshed) + " " + getString(R.string.no_internet_connection),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {
                        viewModel.fetchProjects()
                    }
                }
            }
            setColorSchemeResources(R.color.colorPrimary)
        }

        binding.createReportButton.setOnClickListener {
            showDialogSelectSite()
        }
    }

    private fun showDialogSelectSite() {
        dialog = SelectSiteDialog(this)
        dialog.show(childFragmentManager, SelectSiteDialog::class.java.name)
    }

    override fun onSiteSelected(site: Stream) {
        if (requireContext().isOnAirplaneMode()) {
            Toast.makeText(requireContext(), getString(R.string.pls_off_air_plane_mode), Toast.LENGTH_SHORT).show()
            listener.openCreateReportActivity(site.externalId!!, isUnexpected = true)
            dialog.dismiss()
        }

        locationPermissions.check {
            if (it) {
                listener.getCurrentLocation()?.let { loc ->
                    saveLocation(loc)
                }
                listener.openCreateReportActivity(site.externalId!!, isUnexpected = true)
                dialog.dismiss()
            }
        }
    }

    private fun setObserve() {
        lifecycleScope.launchWhenStarted {
            viewModel.responses.collectLatest { responses ->
                streams = viewModel.getStreamIdsInProjectId()
                val items = responses.sortedByDescending { r -> r.startedAt }.filter { r -> r.submittedAt == null && streams.contains(r.streamId) }
                binding.notHaveDraftReportsGroupView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                reportsAdapter.items =
                    items.map { Pair(it, viewModel.getStream(it.streamId)?.timezone) }
            }
        }
    }

    private fun setOnClickProjectName() {
        if (binding.projectRecyclerView.visibility == View.VISIBLE) {
            binding.toolbarLayout.expandMoreImageView.rotation = 0F
            listener.showBottomAppBar()
            binding.projectRecyclerView.visibility = View.GONE
            binding.projectSwipeRefreshView.visibility = View.GONE
        } else {
            binding.toolbarLayout.expandMoreImageView.rotation = 180F
            listener.hideBottomAppBar()
            binding.projectRecyclerView.visibility = View.VISIBLE
            binding.projectSwipeRefreshView.visibility = View.VISIBLE
        }
    }

    override fun onClickedItem(response: Response) {
        if (response.syncState == SyncState.SENT.value) {
            Toast.makeText(context, getString(R.string.can_not_open_the_report), Toast.LENGTH_SHORT).show()
        } else {
            listener.openCreateResponse(response)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            val projectId = preferences.getString(Preferences.SELECTED_PROJECT, "")
            setProjectTitle(viewModel.getProjectName(projectId))

            streams = viewModel.getStreamIdsInProjectId()
            val items =
                viewModel.getResponsesFromLocal().sortedByDescending { r -> r.startedAt }
                    .filter { r -> r.submittedAt == null && streams.contains(r.streamId) }
            reportsAdapter.items =
                items.map { Pair(it, viewModel.getStream(it.streamId)?.timezone) }
            binding.notHaveDraftReportsGroupView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onProjectClicked(project: Project) {
        binding.toolbarLayout.expandMoreImageView.rotation = 0F

        listener.showBottomAppBar()
        binding.projectRecyclerView.visibility = View.GONE
        binding.projectSwipeRefreshView.visibility = View.GONE
        viewModel.setProjectSelected(project.id)
        firebaseCrashlytics.setCustomKey(CrashlyticsKey.OnSelectedProject.key, project.id)

        when {
            requireContext().isOnAirplaneMode() -> {
                Toast.makeText(requireContext(), getString(R.string.pls_off_air_plane_mode), Toast.LENGTH_LONG).show()
            }
            !requireContext().isNetworkAvailable() -> {
                Toast.makeText(requireContext(), getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show()
            }
            else -> {
                streams = viewModel.getStreamIdsInProjectId()
                val items = viewModel.getResponsesFromLocal().sortedByDescending { r -> r.startedAt }
                    .filter { r -> r.submittedAt == null && streams.contains(r.streamId) }
                reportsAdapter.items =
                    items.map { Pair(it, viewModel.getStream(it.streamId)?.timezone) }
                binding.notHaveDraftReportsGroupView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        setProjectTitle(project.name)
    }

    override fun onLockImageClicked() {
        Toast.makeText(context, R.string.not_have_permission, Toast.LENGTH_LONG).show()
    }

    private fun setProjectTitle(str: String) {
        binding.toolbarLayout.projectTitleTextView.text = str
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

    companion object {
        const val tag = "DraftReportsFragment"

        @JvmStatic
        fun newInstance() = DraftReportsFragment()
    }
}
