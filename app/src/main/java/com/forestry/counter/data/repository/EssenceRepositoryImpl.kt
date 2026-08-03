package com.forestry.counter.data.repository

import com.forestry.counter.data.local.dao.EssenceDao
import com.forestry.counter.data.mapper.toEssence
import com.forestry.counter.data.mapper.toEssenceEntity
import com.forestry.counter.data.service.MetadataService
import com.forestry.counter.domain.model.Essence
import com.forestry.counter.domain.repository.EssenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EssenceRepositoryImpl(
    private val essenceDao: EssenceDao,
    private val metadataService: MetadataService,
) : EssenceRepository {

    override fun getAllEssences(): Flow<List<Essence>> {
        return essenceDao.getAllEssences().map { list -> list.map { it.toEssence() } }
    }

    override fun getEssenceByCode(code: String): Flow<Essence?> {
        return essenceDao.getEssenceByCodeFlow(code).map { it?.toEssence() }
    }

    override suspend fun insertEssence(essence: Essence) {
        val enriched = metadataService.enrichForCreate(essence)
        essenceDao.insertEssence(enriched.toEssenceEntity())
    }

    override suspend fun insertEssences(essences: List<Essence>) {
        val enriched = essences.map { metadataService.enrichForCreate(it) }
        essenceDao.insertEssences(enriched.map { it.toEssenceEntity() })
    }

    override suspend fun updateEssence(essence: Essence) {
        val enriched = metadataService.enrichForUpdate(essence, baseVersion = essence.version)
        essenceDao.updateEssence(enriched.toEssenceEntity())
    }

    override suspend fun deleteEssence(code: String) {
        essenceDao.deleteEssenceByCode(code, System.currentTimeMillis())
    }

    override suspend fun deleteAllEssences() {
        essenceDao.deleteAllEssences(System.currentTimeMillis())
    }
}
