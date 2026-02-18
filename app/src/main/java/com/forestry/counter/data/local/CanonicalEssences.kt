package com.forestry.counter.data.local

import com.forestry.counter.domain.model.Essence

/**
 * Liste canonique des essences forestières françaises.
 * Utilisée pour le seeding initial de la base de données
 * et la mise à jour incrémentale lors des montées de version.
 *
 * Données forestières : densité bois (kg/m³ à 12% HR), qualité typique,
 * type de coupe préférée, usage principal du bois, vitesse de croissance,
 * dimensions maximales, tolérance à l'ombre, et remarques.
 */
object CanonicalEssences {

    // Helpers pour lisibilité
    private fun f(
        code: String, name: String, cat: String = "Feuillu",
        db: Double? = null, qua: String? = null, coupe: String? = null,
        usage: String? = null, vit: String? = null, hMax: Double? = null,
        dMax: Double? = null, ombre: String? = null, rem: String? = null
    ) = Essence(
        code = code, name = name, categorie = cat, densiteBoite = null,
        densiteBois = db, qualiteTypique = qua, typeCoupePreferee = coupe,
        usageBois = usage, vitesseCroissance = vit, hauteurMaxM = hMax,
        diametreMaxCm = dMax, toleranceOmbre = ombre, remarques = rem
    )

