package org.rfcx.incidents.view.report.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.databinding.ActivityResponseDetailBinding
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Screen

class ResponseDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_RESPONSE_CORE_ID = "EXTRA_RESPONSE_CORE_ID"
        private const val SOURCE_LINE = "source.line"
        private const val SOURCE_CHECK_IN = "source.checkin"
        private const val MARKER_CHECK_IN_ID = "marker.checkin"
        private const val MARKER_CHECK_IN_IMAGE = "marker.checkin.pin"

        fun startActivity(context: Context, responseCoreId: String) {
            val intent = Intent(context, ResponseDetailActivity::class.java)
            intent.putExtra(EXTRA_RESPONSE_CORE_ID, responseCoreId)
            context.startActivity(intent)
        }
    }

    lateinit var binding: ActivityResponseDetailBinding

    private val analytics by lazy { Analytics(this) }
    private val viewModel: ResponseDetailViewModel by viewModel()

    private var responseCoreId: String? = null
    private var response: Response? = null

    // private lateinit var mapView: MapView
    // private lateinit var mapBoxMap: MapboxMap
    // private var lineSource: GeoJsonSource? = null
    // private var checkInSource: GeoJsonSource? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResponseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        responseCoreId = intent?.getStringExtra(EXTRA_RESPONSE_CORE_ID)
        response = responseCoreId?.let { viewModel.getResponseByCoreId(it) }
        setupToolbar()
        startFragment(ResponseDetailFragment.newInstance(responseCoreId ?: ""))

        // Setup Mapbox
        // mapView = findViewById(R.id.mapBoxView)
        // mapView.onCreate(savedInstanceState)
        // mapView.getMapAsync(this)
    }

    private fun startFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.responseDetailContainer.id, fragment)
            .commit()
    }

    // override fun onMapReady(mapboxMap: MapboxMap) {
    //     mapBoxMap = mapboxMap
    //     mapboxMap.uiSettings.apply {
    //         setAllGesturesEnabled(false)
    //         isAttributionEnabled = false
    //         isLogoEnabled = false
    //     }
    //
    //     mapboxMap.setStyle(Style.OUTDOORS) { style ->
    //         setupSources(style)
    //
    //         response?.let { res ->
    //             val track = res.trackingAssets.firstOrNull()
    //             if (track != null) {
    //                 val tempTrack = arrayListOf<Feature>()
    //                 val json = File(track.localPath).readText()
    //                 val featureCollection = FeatureCollection.fromJson(json)
    //                 val feature = featureCollection.features()?.get(0)
    //                 feature?.let {
    //                     tempTrack.add(it)
    //                 }
    //                 addLineLayer(style)
    //                 lineSource?.setGeoJson(FeatureCollection.fromFeatures(tempTrack))
    //
    //                 val lastLocation = feature?.geometry() as LineString
    //                 val pointFeatures = lastLocation.coordinates().map {
    //                     Feature.fromGeometry(Point.fromLngLat(it.longitude(), it.latitude()))
    //                 }
    //                 checkInSource?.setGeoJson(FeatureCollection.fromFeatures(pointFeatures))
    //                 moveCameraToLeavesBounds(lastLocation.coordinates())
    //             } else {
    //                 binding.mapBoxCardView.visibility = View.GONE
    //             }
    //         }
    //     }
    // }

    // private fun moveCameraToLeavesBounds(features: List<Point>) {
    //     val latLngList: ArrayList<LatLng> = ArrayList()
    //     for (singleClusterFeature in features) {
    //         latLngList.add(LatLng(singleClusterFeature.latitude(), singleClusterFeature.longitude()))
    //     }
    //     if (latLngList.size > 1) {
    //         moveCameraWithLatLngList(latLngList)
    //     } else {
    //         moveCamera(latLngList[0])
    //     }
    // }

    // private fun moveCameraWithLatLngList(latLngList: List<LatLng>) {
    //     val latLngBounds = LatLngBounds.Builder()
    //         .includes(latLngList)
    //         .build()
    //     mapBoxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 230), 1300)
    // }

    // private fun moveCamera(loc: LatLng) {
    //     mapBoxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 10.0))
    // }

    // private fun setupSources(style: Style) {
    //     lineSource = GeoJsonSource(SOURCE_LINE)
    //     lineSource?.let { style.addSource(it) }
    //
    //     checkInSource = GeoJsonSource(SOURCE_CHECK_IN)
    //     checkInSource?.let { style.addSource(it) }
    // }

    // private fun addLineLayer(style: Style) {
    //     val lineLayer = LineLayer("line-layer", SOURCE_LINE).withProperties(
    //         PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
    //         PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
    //         PropertyFactory.lineWidth(5f),
    //         PropertyFactory.lineColor(Expression.get("color"))
    //     )
    //     style.addLayer(lineLayer)
    //
    //     val drawable = ResourcesCompat.getDrawable(resources, R.drawable.bg_circle_tracking, null)
    //     val mBitmap = BitmapUtils.getBitmapFromDrawable(drawable)
    //     mBitmap?.let { style.addImage(MARKER_CHECK_IN_IMAGE, it) }
    //
    //     val checkInLayer = SymbolLayer(MARKER_CHECK_IN_ID, SOURCE_CHECK_IN).apply {
    //         withProperties(
    //             PropertyFactory.iconImage(MARKER_CHECK_IN_IMAGE),
    //             PropertyFactory.iconAllowOverlap(true),
    //             PropertyFactory.iconIgnorePlacement(true),
    //             PropertyFactory.iconSize(1f)
    //         )
    //     }
    //     style.addLayer(checkInLayer)
    // }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            elevation = 0f
            title = "#" + response?.incidentRef + " " + response?.streamName
        }
    }

    override fun onResume() {
        super.onResume()
        analytics.trackScreen(Screen.RESPONSE_DETAIL)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
