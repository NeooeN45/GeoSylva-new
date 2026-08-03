package com.forestry.counter.domain.model

/**
 * Domain model pour un diagnostic sylvicole.
 * Mappé depuis/vers DiagnosticSylvicoleEntity dans data/mapper.
 */
data class DiagnosticSylvicole(
    val diagnosticId: String,
    val parcelleId: String,
    val sessionId: String?,
    val dateCreation: Long,
    val operateurNom: String?,

    // Scores principaux 0-100
    val scoreStation: Int?,
    val scorePeuplement: Int?,
    val scoreBiodiversite: Int?,
    val scoreRisque: Int?,
    val scoreGlobal: Int?,

    // Indicateurs dendrométriques calculés
    val gHa: Double?,
    val nHa: Int?,
    val vHa: Double?,
    val hoM: Double?,
    val hgM: Double?,
    val dgCm: Double?,
    val siteIndex: Double?,
    val accroissementIg: Double?,
    val accroissementIv: Double?,
    val biomasseTotalTonnes: Double?,
    val carboneTotalTonnes: Double?,

    // Résultats qualitatifs JSON
    val essencesRecommandeesJson: String?,
    val essencesDeconseillees: String?,
    val essencesVigilanceJson: String?,
    val risquesDetectesJson: String?,
    val recommandationsSylvicolesJson: String?,
    val typeSylviculturePreco: String?,
    val volumeEclairciePreco: Double?,
    val delaiInterventionAns: Int?,
    val syntheseTextuelle: String?,

    // Versioning
    val algoVersion: String,
    val dataSourcesJson: String?,
    val remarques: String?,
    val updatedAt: Long = System.currentTimeMillis(),

    // Metadata spec GeoSylva 3.0 §3.1 — provenance et traçabilité
    override val deletedAt: Long? = null,
    override val auteur: String? = null,
    override val source: String? = null,
    override val version: Int = 1
) : Metadatable<DiagnosticSylvicole> {
    override fun withMetadata(auteur: String, source: String, version: Int): DiagnosticSylvicole =
        copy(auteur = auteur, source = source, version = version)
}
