package ru.dgis.sdk.demo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch
import ru.dgis.sdk.Context
import ru.dgis.sdk.DGis
import ru.dgis.sdk.demo.common.Coordinate
import ru.dgis.sdk.demo.common.MarkerSource
import ru.dgis.sdk.demo.common.updateMapCopyrightPosition
import ru.dgis.sdk.geometry.GeoPointWithElevation
import ru.dgis.sdk.map.Gesture
import ru.dgis.sdk.map.GestureManager
import ru.dgis.sdk.map.LogicalPixel
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.MapObjectManager
import ru.dgis.sdk.map.MapView
import ru.dgis.sdk.map.Marker
import ru.dgis.sdk.map.MarkerOptions
import ru.dgis.sdk.map.SimpleClusterObject
import ru.dgis.sdk.map.SimpleClusterOptions
import ru.dgis.sdk.map.SimpleClusterRenderer
import ru.dgis.sdk.map.ZIndex
import ru.dgis.sdk.map.Zoom
import ru.dgis.sdk.map.imageFromResource

class GenericMapActivity : AppCompatActivity() {
    private val sdkContext: Context by lazy { application.sdkContext }

    private val closeables = mutableListOf<AutoCloseable?>()

    private var map: Map? = null

    private lateinit var mapView: MapView
    private lateinit var root: View
    private lateinit var settingsDrawerInnerLayout: View

    private var userMarkerManager: MapObjectManager? = null
    private val mapUserMarker: MutableMap<Int, Marker> = mutableMapOf()
    private val markerSource: MarkerSource = MarkerSource(lifecycleScope)

    private val clusterRenderer = object : SimpleClusterRenderer {
        override fun renderCluster(cluster: SimpleClusterObject): SimpleClusterOptions {
            return SimpleClusterOptions(
                icon = imageFromResource(DGis.context(), R.drawable.ic_marker),
                zIndex = ZIndex(3),
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_map_generic)

        root = findViewById(R.id.content)
        settingsDrawerInnerLayout = findViewById(R.id.settingsDrawerInnerLayout)
        mapView = findViewById<MapView>(R.id.mapView).also {
            it.getMapAsync(this::onMapReady)
            it.showApiVersionInCopyrightView = true
        }

        BottomSheetBehavior.from(findViewById(R.id.settingsDrawerInnerLayout)).apply {
            state = BottomSheetBehavior.STATE_COLLAPSED
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {}
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    mapView.updateMapCopyrightPosition(root, settingsDrawerInnerLayout)
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeables.forEach { it?.close() }
    }

    private fun onMapReady(map: Map) {
        this.map = map
        closeables.add(map)

        val gestureManager = checkNotNull(mapView.gestureManager)
        subscribeGestureSwitches(gestureManager)

        closeables.add(
            map.camera.paddingChannel.connect { _ ->
                mapView.updateMapCopyrightPosition(root, settingsDrawerInnerLayout)
            }
        )
        userMarkerManager =
            MapObjectManager.withClustering(map, LogicalPixel(70F), Zoom(17f), clusterRenderer)

        lifecycleScope.launch {
            markerSource.coodinateFlow.collect { coordinates ->
                if (mapUserMarker.isEmpty()) {
                    userMarkerManager
                        ?.addObjects(
                            coordinates
                                .map { it.id to getMarkerOptions(it) }
                                .map {
                                    val marker = Marker(it.second)
                                    mapUserMarker[it.first] = marker
                                    marker
                                }
                        )
                } else {
                    coordinates.forEach {
                        val currentMarker = mapUserMarker[it.id] ?: return@forEach
                        currentMarker.position = GeoPointWithElevation(it.latitude, it.longitude)
                        mapUserMarker[it.id] = currentMarker
                    }
                }
            }
        }
    }

    private fun getMarkerOptions(point: Coordinate) =
        MarkerOptions(
            position = GeoPointWithElevation(point.latitude, point.longitude),
            icon = imageFromResource(sdkContext, R.drawable.ic_start),
            userData = point.id,
        )

    private fun subscribeGestureSwitches(gm: GestureManager) {
        val enabledGestures = gm.enabledGestures
        val options = listOf(
            Pair(R.id.rotationSwitch, Gesture.ROTATION),
            Pair(R.id.shiftSwitch, Gesture.SHIFT),
            Pair(R.id.scaleSwitch, Gesture.SCALING),
            Pair(R.id.tiltSwitch, Gesture.TILT)
        )

        options.forEach { (viewId, gesture) ->
            findViewById<SwitchCompat>(viewId).apply {
                isEnabled = true
                isChecked = enabledGestures.contains(gesture)

                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        gm.enableGesture(gesture)
                    } else {
                        gm.disableGesture(gesture)
                    }
                }
            }
        }
    }
}
