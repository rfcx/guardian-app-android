package org.rfcx.incidents.view.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentReportViewPagerBinding
import org.rfcx.incidents.view.MainActivity

class ReportViewPagerFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentReportViewPagerBinding? = null
    private val binding get() = _binding!!
    private val reportViewModel: ReportViewPagerFragmentViewModel by viewModel()
    private lateinit var viewPagerAdapter: ReportViewPagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportViewPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()

        reportViewModel.getReports().observe(
            this
        ) { it ->
            viewPagerAdapter.reports = it

            val reportIndex = it.indexOf(
                it.find {
                    it.id == arguments?.getInt(BUNDLE_REPORT_ID)
                }
            )

            binding.viewPager.post {
                binding.viewPager.setCurrentItem(reportIndex, false)
            }
        }
    }

    private fun initAdapter() {
        binding.viewPager.clipToPadding = false
        binding.viewPager.clipChildren = false
        binding.viewPager.setPadding(
            resources.getDimensionPixelSize(R.dimen.viewpager_padding),
            0, resources.getDimensionPixelSize(R.dimen.viewpager_padding), 0
        )

        val marginTransformer = MarginPageTransformer(resources.getDimensionPixelSize(R.dimen.margin_padding_normal))
        binding.viewPager.setPageTransformer(marginTransformer)
        binding.viewPager.offscreenPageLimit = 2
        viewPagerAdapter = ReportViewPagerAdapter(childFragmentManager, lifecycle)
        binding.viewPager.adapter = viewPagerAdapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
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
