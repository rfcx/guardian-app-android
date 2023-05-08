package org.rfcx.incidents.view.guardian.checklist.site



// class MapPickerFragment :
//     Fragment(),
//     OnMapReadyCallback,
//     SearchResultFragment.OnSearchResultListener {
//     private var mapboxMap: MapboxMap? = null
//     private lateinit var mapView: MapView
//     private var mapPickerProtocol: MapPickerProtocol? = null
//     private var editLocationActivityListener: EditLocationActivityListener? = null
//     private var locationEngine: LocationEngine? = null
//     private var currentUserLocation: Location? = null
//     private var selectedLocation: Location? = null
//     private var latitude: Double = 0.0
//     private var longitude: Double = 0.0
//     private var altitude: Double = 0.0
//     private var nameLocation: String? = null
//     private var siteId: Int? = null
//
//     private val analytics by lazy { context?.let { Analytics(it) } }
//
//     private lateinit var binding: FragmentGuardianSiteSetBinding
//     private val viewModel: GuardianSiteSetViewModel by viewModel()
//     private var mainEvent: GuardianDeploymentEventListener? = null
//
//     private val mapboxLocationChangeCallback =
//         object : LocationEngineCallback<LocationEngineResult> {
//             override fun onSuccess(result: LocationEngineResult?) {
//                 if (activity != null) {
//                     val location = result?.lastLocation
//                     location ?: return
//
//                     mapboxMap?.let {
//                         this@MapPickerFragment.currentUserLocation = location
//                     }
//
//                     showLoading(currentUserLocation == null)
//                 }
//             }
//
//             override fun onFailure(exception: Exception) {
//                 Log.e(TAG, exception.localizedMessage ?: "empty localizedMessage")
//             }
//         }
//
//     override fun onCreateView(
//         inflater: LayoutInflater,
//         container: ViewGroup?,
//         savedInstanceState: Bundle?
//     ): View {
//         mainEvent = context as GuardianDeploymentEventListener
//         binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map_picker, container, false)
//         binding.lifecycleOwner = this
//         return binding.root
//     }
//
//     override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)
//         context?.let { Mapbox.getInstance(it, getString(R.string.mapbox_token)) }
//         initIntent()
//     }
//
//     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//         super.onViewCreated(view, savedInstanceState)
//         mapView = view.findViewById(R.id.mapBoxPickerView)
//         mapView.onCreate(savedInstanceState)
//         mapView.getMapAsync(this)
//
//         showLoading(true)
//         setLatLogLabel(LatLng(0.0, 0.0))
//         moveCamera(LatLng(0.0, 0.0), DefaultSetupMap.DEFAULT_ZOOM)
//
//         selectButton.setOnClickListener {
//             val currentCameraPosition = mapboxMap?.cameraPosition?.target
//             currentCameraPosition?.let {
//                 analytics?.trackSelectLocationEvent()
//                 mapPickerProtocol?.onSelectedLocation(it.latitude, it.longitude, siteId ?: -1, nameLocation ?: "")
//             }
//         }
//
//         currentLocationButton.setOnClickListener {
//             selectedLocation = currentUserLocation
//             selectedLocation?.let {
//                 val latLng = LatLng(it.latitude, it.longitude)
//                 moveCamera(latLng, DefaultSetupMap.DEFAULT_ZOOM)
//                 setLatLogLabel(latLng)
//             }
//         }
//         setupSearch()
//     }
//
//     private fun initIntent() {
//         arguments?.let {
//             latitude = it.getDouble(ARG_LATITUDE)
//             longitude = it.getDouble(ARG_LONGITUDE)
//             altitude = it.getDouble(ARG_ALTITUDE)
//             nameLocation = it.getString(ARG_STREAM_NAME)
//             siteId = it.getInt(ARG_STREAM_ID)
//         }
//     }
//
//     private fun showLoading(isLoading: Boolean) {
//         fabProgress.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
//         currentLocationButton.isEnabled = !isLoading
//         currentLocationButton.supportImageTintList =
//             if (isLoading) resources.getColorStateList(R.color.gray_30) else resources.getColorStateList(
//                 R.color.colorPrimary
//             )
//     }
//
//     override fun onMapReady(mapboxMap: MapboxMap) {
//         this.mapboxMap = mapboxMap
//         mapboxMap.uiSettings.isAttributionEnabled = false
//         mapboxMap.uiSettings.isLogoEnabled = false
//         mapboxMap.setStyle(Style.OUTDOORS) {
//             enableLocationComponent()
//             setupScale()
//         }
//
//         mapboxMap.addOnCameraMoveListener {
//             val currentCameraPosition = mapboxMap.cameraPosition.target
//             val loc = Location(LocationManager.GPS_PROVIDER)
//             loc.latitude = currentCameraPosition.latitude
//             loc.longitude = currentCameraPosition.longitude
//             selectedLocation = loc
//             setLatLogLabel(LatLng(currentCameraPosition.latitude, currentCameraPosition.longitude))
//         }
//     }
//
//     private fun moveCamera(latLng: LatLng, zoom: Double) {
//         mapboxMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
//     }
//
//     @SuppressLint("MissingPermission")
//     private fun enableLocationComponent() {
//         if (hasPermissions()) {
//             val loadedMapStyle = mapboxMap?.style
//             val locationComponent = mapboxMap?.locationComponent
//             // Activate the LocationComponent
//             val customLocationComponentOptions = context?.let {
//                 LocationComponentOptions.builder(it)
//                     .trackingGesturesManagement(true)
//                     .accuracyColor(ContextCompat.getColor(it, R.color.colorPrimary))
//                     .build()
//             }
//
//             val locationComponentActivationOptions =
//                 context?.let {
//                     LocationComponentActivationOptions.builder(it, loadedMapStyle!!)
//                         .locationComponentOptions(customLocationComponentOptions)
//                         .build()
//                 }
//
//             mapboxMap?.let { it ->
//                 it.locationComponent.apply {
//                     if (locationComponentActivationOptions != null) {
//                         activateLocationComponent(locationComponentActivationOptions)
//                     }
//
//                     isLocationComponentEnabled = true
//                     renderMode = RenderMode.COMPASS
//                 }
//             }
//
//             if (latitude != 0.0 && longitude != 0.0) {
//                 moveCamera(LatLng(latitude, longitude), DefaultSetupMap.DEFAULT_ZOOM)
//                 setLatLogLabel(LatLng(latitude, longitude))
//             } else {
//                 val lastKnownLocation = locationComponent?.lastKnownLocation
//                 lastKnownLocation?.let {
//                     this.currentUserLocation = it
//                     moveCamera(LatLng(it.latitude, it.longitude), DefaultSetupMap.DEFAULT_ZOOM)
//                 }
//             }
//             initLocationEngine()
//         } else {
//             requestPermissions()
//         }
//     }
//
//     private fun setupScale() {
//         val scaleBarPlugin = ScaleBarPlugin(mapView, mapboxMap!!)
//         val options = ScaleBarOptions(requireContext())
//         options.setMarginTop(R.dimen.legend_top_margin)
//         scaleBarPlugin.create(options)
//     }
//
//     @SuppressLint("MissingPermission")
//     private fun initLocationEngine() {
//         locationEngine = context?.let { LocationEngineProvider.getBestLocationEngine(it) }
//         val request =
//             LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
//                 .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
//                 .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build()
//
//         locationEngine?.requestLocationUpdates(
//             request,
//             mapboxLocationChangeCallback,
//             Looper.getMainLooper()
//         )
//
//         locationEngine?.getLastLocation(mapboxLocationChangeCallback)
//     }
//
//     private fun hasPermissions(): Boolean {
//         val permissionState = context?.let {
//             ActivityCompat.checkSelfPermission(
//                 it,
//                 Manifest.permission.ACCESS_FINE_LOCATION
//             )
//         }
//         return permissionState == PackageManager.PERMISSION_GRANTED
//     }
//
//     private fun requestPermissions() {
//         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//             activity?.requestPermissions(
//                 arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                 REQUEST_PERMISSIONS_REQUEST_CODE
//             )
//         } else {
//             throw Exception("Request permissions not required before API 23 (should never happen)")
//         }
//     }
//
//     private fun setLatLogLabel(location: LatLng) {
//         context?.let {
//             val latLng =
//                 "${location.latitude.latitudeCoordinates(it)}, ${location.longitude.longitudeCoordinates(
//                     it
//                 )}"
//             locationTextView.text = latLng
//         }
//     }
//
//     override fun onRequestPermissionsResult(
//         requestCode: Int,
//         permissions: Array<String>,
//         grantResults: IntArray
//     ) {
//         if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
//             if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                 enableLocationComponent()
//             }
//         }
//     }
//
//     override fun onStart() {
//         super.onStart()
//         mapView.onStart()
//     }
//
//     override fun onResume() {
//         super.onResume()
//         mapView.onResume()
//         analytics?.trackScreen(Screen.MAP_PICKER)
//     }
//
//     override fun onPause() {
//         super.onPause()
//         mapView.onPause()
//     }
//
//     override fun onStop() {
//         super.onStop()
//         mapView.onStop()
//     }
//
//     override fun onLowMemory() {
//         super.onLowMemory()
//         mapView.onLowMemory()
//     }
//
//     override fun onDestroy() {
//         super.onDestroy()
//         mapView.onDestroy()
//     }
//
//     override fun onDestroyView() {
//         super.onDestroyView()
//         mapView.onDestroy()
//     }
//
//     private fun setupSearch() {
//         searchLayoutCardView.setOnClickListener {
//             searchLayoutSearchEditText.clearFocus()
//         }
//
//         searchLayoutSearchEditText.setOnFocusChangeListener { _, hasFocus ->
//             if (hasFocus) {
//                 searchLayout.setBackgroundResource(R.color.backgroundColor)
//                 searchViewActionLeftButton.visibility = View.VISIBLE
//                 editLocationActivityListener?.hideAppbar()
//                 showSearchFragment()
//             } else {
//                 searchLayout.setBackgroundResource(R.color.transparent)
//                 searchViewActionLeftButton.visibility = View.GONE
//                 searchViewActionRightButton.visibility = View.GONE
//                 editLocationActivityListener?.showAppbar()
//                 hideSearchFragment()
//             }
//         }
//
//         searchViewActionRightButton.setOnClickListener {
//             searchLayoutSearchEditText.text = null
//         }
//
//         childFragmentManager.addOnBackStackChangedListener {
//             if (childFragmentManager.backStackEntryCount == 0) {
//                 searchLayoutSearchEditText.clearFocus()
//             }
//         }
//
//         searchLayoutSearchEditText.addTextChangedListener(object : TextWatcher {
//             var timer: Timer = Timer()
//             override fun afterTextChanged(s: Editable?) {
//                 if (s.isNullOrEmpty()) {
//                     searchViewActionRightButton.visibility = View.GONE
//                 } else {
//                     searchViewActionRightButton.visibility = View.VISIBLE
//                 }
//                 timer.cancel()
//                 timer = Timer()
//                 timer.schedule(200L) {
//                     val searchFragment =
//                         childFragmentManager.findFragmentByTag(SearchResultFragment.tag) as SearchResultFragment?
//                     searchFragment?.search(if (s.isNullOrEmpty()) null else s.toString())
//                 }
//             }
//
//             override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                 // do nothing
//             }
//
//             override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                 // do nothing
//             }
//         })
//
//         searchLayoutSearchEditText.setOnEditorActionListener { _, actionId, _ ->
//             if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                 val imm: InputMethodManager? =
//                     context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
//                 imm?.hideSoftInputFromWindow(searchLayoutSearchEditText.windowToken, 0)
//                 return@setOnEditorActionListener true
//             }
//             false
//         }
//
//         searchViewActionLeftButton.setOnClickListener {
//             clearSearchInputAndHideSoftInput()
//         }
//     }
//
//     private fun showSearchFragment() {
//         childFragmentManager.beginTransaction().apply {
//             setCustomAnimations(R.anim.fragment_slide_in_up, 0, 0, R.anim.fragment_slide_out_up)
//         }.addToBackStack(SearchResultFragment.tag)
//             .replace(
//                 searchResultListContainer.id,
//                 SearchResultFragment.newInstance(searchLayoutSearchEditText.text?.toString()),
//                 SearchResultFragment.tag
//             ).commitAllowingStateLoss()
//     }
//
//     private fun hideSearchFragment() {
//         try {
//             if (childFragmentManager.backStackEntryCount > 0) {
//                 childFragmentManager.popBackStack()
//             }
//         } catch (e: Exception) {
//             e.printStackTrace()
//         }
//     }
//
//     private fun clearSearchInputAndHideSoftInput() {
//         val imm: InputMethodManager? =
//             context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
//         imm?.hideSoftInputFromWindow(searchLayoutSearchEditText.windowToken, 0)
//         searchLayoutSearchEditText.text = null
//         searchLayoutSearchEditText.clearFocus()
//     }
//
//     // callback from SearchResultFragment
//     override fun onLocationSelected(latLng: LatLng, placename: String) {
//         clearSearchInputAndHideSoftInput()
//         hideSearchFragment()
//         searchLayoutSearchEditText.setText(placename)
//         selectedLocation = Location("SEARCH").apply {
//             latitude = latLng.latitude
//             longitude = latLng.longitude
//         }
//         moveCamera(latLng, DefaultSetupMap.DEFAULT_ZOOM)
//         setLatLogLabel(latLng)
//     }
//
//     companion object {
//         private const val TAG = "MapPickerFragment"
//         private const val ARG_LATITUDE = "ARG_LATITUDE"
//         private const val ARG_LONGITUDE = "ARG_LONGITUDE"
//         private const val ARG_ALTITUDE = "ARG_ALTITUDE"
//         private const val ARG_STREAM_ID = "ARG_STREAM_ID"
//         private const val ARG_STREAM_NAME = "ARG_STREAM_NAME"
//
//         const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
//         const val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
//         const val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
//
//         @JvmStatic
//         fun newInstance(lat: Double, lng: Double, altitude: Double, id: Int, name: String) =
//             MapPickerFragment()
//                 .apply {
//                     arguments = Bundle().apply {
//                         putDouble(ARG_LATITUDE, lat)
//                         putDouble(ARG_LONGITUDE, lng)
//                         putDouble(ARG_ALTITUDE, altitude)
//                         putInt(ARG_STREAM_ID, id)
//                         putString(ARG_STREAM_NAME, name)
//                     }
//                 }
//
//         fun newInstance(lat: Double, lng: Double, altitude: Double, id: Int) =
//             MapPickerFragment()
//                 .apply {
//                     arguments = Bundle().apply {
//                         putDouble(ARG_LATITUDE, lat)
//                         putDouble(ARG_LONGITUDE, lng)
//                         putDouble(ARG_ALTITUDE, altitude)
//                         putInt(ARG_STREAM_ID, id)
//                     }
//                 }
//     }
// }
