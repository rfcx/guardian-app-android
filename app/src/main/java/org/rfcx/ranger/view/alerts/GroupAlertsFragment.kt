package org.rfcx.ranger.view.alerts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_group_alerts.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.data.remote.success
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.util.handleError
import org.rfcx.ranger.view.alerts.adapter.GroupByGuardianAdapter
import org.rfcx.ranger.view.base.BaseFragment

class GroupAlertsFragment : BaseFragment() {

    private val viewModel: GroupAlertsViewModel by viewModel()
    private val groupByGuardianAdapter by lazy { GroupByGuardianAdapter() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_group_alerts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupAlertsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = groupByGuardianAdapter
        }

        viewModel.groupGuardianAlert.observe(this, Observer {
            it.success({
                loadingProgress.visibility = View.INVISIBLE
                groupByGuardianAdapter.items = it
            }, {
                loadingProgress.visibility = View.INVISIBLE
                context.handleError(it)
            }, {
                loadingProgress.visibility = View.VISIBLE
            })
        })

        groupByGuardianAdapter.mOnItemClickListener = object : OnItemClickListener {
            override fun onItemClick(eventsList: ArrayList<Event>, name: String) {
                //			context?.let { GuardianListDetailActivity.startActivity(it, name, eventsList) }
            }
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
    fun onItemClick(eventsList: ArrayList<Event>, name: String)
}