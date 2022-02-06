package org.rfcx.incidents.view.events.detail

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
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.adapter.classifycation.ClassificationAdapter
import org.rfcx.incidents.data.remote.common.success
import org.rfcx.incidents.databinding.ActivityAlertDetailBinding
import org.rfcx.incidents.entity.event.Event
import org.rfcx.incidents.util.getTokenID
import org.rfcx.incidents.util.setReportImage
import org.rfcx.incidents.util.toTimeSinceStringAlternativeTimeAgo
import org.rfcx.incidents.view.events.detail.AlertDetailViewModel.Companion.maxProgress
import java.util.Date

class AlertDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlertDetailBinding
    private val viewModel: AlertDetailViewModel by viewModel()

    companion object {
        const val EXTRA_EVENT_ID = "EXTRA_EVENT_ID"
        private const val SECOND = 1000L

        fun startActivity(context: Context, eventId: String) {
            val intent = Intent(context, AlertDetailActivity::class.java)
            intent.putExtra(EXTRA_EVENT_ID, eventId)
            context.startActivity(intent)
        }
    }

    private var eventId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        eventId = intent?.getStringExtra(EXTRA_EVENT_ID)
        setupToolbar()

        val event = eventId?.let { viewModel.getEvent(it) }
        binding.guardianNameTextView.text = event?.classification?.title
        binding.timeTextView.text = event?.createdAt?.toTimeSinceStringAlternativeTimeAgo(this)
        val token = this.getTokenID()

        event?.let {
            var eventData = it
            if (event.end.time - event.start.time < 5 * SECOND) {
                eventData = it.setNewTime(Date(event.start.time - 5 * SECOND), Date(event.end.time + 5 * SECOND))
            }
            if (event.end.time - event.start.time > 15 * SECOND) {
                eventData = it.setNewTime(end = Date(event.start.time + 15 * SECOND))
            }

            binding.spectrogramImageView.setReportImage(
                url = viewModel.setFormatUrlOfSpectrogram(eventData),
                fromServer = true,
                token = token,
                progressBar = binding.loadingImageProgressBar
            )
            viewModel.setEvent(eventData)
            setupView(viewModel.setFormatUrlOfAudio(eventData))
            observePlayer()
        }

        binding.soundProgressSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.seekPlayerTo((progress.toLong() * viewModel.getDuration() / maxProgress) * SECOND)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        viewModel.classifiedCation.observe(this) { it ->
            it.success({ confidence ->
                val classificationAdapter = ClassificationAdapter()
                classificationAdapter.setClassification(confidence, viewModel.getDuration())

                val gridLayoutManager = GridLayoutManager(this, viewModel.getDuration().toInt())
                gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return classificationAdapter.lists[position].durationSecond()
                    }
                }
                binding.classificationRecyclerView.apply {
                    setHasFixedSize(true)
                    layoutManager = gridLayoutManager
                    adapter = classificationAdapter
                }
            })
        }
    }

    private fun Event.setNewTime(start: Date? = null, end: Date? = null): Event = Event(
        id = this.id,
        name = this.name,
        streamId = this.streamId,
        projectId = this.projectId,
        createdAt = this.createdAt,
        start = start
            ?: this.start,
        end = end
            ?: this.end,
        classification = this.classification,
    )

    private fun setupView(url: String) {
        binding.soundProgressSeekBar.max = maxProgress

        binding.replayButton.setOnClickListener {
            viewModel.replaySound(url)
        }
    }

    private fun observePlayer() {
        viewModel.playerError.observe(this) {
            Toast.makeText(this, R.string.can_not_play_audio, Toast.LENGTH_SHORT).show()
            binding.loadingSoundProgressBar.visibility = View.INVISIBLE
            binding.replayButton.visibility = View.VISIBLE
        }

        viewModel.playerState.observe(this) {
            when (it) {
                Player.STATE_BUFFERING -> {
                    binding.replayButton.visibility = View.INVISIBLE
                    binding.loadingSoundProgressBar.visibility = View.VISIBLE
                }
                Player.STATE_READY -> {
                    binding.replayButton.visibility = View.INVISIBLE
                    binding.loadingSoundProgressBar.visibility = View.INVISIBLE
                }
                Player.STATE_IDLE -> {
                    binding.loadingSoundProgressBar.visibility = View.INVISIBLE
                }
                Player.STATE_ENDED -> {
                    binding.replayButton.visibility = View.VISIBLE
                }
            }
        }

        viewModel.playerProgress.observe(this) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.soundProgressSeekBar.setProgress(it, true)
            } else {
                binding.soundProgressSeekBar.progress = it
            }
        }

        viewModel.loadAudioError.observe(this) {
            binding.canNotLoadImageLayout.visibility = View.VISIBLE
            binding.loadingSoundProgressBar.visibility = View.INVISIBLE
            binding.soundProgressSeekBar.visibility = View.INVISIBLE
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout)
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
