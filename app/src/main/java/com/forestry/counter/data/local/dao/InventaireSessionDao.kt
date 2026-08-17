package com.forestry.counter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.forestry.counter.data.local.entity.InventaireSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventaireSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: InventaireSessionEntity)

    @Update
    suspend fun update(session: InventaireSessionEntity)

    // --- Soft delete (Vague C) : suppression logique via deletedAt ---

    /** Soft delete d'une session par son identifiant. */
    @Query("UPDATE inventaire_sessions SET deletedAt = :timestamp WHERE sessionId = :id")
    suspend fun delete(id: String, timestamp: Long)

    /** Soft delete d'une session par son identifiant (alias sémantique). */
    @Query("UPDATE inventaire_sessions SET deletedAt = :timestamp WHERE sessionId = :id")
    suspend fun deleteById(id: String, timestamp: Long)

    /** Soft delete massif des sessions non encore supprimées. */
    @Query("UPDATE inventaire_sessions SET deletedAt = :timestamp WHERE deletedAt IS NULL")
    suspend fun deleteAll(timestamp: Long)

    /**
     * Suppression physique de toutes les sessions (droit à l'effacement RGPD).
     * À n'utiliser que depuis [DeleteAllUserDataUseCase].
     */
    @Query("DELETE FROM inventaire_sessions")
    suspend fun hardDeleteAll()

    // --- Lectures : les lignes soft-deleted sont filtrées ---

    @Query("SELECT * FROM inventaire_sessions WHERE parcelleId = :parcelleId AND deletedAt IS NULL ORDER BY dateDebut DESC")
    fun getByParcelle(parcelleId: String): Flow<List<InventaireSessionEntity>>

    @Query("SELECT * FROM inventaire_sessions WHERE sessionId = :id AND deletedAt IS NULL")
    suspend fun getById(id: String): InventaireSessionEntity?

    @Query("SELECT * FROM inventaire_sessions WHERE parcelleId = :parcelleId AND typeSession = :type AND deletedAt IS NULL ORDER BY dateDebut DESC LIMIT 1")
    suspend fun getLatestByType(parcelleId: String, type: String): InventaireSessionEntity?
}
