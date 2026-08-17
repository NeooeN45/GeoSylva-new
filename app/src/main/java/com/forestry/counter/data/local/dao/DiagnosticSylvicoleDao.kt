package com.forestry.counter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.forestry.counter.data.local.entity.DiagnosticSylvicoleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagnosticSylvicoleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(diagnostic: DiagnosticSylvicoleEntity)

    @Update
    suspend fun update(diagnostic: DiagnosticSylvicoleEntity)

    // --- Soft delete (Vague C) : suppression logique via deletedAt ---

    /** Soft delete d'un diagnostic sylvicole par son identifiant. */
    @Query("UPDATE diagnostics_sylvicoles SET deletedAt = :timestamp WHERE diagnosticId = :id")
    suspend fun delete(id: String, timestamp: Long)

    /** Soft delete d'un diagnostic sylvicole par son identifiant (alias sémantique). */
    @Query("UPDATE diagnostics_sylvicoles SET deletedAt = :timestamp WHERE diagnosticId = :id")
    suspend fun deleteById(id: String, timestamp: Long)

    /** Soft delete massif des diagnostics sylvicoles non encore supprimés. */
    @Query("UPDATE diagnostics_sylvicoles SET deletedAt = :timestamp WHERE deletedAt IS NULL")
    suspend fun deleteAll(timestamp: Long)

    /**
     * Suppression physique de tous les diagnostics sylvicoles (droit à l'effacement RGPD).
     * À n'utiliser que depuis [DeleteAllUserDataUseCase].
     */
    @Query("DELETE FROM diagnostics_sylvicoles")
    suspend fun hardDeleteAll()

    // --- Lectures : les lignes soft-deleted sont filtrées ---

    @Query("SELECT * FROM diagnostics_sylvicoles WHERE parcelleId = :parcelleId AND deletedAt IS NULL ORDER BY dateCreation DESC")
    fun getByParcelle(parcelleId: String): Flow<List<DiagnosticSylvicoleEntity>>

    @Query("SELECT * FROM diagnostics_sylvicoles WHERE parcelleId = :parcelleId AND deletedAt IS NULL ORDER BY dateCreation DESC LIMIT 1")
    suspend fun getLatestByParcelle(parcelleId: String): DiagnosticSylvicoleEntity?

    @Query("SELECT * FROM diagnostics_sylvicoles WHERE diagnosticId = :id AND deletedAt IS NULL")
    suspend fun getById(id: String): DiagnosticSylvicoleEntity?

    @Query("SELECT * FROM diagnostics_sylvicoles WHERE sessionId = :sessionId AND deletedAt IS NULL ORDER BY dateCreation DESC LIMIT 1")
    suspend fun getBySession(sessionId: String): DiagnosticSylvicoleEntity?

    @Query("SELECT * FROM diagnostics_sylvicoles WHERE scoreGlobal IS NOT NULL AND deletedAt IS NULL ORDER BY scoreGlobal ASC")
    suspend fun getAllByScoreAsc(): List<DiagnosticSylvicoleEntity>
}
