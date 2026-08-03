package com.forestry.counter.domain.usecase.privacy

import android.content.Context
import com.forestry.counter.data.local.dao.AlerteSanitaireDao
import com.forestry.counter.data.local.dao.ArbreHabitatDao
import com.forestry.counter.data.local.dao.DiagnosticSylvicoleDao
import com.forestry.counter.data.local.dao.ForetDao
import com.forestry.counter.data.local.dao.IbpEvaluationDao
import com.forestry.counter.data.local.dao.InventaireSessionDao
import com.forestry.counter.data.local.dao.ObservationFloreDao
import com.forestry.counter.data.local.dao.ParcelleDao
import com.forestry.counter.data.local.dao.PlacetteDao
import com.forestry.counter.data.local.dao.RipisylveDao
import com.forestry.counter.data.local.dao.StationDao
import com.forestry.counter.data.local.dao.TigeDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * Tests du use case de droit à l'effacement RGPD (Art. 17).
 *
 * Vérifie que tous les DAOs contenant des PII sont bien appelés lors de
 * l'exécution, et que le compte retourné correspond au nombre de DAOs.
 */
class DeleteAllUserDataUseCaseTest {

    private val foretDao = mockk<ForetDao>(relaxed = true)
    private val parcelleDao = mockk<ParcelleDao>(relaxed = true)
    private val tigeDao = mockk<TigeDao>(relaxed = true)
    private val placetteDao = mockk<PlacetteDao>(relaxed = true)
    private val inventaireSessionDao = mockk<InventaireSessionDao>(relaxed = true)
    private val stationDao = mockk<StationDao>(relaxed = true)
    private val ripisylveDao = mockk<RipisylveDao>(relaxed = true)
    private val ibpEvaluationDao = mockk<IbpEvaluationDao>(relaxed = true)
    private val arbreHabitatDao = mockk<ArbreHabitatDao>(relaxed = true)
    private val diagnosticSylvicoleDao = mockk<DiagnosticSylvicoleDao>(relaxed = true)
    private val alerteSanitaireDao = mockk<AlerteSanitaireDao>(relaxed = true)
    private val observationFloreDao = mockk<ObservationFloreDao>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)

    private val useCase = DeleteAllUserDataUseCase(
        foretDao = foretDao,
        parcelleDao = parcelleDao,
        tigeDao = tigeDao,
        placetteDao = placetteDao,
        inventaireSessionDao = inventaireSessionDao,
        stationDao = stationDao,
        ripisylveDao = ripisylveDao,
        ibpEvaluationDao = ibpEvaluationDao,
        arbreHabitatDao = arbreHabitatDao,
        diagnosticSylvicoleDao = diagnosticSylvicoleDao,
        alerteSanitaireDao = alerteSanitaireDao,
        observationFloreDao = observationFloreDao,
        context = context,
    )

    @Test
    fun should_appeler_hardDeleteAll_sur_tous_les_daos_pii() = runTest {
        every { context.getExternalFilesDir(null) } returns createTempDir()

        useCase.execute()

        // Les 11 entités cœur métier (Vague C) : suppression physique via hardDeleteAll.
        coVerify(exactly = 1) { foretDao.hardDeleteAll() }
        coVerify(exactly = 1) { parcelleDao.hardDeleteAll() }
        coVerify(exactly = 1) { tigeDao.hardDeleteAll() }
        coVerify(exactly = 1) { placetteDao.hardDeleteAll() }
        coVerify(exactly = 1) { inventaireSessionDao.hardDeleteAll() }
        coVerify(exactly = 1) { stationDao.hardDeleteAll() }
        coVerify(exactly = 1) { ripisylveDao.hardDeleteAll() }
        coVerify(exactly = 1) { arbreHabitatDao.hardDeleteAll() }
        coVerify(exactly = 1) { diagnosticSylvicoleDao.hardDeleteAll() }
        coVerify(exactly = 1) { alerteSanitaireDao.hardDeleteAll() }
        coVerify(exactly = 1) { observationFloreDao.hardDeleteAll() }
        // IbpEvaluation : pas de colonne deletedAt (hors Vague C), deleteAll reste physique.
        coVerify(exactly = 1) { ibpEvaluationDao.deleteAll() }
    }

    @Test
    fun should_renvoyer_le_nombre_de_daos_purge() = runTest {
        every { context.getExternalFilesDir(null) } returns createTempDir()

        val result = useCase.execute()

        assertEquals(DeleteAllUserDataUseCase.DAO_COUNT, result)
    }
}
