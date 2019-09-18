package org.rfcx.ranger.view.alerts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_group_alerts.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.data.remote.success
import org.rfcx.ranger.util.handleError
import org.rfcx.ranger.view.base.BaseFragment

class GroupAlertsFragment : BaseFragment() {
    
    private val viewModel: GroupAlertsViewModel by viewModel()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_group_alerts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.items.observe(this, Observer { it ->

            it.success({
                // Success block
                Log.d("", it.toString())
                loadingProgress.visibility = View.INVISIBLE
            }, {
                loadingProgress.visibility = View.INVISIBLE
                context.handleError(it)
            }, {
                // Loading block
                loadingProgress.visibility = View.VISIBLE
            })
        })
    }

    companion object {
        const val tag = "GroupAlertsFragment"
        fun newInstance(): GroupAlertsFragment {
            return GroupAlertsFragment()
        }
    }
}