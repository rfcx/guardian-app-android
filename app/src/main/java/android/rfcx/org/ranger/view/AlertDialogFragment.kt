package android.rfcx.org.ranger.view

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.rfcx.org.ranger.R
import android.rfcx.org.ranger.entity.event.Event
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_alert_dialog.*


class AlertDialogFragment : DialogFragment(), MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private var event: Event? = null
    private var mediaPlayer: MediaPlayer? = null
    private var onAlertConfirmCallback: OnAlertConfirmCallback? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnAlertConfirmCallback) {
            onAlertConfirmCallback = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getBundle()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_alert_dialog, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initPlayer()
        alertReplayButton.setOnClickListener {
            rePlay()
        }

        alertNoButton.setOnClickListener {
            dismissAllowingStateLoss()
        }

        alertYesButton.setOnClickListener {
            report()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDetach() {
        super.onDetach()
        if (onAlertConfirmCallback != null)
            onAlertConfirmCallback = null
    }

    private fun getBundle() {
        event = arguments.getParcelable(keyEventArgs)
        if (event == null) {
            dismissAllowingStateLoss()
        }
    }

    private fun initPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setWakeMode(context,
                PowerManager.PARTIAL_WAKE_LOCK)
        mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer?.setOnPreparedListener(this@AlertDialogFragment)
        mediaPlayer?.setOnCompletionListener(this@AlertDialogFragment)
        mediaPlayer?.setOnErrorListener(this@AlertDialogFragment)

        val mp3Source = event?.audio?.mp3
        if (!mp3Source.isNullOrEmpty()) {
            mediaPlayer?.setDataSource(context, Uri.parse(event?.audio?.mp3))
            mediaPlayer?.prepareAsync()
        } else {
            dismissAllowingStateLoss()
        }
    }

    private fun initView() {
        alertTitleTextView.text = getString(R.string.alert_title_format, event?.value)
    }

    private fun rePlay() {
        alertReplayButton.visibility = View.INVISIBLE
        alertPlayerStateTextView.setText(R.string.alert_state_playing)
        alertPlayerStateTextView.visibility = View.VISIBLE
        try {
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun report() {
        // todo report to API
    }

    override fun onError(player: MediaPlayer?, p1: Int, p2: Int): Boolean {
        dismissAllowingStateLoss()
        return false
    }

    override fun onCompletion(player: MediaPlayer?) {
        alertPlayerStateTextView.setText(R.string.alert_state_replay)
        alertReplayButton.visibility = View.VISIBLE
        alertPlayerStateTextView.visibility = View.INVISIBLE
    }

    override fun onPrepared(player: MediaPlayer?) {
        mediaPlayer?.start()
        alertPlayerStateTextView.setText(R.string.alert_state_playing)
    }

    companion object {
        val keyEventArgs = "AlertDialogFragment.Event"
        fun newInstance(event: Event): AlertDialogFragment {
            val fragment = AlertDialogFragment()
            val args = Bundle()
            args.putParcelable(keyEventArgs, event)
            fragment.arguments = args
            return fragment
        }
    }

    interface OnAlertConfirmCallback {
        fun onCurrentAlert(event: Event)
        fun onIncorrectAlert(event: Event)
    }
}
