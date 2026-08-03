package com.forestry.counter.data.sync

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

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
}
