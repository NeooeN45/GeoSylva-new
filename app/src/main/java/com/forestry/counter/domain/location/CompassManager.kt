package com.forestry.counter.domain.location

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.roundToInt

data class CompassReading(
    val bearingDeg: Float,       // 0–360, Nord = 0
    val cardinalPoint: String,   // "N", "NNE", "NE", …
    val stabilityScore: Float    // 0.0 (instable) → 1.0 (stable)
)

object CompassManager {

    fun isAvailable(context: Context): Boolean {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        return sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null ||
            sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null
    }

    /**
     * Flow d'azimut en temps réel.
     * Utilise le vecteur de rotation (fusion capteurs) si disponible, sinon magnétomètre.
     */
    fun bearingFlow(context: Context): Flow<CompassReading> = callbackFlow {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val buffer = ArrayDeque<Float>(10)

        fun stabilityScore(): Float {
            if (buffer.size < 4) return 0f
            // Gestion du wrap-around 360°→0°
            val sorted = buffer.sorted()
            val range1 = sorted.last() - sorted.first()
            val range2 = (sorted.first() + 360f) - sorted.last()
            val range = minOf(range1, range2)
            return (1f - (range / 15f).coerceIn(0f, 1f))
        }

        fun avgBearing(values: Collection<Float>): Float {
            var sinSum = 0.0
            var cosSum = 0.0
            values.forEach { deg ->
                val rad = Math.toRadians(deg.toDouble())
                sinSum += Math.sin(rad)
                cosSum += Math.cos(rad)
            }
            val avg = Math.toDegrees(Math.atan2(sinSum / values.size, cosSum / values.size))
            return ((avg + 360) % 360).toFloat()
        }

        val rotVec = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val magSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val accSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (rotVec != null) {
            val rotMatrix = FloatArray(9)
            val orientation = FloatArray(3)
            val listener = object : SensorEventListener {
                override fun onAccuracyChanged(s: Sensor?, accuracy: Int) {}
                override fun onSensorChanged(event: SensorEvent) {
                    SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                    SensorManager.getOrientation(rotMatrix, orientation)
                    val bearing = ((Math.toDegrees(orientation[0].toDouble()) + 360) % 360).toFloat()
                    buffer.addLast(bearing)
                    if (buffer.size > 8) buffer.removeFirst()
                    trySend(CompassReading(avgBearing(buffer), toCardinal(avgBearing(buffer)), stabilityScore()))
                }
            }
            sm.registerListener(listener, rotVec, SensorManager.SENSOR_DELAY_UI)
            awaitClose { sm.unregisterListener(listener) }

        } else if (magSensor != null && accSensor != null) {
            val gravity   = FloatArray(3)
            val magnetic  = FloatArray(3)
            val rotMatrix = FloatArray(9)
            val orientation = FloatArray(3)

            val listener = object : SensorEventListener {
                override fun onAccuracyChanged(s: Sensor?, accuracy: Int) {}
                override fun onSensorChanged(event: SensorEvent) {
                    when (event.sensor.type) {
                        Sensor.TYPE_ACCELEROMETER -> event.values.copyInto(gravity)
                        Sensor.TYPE_MAGNETIC_FIELD -> event.values.copyInto(magnetic)
                    }
                    if (SensorManager.getRotationMatrix(rotMatrix, null, gravity, magnetic)) {
                        SensorManager.getOrientation(rotMatrix, orientation)
                        val bearing = ((Math.toDegrees(orientation[0].toDouble()) + 360) % 360).toFloat()
                        buffer.addLast(bearing)
                        if (buffer.size > 8) buffer.removeFirst()
                        trySend(CompassReading(avgBearing(buffer), toCardinal(avgBearing(buffer)), stabilityScore()))
                    }
                }
            }
            sm.registerListener(listener, magSensor, SensorManager.SENSOR_DELAY_UI)
            sm.registerListener(listener, accSensor, SensorManager.SENSOR_DELAY_UI)
            awaitClose { sm.unregisterListener(listener) }

        } else {
            close()
        }
    }

    fun toCardinal(deg: Float): String {
        val n = ((deg % 360) + 360) % 360
        return when (((n / 22.5f) + 0.5f).toInt() % 16) {
            0  -> "N";  1 -> "NNE"; 2 -> "NE"; 3 -> "ENE"
            4  -> "E";  5 -> "ESE"; 6 -> "SE"; 7 -> "SSE"
            8  -> "S";  9 -> "SSO"; 10 -> "SO"; 11 -> "OSO"
            12 -> "O"; 13 -> "ONO"; 14 -> "NO"; 15 -> "NNO"
            else -> "N"
        }
    }
}
