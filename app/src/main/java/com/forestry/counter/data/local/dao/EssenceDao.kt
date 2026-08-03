package com.forestry.counter.data.local.dao

import androidx.room.*
import com.forestry.counter.data.local.entity.EssenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EssenceDao {
    @Query("SELECT * FROM essences WHERE deletedAt IS NULL ORDER BY name ASC")
    fun getAllEssences(): Flow<List<EssenceEntity>>

    @Query("SELECT * FROM essences WHERE code = :code AND deletedAt IS NULL")
    fun getEssenceByCodeFlow(code: String): Flow<EssenceEntity?>

    @Query("SELECT * FROM essences WHERE code = :code AND deletedAt IS NULL")
    suspend fun getEssenceByCode(code: String): EssenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEssence(entity: EssenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEssences(entities: List<EssenceEntity>)

    @Update
    suspend fun updateEssence(entity: EssenceEntity)

    // --- Soft delete (Vague C) : suppression logique via deletedAt ---

    /** Soft delete d'une essence par son code. */
    @Query("UPDATE essences SET deletedAt = :timestamp WHERE code = :code")
    suspend fun deleteEssence(code: String, timestamp: Long)

    /** Soft delete d'une essence par son code (alias sémantique). */
    @Query("UPDATE essences SET deletedAt = :timestamp WHERE code = :code")
    suspend fun deleteEssenceByCode(code: String, timestamp: Long)

    /** Soft delete massif des essences non encore supprimées. */
    @Query("UPDATE essences SET deletedAt = :timestamp WHERE deletedAt IS NULL")
    suspend fun deleteAllEssences(timestamp: Long)

    /**
     * Suppression physique de toutes les essences (droit à l'effacement RGPD).
     * À n'utiliser que depuis [DeleteAllUserDataUseCase].
     */
    @Query("DELETE FROM essences")
    suspend fun hardDeleteAll()
}
