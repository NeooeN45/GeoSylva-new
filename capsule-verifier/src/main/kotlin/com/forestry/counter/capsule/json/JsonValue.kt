package com.forestry.counter.capsule.json

import java.math.BigInteger

/**
 * Modèle JSON minimal miroir de `gsie_execution_kit.json_utils` (Python).
 *
 * Distingue explicitement entier (`JsonInteger`, précision arbitraire comme
 * Python) et flottant (`JsonReal`) car seul le second traverse le formateur
 * canonique sensible aux différences de représentation entre langages.
 */
sealed interface JsonValue {
    data object Null : JsonValue

    data class Bool(val value: Boolean) : JsonValue

    data class JsonInteger(val value: BigInteger) : JsonValue

    data class JsonReal(val value: Double) : JsonValue

    data class Str(val value: String) : JsonValue

    data class Arr(val items: List<JsonValue>) : JsonValue

    /**
     * Objet JSON. `entries` conserve l'ordre d'insertion (post-dédoublonnage,
     * déjà garanti par [StrictJsonParser] qui rejette toute clé dupliquée) —
     * le tri alphabétique n'est appliqué qu'au moment de la sérialisation
     * canonique, jamais ici.
     */
    data class Obj(val entries: List<Pair<String, JsonValue>>) : JsonValue {
        fun get(key: String): JsonValue? = entries.firstOrNull { it.first == key }?.second
    }
}

/** Signale un JSON ambigu, invalide ou non canonisable — miroir de `StrictJsonError` (Python). */
class StrictJsonError(message: String) : Exception(message)
