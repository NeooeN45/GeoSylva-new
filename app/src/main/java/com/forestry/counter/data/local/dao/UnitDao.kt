package com.forestry.counter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.forestry.counter.data.local.entity.UnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(unit: UnitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(units: List<UnitEntity>)

    @Query("SELECT * FROM units ORDER BY dimension ASC, code ASC")
    fun getAll(): Flow<List<UnitEntity>>

    @Query("SELECT * FROM units WHERE code = :code")
    suspend fun getByCode(code: String): UnitEntity?

    @Query("SELECT * FROM units WHERE dimension = :dimension ORDER BY toBaseFactor ASC")
    fun getByDimension(dimension: String): Flow<List<UnitEntity>>

    @Query("SELECT COUNT(*) FROM units")
    suspend fun count(): Int

    @Query("DELETE FROM units")
    suspend fun deleteAll()
}
