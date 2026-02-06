package com.forestry.counter.data.parameters

object ParameterDefaults {
    val classesDiametreJson: String = """
        [5,10,15,20,25,30,35,40,45,50,55,60,65,70,75,80]
    """.trimIndent()

    val coefsVolumeJson: String = """
        [
          {"essence":"HETRE","min":15,"max":25,"f":0.46},
          {"essence":"HETRE","min":30,"max":40,"f":0.48},
          {"essence":"HETRE","min":45,"max":60,"f":0.50},
          {"essence":"DOUGLAS","min":20,"max":30,"f":0.44},
          {"essence":"DOUGLAS","min":35,"max":50,"f":0.46},
          {"essence":"DOUGLAS","min":55,"max":70,"f":0.48}
        ]
    """.trimIndent()

    val hauteursDefautJson: String = """
        [
          {"essence":"HETRE","min":15,"max":25,"h":14.0},
          {"essence":"HETRE","min":30,"max":40,"h":18.0},
          {"essence":"HETRE","min":45,"max":60,"h":22.0},
          {"essence":"DOUGLAS","min":20,"max":30,"h":18.0},
          {"essence":"DOUGLAS","min":35,"max":50,"h":24.0},
          {"essence":"DOUGLAS","min":55,"max":70,"h":28.0}
        ]
    """.trimIndent()

    val reglesProduitsJson: String = """
        [
          {"essence":"*","min":35,"product":"BO"},
          {"essence":"*","min":20,"product":"BI"},
          {"essence":"*","min":7,"product":"BCh"},
          {"essence":"*","max":6,"product":"PATE"}
        ]
    """.trimIndent()

    val prixMarcheJson: String = """
        [
          {"essence":"CH_SESSILE","product":"*","min":0,"max":999,"eurPerM3":150.0},
          {"essence":"CH_PEDONCULE","product":"*","min":0,"max":999,"eurPerM3":140.0},
          {"essence":"HETRE_COMMUN","product":"*","min":0,"max":999,"eurPerM3":72.0},
          {"essence":"CHARME","product":"*","min":0,"max":999,"eurPerM3":35.0},
          {"essence":"CHATAIGNIER","product":"*","min":0,"max":999,"eurPerM3":85.0},
          {"essence":"ROBINIER","product":"*","min":0,"max":999,"eurPerM3":100.0},
          {"essence":"FRENE_ELEVE","product":"*","min":0,"max":999,"eurPerM3":95.0},
          {"essence":"ERABLE_SYC","product":"*","min":0,"max":999,"eurPerM3":88.0},
          {"essence":"ERABLE_PLANE","product":"*","min":0,"max":999,"eurPerM3":80.0},
          {"essence":"ERABLE_CHAMP","product":"*","min":0,"max":999,"eurPerM3":55.0},
          {"essence":"BOUL_VERRUQ","product":"*","min":0,"max":999,"eurPerM3":42.0},
          {"essence":"BOUL_PUBESC","product":"*","min":0,"max":999,"eurPerM3":38.0},
          {"essence":"AULNE_GLUT","product":"*","min":0,"max":999,"eurPerM3":50.0},
          {"essence":"AULNE_BLANC","product":"*","min":0,"max":999,"eurPerM3":45.0},
          {"essence":"TIL_PET_FEUIL","product":"*","min":0,"max":999,"eurPerM3":55.0},
          {"essence":"TIL_GR_FEUIL","product":"*","min":0,"max":999,"eurPerM3":55.0},
          {"essence":"ORME_CHAMP","product":"*","min":0,"max":999,"eurPerM3":55.0},
          {"essence":"ORME_LISSE","product":"*","min":0,"max":999,"eurPerM3":60.0},
          {"essence":"ORME_MONT","product":"*","min":0,"max":999,"eurPerM3":62.0},
          {"essence":"SAULE_BLANC","product":"*","min":0,"max":999,"eurPerM3":28.0},
          {"essence":"SAULE_FRAGILE","product":"*","min":0,"max":999,"eurPerM3":28.0},
          {"essence":"SAULE_MARSAULT","product":"*","min":0,"max":999,"eurPerM3":22.0},
          {"essence":"ALISIER_BLANC","product":"*","min":0,"max":999,"eurPerM3":110.0},
          {"essence":"ALISIER_TORMINAL","product":"*","min":0,"max":999,"eurPerM3":170.0},
          {"essence":"SORB_OISEL","product":"*","min":0,"max":999,"eurPerM3":70.0},
          {"essence":"CORMIER","product":"*","min":0,"max":999,"eurPerM3":225.0},
          {"essence":"CERISIER_MERIS","product":"*","min":0,"max":999,"eurPerM3":135.0},
          {"essence":"POMMIER_SAUV","product":"*","min":0,"max":999,"eurPerM3":85.0},
          {"essence":"POIRIER_SAUV","product":"*","min":0,"max":999,"eurPerM3":115.0},
          {"essence":"NOYER_COMMUN","product":"*","min":0,"max":999,"eurPerM3":225.0},
          {"essence":"NOYER_NOIR","product":"*","min":0,"max":999,"eurPerM3":265.0},
          {"essence":"NOISETIER","product":"*","min":0,"max":999,"eurPerM3":28.0},
          {"essence":"PEUPLIER_TREMB","product":"*","min":0,"max":999,"eurPerM3":35.0},
          {"essence":"PEUPLIER_NOIR","product":"*","min":0,"max":999,"eurPerM3":42.0},
          {"essence":"PEUPLIER_HYBR","product":"*","min":0,"max":999,"eurPerM3":50.0},

          {"essence":"PIN_SYLVESTRE","product":"*","min":0,"max":999,"eurPerM3":45.0},
          {"essence":"PIN_MARITIME","product":"*","min":0,"max":999,"eurPerM3":40.0},
          {"essence":"PIN_NOIR_AUTR","product":"*","min":0,"max":999,"eurPerM3":45.0},
          {"essence":"PIN_LARICIO","product":"*","min":0,"max":999,"eurPerM3":52.0},
          {"essence":"PIN_WEYMOUTH","product":"*","min":0,"max":999,"eurPerM3":40.0},
          {"essence":"EPICEA_COMMUN","product":"*","min":0,"max":999,"eurPerM3":48.0},
          {"essence":"SAPIN_PECTINE","product":"*","min":0,"max":999,"eurPerM3":52.0},
          {"essence":"DOUGLAS_VERT","product":"*","min":0,"max":999,"eurPerM3":70.0},
          {"essence":"MEL_EUROPE","product":"*","min":0,"max":999,"eurPerM3":58.0},
          {"essence":"MEL_HYBRIDE","product":"*","min":0,"max":999,"eurPerM3":62.0}
        ]
    """.trimIndent()
}
