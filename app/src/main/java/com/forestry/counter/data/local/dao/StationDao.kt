package com.forestry.counter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.forestry.counter.data.local.entity.StationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StationDao {
    @Query("SELECT * FROM station_diagnostics WHERE parcelleId = :parcelleId AND deletedAt IS NULL ORDER BY observationDate DESC")
    fun getByParcelle(parcelleId: String): Flow<List<StationEntity>>

    @Query("SELECT * FROM station_diagnostics WHERE id = :id AND deletedAt IS NULL")
    fun getById(id: String): Flow<StationEntity?>

    @Query("SELECT * FROM station_diagnostics WHERE deletedAt IS NULL ORDER BY observationDate DESC")
    fun getAll(): Flow<List<StationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: StationEntity)

    // --- Soft delete (Vague C) : suppression logique via deletedAt ---

    /** Soft delete d'une observation station par son identifiant. */
    @Query("UPDATE station_diagnostics SET deletedAt = :timestamp WHERE id = :id")
    suspend fun deleteById(id: String, timestamp: Long)

    /** Soft delete massif des observations station non encore supprimées. */
    @Query("UPDATE station_diagnostics SET deletedAt = :timestamp WHERE deletedAt IS NULL")
    suspend fun deleteAll(timestamp: Long)

    /**
     * Suppression physique de toutes les observations station (droit à l'effacement RGPD).
     * À n'utiliser que depuis [DeleteAllUserDataUseCase].
     */
    @Query("DELETE FROM station_diagnostics")
    suspend fun hardDeleteAll()
}
