package com.forestry.counter.data.local.dao

import androidx.room.*
import com.forestry.counter.data.local.entity.CounterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CounterDao {
    @Query("SELECT * FROM counters WHERE groupOwnerId = :groupId ORDER BY sortIndex ASC, name ASC")
    fun getCountersByGroup(groupId: String): Flow<List<CounterEntity>>

    @Query("SELECT * FROM counters WHERE counterId = :counterId")
    suspend fun getCounterById(counterId: String): CounterEntity?

    @Query("SELECT * FROM counters WHERE counterId = :counterId")
    fun getCounterByIdFlow(counterId: String): Flow<CounterEntity?>

    @Query("SELECT * FROM counters WHERE groupOwnerId = :groupId AND isComputed = 0 ORDER BY sortIndex ASC, name ASC")
    fun getNonComputedCountersByGroup(groupId: String): Flow<List<CounterEntity>>

    @Query("SELECT * FROM counters WHERE groupOwnerId = :groupId AND isComputed = 1")
    fun getComputedCountersByGroup(groupId: String): Flow<List<CounterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCounter(counter: CounterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCounters(counters: List<CounterEntity>)

    @Update
    suspend fun updateCounter(counter: CounterEntity)

    @Update
    suspend fun updateCounters(counters: List<CounterEntity>)

    @Delete
    suspend fun deleteCounter(counter: CounterEntity)

    @Query("DELETE FROM counters WHERE counterId = :counterId")
    suspend fun deleteCounterById(counterId: String)

    @Query("DELETE FROM counters WHERE groupOwnerId = :groupId")
    suspend fun deleteCountersByGroup(groupId: String)

    @Query("DELETE FROM counters")
    suspend fun deleteAllCounters()

    @Query("SELECT MAX(sortIndex) FROM counters WHERE groupOwnerId = :groupId")
    suspend fun getMaxSortIndex(groupId: String): Int?

    @Query("SELECT COUNT(*) FROM counters WHERE groupOwnerId = :groupId")
    suspend fun getCounterCountByGroup(groupId: String): Int

    @Query("SELECT SUM(value) FROM counters WHERE groupOwnerId = :groupId AND isComputed = 0")
    suspend fun getTotalValueByGroup(groupId: String): Double?
}
