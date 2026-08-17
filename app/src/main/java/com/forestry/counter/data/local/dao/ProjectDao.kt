package com.forestry.counter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.forestry.counter.data.local.entity.ProjectEntity
import com.forestry.counter.data.local.entity.ProjectForestCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectEntity)

    @Update
    suspend fun update(project: ProjectEntity)

    @Query("UPDATE projects SET deletedAt = :timestamp WHERE projectId = :id")
    suspend fun delete(id: String, timestamp: Long)

    @Query("DELETE FROM projects")
    suspend fun hardDeleteAll()

    @Query("SELECT * FROM projects WHERE deletedAt IS NULL ORDER BY isFavorite DESC, updatedAt DESC")
    fun getAll(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE projectId = :id AND deletedAt IS NULL")
    suspend fun getById(id: String): ProjectEntity?

    @Query("SELECT * FROM projects WHERE uuid = :uuid AND deletedAt IS NULL")
    suspend fun getByUuid(uuid: String): ProjectEntity?

    @Query("SELECT * FROM projects WHERE name LIKE '%' || :query || '%' AND deletedAt IS NULL ORDER BY updatedAt DESC")
    fun search(query: String): Flow<List<ProjectEntity>>

    @Query("UPDATE projects SET isFavorite = :favorite WHERE projectId = :id")
    suspend fun setFavorite(id: String, favorite: Boolean)

    // --- Relations N-N avec les forêts ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addForest(crossRef: ProjectForestCrossRef)

    @Query("DELETE FROM project_forests WHERE projectId = :projectId AND foretId = :foretId")
    suspend fun removeForest(projectId: String, foretId: String)

    @Query("SELECT foretId FROM project_forests WHERE projectId = :projectId")
    suspend fun getForestIds(projectId: String): List<String>
}
