package com.forestry.counter.domain.location

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.*

enum class SensorCapability {
    NONE,    // Pas d'accéléromètre → saisie manuelle uniquement
    BASIC,   // Accéléromètre seul (précision ±2°)
    MEDIUM,  // Accéléromètre + gyroscope (±1°)
    HIGH     // Vecteur de rotation (fusion capteurs : ±0.5°)
}

data class AngleMeasurement(
    val pitchDeg: Float,
    val stabilityScore: Float   // 0.0 (instable) → 1.0 (stable)
)

data class TreeHeightResult(
    val heightM: Double,
    val precisionM: Double,
    val capability: SensorCapability
)

object TreeHeightMeasureTool {

    /** Détecte le meilleur capteur disponible sur l'appareil. */
    fun detectCapability(context: Context): SensorCapability {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        return when {
            sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null -> SensorCapability.HIGH
            sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null &&
                sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null -> SensorCapability.MEDIUM
            sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null -> SensorCapability.BASIC
            else -> SensorCapability.NONE
        }
    }

    /**
     * Flow de mesures d'angle en temps réel.
     * pitchDeg > 0 : téléphone incliné vers le haut (cime)
     * pitchDeg < 0 : téléphone incliné vers le bas (base)
     */
    fun pitchFlow(context: Context, capability: SensorCapability): Flow<AngleMeasurement> =
        callbackFlow {
            val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val buffer = ArrayDeque<Float>(10)

            fun computeStability(): Float {
                if (buffer.size < 4) return 0f
                val range = buffer.max() - buffer.min()
                return (1f - (range / 8f).coerceIn(0f, 1f))
            }

            when (capability) {
                SensorCapability.HIGH -> {
                    val sensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
                        ?: run { close(); return@callbackFlow }
                    val rotMatrix = FloatArray(9)
                    val orientation = FloatArray(3)
                    val listener = object : SensorEventListener {
                        override fun onAccuracyChanged(s: Sensor?, accuracy: Int) {}
                        override fun onSensorChanged(event: SensorEvent) {
                            SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                            SensorManager.getOrientation(rotMatrix, orientation)
                            // orientation[1] = pitch (inclinaison avant/arrière)
                            val pitchDeg = Math.toDegrees(orientation[1].toDouble()).toFloat()
                            buffer.addLast(pitchDeg)
                            if (buffer.size > 8) buffer.removeFirst()
                            trySend(AngleMeasurement(pitchDeg, computeStability()))
                        }
                    }
                    sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
                    awaitClose { sm.unregisterListener(listener) }
                }

                SensorCapability.MEDIUM, SensorCapability.BASIC -> {
                    val sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                        ?: run { close(); return@callbackFlow }
                    val listener = object : SensorEventListener {
                        override fun onAccuracyChanged(s: Sensor?, accuracy: Int) {}
                        override fun onSensorChanged(event: SensorEvent) {
                            val ax = event.values[0].toDouble()
                            val ay = event.values[1].toDouble()
                            val az = event.values[2].toDouble()
                            val pitchRad = atan2(-ay, sqrt(ax * ax + az * az))
                            val pitchDeg = Math.toDegrees(pitchRad).toFloat()
                            buffer.addLast(pitchDeg)
                            if (buffer.size > 8) buffer.removeFirst()
                            trySend(AngleMeasurement(pitchDeg, computeStability()))
                        }
                    }
                    sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
                    awaitClose { sm.unregisterListener(listener) }
                }

                SensorCapability.NONE -> close()
            }
        }

    /**
     * Calcule la hauteur d'un arbre par la méthode des tangentes.
     *
     * @param distanceM      Distance horizontale au pied de l'arbre (m)
     * @param angleTopDeg    Angle d'élévation vers la cime (degrés, positif = vers le haut)
     * @param angleBaseDeg   Angle vers la base (degrés, négatif = vers le bas, 0 = terrain plat)
     * @param phoneHeightM   Hauteur du téléphone par rapport au sol (m)
     * @param capability     Qualité du capteur (pour l'estimation de précision)
     *
     * Formule : H = hauteur_téléphone + D×tan(α_cime) − D×tan(α_base)
     * (en terrain plat ou légèrement incliné)
     */
    fun calculateHeight(
        distanceM: Double,
        angleTopDeg: Double,
        angleBaseDeg: Double = 0.0,
        phoneHeightM: Double = 1.0,
        capability: SensorCapability = SensorCapability.BASIC
    ): TreeHeightResult {
        val topRad  = Math.toRadians(angleTopDeg)
        val baseRad = Math.toRadians(angleBaseDeg)

        val heightAbovePhone = distanceM * tan(topRad)
        val heightBelowPhone = if (angleBaseDeg < -0.5) distanceM * abs(tan(baseRad)) else 0.0
        val total = (phoneHeightM + heightAbovePhone + heightBelowPhone).coerceAtLeast(0.5)

        // Estimation d'erreur propagée depuis l'erreur angulaire typique
        val angleErrorDeg = when (capability) {
            SensorCapability.HIGH   -> 0.5
            SensorCapability.MEDIUM -> 1.0
            SensorCapability.BASIC  -> 2.0
            SensorCapability.NONE   -> 3.0
        }
        val angleErrorRad = Math.toRadians(angleErrorDeg)
        val precisionM = distanceM * abs(tan(topRad + angleErrorRad) - tan(topRad)) + 0.1

        return TreeHeightResult(total, precisionM, capability)
    }
}
