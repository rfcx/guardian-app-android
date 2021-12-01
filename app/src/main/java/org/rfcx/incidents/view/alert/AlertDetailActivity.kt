package org.rfcx.incidents.view.alert

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.Player
import kotlinx.android.synthetic.main.activity_alert_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.util.getTokenID
import org.rfcx.incidents.util.setReportImage
import org.rfcx.incidents.util.toTimeSinceStringAlternativeTimeAgo
import org.rfcx.incidents.view.alert.AlertDetailViewModel.Companion.maxProgress

class AlertDetailActivity : AppCompatActivity() {
	private val viewModel: AlertDetailViewModel by viewModel()
	
	companion object {
		const val EXTRA_ALERT_ID = "EXTRA_ALERT_ID"
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
			spectrogramImageView.setReportImage(
					url = viewModel.setFormatUrlOfSpectrogram(it),
					fromServer = true,
					token = token,
					progressBar = loadingImageProgressBar
			)
			viewModel.setAlert(it)
			setupView(viewModel.setFormatUrlOfAudio(it))
			observePlayer()
		}
		
		soundProgressSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
				if (fromUser) {
					viewModel.seekPlayerTo(progress.toLong() * viewModel.getDuration() / maxProgress)
				}
			}
			
			override fun onStartTrackingTouch(seekBar: SeekBar?) {}
			
			override fun onStopTrackingTouch(seekBar: SeekBar?) {}
		})
	}
	
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
