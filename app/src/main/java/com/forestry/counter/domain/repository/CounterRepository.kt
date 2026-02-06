package com.forestry.counter.domain.repository

import com.forestry.counter.domain.model.Counter
import kotlinx.coroutines.flow.Flow

interface CounterRepository {
    fun getCountersByGroup(groupId: String): Flow<List<Counter>>
    fun getCounterById(counterId: String): Flow<Counter?>
    suspend fun insertCounter(counter: Counter)
    suspend fun updateCounter(counter: Counter)
    suspend fun deleteCounter(counterId: String)
    suspend fun incrementCounter(counterId: String)
    suspend fun decrementCounter(counterId: String)
    suspend fun resetCounter(counterId: String)
    suspend fun duplicateCounter(counterId: String, count: Int = 1): List<String>
    suspend fun updateComputedCounters(groupId: String)
}
