/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.network.services

import com.google.gson.JsonArray
import com.mifos.core.model.APIEndPoint
import com.mifos.core.network.GenericResponse
import com.mifos.core.objects.user.UserLocation
import org.openapitools.client.models.DeleteDataTablesDatatableAppTableIdDatatableIdResponse
import org.openapitools.client.models.GetDataTablesResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import rx.Observable

/**
 * @author fomenkoo
 */
interface DataTableService {
    @GET(APIEndPoint.DATATABLES)
    suspend fun getDatatables(@Query("apptable") apptable: String? = null): List<GetDataTablesResponse>

    @GET(APIEndPoint.DATATABLES + "/{dataTableName}/{entityId}/")
    suspend fun getDataOfDataTable(
        @Path("dataTableName") dataTableName: String,
        @Path("entityId") entityId: Int,
    ): JsonArray

    // TODO Improve Body Implementation with Payload
    @POST(APIEndPoint.DATATABLES + "/{dataTableName}/{entityId}/")
    suspend fun createEntryInDataTable(
        @Path("dataTableName") dataTableName: String,
        @Path("entityId") entityId: Int,
        @Body requestPayload: Map<String, String>,
    ): GenericResponse

    @DELETE(APIEndPoint.DATATABLES + "/{dataTableName}/{entityId}/{dataTableRowId}")
    suspend fun deleteDatatableEntry(
        @Path("datatable") datatable: String,
        @Path("apptableId") apptableId: Long,
        @Path("datatableId") datatableId: Long,
    ): DeleteDataTablesDatatableAppTableIdDatatableIdResponse

    @POST(APIEndPoint.DATATABLES + "/user_tracking/{userId}")
    fun addUserPathTracking(
        @Path("userId") userId: Int,
        @Body userLocation: UserLocation?,
    ): Observable<GenericResponse>

    @GET(APIEndPoint.DATATABLES + "/user_tracking/{userId}")
    suspend fun getUserPathTracking(@Path("userId") userId: Int): List<UserLocation>
}
