package org.rfcx.incidents.view.alert

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.exoplayer2.Player
import kotlinx.android.synthetic.main.activity_alert_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.adapter.classifycation.ClassificationAdapter
import org.rfcx.incidents.data.remote.success
import org.rfcx.incidents.entity.alert.Alert
import org.rfcx.incidents.util.getTokenID
import org.rfcx.incidents.util.setReportImage
import org.rfcx.incidents.util.toTimeSinceStringAlternativeTimeAgo
import org.rfcx.incidents.view.alert.AlertDetailViewModel.Companion.maxProgress
import java.util.*

class AlertDetailActivity : AppCompatActivity() {
    private val viewModel: AlertDetailViewModel by viewModel()
    
    companion object {
        const val EXTRA_ALERT_ID = "EXTRA_ALERT_ID"
        private const val SECOND = 1000L
        
        fun startActivity(context: Context, alertId: String) {
            val intent = Intent(context, AlertDetailActivity::class.java)
            intent.putExtra(EXTRA_ALERT_ID, alertId)
            context.startActivity(intent)
        }
    }
    
    private var alertId: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert_detail)
        alertId = intent?.getStringExtra(EXTRA_ALERT_ID)
        setupToolbar()
        
        val alert = alertId?.let { viewModel.getAlert(it) }
        guardianNameTextView.text = alert?.classification?.title
        timeTextView.text = alert?.createdAt?.toTimeSinceStringAlternativeTimeAgo(this)
        val token = this.getTokenID()
        
        alert?.let {
            var alertData = it
            if (alert.end.time - alert.start.time < 5 * SECOND) {
                alertData = it.setNewTime(Date(alert.start.time - 5 * SECOND), Date(alert.end.time + 5 * SECOND))
            }
            if (alert.end.time - alert.start.time > 15 * SECOND) {
                alertData = it.setNewTime(end = Date(alert.start.time + 15 * SECOND))
            }
            
            spectrogramImageView.setReportImage(
                url = viewModel.setFormatUrlOfSpectrogram(alertData),
                fromServer = true,
                token = token,
                progressBar = loadingImageProgressBar
            )
            viewModel.setAlert(alertData)
            setupView(viewModel.setFormatUrlOfAudio(alertData))
            observePlayer()
        }
        
        soundProgressSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.seekPlayerTo((progress.toLong() * viewModel.getDuration() / maxProgress) * SECOND)
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        viewModel.classifiedCation.observe(this, { it ->
            it.success({ confidence ->
                val classificationAdapter = ClassificationAdapter()
                classificationAdapter.setClassification(confidence, viewModel.getDuration())
                
                val gridLayoutManager = GridLayoutManager(this, viewModel.getDuration().toInt())
                gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return classificationAdapter.lists[position].durationSecond()
                    }
                }
                classificationRecyclerView.apply {
                    setHasFixedSize(true)
                    layoutManager = gridLayoutManager
                    adapter = classificationAdapter
                }
            })
        })
    }
    
    private fun Alert.setNewTime(start: Date? = null, end: Date? = null): Alert = Alert(
        id = this.id,
        serverId = this.serverId,
        name = this.name,
        streamId = this.streamId,
        projectId = this.projectId,
        createdAt = this.createdAt,
        start = start
            ?: this.start,
        end = end
            ?: this.end,
        classification = this.classification,
        incident = this.incident
    )
    
    private fun setupView(url: String) {
        soundProgressSeekBar.max = maxProgress
        
        replayButton.setOnClickListener {
            viewModel.replaySound(url)
        }
    }
    
    private fun observePlayer() {
        viewModel.playerError.observe(this, {
            Toast.makeText(this, R.string.can_not_play_audio, Toast.LENGTH_SHORT).show()
            loadingSoundProgressBar.visibility = View.INVISIBLE
            replayButton.visibility = View.VISIBLE
        })
        
        viewModel.playerState.observe(this, {
            when (it) {
                Player.STATE_BUFFERING -> {
                    replayButton.visibility = View.INVISIBLE
                    loadingSoundProgressBar.visibility = View.VISIBLE
                }
                Player.STATE_READY -> {
                    replayButton.visibility = View.INVISIBLE
                    loadingSoundProgressBar.visibility = View.INVISIBLE
                }
                Player.STATE_IDLE -> {
                    loadingSoundProgressBar.visibility = View.INVISIBLE
                }
                Player.STATE_ENDED -> {
                    replayButton.visibility = View.VISIBLE
                }
            }
        })
        
        viewModel.playerProgress.observe(this, {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                soundProgressSeekBar?.setProgress(it, true)
            } else {
                soundProgressSeekBar?.progress = it
            }
        })
        
        viewModel.loadAudioError.observe(this, {
            canNotLoadImageLayout.visibility = View.VISIBLE
            loadingSoundProgressBar.visibility = View.INVISIBLE
            soundProgressSeekBar.visibility = View.INVISIBLE
        })
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbarLayout)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            elevation = 0f
            title = getString(R.string.guardian_event_detail)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
