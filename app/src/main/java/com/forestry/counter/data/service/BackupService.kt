package com.forestry.counter.data.service

import com.forestry.counter.data.local.dao.ForetDao
import com.forestry.counter.data.local.dao.ParcelleDao
import com.forestry.counter.data.local.dao.PlacetteDao
import com.forestry.counter.data.local.dao.TigeDao
import com.forestry.counter.data.mapper.toDomain
import com.forestry.counter.data.mapper.toEntity
import com.forestry.counter.data.mapper.toParcelle
import com.forestry.counter.data.mapper.toParcelleEntity
import com.forestry.counter.data.mapper.toPlacette
import com.forestry.counter.data.mapper.toPlacetteEntity
import com.forestry.counter.data.mapper.toTige
import com.forestry.counter.data.mapper.toTigeEntity
import com.forestry.counter.domain.model.Foret
import com.forestry.counter.domain.model.Parcelle
import com.forestry.counter.domain.model.Placette
import com.forestry.counter.domain.model.Tige

/**
 * Service de restauration après crash ou migration appareil — spec GeoSylva 3.0 §11.
 *
 * Export/import JSON round-trip des entités cœur métier forestières
 * (Forêt → Parcelle → Placette → Tige) avec préservation des identifiants
 * et des metadata de traçabilité §3.1 (`auteur`, `source`, `version`).
 *
 * Lot 1 Sprint 2.2 : implémentation du contrat documenté par
 * [com.forestry.counter.data.BackupRestoreTest].
 */
class BackupService(
    private val foretDao: ForetDao,
    private val parcelleDao: ParcelleDao,
    private val placetteDao: PlacetteDao,
    private val tigeDao: TigeDao,
) {

    /**
     * Instantané sérialisable des entités cœur forestières.
     *
     * @param version version du format d'export (forward-compatibilité).
     * @param exportDate horodatage de l'export (ms epoch).
     * @param forets forêts exportées.
     * @param parcelles parcelles exportées (référencent [Foret.foretId]).
     * @param placettes placettes exportées (référencent [Parcelle.id]).
     * @param tiges tiges exportées (référencent [Parcelle.id] / [Placette.id]).
     */
    data class ForestryBackup(
        val version: String = "1.0.0",
        val exportDate: Long,
        val forets: List<Foret> = emptyList(),
        val parcelles: List<Parcelle> = emptyList(),
        val placettes: List<Placette> = emptyList(),
        val tiges: List<Tige> = emptyList(),
    )

    /**
     * Exporte l'intégralité des entités cœur forestières dans un [ForestryBackup].
     *
     * Les lignes soft-deleted (`deletedAt != null`) sont exclues — un backup
     * ne doit pas restaurer des données supprimées logiquement.
     */
    suspend fun export(): ForestryBackup {
        val forets = foretDao.getAllNow().map { it.toDomain() }
        val parcelles = parcelleDao.getAllParcellesNow().map { it.toParcelle() }
        val placettes = placetteDao.getAllPlacettesNow().map { it.toPlacette() }
        val tiges = tigeDao.getAllTigesNow().map { it.toTige() }
        return ForestryBackup(
            exportDate = System.currentTimeMillis(),
            forets = forets,
            parcelles = parcelles,
            placettes = placettes,
            tiges = tiges,
        )
    }

    /**
     * Rejoue un [ForestryBackup] en base, en préservant identifiants et metadata.
     *
     * Stratégie : REPLACE (upsert) — si une entité avec le même ID existe,
     * elle est écrasée par la version du backup. Les identifiants et les
     * metadata §3.1 (`auteur`, `source`, `version`) sont préservés.
     *
     * @throws IllegalArgumentException si le backup est malformé (exportDate <= 0
     *   sans aucune entité).
     */
    suspend fun import(backup: ForestryBackup) {
        if (backup.exportDate <= 0L && backup.forets.isEmpty() && backup.parcelles.isEmpty()) {
            throw IllegalArgumentException("Backup malformé : exportDate invalide et aucune entité")
        }
        // Ordre d'insertion respectant les FK : forets → parcelles → placettes → tiges
        backup.forets.forEach { foretDao.insert(it.toEntity()) }
        backup.parcelles.forEach { parcelleDao.insertParcelle(it.toParcelleEntity()) }
        backup.placettes.forEach { placetteDao.insertPlacette(it.toPlacetteEntity()) }
        backup.tiges.forEach { tigeDao.insertTige(it.toTigeEntity()) }
    }
}
