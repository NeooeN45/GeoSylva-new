package com.forestry.counter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.forestry.counter.data.local.entity.AlerteSanitaireEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlerteSanitaireDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alerte: AlerteSanitaireEntity)

    @Update
    suspend fun update(alerte: AlerteSanitaireEntity)

    // --- Soft delete (Vague C) : suppression logique via deletedAt ---

    /** Soft delete d'une alerte sanitaire par son identifiant. */
    @Query("UPDATE alertes_sanitaires SET deletedAt = :timestamp WHERE alerteId = :id")
    suspend fun delete(id: String, timestamp: Long)

    /** Soft delete d'une alerte sanitaire par son identifiant (alias sémantique). */
    @Query("UPDATE alertes_sanitaires SET deletedAt = :timestamp WHERE alerteId = :id")
    suspend fun deleteById(id: String, timestamp: Long)

    /** Soft delete massif des alertes sanitaires non encore supprimées. */
    @Query("UPDATE alertes_sanitaires SET deletedAt = :timestamp WHERE deletedAt IS NULL")
    suspend fun deleteAll(timestamp: Long)

    /**
     * Suppression physique de toutes les alertes sanitaires (droit à l'effacement RGPD).
     * À n'utiliser que depuis [DeleteAllUserDataUseCase].
     */
    @Query("DELETE FROM alertes_sanitaires")
    suspend fun hardDeleteAll()

    // --- Lectures : les lignes soft-deleted sont filtrées ---

    @Query("SELECT * FROM alertes_sanitaires WHERE parcelleId = :parcelleId AND deletedAt IS NULL ORDER BY dateDetection DESC")
    fun getByParcelle(parcelleId: String): Flow<List<AlerteSanitaireEntity>>

    @Query("SELECT * FROM alertes_sanitaires WHERE parcelleId = :parcelleId AND niveauRisque IN ('ELEVE', 'CRITIQUE') AND deletedAt IS NULL ORDER BY dateDetection DESC")
    fun getCriticalByParcelle(parcelleId: String): Flow<List<AlerteSanitaireEntity>>

    @Query("SELECT * FROM alertes_sanitaires WHERE isOrganismeReglemente = 1 AND isAlerteDsf = 0 AND deletedAt IS NULL")
    suspend fun getPendingDsfAlerts(): List<AlerteSanitaireEntity>

    @Query("SELECT COUNT(*) FROM alertes_sanitaires WHERE parcelleId = :parcelleId AND niveauRisque IN ('ELEVE', 'CRITIQUE') AND deletedAt IS NULL")
    suspend fun countCriticalByParcelle(parcelleId: String): Int
}