    val ALL: List<Essence> = listOf(

        // ════════════════════════════════════════════════════
        // ── FEUILLUS NOBLES ──
        // ════════════════════════════════════════════════════

        f("CH_SESSILE", "Chêne sessile",
            db = 710.0, qua = "A-B", coupe = "Futaie régulière",
            usage = "Tranchage, merrain, charpente", vit = "Lente",
            hMax = 35.0, dMax = 100.0, ombre = "Intermédiaire",
            rem = "Bois de référence en France ; tanins, duramen durable ; convient aux sols secs et filtrants"),

        f("CH_PEDONCULE", "Chêne pédonculé",
            db = 710.0, qua = "A-B", coupe = "Futaie régulière",
            usage = "Tranchage, merrain, tonnellerie", vit = "Lente",
            hMax = 40.0, dMax = 120.0, ombre = "Intermédiaire",
            rem = "Préfère sols frais et profonds ; bois très apprécié en tonnellerie ; sensible à l'oïdium"),

        f("CH_PUBESCENT", "Chêne pubescent",
            db = 750.0, qua = "B-C", coupe = "Taillis sous futaie",
            usage = "Chauffage, charpente", vit = "Lente",
            hMax = 20.0, dMax = 80.0, ombre = "Intolérante",
            rem = "Essence méditerranéenne et thermophile ; très résistant à la sécheresse ; bois très dur"),

        f("CH_ROUGE", "Chêne rouge d'Amérique",
            db = 660.0, qua = "B-C", coupe = "Futaie régulière",
            usage = "Menuiserie, parquet", vit = "Rapide",
            hMax = 35.0, dMax = 90.0, ombre = "Intermédiaire",
            rem = "Introduit ; croissance plus rapide que les chênes autochtones ; bois non durable en extérieur"),

        f("CH_VERT", "Chêne vert",
            db = 950.0, qua = "C-D", coupe = "Taillis",
            usage = "Chauffage, charbon", vit = "Très lente",
            hMax = 15.0, dMax = 60.0, ombre = "Tolérante",
            rem = "Sempervirent ; essence méditerranéenne ; bois extrêmement dur et lourd"),

        f("CH_LIEGE", "Chêne-liège",
            db = 800.0, qua = "C-D", coupe = "Taillis",
            usage = "Liège, chauffage", vit = "Lente",
            hMax = 15.0, dMax = 60.0, ombre = "Intolérante",
            rem = "Écorce récoltée tous les 9-12 ans pour le liège ; maquis et forêts littorales"),

        f("HETRE_COMMUN", "Hêtre commun",
            db = 680.0, qua = "A-B", coupe = "Futaie régulière",
            usage = "Déroulage, ameublement, bois de chauffage", vit = "Moyenne",
            hMax = 40.0, dMax = 100.0, ombre = "Très tolérante",
            rem = "Essence d'ombre par excellence ; sensible aux gelées tardives et à la sécheresse ; bois blanc homogène"),

        f("CHARME", "Charme commun",
            db = 750.0, qua = "C-D", coupe = "Taillis sous futaie",
            usage = "Chauffage, outils, tournage", vit = "Lente",
            hMax = 25.0, dMax = 60.0, ombre = "Très tolérante",
            rem = "Excellent bois de chauffage (PCI élevé) ; accompagnement du chêne ; rejette très bien de souche"),

        f("CHATAIGNIER", "Châtaignier",
            db = 560.0, qua = "B-C", coupe = "Taillis, futaie",
            usage = "Piquets, bardage, parquet, charpente", vit = "Rapide",
            hMax = 30.0, dMax = 80.0, ombre = "Intermédiaire",
            rem = "Bois naturellement durable (tanins) ; roulure fréquente en futaie ; taillis à courte rotation possible"),

        f("FRENE_ELEVE", "Frêne élevé",
            db = 680.0, qua = "A-B", coupe = "Futaie régulière",
            usage = "Manches d'outils, ameublement, sport", vit = "Rapide",
            hMax = 35.0, dMax = 80.0, ombre = "Intermédiaire",
            rem = "Bois souple et résistant aux chocs ; menacé par la chalarose (Hymenoscyphus fraxineus)"),

        f("FRENE_OXYPHYLLE", "Frêne oxyphylle",
            db = 670.0, qua = "B-C", coupe = "Taillis sous futaie",
            usage = "Manches d'outils, chauffage", vit = "Moyenne",
            hMax = 25.0, dMax = 60.0, ombre = "Intermédiaire",
            rem = "Frêne du Midi ; supporte mieux la sécheresse que le frêne élevé"),

        f("ERABLE_SYC", "Érable sycomore",
            db = 620.0, qua = "A-B", coupe = "Futaie irrégulière",
            usage = "Tranchage, lutherie, ameublement", vit = "Moyenne",
            hMax = 35.0, dMax = 80.0, ombre = "Tolérante",
            rem = "Bois blanc très recherché en lutherie (ondé) ; sensible à la suie de l'érable"),

        f("ERABLE_PLANE", "Érable plane",
            db = 620.0, qua = "B-C", coupe = "Futaie irrégulière",
            usage = "Menuiserie, tournage", vit = "Moyenne",
            hMax = 30.0, dMax = 70.0, ombre = "Tolérante",
            rem = "Bois clair homogène ; moins recherché que le sycomore mais intéressant en tranchage"),

        f("ERABLE_CHAMP", "Érable champêtre",
            db = 650.0, qua = "C-D", coupe = "Taillis sous futaie",
            usage = "Tournage, bois de chauffage", vit = "Lente",
            hMax = 15.0, dMax = 50.0, ombre = "Tolérante",
            rem = "Petit arbre des haies et lisières ; bois dur et dense ; intérêt paysager et biodiversité"),

        f("BOUL_VERRUQ", "Bouleau verruqueux",
            db = 620.0, qua = "C-D", coupe = "Futaie à courte révolution",
            usage = "Déroulage, contreplaqué, papeterie", vit = "Rapide",
            hMax = 25.0, dMax = 50.0, ombre = "Intolérante",
            rem = "Essence pionnière ; colonise rapidement les trouées ; bois blanc à grain fin"),

        f("BOUL_PUBESC", "Bouleau pubescent",
            db = 600.0, qua = "C-D", coupe = "Futaie à courte révolution",
            usage = "Papeterie, déroulage", vit = "Rapide",
            hMax = 20.0, dMax = 40.0, ombre = "Intolérante",
            rem = "Supporte les sols hydromorphes ; plus tolérant au froid que le bouleau verruqueux"),

        f("AULNE_GLUT", "Aulne glutineux",
            db = 510.0, qua = "C-D", coupe = "Taillis",
            usage = "Contreplaqué, bois immergé, tournage", vit = "Rapide",
            hMax = 25.0, dMax = 60.0, ombre = "Intermédiaire",
            rem = "Fixateur d'azote ; bords de cours d'eau ; bois durable sous l'eau (pilotis historiques)"),

        f("AULNE_BLANC", "Aulne blanc",
            db = 490.0, qua = "D", coupe = "Taillis",
            usage = "Bois énergie, pâte à papier", vit = "Rapide",
            hMax = 20.0, dMax = 40.0, ombre = "Intermédiaire",
            rem = "Essence montagnarde ; fixateur d'azote ; intérêt écologique pour ripisylves"),

        f("TIL_PET_FEUIL", "Tilleul à petites feuilles",
            db = 490.0, qua = "C-D", coupe = "Futaie irrégulière",
            usage = "Sculpture, tournage, crayons", vit = "Moyenne",
            hMax = 30.0, dMax = 80.0, ombre = "Très tolérante",
            rem = "Bois très tendre, facile à sculpter ; mellifère ; fréquent en sous-étage"),

        f("TIL_GR_FEUIL", "Tilleul à grandes feuilles",
            db = 490.0, qua = "C-D", coupe = "Futaie irrégulière",
            usage = "Sculpture, tournage", vit = "Moyenne",
            hMax = 35.0, dMax = 90.0, ombre = "Très tolérante",
            rem = "Plus exigeant en eau que le tilleul à petites feuilles ; utilisé en ornement"),

        f("ORME_CHAMP", "Orme champêtre",
            db = 640.0, qua = "B-C", coupe = "Taillis sous futaie",
            usage = "Ameublement, charronnage, charpente navale", vit = "Moyenne",
            hMax = 30.0, dMax = 80.0, ombre = "Intermédiaire",
            rem = "Décimé par la graphiose ; bois contrefil caractéristique ; résistant à l'eau"),

        f("ORME_LISSE", "Orme lisse",
            db = 640.0, qua = "B-C", coupe = "Futaie",
            usage = "Ameublement, charpente", vit = "Moyenne",
            hMax = 30.0, dMax = 80.0, ombre = "Tolérante",
            rem = "Plus tolérant à la graphiose ; zones alluviales et bords de rivière"),

        f("ORME_MONT", "Orme de montagne",
            db = 640.0, qua = "B-C", coupe = "Futaie",
            usage = "Ameublement, placage", vit = "Moyenne",
            hMax = 30.0, dMax = 80.0, ombre = "Tolérante",
            rem = "Sensible à la graphiose ; essence montagnarde de vallées fraîches"),

        f("ROBINIER", "Robinier faux-acacia",
            db = 770.0, qua = "B-C", coupe = "Taillis à courte rotation",
            usage = "Piquets, vignes, parquet, extérieur", vit = "Très rapide",
            hMax = 25.0, dMax = 60.0, ombre = "Intolérante",
            rem = "Bois le plus durable d'Europe sans traitement (classe 4) ; fixateur d'azote ; invasif localement"),

        f("NOYER_COMMUN", "Noyer commun",
            db = 640.0, qua = "A", coupe = "Futaie régulière",
            usage = "Tranchage, ébénisterie, crosses d'armes", vit = "Moyenne",
            hMax = 25.0, dMax = 80.0, ombre = "Intolérante",
            rem = "Bois de luxe très recherché ; production de noix ; sensible aux gelées tardives"),

        f("NOYER_NOIR", "Noyer noir",
            db = 610.0, qua = "A-B", coupe = "Futaie régulière",
            usage = "Tranchage, ébénisterie", vit = "Moyenne",
            hMax = 30.0, dMax = 80.0, ombre = "Intolérante",
            rem = "Introduit d'Amérique ; bois sombre très valorisé ; croissance plus rapide que le noyer commun"),

        f("CERISIER_MERIS", "Merisier",
            db = 600.0, qua = "A-B", coupe = "Futaie régulière",
            usage = "Tranchage, ébénisterie, lutherie", vit = "Rapide",
            hMax = 25.0, dMax = 60.0, ombre = "Intolérante",
            rem = "Bois rosé très esthétique ; exploitable dès 60 ans ; tendance à fourcher"),

        f("CORMIER", "Cormier",
            db = 780.0, qua = "A", coupe = "Arbre isolé",
            usage = "Tournage, ébénisterie fine", vit = "Très lente",
            hMax = 18.0, dMax = 50.0, ombre = "Intermédiaire",
            rem = "Bois extrêmement dense et précieux ; essence rare et protégée dans certaines régions"),

        f("PEUPLIER_NOIR", "Peuplier noir",
            db = 410.0, qua = "C-D", coupe = "Futaie à courte révolution",
            usage = "Déroulage, emballage, contreplaqué", vit = "Très rapide",
            hMax = 30.0, dMax = 80.0, ombre = "Intolérante",
            rem = "Espèce indigène des ripisylves ; hybridation fréquente avec cultivars"),

        f("PEUPLIER_TREMB", "Peuplier tremble",
            db = 440.0, qua = "C-D", coupe = "Futaie",
            usage = "Allumettes, pâte à papier, emballage", vit = "Rapide",
            hMax = 25.0, dMax = 50.0, ombre = "Intolérante",
            rem = "Essence pionnière ; drageonneur ; souvent premier arbre après incendie"),

        f("PEUPLIER_HYBR", "Peuplier hybride",
            db = 380.0, qua = "B-C", coupe = "Populiculture (courte révolution)",
            usage = "Déroulage, emballage, contreplaqué", vit = "Très rapide",
            hMax = 35.0, dMax = 80.0, ombre = "Intolérante",
            rem = "Cultivars sélectionnés (I-214, Beaupré…) ; récolte à 15-25 ans ; sols alluviaux fertiles"),

        f("TREMBLE", "Tremble",
            db = 440.0, qua = "C-D", coupe = "Futaie",
            usage = "Pâte à papier, emballage", vit = "Rapide",
            hMax = 25.0, dMax = 50.0, ombre = "Intolérante",
            rem = "Synonyme courant du peuplier tremble ; colonisateur rapide des coupes rases"),

        f("SAULE_BLANC", "Saule blanc",
            db = 450.0, qua = "D", coupe = "Têtard, taillis",
            usage = "Vannerie, bois énergie, biomasse", vit = "Très rapide",
            hMax = 25.0, dMax = 60.0, ombre = "Intolérante",
            rem = "Bords de rivière ; conduit en têtard traditionnel ; intérêt apicole"),

        f("SAULE_FRAGILE", "Saule fragile",
            db = 440.0, qua = "D", coupe = "Taillis",
            usage = "Vannerie, bois énergie", vit = "Très rapide",
            hMax = 15.0, dMax = 40.0, ombre = "Intolérante",
            rem = "Rameaux cassants ; ripisylves ; multiplication végétative très facile"),

        f("SAULE_MARSAULT", "Saule marsault",
            db = 470.0, qua = "D", coupe = "Taillis",
            usage = "Chauffage, vannerie grossière", vit = "Rapide",
            hMax = 12.0, dMax = 30.0, ombre = "Intolérante",
            rem = "Essence pionnière mellifère ; premiers chatons au printemps ; petite taille"),

        f("NOISETIER", "Noisetier commun",
            db = 600.0, qua = "D", coupe = "Taillis",
            usage = "Vannerie, piquets, gaulettes", vit = "Rapide",
            hMax = 6.0, dMax = 15.0, ombre = "Tolérante",
            rem = "Arbuste de sous-bois ; production de noisettes ; importante biodiversité associée"),

        f("SORB_OISEL", "Sorbier des oiseleurs",
            db = 700.0, qua = "C-D", coupe = "Futaie irrégulière",
            usage = "Tournage, sculpture", vit = "Moyenne",
            hMax = 15.0, dMax = 30.0, ombre = "Intermédiaire",
            rem = "Essence montagnarde décorative ; baies rouges appréciées des oiseaux ; bois dur"),

        f("ALISIER_BLANC", "Alisier blanc",
            db = 720.0, qua = "B-C", coupe = "Futaie irrégulière",
            usage = "Tournage, marqueterie", vit = "Lente",
            hMax = 15.0, dMax = 40.0, ombre = "Intermédiaire",
            rem = "Essence frugale des coteaux calcaires ; bois rosé apprécié en tournage"),

        f("ALISIER_TORM", "Alisier torminal",
            db = 730.0, qua = "A-B", coupe = "Futaie irrégulière",
            usage = "Ébénisterie, placage, tournage", vit = "Lente",
            hMax = 20.0, dMax = 50.0, ombre = "Intermédiaire",
            rem = "Bois très recherché en placage ; rare en gros diamètre ; essence de complément précieuse"),

        f("POMMIER_SAUV", "Pommier sauvage",
            db = 700.0, qua = "C-D", coupe = "Arbre isolé",
            usage = "Tournage, sculpture", vit = "Lente",
            hMax = 10.0, dMax = 30.0, ombre = "Intermédiaire",
            rem = "Petit arbre de lisière ; biodiversité importante ; bois dur coloré"),

        f("POIRIER_SAUV", "Poirier sauvage",
            db = 710.0, qua = "B-C", coupe = "Arbre isolé",
            usage = "Tournage, gravure sur bois, ébénisterie", vit = "Lente",
            hMax = 15.0, dMax = 35.0, ombre = "Intermédiaire",
            rem = "Bois rosé très fin, se teinte bien en noir (imitation ébène) ; rare en forêt"),

        f("FUSAIN", "Fusain d'Europe",
            db = 650.0, qua = "D", coupe = "Taillis",
            usage = "Charbon à dessin, artisanat", vit = "Lente",
            hMax = 6.0, dMax = 10.0, ombre = "Tolérante",
            rem = "Arbuste de sous-bois ; carbonisé pour les crayons de fusain ; toxique"),

        f("HOUX", "Houx",
            db = 750.0, qua = "D", coupe = "Taillis",
            usage = "Marqueterie, tournage", vit = "Très lente",
            hMax = 10.0, dMax = 25.0, ombre = "Très tolérante",
            rem = "Sempervirent ; bois blanc très dur ; protégé dans certaines régions"),

        f("PLATANE", "Platane commun",
            db = 640.0, qua = "C-D", coupe = "Futaie",
            usage = "Placage, ameublement", vit = "Rapide",
            hMax = 35.0, dMax = 100.0, ombre = "Intermédiaire",
            rem = "Fréquent en alignement et parcs ; maille caractéristique recherchée ; sensible au chancre coloré"),

        f("MICOCOULIER", "Micocoulier de Provence",
            db = 730.0, qua = "C-D", coupe = "Taillis",
            usage = "Manches d'outils, fouets (fourches)", vit = "Moyenne",
            hMax = 15.0, dMax = 50.0, ombre = "Intermédiaire",
            rem = "Tradition de fabrication de fourches à Sauve (Gard) ; thermophile"),

        f("ACACIA_3EPINES", "Févier d'Amérique",
            db = 660.0, qua = "C-D", coupe = "Futaie",
            usage = "Piquets, bois énergie", vit = "Rapide",
            hMax = 20.0, dMax = 60.0, ombre = "Intolérante",
            rem = "Ornement et agroforesterie ; légumineuse fixatrice d'azote ; épines dangereuses"),

        f("MARRONNIER", "Marronnier d'Inde",
            db = 510.0, qua = "D", coupe = "Arbre isolé",
            usage = "Caisserie, bois tendre", vit = "Moyenne",
            hMax = 25.0, dMax = 80.0, ombre = "Intermédiaire",
            rem = "Essence ornementale ; bois de faible valeur ; sensible à la mineuse"),

        // ════════════════════════════════════════════════════
        // ── RÉSINEUX ──
        // ════════════════════════════════════════════════════

        f("PIN_SYLVESTRE", "Pin sylvestre", cat = "Résineux",
            db = 510.0, qua = "B-C", coupe = "Futaie régulière",
            usage = "Charpente, menuiserie, caisserie", vit = "Moyenne",
            hMax = 35.0, dMax = 70.0, ombre = "Intolérante",
            rem = "Essence pionnière ubiquiste ; aubier bleuissant à traiter ; qualité variable selon station"),

        f("PIN_MARITIME", "Pin maritime", cat = "Résineux",
            db = 520.0, qua = "B-C", coupe = "Futaie régulière",
            usage = "Charpente, papeterie, résine", vit = "Rapide",
            hMax = 30.0, dMax = 70.0, ombre = "Intolérante",
            rem = "Forêt landaise (1M ha) ; gemmage historique ; bois résineux noué ; sensible au feu"),

        f("PIN_NOIR_AUTR", "Pin noir d'Autriche", cat = "Résineux",
            db = 530.0, qua = "C-D", coupe = "Futaie régulière",
            usage = "Charpente, reboisement", vit = "Moyenne",
            hMax = 35.0, dMax = 70.0, ombre = "Intermédiaire",
            rem = "Très rustique ; sols calcaires et secs ; bois dense mais noueux"),

        f("PIN_LARICIO", "Pin laricio de Corse", cat = "Résineux",
            db = 540.0, qua = "A-B", coupe = "Futaie régulière",
            usage = "Charpente, menuiserie de qualité", vit = "Moyenne",
            hMax = 40.0, dMax = 80.0, ombre = "Intermédiaire",
            rem = "Fût très droit et élagué ; bois de qualité supérieure parmi les pins ; endémique Corse/Calabre"),

        f("PIN_WEYMOUTH", "Pin de Weymouth", cat = "Résineux",
            db = 400.0, qua = "C-D", coupe = "Futaie",
            usage = "Menuiserie légère, maquettisme", vit = "Rapide",
            hMax = 35.0, dMax = 70.0, ombre = "Intermédiaire",
            rem = "Bois très léger et tendre ; sensible à la rouille vésiculeuse"),

        f("PIN_ALEP", "Pin d'Alep", cat = "Résineux",
            db = 530.0, qua = "D", coupe = "Taillis, protection",
            usage = "Chauffage, papeterie, bois énergie", vit = "Lente",
            hMax = 20.0, dMax = 50.0, ombre = "Intolérante",
            rem = "Essence méditerranéenne par excellence ; très résistant à la sécheresse ; inflammable"),

        f("PIN_PIGNON", "Pin pignon (pin parasol)", cat = "Résineux",
            db = 500.0, qua = "C-D", coupe = "Arbre isolé",
            usage = "Pignons de pin, bois énergie", vit = "Lente",
            hMax = 20.0, dMax = 60.0, ombre = "Intolérante",
            rem = "Silhouette en parasol caractéristique ; pignons comestibles très valorisés"),

        f("PIN_CEMBRO", "Pin cembro (arolle)", cat = "Résineux",
            db = 470.0, qua = "B-C", coupe = "Arbre isolé, protection",
            usage = "Sculpture, ébénisterie alpine", vit = "Très lente",
            hMax = 20.0, dMax = 60.0, ombre = "Tolérante",
            rem = "Haute montagne (1700-2400 m) ; bois odorant facile à sculpter ; longévité > 1000 ans"),

        f("PIN_MUGO", "Pin à crochets / Pin mugo", cat = "Résineux",
            db = 510.0, qua = "D", coupe = "Protection",
            usage = "Protection, bois énergie", vit = "Très lente",
            hMax = 12.0, dMax = 30.0, ombre = "Intolérante",
            rem = "Arbuste ou petit arbre de haute montagne ; rôle anti-avalanche essentiel"),

        f("EPICEA_COMMUN", "Épicéa commun", cat = "Résineux",
            db = 430.0, qua = "A-B", coupe = "Futaie régulière",
            usage = "Charpente, lutherie, papeterie", vit = "Moyenne",
            hMax = 45.0, dMax = 80.0, ombre = "Tolérante",
            rem = "Bois de résonance pour lutherie ; sensible au bostryche et à la sécheresse ; enracinement superficiel"),

        f("EPICEA_SITKA", "Épicéa de Sitka", cat = "Résineux",
            db = 400.0, qua = "B-C", coupe = "Futaie régulière",
            usage = "Charpente, papeterie, lutherie", vit = "Rapide",
            hMax = 50.0, dMax = 100.0, ombre = "Tolérante",
            rem = "Introduit d'Amérique du Nord ; très productif en climat océanique ; bois léger"),

        f("SAPIN_PECTINE", "Sapin pectiné", cat = "Résineux",
            db = 440.0, qua = "A-B", coupe = "Futaie irrégulière",
            usage = "Charpente, menuiserie, coffrage", vit = "Moyenne",
            hMax = 50.0, dMax = 90.0, ombre = "Très tolérante",
            rem = "Essence d'ombre des sapinières ; fût très droit ; convient à la futaie jardinée"),

        f("SAPIN_NORDMANN", "Sapin de Nordmann", cat = "Résineux",
            db = 440.0, qua = "B-C", coupe = "Futaie régulière",
            usage = "Charpente, sapin de Noël", vit = "Moyenne",
            hMax = 45.0, dMax = 80.0, ombre = "Très tolérante",
            rem = "Introduit du Caucase ; très utilisé comme sapin de Noël ; robuste en plantation"),

        f("DOUGLAS_VERT", "Douglas vert", cat = "Résineux",
            db = 510.0, qua = "A-B", coupe = "Futaie régulière",
            usage = "Charpente, bardage, menuiserie extérieure", vit = "Rapide",
            hMax = 50.0, dMax = 100.0, ombre = "Intermédiaire",
            rem = "Essence de reboisement majeure ; bois durable naturellement (classe 3) ; très productif en France"),

        f("MEL_EUROPE", "Mélèze d'Europe", cat = "Résineux",
            db = 590.0, qua = "A-B", coupe = "Futaie régulière",
            usage = "Bardage, bardeaux, charpente extérieure", vit = "Rapide",
            hMax = 35.0, dMax = 70.0, ombre = "Intolérante",
            rem = "Seul résineux caducifolié d'Europe ; bois très durable naturellement ; haute montagne"),

        f("MEL_JAPON", "Mélèze du Japon", cat = "Résineux",
            db = 530.0, qua = "B-C", coupe = "Futaie régulière",
            usage = "Charpente, bardage", vit = "Rapide",
            hMax = 30.0, dMax = 60.0, ombre = "Intolérante",
            rem = "Introduit ; croissance rapide ; hybridation fréquente avec le mélèze d'Europe"),

        f("MEL_HYBRIDE", "Mélèze hybride", cat = "Résineux",
            db = 550.0, qua = "B-C", coupe = "Futaie régulière",
            usage = "Charpente, bardage", vit = "Rapide",
            hMax = 35.0, dMax = 65.0, ombre = "Intolérante",
            rem = "Croisement Europe × Japon ; vigueur hybride ; utilisé en reboisement productif"),

        f("CEDRE_ATLAS", "Cèdre de l'Atlas", cat = "Résineux",
            db = 580.0, qua = "A-B", coupe = "Futaie régulière",
            usage = "Charpente, menuiserie, ameublement", vit = "Moyenne",
            hMax = 40.0, dMax = 80.0, ombre = "Intermédiaire",
            rem = "Très résistant à la sécheresse ; essence d'avenir face au changement climatique ; bois parfumé"),

        f("CEDRE_LIBAN", "Cèdre du Liban", cat = "Résineux",
            db = 560.0, qua = "B-C", coupe = "Futaie",
            usage = "Charpente, menuiserie", vit = "Lente",
            hMax = 35.0, dMax = 80.0, ombre = "Intermédiaire",
            rem = "Majestueux ; plantations ornementales historiques ; bois similaire au cèdre de l'Atlas"),

        f("THUYA_GEANT", "Thuya géant", cat = "Résineux",
            db = 370.0, qua = "B-C", coupe = "Futaie",
            usage = "Bardage, bardeaux, clôture", vit = "Moyenne",
            hMax = 40.0, dMax = 80.0, ombre = "Très tolérante",
            rem = "Originaire Amérique du Nord ; bois extrêmement durable et léger ; thuyone aromatique"),

        f("CYPRES_PROVENCE", "Cyprès de Provence", cat = "Résineux",
            db = 510.0, qua = "C-D", coupe = "Arbre isolé",
            usage = "Bois énergie, menuiserie", vit = "Moyenne",
            hMax = 20.0, dMax = 40.0, ombre = "Intolérante",
            rem = "Paysage méditerranéen ; forme colonnaire ; bois durable résistant aux champignons"),

        f("SEQUOIA_TOUJOURS_VERT", "Séquoia sempervirens", cat = "Résineux",
            db = 420.0, qua = "B-C", coupe = "Futaie",
            usage = "Bardage, charpente", vit = "Très rapide",
            hMax = 60.0, dMax = 200.0, ombre = "Tolérante",
            rem = "Croissance exceptionnelle ; bois léger et naturellement durable ; acclimaté en Bretagne et Sud-Ouest"),

        f("SAPIN_GRANDIS", "Sapin de Vancouver (grandis)", cat = "Résineux",
            db = 430.0, qua = "B-C", coupe = "Futaie régulière",
            usage = "Charpente, menuiserie, coffrage, papeterie", vit = "Très rapide",
            hMax = 55.0, dMax = 100.0, ombre = "Très tolérante",
            rem = "Abies grandis ; croissance parmi les plus rapides des résineux (15-20 m³/ha/an) ; bois léger et blanc ; planté en Bretagne, Normandie, Massif central"),

        f("SAPIN_CEPHALONIE", "Sapin de Céphalonie", cat = "Résineux",
            db = 460.0, qua = "B-C", coupe = "Futaie régulière",
            usage = "Charpente, menuiserie", vit = "Moyenne",
            hMax = 30.0, dMax = 70.0, ombre = "Intermédiaire",
            rem = "Abies cephalonica ; très résistant à la sécheresse ; planté en région méditerranéenne comme essence d'avenir"),

        f("SAPIN_ESPAGNE", "Sapin d'Espagne (pinsapo)", cat = "Résineux",
            db = 450.0, qua = "C-D", coupe = "Protection",
            usage = "Reboisement, ornement", vit = "Lente",
            hMax = 25.0, dMax = 60.0, ombre = "Tolérante",
            rem = "Abies pinsapo ; aiguilles raides en brosse ; planté ponctuellement dans le Midi"),

        f("PIN_SALZMANN", "Pin de Salzmann", cat = "Résineux",
            db = 540.0, qua = "B-C", coupe = "Futaie régulière",
            usage = "Charpente, menuiserie", vit = "Lente",
            hMax = 30.0, dMax = 70.0, ombre = "Intolérante",
            rem = "Sous-espèce du pin noir ; endémique Cévennes et Pyrénées orientales ; très résistant à la sécheresse"),

        f("PIN_MONTEREY", "Pin de Monterey (radiata)", cat = "Résineux",
            db = 480.0, qua = "C-D", coupe = "Futaie régulière",
            usage = "Papeterie, caisserie, panneaux", vit = "Très rapide",
            hMax = 30.0, dMax = 70.0, ombre = "Intolérante",
            rem = "Pinus radiata ; planté en Pays basque et Landes ; croissance très rapide mais sensible au gel"),

        f("CRYPTOMERE", "Cryptomère du Japon", cat = "Résineux",
            db = 380.0, qua = "C-D", coupe = "Futaie",
            usage = "Bardage, coffrage, bois léger", vit = "Rapide",
            hMax = 35.0, dMax = 70.0, ombre = "Tolérante",
            rem = "Cryptomeria japonica ; planté en Bretagne et Pays basque ; bois très léger et aromatique"),

        f("CYPRES_CHAUVE", "Cyprès chauve", cat = "Résineux",
            db = 460.0, qua = "B-C", coupe = "Futaie",
            usage = "Charpente, bardage extérieur", vit = "Moyenne",
            hMax = 35.0, dMax = 80.0, ombre = "Intolérante",
            rem = "Taxodium distichum ; conifère caduc tolérant l'engorgement ; pneumatophores ; planté zones humides"),

        f("TSUGA_HETEROPHYLLE", "Tsuga de l'Ouest", cat = "Résineux",
            db = 450.0, qua = "B-C", coupe = "Futaie",
            usage = "Charpente, menuiserie, papeterie", vit = "Moyenne",
            hMax = 45.0, dMax = 80.0, ombre = "Très tolérante",
            rem = "Tsuga heterophylla ; planté en Bretagne et Normandie ; bois fin ; tolère très bien l'ombre"),

        f("EPICEA_OMORIKA", "Épicéa de Serbie", cat = "Résineux",
            db = 430.0, qua = "C-D", coupe = "Futaie",
            usage = "Reboisement, ornement", vit = "Moyenne",
            hMax = 30.0, dMax = 50.0, ombre = "Tolérante",
            rem = "Picea omorika ; silhouette étroite ; tolérant sols calcaires ; planté en ornement et reboisement"),

        // ════════════════════════════════════════════════════
        // ── FEUILLUS COMPLÉMENTAIRES ──
        // ════════════════════════════════════════════════════

        f("CH_TAUZIN", "Chêne tauzin",
            db = 730.0, qua = "C-D", coupe = "Taillis",
            usage = "Chauffage, charbon", vit = "Lente",
            hMax = 15.0, dMax = 50.0, ombre = "Intolérante",
            rem = "Endémique du Sud-Ouest ; feuilles très découpées et pubescentes ; drageonneur ; sols acides"),

        f("CH_KERMES", "Chêne kermès",
            db = 900.0, qua = "D", coupe = "Taillis",
            usage = "Chauffage", vit = "Très lente",
            hMax = 3.0, dMax = 15.0, ombre = "Intolérante",
            rem = "Arbuste de garrigue méditerranéenne ; sempervirent épineux ; colonisateur après incendie"),

        f("ERABLE_MONTPELLIER", "Érable de Montpellier",
            db = 670.0, qua = "C-D", coupe = "Taillis",
            usage = "Tournage, chauffage", vit = "Lente",
            hMax = 10.0, dMax = 30.0, ombre = "Intermédiaire",
            rem = "Essence méditerranéenne thermophile ; petites feuilles trilobées ; sols calcaires secs"),

        f("ERABLE_OBIER", "Érable à feuilles d'obier",
            db = 640.0, qua = "D", coupe = "Taillis",
            usage = "Chauffage, artisanat", vit = "Lente",
            hMax = 10.0, dMax = 25.0, ombre = "Tolérante",
            rem = "Montagne méditerranéenne ; fleurs jaunes précoces ; intérêt paysager et mellifère"),

        f("FRENE_FLEURS", "Frêne à fleurs (orne)",
            db = 700.0, qua = "C-D", coupe = "Taillis",
            usage = "Chauffage, manne (résine sucrée)", vit = "Lente",
            hMax = 12.0, dMax = 30.0, ombre = "Intolérante",
            rem = "Fraxinus ornus ; abondante floraison blanche ; garrigues et chênaies du Midi ; production de manne en Sicile"),

        f("AULNE_CORSE", "Aulne de Corse",
            db = 500.0, qua = "C-D", coupe = "Taillis, futaie",
            usage = "Bois énergie, reboisement", vit = "Rapide",
            hMax = 20.0, dMax = 50.0, ombre = "Intermédiaire",
            rem = "Alnus cordata ; fixateur d'azote ; feuilles cordiformes ; utilisé en reboisement partout en France"),

        f("TULIPIER", "Tulipier de Virginie",
            db = 510.0, qua = "B-C", coupe = "Futaie",
            usage = "Déroulage, menuiserie légère, tournage", vit = "Rapide",
            hMax = 35.0, dMax = 80.0, ombre = "Intermédiaire",
            rem = "Liriodendron tulipifera ; feuilles à forme unique ; bois blanc-verdâtre léger ; parcs et forêts"),

        f("EUCALYPTUS_GUNNII", "Eucalyptus de Gunn",
            db = 650.0, qua = "C-D", coupe = "Taillis à très courte rotation",
            usage = "Papeterie, bois énergie, biomasse", vit = "Très rapide",
            hMax = 25.0, dMax = 60.0, ombre = "Intolérante",
            rem = "Le plus rustique des eucalyptus (-18°C) ; planté en Bretagne et Sud-Ouest ; TCR pour biomasse"),

        f("EUCALYPTUS_GLOBULUS", "Eucalyptus globuleux",
            db = 700.0, qua = "C-D", coupe = "Taillis à courte rotation",
            usage = "Papeterie, huile essentielle", vit = "Très rapide",
            hMax = 30.0, dMax = 80.0, ombre = "Intolérante",
            rem = "Sensible au gel ; uniquement en climat doux (Côte basque, Corse) ; bois dur et dense"),

        f("CORNOUILLER_MALE", "Cornouiller mâle",
            db = 900.0, qua = "D", coupe = "Taillis",
            usage = "Manches d'outils, engrenages, tournage", vit = "Très lente",
            hMax = 8.0, dMax = 20.0, ombre = "Intermédiaire",
            rem = "Bois extrêmement dur et lourd ; floraison jaune très précoce (février) ; fruits comestibles (cornouilles)"),

        f("CORNOUILLER_SANG", "Cornouiller sanguin",
            db = 700.0, qua = "D", coupe = "Taillis",
            usage = "Vannerie, chauffage", vit = "Rapide",
            hMax = 5.0, dMax = 10.0, ombre = "Tolérante",
            rem = "Arbuste des haies et lisières ; rameaux rouges en hiver ; biodiversité associée importante"),

        f("SUREAU_NOIR", "Sureau noir",
            db = 600.0, qua = "D", coupe = "Taillis",
            usage = "Artisanat, vannerie", vit = "Très rapide",
            hMax = 6.0, dMax = 15.0, ombre = "Intermédiaire",
            rem = "Arbuste nitrophile des haies ; fleurs et baies comestibles ; colonisateur rapide ; bois à moelle"),

        f("AUBEPINE_MONOGYNE", "Aubépine monogyne",
            db = 800.0, qua = "D", coupe = "Taillis",
            usage = "Chauffage, manches d'outils", vit = "Très lente",
            hMax = 8.0, dMax = 20.0, ombre = "Intermédiaire",
            rem = "Arbuste épineux des haies ; biodiversité très riche ; bois très dur ; longévité remarquable"),

        f("PRUNELLIER", "Prunellier (épine noire)",
            db = 750.0, qua = "D", coupe = "Taillis",
            usage = "Chauffage, cannes, prunelles", vit = "Lente",
            hMax = 4.0, dMax = 10.0, ombre = "Intolérante",
            rem = "Arbuste de haie très épineux ; fleurs blanches précoces ; prunelles utilisées en liqueur"),

        f("BUIS", "Buis commun",
            db = 920.0, qua = "C-D", coupe = "Taillis",
            usage = "Gravure, tournage, lutherie, boules", vit = "Très lente",
            hMax = 5.0, dMax = 15.0, ombre = "Très tolérante",
            rem = "Bois le plus dense d'Europe (coule dans l'eau) ; menacé par la pyrale du buis ; sous-bois calcaires"),

        f("TROENE", "Troène commun",
            db = 720.0, qua = "D", coupe = "Taillis",
            usage = "Artisanat, haies", vit = "Moyenne",
            hMax = 5.0, dMax = 10.0, ombre = "Tolérante",
            rem = "Semi-persistant ; abondant en lisière calcaire ; baies toxiques ; intérêt pour pollinisateurs"),

        f("VIORNE_LANTANE", "Viorne lantane",
            db = 680.0, qua = "D", coupe = "Taillis",
            usage = "Vannerie, glu (écorce)", vit = "Lente",
            hMax = 4.0, dMax = 8.0, ombre = "Intermédiaire",
            rem = "Arbuste des lisières calcaires ; feuilles gaufrées ; baies noir-rouge appréciées des oiseaux"),

        f("VIORNE_OBIER", "Viorne obier",
            db = 680.0, qua = "D", coupe = "Taillis",
            usage = "Vannerie", vit = "Moyenne",
            hMax = 4.0, dMax = 8.0, ombre = "Tolérante",
            rem = "Arbuste des milieux humides ; fleurs blanches en plateau ; baies rouges translucides"),

        f("GENETS_SCORPION", "Genêt à balais",
            db = 600.0, qua = "D", coupe = "Taillis",
            usage = "Fagots, fixation de sols", vit = "Rapide",
            hMax = 3.0, dMax = 5.0, ombre = "Intolérante",
            rem = "Arbuste des landes acides ; colonisateur de friches ; fixateur d'azote ; très inflammable"),

        f("MURIER_BLANC", "Mûrier blanc",
            db = 640.0, qua = "C-D", coupe = "Têtard",
            usage = "Menuiserie, tonnellerie", vit = "Rapide",
            hMax = 15.0, dMax = 50.0, ombre = "Intolérante",
            rem = "Historiquement cultivé pour l'élevage du ver à soie (magnaneries) ; Drôme, Ardèche, Cévennes"),

        f("FIGUIER", "Figuier commun",
            db = 420.0, qua = "D", coupe = "Arbre isolé",
            usage = "Fruits, bois léger", vit = "Rapide",
            hMax = 10.0, dMax = 40.0, ombre = "Intolérante",
            rem = "Spontané dans le Midi ; bois très léger, peu utilisé ; latex irritant ; grande biodiversité"),

        f("ARBRE_JUDEE", "Arbre de Judée",
            db = 720.0, qua = "D", coupe = "Arbre isolé",
            usage = "Ornement, artisanat", vit = "Moyenne",
            hMax = 10.0, dMax = 30.0, ombre = "Intolérante",
            rem = "Cercis siliquastrum ; floraison rose sur le tronc (cauliflorie) ; spontané en garrigue"),

        f("OLIVIER", "Olivier",
            db = 850.0, qua = "B-C", coupe = "Arbre isolé",
            usage = "Tournage, ébénisterie, sculpture", vit = "Très lente",
            hMax = 12.0, dMax = 60.0, ombre = "Intolérante",
            rem = "Bois magnifique veiné ; longévité millénaire ; spontané en Provence ; gel limite sa distribution"),

        f("SORBIER_DOMESTIQUE", "Sorbier domestique",
            db = 750.0, qua = "B-C", coupe = "Arbre isolé",
            usage = "Tournage, ébénisterie, mécanique", vit = "Lente",
            hMax = 15.0, dMax = 40.0, ombre = "Intermédiaire",
            rem = "Proche du cormier ; fruits (cormes) utilisés pour boisson fermentée ; bois très dur"),

        // ── Conifères divers ──
        f("GENEVRIER", "Genévrier commun", cat = "Conifère",
            db = 550.0, qua = "D", coupe = "Taillis",
            usage = "Artisanat, aromate (baies)", vit = "Très lente",
            hMax = 6.0, dMax = 15.0, ombre = "Intolérante",
            rem = "Arbuste épineux des landes et pelouses ; baies utilisées en cuisine ; intérêt écologique"),

        f("GENEVRIER_CADE", "Genévrier oxycèdre (cade)", cat = "Conifère",
            db = 580.0, qua = "D", coupe = "Taillis",
            usage = "Huile de cade, artisanat", vit = "Très lente",
            hMax = 8.0, dMax = 20.0, ombre = "Intolérante",
            rem = "Garrigue méditerranéenne ; huile de cade utilisée en dermatologie ; bois odorant"),

        f("GENEVRIER_PHENICIE", "Genévrier de Phénicie", cat = "Conifère",
            db = 570.0, qua = "D", coupe = "Protection",
            usage = "Protection littorale", vit = "Très lente",
            hMax = 6.0, dMax = 15.0, ombre = "Intolérante",
            rem = "Littoral méditerranéen ; dunes et falaises ; protégé dans plusieurs départements"),

        f("IF", "If commun", cat = "Conifère",
            db = 670.0, qua = "B-C", coupe = "Arbre isolé",
            usage = "Tournage, ébénisterie, archerie", vit = "Très lente",
            hMax = 15.0, dMax = 50.0, ombre = "Très tolérante",
            rem = "Longévité millénaire ; bois d'if utilisé pour les arcs médiévaux ; toxique (taxine) ; protégé")
    )
}
