package com.forestry.counter.data.local.dao

import androidx.room.*
import com.forestry.counter.data.local.entity.GroupVariableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupVariableDao {
    @Query("SELECT * FROM group_variables WHERE groupOwnerId = :groupId ORDER BY name ASC")
    fun getVariablesByGroup(groupId: String): Flow<List<GroupVariableEntity>>

    @Query("SELECT * FROM group_variables WHERE variableId = :variableId")
    suspend fun getVariableById(variableId: String): GroupVariableEntity?

    @Query("SELECT * FROM group_variables WHERE groupOwnerId = :groupId AND name = :name")
    suspend fun getVariableByName(groupId: String, name: String): GroupVariableEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariable(variable: GroupVariableEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariables(variables: List<GroupVariableEntity>)

    @Update
    suspend fun updateVariable(variable: GroupVariableEntity)

    @Delete
    suspend fun deleteVariable(variable: GroupVariableEntity)

    @Query("DELETE FROM group_variables WHERE variableId = :variableId")
    suspend fun deleteVariableById(variableId: String)

    @Query("DELETE FROM group_variables WHERE groupOwnerId = :groupId")
    suspend fun deleteVariablesByGroup(groupId: String)
}
