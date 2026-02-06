package com.forestry.counter.domain.repository

import com.forestry.counter.domain.model.Tige
import kotlinx.coroutines.flow.Flow

interface TigeRepository {
    fun getAllTiges(): Flow<List<Tige>>
    fun getTigesByParcelle(parcelleId: String): Flow<List<Tige>>
    fun getTigesByPlacette(placetteId: String): Flow<List<Tige>>
    fun getTigeById(tigeId: String): Flow<Tige?>
    suspend fun insertTige(tige: Tige)
    suspend fun insertTiges(tiges: List<Tige>)
    suspend fun updateTige(tige: Tige)
    suspend fun deleteTige(tigeId: String)
    suspend fun deleteTigesByParcelle(parcelleId: String)
    suspend fun deleteTigesByPlacette(placetteId: String)
    suspend fun deleteTigesByPlacetteAndEssence(placetteId: String, essenceCode: String)
    suspend fun deleteLatest(
        parcelleId: String,
        placetteId: String?,
        essenceCode: String,
        diamCm: Double
    ): Boolean
    suspend fun updateLatestGPS(
        parcelleId: String,
        placetteId: String?,
        essenceCode: String,
        diamCm: Double,
        wkt: String,
        precisionM: Double?,
        altitudeM: Double?
    ): Boolean
    suspend fun updateLatestHeight(
        parcelleId: String,
        placetteId: String?,
        essenceCode: String,
        diamCm: Double,
        heightM: Double?
    ): Boolean
    suspend fun setTigeHeight(tigeId: String, heightM: Double?): Boolean

    suspend fun setTigeGps(
        tigeId: String,
        wkt: String,
        precisionM: Double?,
        altitudeM: Double?
    ): Boolean

    suspend fun updateLatestCategory(
        parcelleId: String,
        placetteId: String?,
        essenceCode: String,
        diamCm: Double,
        category: String?
    ): Boolean

    suspend fun updateLatestQuality(
        parcelleId: String,
        placetteId: String?,
        essenceCode: String,
        diamCm: Double,
        quality: Int?
    ): Boolean

    suspend fun updateLatestDefects(
        parcelleId: String,
        placetteId: String?,
        essenceCode: String,
        diamCm: Double,
        defects: List<String>?
    ): Boolean

    suspend fun updateLatestNumero(
        parcelleId: String,
        placetteId: String?,
        essenceCode: String,
        diamCm: Double,
        numero: Int?
    ): Boolean

    suspend fun updateLatestPhoto(
        parcelleId: String,
        placetteId: String?,
        essenceCode: String,
        diamCm: Double,
        photoUri: String?
    ): Boolean
}
