package com.forestry.counter.data.sync

import com.forestry.counter.data.local.entity.ParcelleEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ParcelSyncMapperTest {
    @Test
    fun `la projection reseau conserve les champs cadastraux et forestiers`() {
        val entity = ParcelleEntity(
            parcelleId = "p-1",
            forestOwnerId = "g-1",
            foretId = "f-1",
            name = "Bois du test",
            surfaceHa = 12.5,
            shape = "IRREGULIERE",
            slopePct = 18.0,
            aspect = "N",
            access = "PISTE",
            altitudeM = 420.0,
            objectifType = "VOLUME",
            objectifVal = 250.0,
            tolerancePct = 5.0,
            samplingMode = "CIRCULAR",
            sampleAreaM2 = 2_000.0,
            targetSpeciesCsv = "CHS,HET",
            srid = 4326,
            remarks = "Terrain humide",
            codeInseeCommune = "33063",
            nomCommune = "Bordeaux",
            sectionCadastrale = "AB",
            numeroCadastral = "42",
            contenanceCadastraleHa = 12.4,
            geometrieIgnWkt = "POLYGON EMPTY",
            natureCadastraleCode = "BT",
            localisationMode = "IGN",
            codeSer = "F21",
            nomSer = "Landes",
            createdAt = 100L,
            updatedAt = 200L,
        )

        val payload = entity.toSyncPayload()

        assertEquals("Bois du test", payload.name)
        assertEquals("33063", payload.municipalityCode)
        assertEquals("POLYGON EMPTY", payload.ignGeometryWkt)
        assertEquals(200L, payload.updatedAtMs)
        assertEquals("F21", payload.serCode)
    }
}
