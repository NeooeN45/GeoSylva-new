package com.forestry.counter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.forestry.counter.data.local.entity.PermanentTreeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PermanentTreeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tree: PermanentTreeEntity)

    @Update
    suspend fun update(tree: PermanentTreeEntity)

    @Query("UPDATE permanent_trees SET deletedAt = :timestamp WHERE treeId = :id")
    suspend fun delete(id: String, timestamp: Long)

    @Query("DELETE FROM permanent_trees")
    suspend fun hardDeleteAll()

    @Query("SELECT * FROM permanent_trees WHERE deletedAt IS NULL ORDER BY createdAt DESC")
    fun getAll(): Flow<List<PermanentTreeEntity>>

    @Query("SELECT * FROM permanent_trees WHERE treeId = :id AND deletedAt IS NULL")
    suspend fun getById(id: String): PermanentTreeEntity?

    @Query("SELECT * FROM permanent_trees WHERE uuid = :uuid AND deletedAt IS NULL")
    suspend fun getByUuid(uuid: String): PermanentTreeEntity?

    @Query("SELECT * FROM permanent_trees WHERE parcelleOwnerId = :parcelleId AND deletedAt IS NULL ORDER BY numeroArbre ASC")
    fun getByParcelle(parcelleId: String): Flow<List<PermanentTreeEntity>>

    @Query("SELECT * FROM permanent_trees WHERE placetteOwnerId = :placetteId AND deletedAt IS NULL")
    fun getByPlacette(placetteId: String): Flow<List<PermanentTreeEntity>>

    @Query("SELECT * FROM permanent_trees WHERE uuid IS NULL AND deletedAt IS NULL")
    suspend fun getWithoutUuid(): List<PermanentTreeEntity>

    @Query("UPDATE permanent_trees SET uuid = :uuid WHERE treeId = :id")
    suspend fun setUuid(id: String, uuid: String)
}
