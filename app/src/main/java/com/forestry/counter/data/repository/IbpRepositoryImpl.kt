package com.forestry.counter.data.repository

import com.forestry.counter.data.local.dao.IbpEvaluationDao
import com.forestry.counter.data.local.entity.IbpEvaluationEntity
import com.forestry.counter.domain.model.IbpAnswers
import com.forestry.counter.domain.model.IbpEvaluation
import com.forestry.counter.domain.repository.IbpRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

private fun IbpEvaluationEntity.toDomain(): IbpEvaluation {
    val answers = runCatching { json.decodeFromString<IbpAnswers>(answersJson) }.getOrElse { IbpAnswers() }
    return IbpEvaluation(
        id = id,
        placetteId = placetteId,
        parcelleId = parcelleId,
        observationDate = observationDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        evaluatorName = evaluatorName,
        answers = answers,
        globalNote = globalNote
    )
}

private fun IbpEvaluation.toEntity(): IbpEvaluationEntity = IbpEvaluationEntity(
    id = id,
    placetteId = placetteId,
    parcelleId = parcelleId,
    observationDate = observationDate,
    createdAt = createdAt,
    updatedAt = updatedAt,
    evaluatorName = evaluatorName,
    answersJson = json.encodeToString(answers),
    globalNote = globalNote
)

class IbpRepositoryImpl(private val dao: IbpEvaluationDao) : IbpRepository {
    override fun getByPlacette(placetteId: String): Flow<List<IbpEvaluation>> =
        dao.getByPlacette(placetteId).map { list -> list.map { it.toDomain() } }

    override fun getByParcelle(parcelleId: String): Flow<List<IbpEvaluation>> =
        dao.getByParcelle(parcelleId).map { list -> list.map { it.toDomain() } }

    override fun getById(id: String): Flow<IbpEvaluation?> =
        dao.getById(id).map { it?.toDomain() }

    override fun getAll(): Flow<List<IbpEvaluation>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun save(evaluation: IbpEvaluation) {
        dao.upsert(evaluation.toEntity())
    }

    override suspend fun delete(id: String) {
        dao.deleteById(id)
    }

    override suspend fun deleteByPlacette(placetteId: String) {
        dao.deleteByPlacette(placetteId)
    }
}
