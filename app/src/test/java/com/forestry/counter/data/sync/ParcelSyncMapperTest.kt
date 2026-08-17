package com.forestry.counter.data.sync

import com.forestry.counter.data.local.entity.ParcelleEntity
import com.forestry.counter.data.local.entity.ProvenanceEmbed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

    // --- toParcelleEntity (sens inverse, GEOSYLVA P0-3, pull) ---

    private fun responseDto(
        clientId: String = "p-1",
        serverVersion: Int = 3,
        parcel: ParcelSyncPayloadDto? = ParcelSyncPayloadDto(
            name = "Bois du pull",
            municipalityCode = "33063",
            createdAtMs = 100L,
            updatedAtMs = 200L,
        ),
    ) = ParcelSyncResponseDto(
        clientId = clientId,
        status = if (parcel == null) PARCEL_STATUS_DELETED else "active",
        serverVersion = serverVersion,
        parcel = parcel,
    )

    @Test
    fun `toParcelleEntity retourne null pour un tombstone sans charge utile`() {
        assertNull(responseDto(parcel = null).toParcelleEntity(existing = null))
    }

    @Test
    fun `toParcelleEntity reconstruit une nouvelle parcelle depuis la charge utile serveur`() {
        val entity = responseDto().toParcelleEntity(existing = null)

        assertEquals("p-1", entity?.parcelleId)
        assertEquals("Bois du pull", entity?.name)
        assertEquals("33063", entity?.codeInseeCommune)
        assertEquals(200L, entity?.updatedAt)
        assertEquals(3, entity?.version)
        assertNull(entity?.uuid)
    }

    @Test
    fun `toParcelleEntity preserve les champs locaux que le contrat reseau ne transporte pas`() {
        val existing = ParcelleEntity(
            parcelleId = "p-1",
            uuid = "11111111-1111-1111-1111-111111111111",
            forestOwnerId = null,
            foretId = null,
            name = "Ancien nom local",
            surfaceHa = null,
            shape = null,
            slopePct = null,
            aspect = null,
            access = null,
            altitudeM = null,
            objectifType = null,
            objectifVal = null,
            tolerancePct = null,
            samplingMode = null,
            sampleAreaM2 = null,
            targetSpeciesCsv = null,
            srid = null,
            remarks = null,
            codeInseeCommune = null,
            nomCommune = null,
            sectionCadastrale = null,
            numeroCadastral = null,
            contenanceCadastraleHa = null,
            geometrieIgnWkt = null,
            natureCadastraleCode = null,
            localisationMode = null,
            codeSer = null,
            nomSer = null,
            provenance = ProvenanceEmbed("IGN", 42L, "ODbL", 1.5, "official"),
            auteur = "technicien-1",
            source = "import",
        )

        val entity = responseDto().toParcelleEntity(existing)

        // Champs venant du serveur (le serveur gagne, c'est un pull).
        assertEquals("Bois du pull", entity?.name)
        // Champs absents du contrat reseau (preserves depuis le local existant).
        assertEquals("11111111-1111-1111-1111-111111111111", entity?.uuid)
        assertEquals("technicien-1", entity?.auteur)
        assertEquals("import", entity?.source)
        assertEquals("IGN", entity?.provenance?.organism)
    }
}
