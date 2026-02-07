package com.forestry.counter.data.local

import com.forestry.counter.domain.model.Essence

/**
 * Liste canonique des essences forestières françaises.
 * Utilisée pour le seeding initial de la base de données
 * et la mise à jour incrémentale lors des montées de version.
 */
object CanonicalEssences {

    val ALL: List<Essence> = listOf(
        // ── Feuillus ──
        Essence(code = "CH_SESSILE",      name = "Chêne sessile",                categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "CH_PEDONCULE",    name = "Chêne pédonculé",              categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "HETRE_COMMUN",    name = "Hêtre commun",                 categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "CHARME",          name = "Charme commun",                categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "CHATAIGNIER",     name = "Châtaignier",                  categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "FRENE_ELEVE",     name = "Frêne élevé",                  categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "ERABLE_SYC",      name = "Érable sycomore",              categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "ERABLE_PLANE",    name = "Érable plane",                 categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "ERABLE_CHAMP",    name = "Érable champêtre",             categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "BOUL_VERRUQ",     name = "Bouleau verruqueux",           categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "BOUL_PUBESC",     name = "Bouleau pubescent",            categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "AULNE_GLUT",      name = "Aulne glutineux",              categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "AULNE_BLANC",     name = "Aulne blanc",                  categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "TIL_PET_FEUIL",   name = "Tilleul à petites feuilles",   categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "TIL_GR_FEUIL",    name = "Tilleul à grandes feuilles",   categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "ORME_CHAMP",      name = "Orme champêtre",               categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "ORME_LISSE",      name = "Orme lisse",                   categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "ORME_MONT",       name = "Orme montagne",                categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "ROBINIER",        name = "Robinier faux-acacia",         categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "NOYER_COMMUN",    name = "Noyer commun",                 categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "NOYER_NOIR",      name = "Noyer noir",                   categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "CERISIER_MERIS",  name = "Cerisier merisier",            categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "CORMIER",         name = "Cormier",                      categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "PEUPLIER_NOIR",   name = "Peuplier noir",                categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "PEUPLIER_TREMB",  name = "Peuplier tremble",             categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "PEUPLIER_HYBR",   name = "Peuplier hybride",             categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "TREMBLE",         name = "Tremble",                      categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "SAULE_BLANC",     name = "Saule blanc",                  categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "SAULE_FRAGILE",   name = "Saule fragile",                categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "SAULE_MARSAULT",  name = "Saule marsault",               categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "NOISETIER",       name = "Noisetier commun",             categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "SORB_OISEL",      name = "Sorbier des oiseleurs",        categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "ALISIER_BLANC",   name = "Alisier blanc",                categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "ALISIER_TORM",    name = "Alisier torminal",             categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "POMMIER_SAUV",    name = "Pommier sauvage",              categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "POIRIER_SAUV",    name = "Poirier sauvage",              categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "FUSAIN",          name = "Fusain d'Europe",              categorie = "Feuillu",  densiteBoite = null),
        Essence(code = "HOUX",            name = "Houx",                         categorie = "Feuillu",  densiteBoite = null),

        // ── Résineux ──
        Essence(code = "PIN_SYLVESTRE",   name = "Pin sylvestre",                categorie = "Résineux", densiteBoite = null),
        Essence(code = "PIN_MARITIME",    name = "Pin maritime",                 categorie = "Résineux", densiteBoite = null),
        Essence(code = "PIN_NOIR_AUTR",   name = "Pin noir d'Autriche",          categorie = "Résineux", densiteBoite = null),
        Essence(code = "PIN_LARICIO",     name = "Pin laricio de Corse",         categorie = "Résineux", densiteBoite = null),
        Essence(code = "PIN_WEYMOUTH",    name = "Pin de Weymouth",              categorie = "Résineux", densiteBoite = null),
        Essence(code = "EPICEA_COMMUN",   name = "Épicéa commun",               categorie = "Résineux", densiteBoite = null),
        Essence(code = "SAPIN_PECTINE",   name = "Sapin pectiné",               categorie = "Résineux", densiteBoite = null),
        Essence(code = "DOUGLAS_VERT",    name = "Douglas vert",                 categorie = "Résineux", densiteBoite = null),
        Essence(code = "MEL_EUROPE",      name = "Mélèze d'Europe",              categorie = "Résineux", densiteBoite = null),
        Essence(code = "MEL_HYBRIDE",     name = "Mélèze hybride",               categorie = "Résineux", densiteBoite = null),

        // ── Conifères divers ──
        Essence(code = "GENEVRIER",       name = "Genévrier commun",             categorie = "Conifère", densiteBoite = null),
        Essence(code = "IF",              name = "If commun",                    categorie = "Conifère", densiteBoite = null)
    )
}
