package com.forestry.counter.capsule.json

import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.math.BigInteger

/**
 * Analyseur JSON strict, miroir de `gsie_execution_kit.json_utils.loads_strict`.
 *
 * Rejette explicitement ce que l'extension Python de la bibliothèque `json`
 * accepterait sans ce garde-fou :
 * - clés d'objet dupliquées ;
 * - constantes non finies `NaN` / `Infinity` / `-Infinity` (rejetées ici de
 *   façon équivalente : ce ne sont simplement pas des jetons JSON valides
 *   selon la grammaire stricte RFC 8259 implémentée ci-dessous) ;
 * - séquences UTF-8 invalides (décodage strict, jamais de remplacement
 *   silencieux par U+FFFD comme le ferait un décodeur Java par défaut).
 */
class StrictJsonParser private constructor(private val text: String) {
    private var pos = 0

    companion object {
        fun parse(bytes: ByteArray): JsonValue {
            val text = decodeStrictUtf8(bytes)
            val parser = StrictJsonParser(text)
            parser.skipWhitespace()
            val value = parser.parseValue()
            parser.skipWhitespace()
            if (parser.pos != text.length) {
                throw StrictJsonError(
                    "Caractères en trop après la valeur JSON à la position ${parser.pos}"
                )
            }
            return value
        }

        private fun decodeStrictUtf8(bytes: ByteArray): String {
            val decoder =
                Charsets.UTF_8
                    .newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
            return try {
                decoder.decode(ByteBuffer.wrap(bytes)).toString()
            } catch (exc: CharacterCodingException) {
                throw StrictJsonError("Séquence UTF-8 invalide : ${exc.message}")
            }
        }
    }

    private fun skipWhitespace() {
        while (pos < text.length && (text[pos] == ' ' || text[pos] == '\t' || text[pos] == '\n' || text[pos] == '\r')) {
            pos++
        }
    }

    private fun peek(): Char {
        if (pos >= text.length) throw StrictJsonError("Fin de JSON inattendue à la position $pos")
        return text[pos]
    }

    private fun expect(c: Char) {
        if (pos >= text.length || text[pos] != c) {
            throw StrictJsonError("Caractère '$c' attendu à la position $pos")
        }
        pos++
    }

    private fun expectLiteral(literal: String) {
        if (pos + literal.length > text.length || text.substring(pos, pos + literal.length) != literal) {
            throw StrictJsonError("Jeton JSON invalide à la position $pos")
        }
        pos += literal.length
    }

    private fun parseValue(): JsonValue {
        skipWhitespace()
        val c = peek()
        return when {
            c == '{' -> parseObject()
            c == '[' -> parseArray()
            c == '"' -> JsonValue.Str(parseStringLiteral())
            c == 't' -> { expectLiteral("true"); JsonValue.Bool(true) }
            c == 'f' -> { expectLiteral("false"); JsonValue.Bool(false) }
            c == 'n' -> { expectLiteral("null"); JsonValue.Null }
            c == '-' || c.isAsciiDigit() -> parseNumber()
            else -> throw StrictJsonError("Jeton JSON inattendu '$c' à la position $pos")
        }
    }

    private fun parseObject(): JsonValue.Obj {
        expect('{')
        skipWhitespace()
        val entries = mutableListOf<Pair<String, JsonValue>>()
        val seenKeys = HashSet<String>()
        if (peek() == '}') {
            pos++
            return JsonValue.Obj(entries)
        }
        while (true) {
            skipWhitespace()
            if (peek() != '"') throw StrictJsonError("Clé JSON attendue (chaîne) à la position $pos")
            val key = parseStringLiteral()
            skipWhitespace()
            expect(':')
            val value = parseValue()
            if (!seenKeys.add(key)) {
                throw StrictJsonError("Clé JSON dupliquée : $key")
            }
            entries.add(key to value)
            skipWhitespace()
            when (peek()) {
                ',' -> { pos++ }
                '}' -> { pos++; return JsonValue.Obj(entries) }
                else -> throw StrictJsonError("',' ou '}' attendu à la position $pos")
            }
        }
    }

