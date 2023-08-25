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
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentEditLocationBinding
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.view.base.BaseMapFragment

class EditDeploymentSiteFragment : BaseMapFragment() {
    private lateinit var binding: FragmentEditLocationBinding
    private val viewModel: EditSiteViewModel by viewModel()

    private var listener: EditDeploymentSiteListener? = null

    private lateinit var site: Stream
    private var siteLatitude: Double = 0.0
    private var siteLongitude: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        listener = context as EditDeploymentSiteListener
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_location, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        initIntent()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapView!!.getMapAsync(this)

        binding.viewModel = viewModel

        view.viewTreeObserver.addOnGlobalLayoutListener { setOnFocusEditText() }
        setHideKeyboard()

        binding.changeButton.setOnClickListener {
            site.let {
                it.name = binding.siteNameEditText.text.toString()
                listener?.startMapPickerPage(it)
            }
        }

        lifecycleScope.launch {
            viewModel.stream.collectLatest {
                if (it != null) {
                    site = it
                    if (siteLatitude != 0.0 && siteLongitude != 0.0) {
                        site.latitude = siteLatitude
                        site.longitude = siteLongitude
                    }
                    val siteLoc = LatLng(site.latitude, site.longitude)
                    setSiteLocation(siteLoc)
                    addMarker(siteLoc)
                }
            }
        }

        binding.saveButton.setOnClickListener {
            if (binding.siteNameEditText.text.isNullOrBlank()) {
                Toast.makeText(
                    context,
                    getString(R.string.fill_name),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                site.apply {
                    name = binding.siteNameEditText.text.toString()
                    this.latitude = siteLatitude
                    this.longitude = siteLongitude
                    altitude = binding.altitudeEditText.text.toString().toDouble()
                }
                viewModel.updateDeploymentSite(site)
                listener?.finishEdit()
            }
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        setGoogleMap(p0, false)
    }

    private fun setOnFocusEditText() {
        val screenHeight: Int = view?.rootView?.height ?: 0
        val r = Rect()
        view?.getWindowVisibleDisplayFrame(r)
        val keypadHeight: Int = screenHeight - r.bottom
        if (keypadHeight > screenHeight * 0.15) {
            binding.saveButton.visibility = View.GONE
        } else {
            binding.saveButton.visibility = View.VISIBLE
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
            siteLatitude = it.getDouble(ARG_LATITUDE)
            siteLongitude = it.getDouble(ARG_LONGITUDE)
            viewModel.fromMapPickerData(
                it.getString(ARG_NAME) ?: getString(R.string.common_none),
                siteLatitude,
                siteLongitude
            )
        }
    }

    private fun View.hideKeyboard() = this.let {
        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(windowToken, 0)
    }

    companion object {
        private const val ARG_STREAM_ID = "ARG_STREAM_ID"
        private const val ARG_NAME = "ARG_NAME"
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
        fun newInstance(id: Int, name: String, latitude: Double, longitude: Double) =
            EditDeploymentSiteFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_STREAM_ID, id)
                    putString(ARG_NAME, name)
                    putDouble(ARG_LATITUDE, latitude)
                    putDouble(ARG_LONGITUDE, longitude)
                }
            }
    }
}
