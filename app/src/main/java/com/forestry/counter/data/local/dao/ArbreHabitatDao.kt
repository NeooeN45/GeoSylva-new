package com.forestry.counter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.forestry.counter.data.local.entity.ArbreHabitatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArbreHabitatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(arbre: ArbreHabitatEntity)

    @Update
    suspend fun update(arbre: ArbreHabitatEntity)

    // --- Soft delete (Vague C) : suppression logique via deletedAt ---

    /** Soft delete d'un arbre habitat par son identifiant. */
    @Query("UPDATE arbres_habitat SET deletedAt = :timestamp WHERE arbreHabitatId = :id")
    suspend fun delete(id: String, timestamp: Long)

    /** Soft delete d'un arbre habitat par son identifiant (alias sémantique). */
    @Query("UPDATE arbres_habitat SET deletedAt = :timestamp WHERE arbreHabitatId = :id")
    suspend fun deleteById(id: String, timestamp: Long)

    /** Soft delete massif des arbres habitat non encore supprimés. */
    @Query("UPDATE arbres_habitat SET deletedAt = :timestamp WHERE deletedAt IS NULL")
    suspend fun deleteAll(timestamp: Long)

    /**
     * Suppression physique de tous les arbres habitat (droit à l'effacement RGPD).
     * À n'utiliser que depuis [DeleteAllUserDataUseCase].
     */
    @Query("DELETE FROM arbres_habitat")
    suspend fun hardDeleteAll()

    // --- Lectures : les lignes soft-deleted sont filtrées ---

    @Query("SELECT * FROM arbres_habitat WHERE parcelleId = :parcelleId AND deletedAt IS NULL ORDER BY diamCm DESC")
    fun getByParcelle(parcelleId: String): Flow<List<ArbreHabitatEntity>>

    @Query("SELECT * FROM arbres_habitat WHERE placetteId = :placetteId AND deletedAt IS NULL ORDER BY diamCm DESC")
    fun getByPlacette(placetteId: String): Flow<List<ArbreHabitatEntity>>

    @Query("SELECT * FROM arbres_habitat WHERE isArbreRemarquable = 1 AND parcelleId = :parcelleId AND deletedAt IS NULL")
    suspend fun getRemarquablesByParcelle(parcelleId: String): List<ArbreHabitatEntity>

    @Query("SELECT SUM(treemScore) FROM arbres_habitat WHERE parcelleId = :parcelleId AND treemScore IS NOT NULL AND deletedAt IS NULL")
    suspend fun sumTreemScoreByParcelle(parcelleId: String): Int?

    @Query("SELECT COUNT(*) FROM arbres_habitat WHERE parcelleId = :parcelleId AND boisMortSurPied = 1 AND deletedAt IS NULL")
    suspend fun countBoisMortByParcelle(parcelleId: String): Int
}
