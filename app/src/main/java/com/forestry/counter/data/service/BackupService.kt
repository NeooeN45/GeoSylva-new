package com.forestry.counter.data.service

import com.forestry.counter.domain.model.Foret
import com.forestry.counter.domain.model.Parcelle
import com.forestry.counter.domain.model.Placette
import com.forestry.counter.domain.model.Tige

/**
 * Service de restauration après crash ou migration appareil — spec GeoSylva 3.0 §11.
 *
 * Contrairement à [com.forestry.counter.domain.usecase.export.ExportDataUseCase] /
 * [com.forestry.counter.domain.usecase.import.ImportDataUseCase] qui couvrent les
 * entités « compteur » (Group / Counter / Formula), ce service est chargé de
 * l'export/import des **entités cœur métier forestières** (Forêt → Parcelle →
 * Placette → Tige) ainsi que de leurs metadata de traçabilité §3.1
 * (`auteur`, `source`, `version`).
 *
 * TODO(issue #14, Vague C P0) : implémenter l'export/import JSON round-trip des
 * entités forestières. Le contrat attendu est un round-trip sans perte :
 * `export()` produit un [ForestryBackup] sérialisable, `import()` le rejoue en
 * préservant les identifiants et les metadata. Les tests correspondants sont
 * marqués `@Ignore` dans [com.forestry.counter.data.BackupRestoreTest] en
 * attendant l'implémentation.
 */
class BackupService {

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
     * @throws NotImplementedError tant que l'implémentation n'est pas disponible.
     */
    suspend fun export(): ForestryBackup = throw NotImplementedError(
        "BackupService.export() n'est pas encore implémenté (TODO §11, issue #14)"
    )

    /**
     * Rejoue un [ForestryBackup] en base, en préservant identifiants et metadata.
     *
     * @throws NotImplementedError tant que l'implémentation n'est pas disponible.
     */
    suspend fun import(backup: ForestryBackup): Unit {
        throw NotImplementedError("BackupService.import() n'est pas encore implémenté (TODO §11, issue #14)")
    }
}
