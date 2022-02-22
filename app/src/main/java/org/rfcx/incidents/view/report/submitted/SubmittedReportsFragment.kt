package org.rfcx.incidents.view.report.submitted

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.data.preferences.Preferences
import org.rfcx.incidents.data.remote.common.success
import org.rfcx.incidents.databinding.FragmentSubmittedReportsBinding
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.response.SyncState
import org.rfcx.incidents.entity.stream.Project
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Screen
import org.rfcx.incidents.util.isNetworkAvailable
import org.rfcx.incidents.util.isOnAirplaneMode
import org.rfcx.incidents.view.MainActivityEventListener
import org.rfcx.incidents.view.MainActivityViewModel
import org.rfcx.incidents.view.events.adapter.ProjectAdapter
import org.rfcx.incidents.view.events.adapter.ProjectOnClickListener
import java.util.TimeZone

class SubmittedReportsFragment : Fragment(), SubmittedReportsOnClickListener, ProjectOnClickListener {
    private var _binding: FragmentSubmittedReportsBinding? = null
    private val binding get() = _binding!!

    private val analytics by lazy { context?.let { Analytics(it) } }
    private val viewModel: MainActivityViewModel by viewModel() // TODO should have its own view model
    private val reportsAdapter by lazy { SubmittedReportsAdapter(this) }
    private val projectAdapter by lazy { ProjectAdapter(this) }
    lateinit var listener: MainActivityEventListener
    lateinit var preferences: Preferences
    private var streams = listOf<String>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = (context as MainActivityEventListener)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            val projectId = preferences.getString(Preferences.SELECTED_PROJECT, "")
            setProjectTitle(viewModel.getProjectName(projectId))
            streams = viewModel.getStreamIdsInProjectId()
            val items = viewModel.getResponsesFromLocal().sortedByDescending { r -> r.submittedAt }
                .filter { r -> r.syncState == SyncState.SENT.value && streams.contains(r.streamId) }
            reportsAdapter.items = items.map { ResponseItem(it, TimeZone.getTimeZone(viewModel.getStream(it.streamId)?.timezone)) }
            binding.notHaveSubmittedReportsGroupView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubmittedReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences = Preferences.getInstance(requireContext())
        setRecyclerView()
        setObserve()
        setOnClickListener()
        binding.toolbarLayout.changePageImageView.visibility = View.GONE

        val projectId = preferences.getString(Preferences.SELECTED_PROJECT, "")
        setProjectTitle(viewModel.getProjectName(projectId))
    }

    private fun setRecyclerView() {
        binding.projectRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = projectAdapter
            projectAdapter.items = viewModel.getProjectsFromLocal()
        }

        binding.submittedReportsRecyclerView.apply {
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

    override fun onResume() {
        super.onResume()
        analytics?.trackScreen(Screen.SUBMITTED_REPORTS)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setObserve() {
        viewModel.getResponses().observe(viewLifecycleOwner, { res ->
            streams = viewModel.getStreamIdsInProjectId()
            val items = res.sortedByDescending { r -> r.submittedAt }
                .filter { r -> r.syncState == SyncState.SENT.value && streams.contains(r.streamId) }
            reportsAdapter.items = items.map { ResponseItem(it, TimeZone.getTimeZone(viewModel.getStream(it.streamId)?.timezone)) }
            reportsAdapter.notifyDataSetChanged()
            binding.notHaveSubmittedReportsGroupView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        })

        viewModel.getProjectsFromRemote.observe(viewLifecycleOwner, { it ->
            it.success({
                binding.projectSwipeRefreshView.isRefreshing = false
                projectAdapter.items = listOf()
                projectAdapter.items = viewModel.getProjectsFromLocal()
                projectAdapter.notifyDataSetChanged()
            }, {
                binding.projectSwipeRefreshView.isRefreshing = false
                Toast.makeText(
                    context,
                    it.message
                        ?: getString(R.string.something_is_wrong),
                    Toast.LENGTH_LONG
                ).show()
            }, {
            })
        })
    }

    override fun onClicked(project: Project) {
        binding.toolbarLayout.expandMoreImageView.rotation = 0F

        listener.showBottomAppBar()
        binding.projectRecyclerView.visibility = View.GONE
        binding.projectSwipeRefreshView.visibility = View.GONE
        viewModel.setProjectSelected(project.id)

        when {
            requireContext().isOnAirplaneMode() -> {
                Toast.makeText(requireContext(), getString(R.string.pls_off_air_plane_mode), Toast.LENGTH_LONG).show()
            }
            !requireContext().isNetworkAvailable() -> {
                Toast.makeText(requireContext(), getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show()
            }
            else -> {
                streams = viewModel.getStreamIdsInProjectId()
                val items = viewModel.getResponsesFromLocal().sortedByDescending { r -> r.submittedAt }
                    .filter { r -> r.syncState == SyncState.SENT.value && streams.contains(r.streamId) }
                reportsAdapter.items = items.map { ResponseItem(it, TimeZone.getTimeZone(viewModel.getStream(it.streamId)?.timezone)) }
                binding.notHaveSubmittedReportsGroupView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        setProjectTitle(project.name)
    }

    private fun setProjectTitle(str: String) {
        binding.toolbarLayout.projectTitleTextView.text = str
    }

    override fun onLockImageClicked() {
        Toast.makeText(context, R.string.not_have_permission, Toast.LENGTH_LONG).show()
    }

    override fun onClickedItem(response: Response) {
        if (response.syncState == SyncState.SENT.value) {
            response.guid?.let { listener.openDetailResponse(it) }
        }
    }

    companion object {
        const val tag = "SubmittedReportsFragment"

        @JvmStatic
        fun newInstance() = SubmittedReportsFragment()
    }
}