    private fun parseArray(): JsonValue.Arr {
        expect('[')
        skipWhitespace()
        val items = mutableListOf<JsonValue>()
        if (peek() == ']') {
            pos++
            return JsonValue.Arr(items)
        }
        while (true) {
            items.add(parseValue())
            skipWhitespace()
            when (peek()) {
                ',' -> { pos++ }
                ']' -> { pos++; return JsonValue.Arr(items) }
                else -> throw StrictJsonError("',' ou ']' attendu à la position $pos")
            }
        }
    }

    private fun parseStringLiteral(): String {
        expect('"')
        val sb = StringBuilder()
        while (true) {
            if (pos >= text.length) throw StrictJsonError("Chaîne JSON non terminée")
            val c = text[pos]
            when {
                c == '"' -> { pos++; return sb.toString() }
                c == '\\' -> {
                    pos++
                    if (pos >= text.length) throw StrictJsonError("Séquence d'échappement incomplète")
                    when (val esc = text[pos]) {
                        '"' -> { sb.append('"'); pos++ }
                        '\\' -> { sb.append('\\'); pos++ }
                        '/' -> { sb.append('/'); pos++ }
                        'b' -> { sb.append('\b'); pos++ }
                        'f' -> { sb.append('\u000C'); pos++ }
                        'n' -> { sb.append('\n'); pos++ }
                        'r' -> { sb.append('\r'); pos++ }
                        't' -> { sb.append('\t'); pos++ }
                        'u' -> {
                            pos++
                            if (pos + 4 > text.length) throw StrictJsonError("Séquence \\u incomplète")
                            val hex = text.substring(pos, pos + 4)
                            val code =
                                hex.toIntOrNull(16)
                                    ?: throw StrictJsonError("Séquence \\u invalide : $hex")
                            sb.append(code.toChar())
                            pos += 4
                        }
                        else -> throw StrictJsonError("Séquence d'échappement invalide : \\$esc")
                    }
                }
                c.code < 0x20 -> throw StrictJsonError(
                    "Caractère de contrôle non échappé dans une chaîne JSON (0x${c.code.toString(16)})"
                )
                else -> { sb.append(c); pos++ }
            }
        }
    }

    private fun parseNumber(): JsonValue {
        val start = pos
        if (peek() == '-') pos++
        if (pos >= text.length || !text[pos].isAsciiDigit()) {
            throw StrictJsonError("Nombre JSON invalide à la position $start")
        }
        if (text[pos] == '0') {
            pos++
        } else {
            while (pos < text.length && text[pos].isAsciiDigit()) pos++
        }
        var isReal = false
        if (pos < text.length && text[pos] == '.') {
            isReal = true
            pos++
            if (pos >= text.length || !text[pos].isAsciiDigit()) {
                throw StrictJsonError("Partie fractionnaire invalide à la position $pos")
            }
            while (pos < text.length && text[pos].isAsciiDigit()) pos++
        }
        if (pos < text.length && (text[pos] == 'e' || text[pos] == 'E')) {
            isReal = true
            pos++
            if (pos < text.length && (text[pos] == '+' || text[pos] == '-')) pos++
            if (pos >= text.length || !text[pos].isAsciiDigit()) {
                throw StrictJsonError("Exposant invalide à la position $pos")
            }
            while (pos < text.length && text[pos].isAsciiDigit()) pos++
        }
        val literal = text.substring(start, pos)
        return if (isReal) {
            JsonValue.JsonReal(literal.toDouble())
        } else {
            JsonValue.JsonInteger(BigInteger(literal))
        }
    }
}

private fun Char.isAsciiDigit(): Boolean = this in '0'..'9'
