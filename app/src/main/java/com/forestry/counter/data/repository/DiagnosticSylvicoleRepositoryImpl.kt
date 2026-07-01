package com.forestry.counter.data.repository

import com.forestry.counter.data.local.dao.DiagnosticSylvicoleDao
import com.forestry.counter.data.mapper.toDomain
import com.forestry.counter.data.mapper.toEntity
import com.forestry.counter.domain.model.DiagnosticSylvicole
import com.forestry.counter.domain.repository.DiagnosticSylvicoleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DiagnosticSylvicoleRepositoryImpl(private val dao: DiagnosticSylvicoleDao) : DiagnosticSylvicoleRepository {
    override fun getByParcelle(parcelleId: String): Flow<List<DiagnosticSylvicole>> =
        dao.getByParcelle(parcelleId).map { list -> list.map { it.toDomain() } }
    override suspend fun getLatestByParcelle(parcelleId: String): DiagnosticSylvicole? =
        dao.getLatestByParcelle(parcelleId)?.toDomain()
    override suspend fun getById(id: String): DiagnosticSylvicole? =
        dao.getById(id)?.toDomain()
    override suspend fun insert(diagnostic: DiagnosticSylvicole) = dao.insert(diagnostic.toEntity())
    override suspend fun update(diagnostic: DiagnosticSylvicole) = dao.update(diagnostic.toEntity())
    override suspend fun deleteById(id: String) = dao.deleteById(id)
}
