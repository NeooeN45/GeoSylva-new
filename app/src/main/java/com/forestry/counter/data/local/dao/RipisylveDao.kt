package com.forestry.counter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.forestry.counter.data.local.entity.RipisylveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RipisylveDao {
    @Query("SELECT * FROM ripisylve_observation WHERE parcelleId = :parcelleId AND deletedAt IS NULL ORDER BY observationDate DESC")
    fun getByParcelle(parcelleId: String): Flow<List<RipisylveEntity>>

    @Query("SELECT * FROM ripisylve_observation WHERE deletedAt IS NULL ORDER BY observationDate DESC")
    fun getAll(): Flow<List<RipisylveEntity>>

    @Query("SELECT * FROM ripisylve_observation WHERE id = :id AND deletedAt IS NULL LIMIT 1")
    suspend fun getById(id: String): RipisylveEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RipisylveEntity)

    // --- Soft delete (Vague C) : suppression logique via deletedAt ---

    /** Soft delete d'une observation ripisylve par son identifiant. */
    @Query("UPDATE ripisylve_observation SET deletedAt = :timestamp WHERE id = :id")
    suspend fun deleteById(id: String, timestamp: Long)

    /** Soft delete massif des observations ripisylve non encore supprimées. */
    @Query("UPDATE ripisylve_observation SET deletedAt = :timestamp WHERE deletedAt IS NULL")
    suspend fun deleteAll(timestamp: Long)

    /**
     * Suppression physique de toutes les observations ripisylve (droit à l'effacement RGPD).
     * À n'utiliser que depuis [DeleteAllUserDataUseCase].
     */
    @Query("DELETE FROM ripisylve_observation")
    suspend fun hardDeleteAll()
}
