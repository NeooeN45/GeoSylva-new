package com.forestry.counter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.forestry.counter.data.local.entity.ObservationFloreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservationFloreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(observation: ObservationFloreEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(observations: List<ObservationFloreEntity>)

    @Update
    suspend fun update(observation: ObservationFloreEntity)

    // --- Soft delete (Vague C) : suppression logique via deletedAt ---

    /** Soft delete d'une observation flore par son identifiant. */
    @Query("UPDATE observations_flore SET deletedAt = :timestamp WHERE observationId = :id")
    suspend fun delete(id: String, timestamp: Long)

    /** Soft delete massif des observations d'une parcelle. */
    @Query("UPDATE observations_flore SET deletedAt = :timestamp WHERE parcelleId = :parcelleId AND deletedAt IS NULL")
    suspend fun deleteByParcelle(parcelleId: String, timestamp: Long)

    /** Soft delete massif des observations non encore supprimées. */
    @Query("UPDATE observations_flore SET deletedAt = :timestamp WHERE deletedAt IS NULL")
    suspend fun deleteAll(timestamp: Long)

    /**
     * Suppression physique de toutes les observations flore (droit à l'effacement RGPD).
     * À n'utiliser que depuis [DeleteAllUserDataUseCase].
     */
    @Query("DELETE FROM observations_flore")
    suspend fun hardDeleteAll()

    // --- Lectures : les lignes soft-deleted sont filtrées ---

    @Query("SELECT * FROM observations_flore WHERE parcelleId = :parcelleId AND deletedAt IS NULL ORDER BY strate ASC, nomScientifique ASC")
    fun getByParcelle(parcelleId: String): Flow<List<ObservationFloreEntity>>

    @Query("SELECT * FROM observations_flore WHERE placetteId = :placetteId AND deletedAt IS NULL ORDER BY strate ASC, nomScientifique ASC")
    fun getByPlacette(placetteId: String): Flow<List<ObservationFloreEntity>>

    @Query("SELECT * FROM observations_flore WHERE sessionId = :sessionId AND deletedAt IS NULL ORDER BY strate ASC, nomScientifique ASC")
    suspend fun getBySession(sessionId: String): List<ObservationFloreEntity>

    @Query("SELECT COUNT(DISTINCT codeEspece) FROM observations_flore WHERE parcelleId = :parcelleId AND deletedAt IS NULL")
    suspend fun countSpeciesByParcelle(parcelleId: String): Int

    @Query("SELECT * FROM observations_flore WHERE parcelleId = :parcelleId AND isEspeceProtegee = 1 AND deletedAt IS NULL")
    suspend fun getProtectedSpeciesByParcelle(parcelleId: String): List<ObservationFloreEntity>
}
