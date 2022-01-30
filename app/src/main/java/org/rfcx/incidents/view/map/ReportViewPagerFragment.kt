package org.rfcx.incidents.view.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_report_view_pager.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.view.MainActivity

class ReportViewPagerFragment : BottomSheetDialogFragment() {
    
    private val reportViewModel: ReportViewPagerFragmentViewModel by viewModel()
    private lateinit var viewPagerAdapter: ReportViewPagerAdapter
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_report_view_pager, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initAdapter()
        
        reportViewModel.getReports().observe(this, Observer { it ->
            viewPagerAdapter.reports = it
            
            val reportIndex = it.indexOf(it.find {
                it.id == arguments?.getInt(BUNDLE_REPORT_ID)
            })
            
            viewPager.post {
                viewPager.setCurrentItem(reportIndex, false)
            }
            
        })
    }
    
    private fun initAdapter() {
        viewPager.clipToPadding = false
        viewPager.clipChildren = false
        viewPager.setPadding(
            resources.getDimensionPixelSize(R.dimen.viewpager_padding),
            0, resources.getDimensionPixelSize(R.dimen.viewpager_padding), 0
        )
        
        val marginTransformer = MarginPageTransformer(resources.getDimensionPixelSize(R.dimen.margin_padding_normal))
        viewPager.setPageTransformer(marginTransformer)
        viewPager.offscreenPageLimit = 2
        viewPagerAdapter = ReportViewPagerAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = viewPagerAdapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                
                if (activity is MainActivity && position < viewPagerAdapter.itemCount) {
                    (activity as MainActivity).moveMapIntoReportMarker(
                        viewPagerAdapter.reports[position]
                    )
                }
            }
        })
    }
    
    companion object {
        const val tag = "ReportViewPagerFragment"
        
        fun newInstance(reportId: Int): ReportViewPagerFragment {
            return ReportViewPagerFragment().apply {
                arguments = Bundle().apply {
                    putInt(BUNDLE_REPORT_ID, reportId)
                }
            }
        }
        
        private const val BUNDLE_REPORT_ID = "BUNDLE_REPORT_ID"
    }
}
