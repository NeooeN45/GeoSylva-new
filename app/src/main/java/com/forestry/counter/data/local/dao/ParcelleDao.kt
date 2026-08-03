package com.forestry.counter.data.local.dao

import androidx.room.*
import com.forestry.counter.data.local.entity.ParcelleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParcelleDao {
    @Query("SELECT * FROM parcelles WHERE deletedAt IS NULL ORDER BY name ASC")
    fun getAllParcelles(): Flow<List<ParcelleEntity>>

    @Query("SELECT * FROM parcelles WHERE deletedAt IS NULL ORDER BY name ASC")
    suspend fun getAllParcellesNow(): List<ParcelleEntity>

    @Query("SELECT * FROM parcelles WHERE forestOwnerId = :forestId AND deletedAt IS NULL ORDER BY name ASC")
    fun getParcellesByForest(forestId: String): Flow<List<ParcelleEntity>>

    @Query("SELECT * FROM parcelles WHERE parcelleId = :id AND deletedAt IS NULL")
    fun getParcelleByIdFlow(id: String): Flow<ParcelleEntity?>

    @Query("SELECT * FROM parcelles WHERE parcelleId = :id AND deletedAt IS NULL")
    suspend fun getParcelleById(id: String): ParcelleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParcelle(entity: ParcelleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParcelles(entities: List<ParcelleEntity>)

    @Update
    suspend fun updateParcelle(entity: ParcelleEntity)

    // --- Soft delete (Vague C) : suppression logique via deletedAt ---

    /** Soft delete d'une parcelle par son identifiant. */
    @Query("UPDATE parcelles SET deletedAt = :timestamp WHERE parcelleId = :id")
    suspend fun deleteParcelle(id: String, timestamp: Long)

    /** Soft delete d'une parcelle par son identifiant (alias sémantique). */
    @Query("UPDATE parcelles SET deletedAt = :timestamp WHERE parcelleId = :id")
    suspend fun deleteParcelleById(id: String, timestamp: Long)

    /** Soft delete massif des parcelles non encore supprimées. */
    @Query("UPDATE parcelles SET deletedAt = :timestamp WHERE deletedAt IS NULL")
    suspend fun deleteAllParcelles(timestamp: Long)

    /**
     * Suppression physique de toutes les parcelles (droit à l'effacement RGPD).
     * À n'utiliser que depuis [DeleteAllUserDataUseCase].
     */
    @Query("DELETE FROM parcelles")
    suspend fun hardDeleteAll()
}
