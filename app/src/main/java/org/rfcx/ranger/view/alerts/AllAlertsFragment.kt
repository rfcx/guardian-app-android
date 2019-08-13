package org.rfcx.ranger.view.alerts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_all_alerts.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.view.alerts.adapter.AlertClickListener
import org.rfcx.ranger.view.alerts.adapter.AlertsAdapter
import org.rfcx.ranger.view.base.BaseFragment

class AllAlertsFragment : BaseFragment(), AlertClickListener {
    private val alertsViewModel: AlertsViewModel by viewModel()
    private val alertsAdapter by lazy {
        AlertsAdapter(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_all_alerts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        alertsViewModel.getLoading().observe(this, Observer {
            loadingProgress.visibility = if (it) View.VISIBLE else View.INVISIBLE
        })

        setupAlertList()
    }

    override fun onClickedAlert(event: Event) {
        // TODO: handle on click alert's item
    }

    private fun setupAlertList() {
        alertsRecyclerView?.apply {
            adapter = alertsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    companion object {
        const val tag = "AllAlertsFragment"
        fun newInstance(): AllAlertsFragment {
            return AllAlertsFragment()
        }
    }
}