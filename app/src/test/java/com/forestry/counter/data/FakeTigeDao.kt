package com.forestry.counter.data

import com.forestry.counter.data.local.dao.TigeDao
import com.forestry.counter.data.local.entity.TigeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeTigeDao : TigeDao {
    val data = mutableListOf<TigeEntity>()

    override fun getAllTiges(): Flow<List<TigeEntity>> = flowOf(data.filter { it.deletedAt == null })
    override suspend fun getAllTigesNow(): List<TigeEntity> = data.filter { it.deletedAt == null }
    override fun getTigesByParcelle(parcelleId: String): Flow<List<TigeEntity>> =
        flowOf(data.filter { it.parcelleOwnerId == parcelleId && it.deletedAt == null })
    override fun getTigesByPlacette(placetteId: String): Flow<List<TigeEntity>> =
        flowOf(data.filter { it.placetteOwnerId == placetteId && it.deletedAt == null })
    override fun getTigeByIdFlow(id: String): Flow<TigeEntity?> =
        flowOf(data.find { it.tigeId == id && it.deletedAt == null })
    override suspend fun getTigeById(id: String): TigeEntity? =
        data.find { it.tigeId == id && it.deletedAt == null }
    override suspend fun getLatestMatching(
        parcelleId: String, placetteId: String?, essenceCode: String, diamCm: Double
    ): TigeEntity? = data.find {
        it.parcelleOwnerId == parcelleId &&
        it.placetteOwnerId == placetteId &&
        it.essenceCode == essenceCode &&
        it.diamCm == diamCm &&
        it.deletedAt == null
    }
    override suspend fun insertTige(entity: TigeEntity) { data.add(entity) }
    override suspend fun insertTiges(entities: List<TigeEntity>) { data.addAll(entities) }
    override suspend fun updateTige(entity: TigeEntity) {
        val idx = data.indexOfFirst { it.tigeId == entity.tigeId }
        if (idx >= 0) data[idx] = entity
    }
    override suspend fun deleteTige(id: String, timestamp: Long) {
        val idx = data.indexOfFirst { it.tigeId == id }
        if (idx >= 0) data[idx] = data[idx].copy(deletedAt = timestamp)
    }
    override suspend fun deleteTigeById(id: String, timestamp: Long) = deleteTige(id, timestamp)
    override suspend fun deleteTigesByParcelle(parcelleId: String, timestamp: Long) {
        data.indices.forEach { i ->
            if (data[i].parcelleOwnerId == parcelleId) data[i] = data[i].copy(deletedAt = timestamp)
        }
    }
    override suspend fun deleteTigesByPlacette(placetteId: String, timestamp: Long) {
        data.indices.forEach { i ->
            if (data[i].placetteOwnerId == placetteId) data[i] = data[i].copy(deletedAt = timestamp)
        }
    }
    override suspend fun deleteTigesByPlacetteAndEssence(placetteId: String, essenceCode: String, timestamp: Long) {
        data.indices.forEach { i ->
            if (data[i].placetteOwnerId == placetteId && data[i].essenceCode == essenceCode)
                data[i] = data[i].copy(deletedAt = timestamp)
        }
    }
    override suspend fun deleteAll(timestamp: Long) {
        data.indices.forEach { i -> data[i] = data[i].copy(deletedAt = timestamp) }
    }
    override suspend fun hardDeleteAll() { data.clear() }
    override suspend fun updateTigeSylviculture(
        tigeId: String, classeKraft: Int?, etatSanitaire: String?,
        vigueur: String?, origine: String?, isTigeHabitat: Boolean
    ): Int {
        val idx = data.indexOfFirst { it.tigeId == tigeId }
        if (idx >= 0) {
            data[idx] = data[idx].copy(
                classeKraft = classeKraft, etatSanitaire = etatSanitaire,
                vigueur = vigueur, origine = origine, isTigeHabitat = isTigeHabitat,
            )
            return 1
        }
        return 0
    }
    override suspend fun getWithoutUuid(): List<TigeEntity> = data.filter { it.uuid == null && it.deletedAt == null }
    override suspend fun setUuid(id: String, uuid: String) {
        val idx = data.indexOfFirst { it.tigeId == id }
        if (idx >= 0) data[idx] = data[idx].copy(uuid = uuid)
    }
}
