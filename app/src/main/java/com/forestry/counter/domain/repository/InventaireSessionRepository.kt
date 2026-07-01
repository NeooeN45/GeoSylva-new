package com.forestry.counter.domain.repository

import com.forestry.counter.domain.model.InventaireSession
import kotlinx.coroutines.flow.Flow

interface InventaireSessionRepository {
    fun getByParcelle(parcelleId: String): Flow<List<InventaireSession>>
    suspend fun getById(id: String): InventaireSession?
    suspend fun getLatestByType(parcelleId: String, type: String): InventaireSession?
    suspend fun insert(session: InventaireSession)
    suspend fun update(session: InventaireSession)
    suspend fun deleteById(id: String)
}
