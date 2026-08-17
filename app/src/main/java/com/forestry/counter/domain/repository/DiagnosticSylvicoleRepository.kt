package com.forestry.counter.domain.repository

import com.forestry.counter.domain.model.DiagnosticSylvicole
import kotlinx.coroutines.flow.Flow

interface DiagnosticSylvicoleRepository {
    fun getByParcelle(parcelleId: String): Flow<List<DiagnosticSylvicole>>
    suspend fun getLatestByParcelle(parcelleId: String): DiagnosticSylvicole?
    suspend fun getById(id: String): DiagnosticSylvicole?
    suspend fun insert(diagnostic: DiagnosticSylvicole)
    suspend fun update(diagnostic: DiagnosticSylvicole)
    suspend fun deleteById(id: String)
}
