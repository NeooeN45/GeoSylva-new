package com.forestry.counter.domain.repository

import com.forestry.counter.domain.model.ValeurFonciere
import kotlinx.coroutines.flow.Flow

interface ValeurFonciereRepository {
    fun getByParcelle(parcelleId: String): Flow<ValeurFonciere?>
    suspend fun getByParcelleOnce(parcelleId: String): ValeurFonciere?
    suspend fun sumPatrimoineTotal(): Double?
    suspend fun sumCarboneTotal(): Double?
    suspend fun insert(valeur: ValeurFonciere)
    suspend fun update(valeur: ValeurFonciere)
    suspend fun deleteByParcelle(parcelleId: String)
}
