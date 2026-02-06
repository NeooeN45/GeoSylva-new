package com.forestry.counter.data.local.dao

import androidx.room.*
import com.forestry.counter.data.local.entity.PlacetteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlacetteDao {
    @Query("SELECT * FROM placettes WHERE parcelleOwnerId = :parcelleId ORDER BY placetteId ASC")
    fun getPlacettesByParcelle(parcelleId: String): Flow<List<PlacetteEntity>>

    @Query("SELECT * FROM placettes WHERE placetteId = :id")
    fun getPlacetteByIdFlow(id: String): Flow<PlacetteEntity?>

    @Query("SELECT * FROM placettes WHERE placetteId = :id")
    suspend fun getPlacetteById(id: String): PlacetteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlacette(entity: PlacetteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlacettes(entities: List<PlacetteEntity>)

    @Update
    suspend fun updatePlacette(entity: PlacetteEntity)

    @Delete
    suspend fun deletePlacette(entity: PlacetteEntity)

    @Query("DELETE FROM placettes WHERE placetteId = :id")
    suspend fun deletePlacetteById(id: String)

    @Query("DELETE FROM placettes WHERE parcelleOwnerId = :parcelleId")
    suspend fun deletePlacettesByParcelle(parcelleId: String)
}
