package org.rfcx.incidents.view.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.opensooq.supernova.gligar.GligarPicker
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.adapter.entity.BaseListItem
import org.rfcx.incidents.databinding.ActivityFeedbackBinding
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.CameraPermissions
import org.rfcx.incidents.util.GalleryPermissions
import org.rfcx.incidents.util.ReportUtils
import org.rfcx.incidents.util.Screen
import java.io.File

class FeedbackActivity : AppCompatActivity() {
    lateinit var binding: ActivityFeedbackBinding
    private val feedbackViewModel: FeedbackViewModel by viewModel()
    private var imageFile: File? = null
    private val cameraPermissions by lazy { CameraPermissions(this) }
    private val galleryPermissions by lazy { GalleryPermissions(this) }
    private val feedbackImageAdapter by lazy { FeedbackImageAdapter() }
    private var pathListArray: List<String>? = null
    private var menuAll: Menu? = null
    private val analytics by lazy { Analytics(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setTextFrom()
        setupFeedbackImages()

        binding.feedbackEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    if (p0.isEmpty()) {
                        setEnableSendFeedbackView(false)
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d("", "beforeTextChanged")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                setEnableSendFeedbackView()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        analytics.trackScreen(Screen.FEEDBACK)
    }

    private fun setupFeedbackImages() {
        binding.feedbackRecycler.apply {
            layoutManager = LinearLayoutManager(this@FeedbackActivity)
            adapter = feedbackImageAdapter
        }

        feedbackImageAdapter.onFeedbackImageAdapterClickListener = object : OnFeedbackImageAdapterClickListener {
            override fun pathListArray(path: ArrayList<BaseListItem>) {
                val pathList = mutableListOf<String>()
                path.forEach {
                    val itemImage = it as LocalImageItem
                    pathList.add(itemImage.localPath)
                }
                pathListArray = pathList
            }

            override fun onDeleteImageClick(position: Int) {
                feedbackImageAdapter.removeAt(position)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        handleGalleryResult(requestCode, resultCode, data)
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuAll = menu
        val inflater = menuInflater
        inflater.inflate(R.menu.feedback_menu, menu)
        setEnableSendFeedbackView(false)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        R.id.attachView
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.attachView -> {
                if (!cameraPermissions.allowed() || !galleryPermissions.allowed()) {
                    imageFile = null
                    if (!cameraPermissions.allowed()) cameraPermissions.check { }
                    if (!galleryPermissions.allowed()) galleryPermissions.check { }
                } else {
                    startOpenGallery()
                }
            }
            R.id.sendFeedbackView -> sendFeedback()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setEnableSendFeedbackView(start: Boolean = true) {
        menuAll?.findItem(R.id.sendFeedbackView)?.isEnabled = start
        val itemSend = menuAll?.findItem(R.id.sendFeedbackView)?.icon
        val wrapDrawable = itemSend?.let { DrawableCompat.wrap(it) }

        if (wrapDrawable != null) {
            if (start) DrawableCompat.setTint(
                wrapDrawable,
                ContextCompat.getColor(this, R.color.colorPrimary)
            ) else DrawableCompat.setTint(wrapDrawable, ContextCompat.getColor(this, android.R.color.darker_gray))
        }
    }

    private fun openGallery() {
        if (!galleryPermissions.allowed()) {
            imageFile = null
            galleryPermissions.check { }
        } else {
            startOpenGallery()
        }
    }

    private fun startOpenGallery() {
        if (feedbackImageAdapter.getImageCount() < FeedbackImageAdapter.MAX_IMAGE_SIZE) {
            val remainingImage = FeedbackImageAdapter.MAX_IMAGE_SIZE - feedbackImageAdapter.getImageCount()
            GligarPicker()
                .requestCode(ReportUtils.REQUEST_GALLERY)
                .limit(remainingImage)
                .withActivity(this)
                .show()
        } else {
            Toast.makeText(this, R.string.maximum_number_of_attachments, Toast.LENGTH_LONG).show()
        }
    }

    private fun handleGalleryResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        if (requestCode != ReportUtils.REQUEST_GALLERY || resultCode != Activity.RESULT_OK || intentData == null) return

        val pathList = mutableListOf<String>()
        val results = intentData.extras?.getStringArray(GligarPicker.IMAGES_RESULT)
        results?.forEach {
            pathList.add(it)
        }
        feedbackImageAdapter.addImages(pathList)
    }

    private fun sendFeedback() {
        val sendFeedbackView = findViewById<View>(R.id.sendFeedbackView)
        val contextView = findViewById<View>(R.id.content)

        binding.feedbackGroupView.visibility = View.GONE
        binding.feedbackProgressBar.visibility = View.VISIBLE

        setEnableSendFeedbackView(false)
        sendFeedbackView.hideKeyboard()

        val feedbackInput = binding.feedbackEditText.text.toString()
        feedbackViewModel.saveDataInFirestore(pathListArray, feedbackInput, contextView)

        feedbackViewModel.statusToSaveData.observe(
            this
        ) {
            if (it == "Success") {
                analytics.trackFeedbackSentEvent()
                val intent = Intent()
                setResult(ProfileFragment.RESULT_CODE, intent)
                finish()
            } else if (it == "Fail") {
                setEnableSendFeedbackView()
                binding.feedbackGroupView.visibility = View.VISIBLE
                binding.feedbackProgressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun View.hideKeyboard() = this.let {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    @SuppressLint("SetTextI18n")
    fun setTextFrom() {
        val fromWho = feedbackViewModel.from()
        binding.fromEmailTextView.text = "${getString(R.string.from)} $fromWho"
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            elevation = 0f
            title = getString(R.string.profile_send_feedback)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, FeedbackActivity::class.java)
            context.startActivity(intent)
        }
    }
}
