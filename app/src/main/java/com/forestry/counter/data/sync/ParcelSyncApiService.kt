package com.forestry.counter.data.sync

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

internal interface ParcelSyncApiService {
    @PUT("api/v1/sync/geosylva/parcelles/{clientId}")
    suspend fun upsert(
        @Header("Authorization") authorization: String,
        @Path("clientId") clientId: String,
        @Body request: ParcelUpsertRequestDto,
    ): Response<ParcelSyncResponseDto>

    @HTTP(method = "DELETE", path = "api/v1/sync/geosylva/parcelles/{clientId}", hasBody = true)
    suspend fun delete(
        @Header("Authorization") authorization: String,
        @Path("clientId") clientId: String,
        @Body request: ParcelDeleteRequestDto,
    ): Response<ParcelSyncResponseDto>

    @GET("api/v1/sync/geosylva/parcelles")
    suspend fun list(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 50,
    ): Response<GeoSylvaParcelPageDto>
}
