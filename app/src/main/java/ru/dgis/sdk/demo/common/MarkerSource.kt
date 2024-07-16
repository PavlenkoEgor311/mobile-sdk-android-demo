package ru.dgis.sdk.demo.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

data class Coordinate(
    val id: Int,
    var latitude: Double,
    var longitude: Double,
)

class MarkerSource(coroutineScope: CoroutineScope) {
    private val coordinates = mutableListOf<Coordinate>()
    private val _coodinateFlow = MutableSharedFlow<List<Coordinate>>()
    val coodinateFlow = _coodinateFlow.asSharedFlow()
    private val random = Random(System.currentTimeMillis())

    init {
        for (i in 0 until 15) {
            coordinates.add(Coordinate(id = i, randomLatitude(), randomLongitude()))
        }
        coroutineScope.launch {
            while (isActive) {
                updateCoordinates()
                _coodinateFlow.emit(coordinates)
                delay(5000)
            }
        }
    }

    private fun randomLatitude() =
        random.nextDouble(59.8, 60.1)

    private fun randomLongitude() =
        random.nextDouble(30.1, 30.7)

    private fun updateCoordinates() {
        coordinates.forEach { coordinate ->
            coordinate.latitude = (coordinate.latitude + random.nextDouble(-0.0075, 0.0075)).coerceIn(59.8, 60.1)
            coordinate.longitude = (coordinate.longitude + random.nextDouble(-0.0075, 0.0075)).coerceIn(30.1, 30.7)
        }
    }
}
