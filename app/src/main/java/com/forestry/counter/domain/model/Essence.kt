package com.forestry.counter.domain.model

data class Essence(
    val code: String,
    val name: String,
    val categorie: String?,
    val densiteBoite: Double?,
    val colorHex: String? = null,
    // ── Propriétés forestières enrichies ──
    val densiteBois: Double? = null,       // densité du bois (kg/m³)
    val qualiteTypique: String? = null,     // ex: "A/B", "C/D", "A-C"
    val typeCoupePreferee: String? = null,  // ex: "Futaie régulière", "Taillis", "TSF"
    val usageBois: String? = null,          // ex: "Tranchage", "Menuiserie", "Charpente"
    val vitesseCroissance: String? = null,  // "Lente", "Moyenne", "Rapide"
    val hauteurMaxM: Double? = null,        // hauteur max typique (m)
    val diametreMaxCm: Double? = null,      // diamètre max typique (cm)
    val toleranceOmbre: String? = null,     // "Très tolérante", "Tolérante", "Intermédiaire", "Intolérante"
    val remarques: String? = null,           // particularités remarquables

    // Metadata spec GeoSylva 3.0 §3.1 — provenance et traçabilité
    override val deletedAt: Long? = null,
    override val auteur: String? = null,
    override val source: String? = null,
    override val version: Int = 1
) : Metadatable<Essence> {
    override fun withMetadata(auteur: String, source: String, version: Int): Essence =
        copy(auteur = auteur, source = source, version = version)
}
