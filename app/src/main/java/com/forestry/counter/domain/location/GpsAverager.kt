package com.forestry.counter.domain.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale
import kotlin.math.sqrt

/**
 * Résultat d'un moyennage GPS multi-lectures.
 */
data class AveragedGpsResult(
    val longitude: Double,
    val latitude: Double,
    val altitude: Double?,
    val precisionM: Double,
    val readingCount: Int,
    val wkt: String
) {
    val qualityLabel: GpsQuality
        get() = when {
            precisionM <= 3.0 -> GpsQuality.EXCELLENT
            precisionM <= 6.0 -> GpsQuality.GOOD
            precisionM <= 12.0 -> GpsQuality.MODERATE
            else -> GpsQuality.POOR
        }
}

enum class GpsQuality { EXCELLENT, GOOD, MODERATE, POOR }

/**
 * Données intermédiaires émises pendant le moyennage GPS.
 */
data class GpsReadingProgress(
    val currentReading: Int,
    val targetReadings: Int,
    val currentPrecisionM: Double?,
    val bestPrecisionM: Double?,
    val currentLon: Double?,
    val currentLat: Double?
)

/**
 * Utilitaire de moyennage GPS pour améliorer la précision de positionnement des tiges.
 *
 * Collecte N lectures GPS haute précision et retourne la position moyennée
 * (moyenne pondérée par l'inverse de la précision pour favoriser les meilleures lectures).
 */
object GpsAverager {

    /**
     * Lance un moyennage GPS de [targetReadings] lectures.
     * Émet des [GpsReadingProgress] au fur et à mesure puis retourne le résultat final.
     *
     * @param context Application context
     * @param targetReadings Nombre de lectures à collecter (3–10 recommandé)
     * @param maxAccuracyM Précision max acceptée par lecture (filtre les lectures trop imprécises)
     * @param intervalMs Intervalle entre lectures
     */
    @SuppressLint("MissingPermission")
    fun averageFlow(
        context: Context,
        targetReadings: Int = 5,
        maxAccuracyM: Float = 25f,
        intervalMs: Long = 800L
    ): Flow<GpsReadingProgress> = callbackFlow {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val readings = mutableListOf<Location>()

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .setMaxUpdateDelayMillis(0L)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (loc in result.locations) {
                    if (loc.accuracy > 0f && loc.accuracy <= maxAccuracyM) {
                        readings.add(loc)
                        val progress = GpsReadingProgress(
                            currentReading = readings.size,
                            targetReadings = targetReadings,
                            currentPrecisionM = loc.accuracy.toDouble(),
                            bestPrecisionM = readings.minOfOrNull { it.accuracy.toDouble() },
                            currentLon = loc.longitude,
                            currentLat = loc.latitude
                        )
                        trySend(progress)

                        if (readings.size >= targetReadings) {
                            client.removeLocationUpdates(this)
                            channel.close()
                            return
                        }
                    }
                }
            }
        }

        client.requestLocationUpdates(request, callback, android.os.Looper.getMainLooper())

        awaitClose {
            client.removeLocationUpdates(callback)
        }
    }

    /**
     * Calcule la position moyennée pondérée à partir d'une liste de Location.
     * Pondération : inverse du carré de la précision (plus la lecture est précise, plus elle pèse).
     */
    fun computeAverage(locations: List<Location>): AveragedGpsResult? {
        if (locations.isEmpty()) return null

        var weightSum = 0.0
        var lonSum = 0.0
        var latSum = 0.0
        var altSum = 0.0
        var altCount = 0
        var accSquaredSum = 0.0

        for (loc in locations) {
            val w = if (loc.accuracy > 0f) 1.0 / (loc.accuracy * loc.accuracy) else 1.0
            weightSum += w
            lonSum += loc.longitude * w
            latSum += loc.latitude * w
            if (loc.hasAltitude()) {
                altSum += loc.altitude * w
                altCount++
            }
            accSquaredSum += loc.accuracy * loc.accuracy
        }

        if (weightSum <= 0.0) return null

        val lon = lonSum / weightSum
        val lat = latSum / weightSum
        val alt = if (altCount > 0) altSum / weightSum else null

        // Précision combinée : sqrt(somme des carrés) / N (propagation des erreurs)
        val combinedPrecision = sqrt(accSquaredSum) / locations.size

        val wkt = if (alt != null) {
            String.format(Locale.US, "POINT Z (%.7f %.7f %.1f)", lon, lat, alt)
        } else {
            String.format(Locale.US, "POINT (%.7f %.7f)", lon, lat)
        }

        return AveragedGpsResult(
            longitude = lon,
            latitude = lat,
            altitude = alt,
            precisionM = combinedPrecision,
            readingCount = locations.size,
            wkt = wkt
        )
    }

    /**
     * Méthode simplifiée : collecte N lectures et retourne la position moyennée.
     * Pour usage avec coroutines dans un scope.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    suspend fun collectAndAverage(
        context: Context,
        targetReadings: Int = 5,
        maxAccuracyM: Float = 25f,
        timeoutMs: Long = 15_000L
    ): AveragedGpsResult? {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val readings = mutableListOf<Location>()

        return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 800L)
                .setMinUpdateIntervalMillis(400L)
                .setMaxUpdateDelayMillis(0L)
                .build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    for (loc in result.locations) {
                        if (loc.accuracy > 0f && loc.accuracy <= maxAccuracyM) {
                            readings.add(loc)
                            if (readings.size >= targetReadings) {
                                client.removeLocationUpdates(this)
                                if (!cont.isCompleted) {
                                    cont.resume(computeAverage(readings)) {}
                                }
                                return
                            }
                        }
                    }
                }
            }

            cont.invokeOnCancellation {
                client.removeLocationUpdates(callback)
            }

            client.requestLocationUpdates(request, callback, android.os.Looper.getMainLooper())

            // Timeout
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                client.removeLocationUpdates(callback)
                if (!cont.isCompleted) {
                    cont.resume(if (readings.isNotEmpty()) computeAverage(readings) else null) {}
                }
            }, timeoutMs)
        }
    }
}
