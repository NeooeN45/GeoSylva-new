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

    // Prix par essence × produit × plage diamètre (€/m³ bord de route, qualité C).
    // Sources : ONF adjudications 2023-24, FBF observatoire, DRAAF mercuriales.
    // L'utilisateur peut charger des préréglages régionaux ou modifier ces prix.
    val prixMarcheJson: String = """
        [
          {"essence":"DOUGLAS_VERT","product":"BO","min":25,"max":34,"eurPerM3":80.0},
          {"essence":"DOUGLAS_VERT","product":"BO","min":35,"max":44,"eurPerM3":95.0},
          {"essence":"DOUGLAS_VERT","product":"BO","min":45,"max":999,"eurPerM3":120.0},
          {"essence":"DOUGLAS_VERT","product":"BI","min":15,"max":34,"eurPerM3":28.0},
          {"essence":"DOUGLAS_VERT","product":"BCh","min":0,"max":999,"eurPerM3":18.0},
          {"essence":"DOUGLAS_VERT","product":"PATE","min":0,"max":999,"eurPerM3":14.0},

          {"essence":"CH_SESSILE","product":"BO","min":30,"max":39,"eurPerM3":130.0},
          {"essence":"CH_SESSILE","product":"BO","min":40,"max":49,"eurPerM3":185.0},
          {"essence":"CH_SESSILE","product":"BO","min":50,"max":999,"eurPerM3":260.0},
          {"essence":"CH_SESSILE","product":"BI","min":15,"max":999,"eurPerM3":30.0},
          {"essence":"CH_SESSILE","product":"BCh","min":0,"max":999,"eurPerM3":38.0},

          {"essence":"CH_PEDONCULE","product":"BO","min":30,"max":39,"eurPerM3":115.0},
          {"essence":"CH_PEDONCULE","product":"BO","min":40,"max":49,"eurPerM3":165.0},
          {"essence":"CH_PEDONCULE","product":"BO","min":50,"max":999,"eurPerM3":230.0},
          {"essence":"CH_PEDONCULE","product":"BI","min":15,"max":999,"eurPerM3":28.0},
          {"essence":"CH_PEDONCULE","product":"BCh","min":0,"max":999,"eurPerM3":36.0},

          {"essence":"HETRE_COMMUN","product":"BO","min":30,"max":39,"eurPerM3":55.0},
          {"essence":"HETRE_COMMUN","product":"BO","min":40,"max":49,"eurPerM3":72.0},
          {"essence":"HETRE_COMMUN","product":"BO","min":50,"max":999,"eurPerM3":95.0},
          {"essence":"HETRE_COMMUN","product":"BI","min":15,"max":999,"eurPerM3":22.0},
          {"essence":"HETRE_COMMUN","product":"BCh","min":0,"max":999,"eurPerM3":30.0},
          {"essence":"HETRE_COMMUN","product":"PATE","min":0,"max":999,"eurPerM3":15.0},

          {"essence":"SAPIN_PECTINE","product":"BO","min":25,"max":34,"eurPerM3":65.0},
          {"essence":"SAPIN_PECTINE","product":"BO","min":35,"max":44,"eurPerM3":80.0},
          {"essence":"SAPIN_PECTINE","product":"BO","min":45,"max":999,"eurPerM3":100.0},
          {"essence":"SAPIN_PECTINE","product":"BI","min":15,"max":999,"eurPerM3":24.0},
          {"essence":"SAPIN_PECTINE","product":"BCh","min":0,"max":999,"eurPerM3":16.0},
          {"essence":"SAPIN_PECTINE","product":"PATE","min":0,"max":999,"eurPerM3":12.0},

          {"essence":"EPICEA_COMMUN","product":"BO","min":25,"max":34,"eurPerM3":60.0},
          {"essence":"EPICEA_COMMUN","product":"BO","min":35,"max":44,"eurPerM3":75.0},
          {"essence":"EPICEA_COMMUN","product":"BO","min":45,"max":999,"eurPerM3":95.0},
          {"essence":"EPICEA_COMMUN","product":"BI","min":15,"max":999,"eurPerM3":22.0},
          {"essence":"EPICEA_COMMUN","product":"BCh","min":0,"max":999,"eurPerM3":15.0},
          {"essence":"EPICEA_COMMUN","product":"PATE","min":0,"max":999,"eurPerM3":12.0},

          {"essence":"MEL_EUROPE","product":"BO","min":25,"max":34,"eurPerM3":75.0},
          {"essence":"MEL_EUROPE","product":"BO","min":35,"max":44,"eurPerM3":90.0},
          {"essence":"MEL_EUROPE","product":"BO","min":45,"max":999,"eurPerM3":115.0},
          {"essence":"MEL_EUROPE","product":"BI","min":15,"max":999,"eurPerM3":26.0},
          {"essence":"MEL_EUROPE","product":"BCh","min":0,"max":999,"eurPerM3":18.0},

          {"essence":"PIN_SYLVESTRE","product":"BO","min":25,"max":34,"eurPerM3":40.0},
          {"essence":"PIN_SYLVESTRE","product":"BO","min":35,"max":999,"eurPerM3":55.0},
          {"essence":"PIN_SYLVESTRE","product":"BI","min":15,"max":999,"eurPerM3":20.0},
          {"essence":"PIN_SYLVESTRE","product":"BCh","min":0,"max":999,"eurPerM3":14.0},
          {"essence":"PIN_SYLVESTRE","product":"PATE","min":0,"max":999,"eurPerM3":10.0},

          {"essence":"PIN_MARITIME","product":"BO","min":25,"max":34,"eurPerM3":38.0},
          {"essence":"PIN_MARITIME","product":"BO","min":35,"max":999,"eurPerM3":50.0},
          {"essence":"PIN_MARITIME","product":"BI","min":15,"max":999,"eurPerM3":18.0},
          {"essence":"PIN_MARITIME","product":"PATE","min":0,"max":999,"eurPerM3":10.0},

          {"essence":"PIN_NOIR_AUTR","product":"BO","min":25,"max":34,"eurPerM3":40.0},
          {"essence":"PIN_NOIR_AUTR","product":"BO","min":35,"max":999,"eurPerM3":55.0},
          {"essence":"PIN_NOIR_AUTR","product":"BI","min":15,"max":999,"eurPerM3":20.0},
          {"essence":"PIN_NOIR_AUTR","product":"PATE","min":0,"max":999,"eurPerM3":10.0},

          {"essence":"PIN_LARICIO","product":"BO","min":25,"max":34,"eurPerM3":50.0},
          {"essence":"PIN_LARICIO","product":"BO","min":35,"max":999,"eurPerM3":65.0},
          {"essence":"PIN_LARICIO","product":"BI","min":15,"max":999,"eurPerM3":22.0},

          {"essence":"FRENE_ELEVE","product":"BO","min":30,"max":39,"eurPerM3":80.0},
          {"essence":"FRENE_ELEVE","product":"BO","min":40,"max":999,"eurPerM3":130.0},
          {"essence":"FRENE_ELEVE","product":"BI","min":15,"max":999,"eurPerM3":25.0},
          {"essence":"FRENE_ELEVE","product":"BCh","min":0,"max":999,"eurPerM3":32.0},

          {"essence":"ERABLE_SYC","product":"BO","min":30,"max":39,"eurPerM3":85.0},
          {"essence":"ERABLE_SYC","product":"BO","min":40,"max":999,"eurPerM3":140.0},
          {"essence":"ERABLE_SYC","product":"BI","min":15,"max":999,"eurPerM3":25.0},

          {"essence":"NOYER_COMMUN","product":"BO","min":30,"max":39,"eurPerM3":200.0},
          {"essence":"NOYER_COMMUN","product":"BO","min":40,"max":999,"eurPerM3":350.0},
          {"essence":"NOYER_COMMUN","product":"BI","min":15,"max":999,"eurPerM3":60.0},

          {"essence":"CERISIER_MERIS","product":"BO","min":30,"max":39,"eurPerM3":120.0},
          {"essence":"CERISIER_MERIS","product":"BO","min":40,"max":999,"eurPerM3":180.0},
          {"essence":"CERISIER_MERIS","product":"BI","min":15,"max":999,"eurPerM3":35.0},

          {"essence":"CHATAIGNIER","product":"BO","min":25,"max":34,"eurPerM3":65.0},
          {"essence":"CHATAIGNIER","product":"BO","min":35,"max":999,"eurPerM3":95.0},
          {"essence":"CHATAIGNIER","product":"BI","min":15,"max":999,"eurPerM3":25.0},
          {"essence":"CHATAIGNIER","product":"BCh","min":0,"max":999,"eurPerM3":30.0},

          {"essence":"ROBINIER","product":"BO","min":25,"max":34,"eurPerM3":80.0},
          {"essence":"ROBINIER","product":"BO","min":35,"max":999,"eurPerM3":120.0},
          {"essence":"ROBINIER","product":"BI","min":15,"max":999,"eurPerM3":30.0},

          {"essence":"CHARME","product":"BCh","min":0,"max":999,"eurPerM3":30.0},
          {"essence":"CHARME","product":"BI","min":15,"max":999,"eurPerM3":20.0},

          {"essence":"PEUPLIER_HYBR","product":"BO","min":30,"max":999,"eurPerM3":55.0},
          {"essence":"PEUPLIER_HYBR","product":"BI","min":15,"max":999,"eurPerM3":22.0},
          {"essence":"PEUPLIER_HYBR","product":"PATE","min":0,"max":999,"eurPerM3":14.0},

          {"essence":"SAPIN_GRANDIS","product":"BO","min":25,"max":34,"eurPerM3":60.0},
          {"essence":"SAPIN_GRANDIS","product":"BO","min":35,"max":999,"eurPerM3":80.0},
          {"essence":"SAPIN_GRANDIS","product":"BI","min":15,"max":999,"eurPerM3":22.0},

          {"essence":"CEDRE_ATLAS","product":"BO","min":30,"max":999,"eurPerM3":75.0},
          {"essence":"CEDRE_ATLAS","product":"BI","min":15,"max":999,"eurPerM3":22.0},

          {"essence":"MEL_HYBRIDE","product":"BO","min":25,"max":34,"eurPerM3":70.0},
          {"essence":"MEL_HYBRIDE","product":"BO","min":35,"max":999,"eurPerM3":85.0},
          {"essence":"MEL_HYBRIDE","product":"BI","min":15,"max":999,"eurPerM3":24.0},

          {"essence":"*","product":"BO","min":25,"max":34,"eurPerM3":50.0},
          {"essence":"*","product":"BO","min":35,"max":44,"eurPerM3":65.0},
          {"essence":"*","product":"BO","min":45,"max":999,"eurPerM3":80.0},
          {"essence":"*","product":"BI","min":15,"max":999,"eurPerM3":22.0},
          {"essence":"*","product":"BCh","min":0,"max":999,"eurPerM3":28.0},
          {"essence":"*","product":"PATE","min":0,"max":999,"eurPerM3":12.0}
        ]
    """.trimIndent()
}
