package ru.dgis.sdk.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.dgis.sdk.coordinates.Bearing
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.coordinates.GeoRect
import ru.dgis.sdk.demo.databinding.ActivitySearchBinding
import ru.dgis.sdk.map.CameraPosition
import ru.dgis.sdk.map.MapObjectManager
import ru.dgis.sdk.map.Tilt
import ru.dgis.sdk.map.Zoom

// Spatial restriction for search in this activity.
private val dubaiGeoRect = GeoRect(GeoPoint(25.140595, 55.240626), GeoPoint(25.226267, 55.318421))

/**
 * Showcase for search UI control.
 *
 * Demonstration: open activity, tap the search bar at the top of screen, type "cafe" or any other search query.
 * See documentation on Search UI: https://docs.2gis.com/en/android/sdk/examples/directory#nav-lvl1--Search_UI
 */
class SearchActivity : AppCompatActivity() {
    private val binding by lazy { ActivitySearchBinding.inflate(layoutInflater) }

    private var mapObjectManager: MapObjectManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.mapView.getMapAsync { map ->
            mapObjectManager = MapObjectManager(map)
            map.camera.position = CameraPosition(
                point = GeoPoint(latitude = 25.200194699171405, longitude = 55.27539446018636),
                zoom = Zoom(16.856537f),
                tilt = Tilt(50.0f),
                bearing = Bearing(19.00000166708803)
            )
        }
    }
}
