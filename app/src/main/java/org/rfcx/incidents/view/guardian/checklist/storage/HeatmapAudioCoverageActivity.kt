package org.rfcx.incidents.view.guardian.checklist.storage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ActivityHeatmapAudioCoverageBinding
import org.rfcx.incidents.entity.guardian.socket.GuardianArchived
import org.rfcx.incidents.widget.MonthYearPickerDialog

class HeatmapAudioCoverageActivity :
    AppCompatActivity(), MonthYearPickerDialog.OnPickListener {

    lateinit var binding: ActivityHeatmapAudioCoverageBinding
    private val viewModel: HeatmapAudioCoverageViewModel by viewModel()

    private val archivedHeatmapAdapter by lazy { ArchivedHeatmapAdapter() }

    private var menuAll: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_heatmap_audio_coverage)
        binding.lifecycleOwner = this
        setContentView(binding.root)

        binding.viewModel = viewModel

        setupToolbar()

        showLoading()
        getExtra()
        binding.archivedHeatmap.apply {
            adapter = archivedHeatmapAdapter
            layoutManager = GridLayoutManager(context, 25)
        }
        addHoursItem()

        lifecycleScope.launch {
            viewModel.archivedItemsState.collectLatest {
                if (it.isNotEmpty()) {
                    archivedHeatmapAdapter.setData(it)
                    hideLoading()
                }
            }
        }

        viewModel.onPick(viewModel.selectedMonth, viewModel.selectedYear)
    }

    private fun getExtra() {
        val parcel = intent?.extras?.getParcelableArray(EXTRA_ARCHIVED_AUDIO) ?: return
        viewModel.setArchivedData(parcel.map { it as GuardianArchived })
    }

    private fun addHoursItem() {
        val text = TextView(this).apply {
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            textSize = 8f
        }
        binding.hoursLayout.addView(text)
        val params = text.layoutParams as LinearLayout.LayoutParams
        params.width = 0
        params.weight = 1f
        text.layoutParams = params
        for (k in 0..23) {
            if (k < 10) {
                val text2 = TextView(this).apply {
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    textSize = 8f
                }
                text2.text = getString(R.string.hour_less_than_10, k)
                binding.hoursLayout.addView(text2)
                val params2 = text2.layoutParams as LinearLayout.LayoutParams
                params2.width = 0
                params2.weight = 1f
                text2.layoutParams = params2
            } else {
                val text3 = TextView(this).apply {
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    textSize = 8f
                }
                text3.text = k.toString()
                binding.hoursLayout.addView(text3)
                val params3 = text3.layoutParams as LinearLayout.LayoutParams
                params3.width = 0
                params3.weight = 1f
                text3.layoutParams = params3
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbarDefault)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Audio Coverage"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuAll = menu
        val inflater = menuInflater
        inflater.inflate(R.menu.month_year_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.picker -> {
                MonthYearPickerDialog.newInstance(
                    System.currentTimeMillis(),
                    viewModel.selectedMonth,
                    viewModel.selectedYear,
                    viewModel.availableYearMonths,
                    this
                ).show(supportFragmentManager, MonthYearPickerDialog::class.java.name)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPick(month: Int, year: Int) {
        showLoading()
        viewModel.onPick(month, year)
    }

    private fun showLoading() {
        binding.coverageLoading.visibility = View.VISIBLE
        binding.archivedDate.visibility = View.GONE
        binding.archivedHeatmap.visibility = View.GONE
        binding.hoursLayout.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.coverageLoading.visibility = View.GONE
        binding.archivedDate.visibility = View.VISIBLE
        binding.archivedHeatmap.visibility = View.VISIBLE
        binding.hoursLayout.visibility = View.VISIBLE
    }

    companion object {

        private const val EXTRA_ARCHIVED_AUDIO = "EXTRA_ARCHIVED_AUDIO"

        fun startActivity(context: Context, archivedAudios: List<GuardianArchived>) {
            val intent = Intent(context, HeatmapAudioCoverageActivity::class.java)
            intent.putExtra(EXTRA_ARCHIVED_AUDIO, archivedAudios.toTypedArray())
            context.startActivity(intent)
        }
    }
}
