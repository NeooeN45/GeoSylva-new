package com.forestry.counter.capsule.json

/**
 * Sérialisation JSON canonique, miroir de `gsie_execution_kit.json_utils.canonical_json`.
 *
 * Doit produire des octets strictement identiques au Python source pour la
 * même donnée logique — c'est ce que compare `verify_capsule` avant toute
 * vérification cryptographique. Règles reproduites : clés triées, séparateurs
 * compacts (`,` et `:`, sans espace), UTF-8 non échappé pour les caractères
 * imprimables (`ensure_ascii=False`).
 *
 * Limite connue et assumée : les nombres flottants nécessitant la notation
 * scientifique (hors de portée des coordonnées géographiques réelles que ce
 * champ transporte) ne sont pas supportés — voir [UnsupportedNumberFormatException].
 * Le tri des clés utilise `String.compareTo` (comparaison par unité UTF-16),
 * identique à la comparaison par point de code Python pour tout le plan
 * multilingue de base (BMP) — les clés réelles du manifeste sont des
 * identifiants ASCII, donc cette divergence théorique ne s'applique jamais
 * en pratique ici.
 */
object CanonicalJson {
    fun encode(value: JsonValue): ByteArray {
        val sb = StringBuilder()
        writeValue(value, sb)
        return sb.toString().toByteArray(Charsets.UTF_8)
    }

    private fun writeValue(value: JsonValue, sb: StringBuilder) {
        when (value) {
            is JsonValue.Null -> sb.append("null")
            is JsonValue.Bool -> sb.append(if (value.value) "true" else "false")
            is JsonValue.JsonInteger -> sb.append(value.value.toString())
            is JsonValue.JsonReal -> sb.append(formatPythonCompatibleDouble(value.value))
            is JsonValue.Str -> writeString(value.value, sb)
            is JsonValue.Arr -> {
                sb.append('[')
                value.items.forEachIndexed { index, item ->
                    if (index > 0) sb.append(',')
                    writeValue(item, sb)
                }
                sb.append(']')
            }
            is JsonValue.Obj -> {
                sb.append('{')
                val sortedEntries = value.entries.sortedBy { it.first }
                sortedEntries.forEachIndexed { index, (key, entryValue) ->
                    if (index > 0) sb.append(',')
                    writeString(key, sb)
                    sb.append(':')
                    writeValue(entryValue, sb)
                }
                sb.append('}')
            }
        }
    }

    private fun writeString(value: String, sb: StringBuilder) {
        sb.append('"')
        for (c in value) {
            when {
                c == '"' -> sb.append("\\\"")
                c == '\\' -> sb.append("\\\\")
                c == '\b' -> sb.append("\\b")
                c == '\u000C' -> sb.append("\\f")
                c == '\n' -> sb.append("\\n")
                c == '\r' -> sb.append("\\r")
                c == '\t' -> sb.append("\\t")
                c.code < 0x20 -> sb.append("\\u%04x".format(c.code))
                else -> sb.append(c)
            }
        }
        sb.append('"')
    }

    internal fun formatPythonCompatibleDouble(value: Double): String {
        require(value.isFinite()) { "Un nombre JSON canonique doit être fini" }
        val javaRepr = value.toString()
        if ('E' in javaRepr) {
            throw UnsupportedNumberFormatException(value)
        }
        return javaRepr
    }
}

/**
 * Levée quand un nombre flottant nécessiterait une notation scientifique pour
 * être canonicalisé — non supporté par cette tranche verticale (voir ADR-008,
 * gate "validation croisée des types numériques étendus"). Aucune capsule
 * réelle transportant des coordonnées géographiques ordinaires ne devrait
 * jamais déclencher ce cas ; il est levé explicitement plutôt que de produire
 * silencieusement une forme canonique divergente de Python.
 */
class UnsupportedNumberFormatException(val value: Double) :
    Exception("Notation scientifique non supportée par cette tranche verticale : $value")
