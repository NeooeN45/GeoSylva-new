package com.forestry.counter.data.local.dao

import androidx.room.*
import com.forestry.counter.data.local.entity.TigeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TigeDao {
    @Query("SELECT * FROM tiges WHERE deletedAt IS NULL ORDER BY timestamp ASC")
    fun getAllTiges(): Flow<List<TigeEntity>>

    @Query("SELECT * FROM tiges WHERE parcelleOwnerId = :parcelleId AND deletedAt IS NULL ORDER BY timestamp ASC")
    fun getTigesByParcelle(parcelleId: String): Flow<List<TigeEntity>>

    @Query("SELECT * FROM tiges WHERE placetteOwnerId = :placetteId AND deletedAt IS NULL ORDER BY timestamp ASC")
    fun getTigesByPlacette(placetteId: String): Flow<List<TigeEntity>>

    @Query("SELECT * FROM tiges WHERE tigeId = :id AND deletedAt IS NULL")
    fun getTigeByIdFlow(id: String): Flow<TigeEntity?>

    @Query("SELECT * FROM tiges WHERE tigeId = :id AND deletedAt IS NULL")
    suspend fun getTigeById(id: String): TigeEntity?

    @Query("""
        SELECT * FROM tiges
        WHERE parcelleOwnerId = :parcelleId
          AND ((:placetteId IS NULL AND placetteOwnerId IS NULL) OR placetteOwnerId = :placetteId)
          AND essenceCode = :essenceCode
          AND diamCm = :diamCm
          AND deletedAt IS NULL
        ORDER BY timestamp DESC
        LIMIT 1
    """)
    suspend fun getLatestMatching(
        parcelleId: String,
        placetteId: String?,
        essenceCode: String,
        diamCm: Double
    ): TigeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTige(entity: TigeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTiges(entities: List<TigeEntity>)

    @Update
    suspend fun updateTige(entity: TigeEntity)

    // --- Soft delete (Vague C) : suppression logique via deletedAt ---

    /** Soft delete d'une tige par son identifiant. */
    @Query("UPDATE tiges SET deletedAt = :timestamp WHERE tigeId = :id")
    suspend fun deleteTige(id: String, timestamp: Long)

    /** Soft delete d'une tige par son identifiant (alias sémantique). */
    @Query("UPDATE tiges SET deletedAt = :timestamp WHERE tigeId = :id")
    suspend fun deleteTigeById(id: String, timestamp: Long)

    /** Soft delete massif des tiges rattachées à une parcelle. */
    @Query("UPDATE tiges SET deletedAt = :timestamp WHERE parcelleOwnerId = :parcelleId AND deletedAt IS NULL")
    suspend fun deleteTigesByParcelle(parcelleId: String, timestamp: Long)

    /** Soft delete massif des tiges rattachées à une placette. */
    @Query("UPDATE tiges SET deletedAt = :timestamp WHERE placetteOwnerId = :placetteId AND deletedAt IS NULL")
    suspend fun deleteTigesByPlacette(placetteId: String, timestamp: Long)

    /** Soft delete massif des tiges d'une placette pour une essence donnée. */
    @Query("UPDATE tiges SET deletedAt = :timestamp WHERE placetteOwnerId = :placetteId AND essenceCode = :essenceCode AND deletedAt IS NULL")
    suspend fun deleteTigesByPlacetteAndEssence(placetteId: String, essenceCode: String, timestamp: Long)

    /** Soft delete massif des tiges non encore supprimées. */
    @Query("UPDATE tiges SET deletedAt = :timestamp WHERE deletedAt IS NULL")
    suspend fun deleteAll(timestamp: Long)

    /**
     * Suppression physique de toutes les tiges (droit à l'effacement RGPD).
     * À n'utiliser que depuis [DeleteAllUserDataUseCase].
     */
    @Query("DELETE FROM tiges")
    suspend fun hardDeleteAll()

    @Query("""
        UPDATE tiges
        SET classeKraft = :classeKraft,
            etatSanitaire = :etatSanitaire,
            vigueur = :vigueur,
            origine = :origine,
            isTigeHabitat = :isTigeHabitat
        WHERE tigeId = :tigeId
    """)
    suspend fun updateTigeSylviculture(
        tigeId: String,
        classeKraft: Int?,
        etatSanitaire: String?,
        vigueur: String?,
        origine: String?,
        isTigeHabitat: Boolean
    ): Int
}
