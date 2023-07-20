package org.rfcx.incidents.view.report.draft

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.databinding.WidgetSelectSiteDialogBinding
import org.rfcx.incidents.view.MainActivityViewModel

class SelectSiteDialog(private val listener: SelectSiteListener) : DialogFragment() {

    private lateinit var _binding: WidgetSelectSiteDialogBinding
    private val viewModel: MainActivityViewModel by viewModel()
    private val selectSiteAdapter by lazy { SelectSiteAdapter(listener) }

    private var isFirstTime = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = WidgetSelectSiteDialogBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding.siteListView.let {
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = selectSiteAdapter
            it.addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        lifecycleScope.launchWhenStarted {
            viewModel.currentLocationState.collectLatest {
                if (it != null && isFirstTime) {
                    selectSiteAdapter.items = viewModel.getStreamsByDistance()
                    isFirstTime = false
                }
            }
        }
    }
}
