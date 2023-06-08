package org.rfcx.incidents.view.report.deployment.detail.edit

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentEditLocationBinding
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.view.guardian.checklist.site.MapPickerFragment

class EditDeploymentSiteFragment : Fragment() {
    private lateinit var binding: FragmentEditLocationBinding
    private val viewModel: EditSiteViewModel by viewModel()

    private var listener: EditDeploymentSiteListener? = null

    private lateinit var site: Stream
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        listener = context as EditDeploymentSiteListener
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_location, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initIntent()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        setMap(savedInstanceState)

        view.viewTreeObserver.addOnGlobalLayoutListener { setOnFocusEditText() }
        setHideKeyboard()

        binding.changeButton.setOnClickListener {
            site.let {
                listener?.startMapPickerPage(it)
            }
        }

        lifecycleScope.launch {
            viewModel.stream.collectLatest {
                if (it != null) {
                    site = it
                    if (latitude != 0.0 && longitude != 0.0) {
                        site.latitude = latitude
                        site.longitude = longitude
                    }
                    val siteLoc = LatLng(site.latitude, site.longitude)
                    binding.mapBoxView.setSiteLocation(siteLoc)
                }
            }
        }

        binding.saveButton.setOnClickListener {
            if (binding.siteNameEditText.text.isNullOrBlank()) {
                Toast.makeText(
                    context,
                    "Please fill the name",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                //view model update site
            }
        }
    }

    private fun setMap(savedInstanceState: Bundle?) {
        binding.mapBoxView.onCreate(savedInstanceState)
        binding.mapBoxView.setParam(canMove = false, fromDeploymentList = false)
    }

    private fun setOnFocusEditText() {
        val screenHeight: Int = view?.rootView?.height ?: 0
        val r = Rect()
        view?.getWindowVisibleDisplayFrame(r)
        val keypadHeight: Int = screenHeight - r.bottom
        if (keypadHeight > screenHeight * 0.15) {
            binding.saveButton.visibility = View.GONE
        } else {
            if (binding.saveButton != null) {
                binding.saveButton.visibility = View.VISIBLE
            }
        }
    }

    private fun setHideKeyboard() {
        val editorActionListener =
            TextView.OnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    v.clearFocus()
                    v.hideKeyboard()
                }
                false
            }
        binding.siteNameEditText.setOnEditorActionListener(editorActionListener)
    }

    private fun initIntent() {
        arguments?.let {
            viewModel.getStreamById(it.getInt(ARG_STREAM_ID))
            latitude = it.getDouble(ARG_LATITUDE)
            longitude = it.getDouble(ARG_LONGITUDE)
            viewModel.pickLatitudeLongitude(
                latitude,
                longitude
            )
        }
    }

    private fun View.hideKeyboard() = this.let {
        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(windowToken, 0)
    }

    override fun onStart() {
        super.onStart()
        binding.mapBoxView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapBoxView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapBoxView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapBoxView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapBoxView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapBoxView.onDestroy()
    }

    companion object {
        private const val ARG_STREAM_ID = "ARG_STREAM_ID"
        private const val ARG_LATITUDE = "ARG_LATITUDE"
        private const val ARG_LONGITUDE = "ARG_LONGITUDE"

        @JvmStatic
        fun newInstance(id: Int) =
            EditDeploymentSiteFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_STREAM_ID, id)
                }
            }

        @JvmStatic
        fun newInstance(id: Int, latitude: Double, longitude: Double) =
            EditDeploymentSiteFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_STREAM_ID, id)
                    putDouble(ARG_LATITUDE, latitude)
                    putDouble(ARG_LONGITUDE, longitude)
                }
            }
    }
}
