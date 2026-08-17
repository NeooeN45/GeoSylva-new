package com.forestry.counter.data.local.dao

import androidx.room.*
import com.forestry.counter.data.local.entity.PlacetteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlacetteDao {
    @Query("SELECT * FROM placettes WHERE parcelleOwnerId = :parcelleId AND deletedAt IS NULL ORDER BY placetteId ASC")
    fun getPlacettesByParcelle(parcelleId: String): Flow<List<PlacetteEntity>>

    @Query("SELECT * FROM placettes WHERE deletedAt IS NULL ORDER BY placetteId ASC")
    suspend fun getAllPlacettesNow(): List<PlacetteEntity>

    @Query("SELECT * FROM placettes WHERE placetteId = :id AND deletedAt IS NULL")
    fun getPlacetteByIdFlow(id: String): Flow<PlacetteEntity?>

    @Query("SELECT * FROM placettes WHERE placetteId = :id AND deletedAt IS NULL")
    suspend fun getPlacetteById(id: String): PlacetteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlacette(entity: PlacetteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlacettes(entities: List<PlacetteEntity>)

    @Update
    suspend fun updatePlacette(entity: PlacetteEntity)

    // --- Soft delete (Vague C) : suppression logique via deletedAt ---

    /** Soft delete d'une placette par son identifiant. */
    @Query("UPDATE placettes SET deletedAt = :timestamp WHERE placetteId = :id")
    suspend fun deletePlacette(id: String, timestamp: Long)

    /** Soft delete d'une placette par son identifiant (alias sémantique). */
    @Query("UPDATE placettes SET deletedAt = :timestamp WHERE placetteId = :id")
    suspend fun deletePlacetteById(id: String, timestamp: Long)

    /** Soft delete massif des placettes rattachées à une parcelle. */
    @Query("UPDATE placettes SET deletedAt = :timestamp WHERE parcelleOwnerId = :parcelleId AND deletedAt IS NULL")
    suspend fun deletePlacettesByParcelle(parcelleId: String, timestamp: Long)

    /** Soft delete massif des placettes non encore supprimées. */
    @Query("UPDATE placettes SET deletedAt = :timestamp WHERE deletedAt IS NULL")
    suspend fun deleteAll(timestamp: Long)

    /**
     * Suppression physique de toutes les placettes (droit à l'effacement RGPD).
     * À n'utiliser que depuis [DeleteAllUserDataUseCase].
     */
    @Query("DELETE FROM placettes")
    suspend fun hardDeleteAll()

    // --- Backfill UUID (Lot 1) ---

    @Query("SELECT * FROM placettes WHERE uuid IS NULL AND deletedAt IS NULL")
    suspend fun getWithoutUuid(): List<PlacetteEntity>

    @Query("UPDATE placettes SET uuid = :uuid WHERE placetteId = :id")
    suspend fun setUuid(id: String, uuid: String)
}
