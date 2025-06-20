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

import com.mifos.core.model.APIEndPoint
import com.mifos.core.network.GenericResponse
import com.mifos.core.objects.accounts.ClientAccounts
import com.mifos.core.objects.client.Client
import com.mifos.core.objects.client.ClientAddressRequest
import com.mifos.core.objects.client.ClientAddressResponse
import com.mifos.core.objects.client.ClientPayload
import com.mifos.core.objects.noncore.Identifier
import com.mifos.core.objects.noncore.IdentifierCreationResponse
import com.mifos.core.objects.noncore.IdentifierPayload
import com.mifos.core.objects.noncore.IdentifierTemplate
import com.mifos.core.objects.templates.clients.AddressConfiguration
import com.mifos.core.objects.templates.clients.AddressTemplate
import com.mifos.core.objects.templates.clients.ClientsTemplate
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.openapitools.client.models.DeleteClientsClientIdIdentifiersIdentifierIdResponse
import org.openapitools.client.models.GetClientsResponse
import org.openapitools.client.models.PostClientsClientIdRequest
import org.openapitools.client.models.PostClientsClientIdResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import rx.Observable

/**
 * @author fomenkoo
 */
interface ClientService {
    /**
     * @param b      True Enabling the Pagination of the API
     * @param offset Value give from which position Fetch ClientList
     * @param limit  Maximum size of the Client
     * @return List of Clients
     */
    @GET(APIEndPoint.CLIENTS)
    suspend fun getAllClients(
        @Query("paged") b: Boolean,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): GetClientsResponse

    @GET(APIEndPoint.CLIENTS + "/{clientId}")
    suspend fun getClient(@Path("clientId") clientId: Int): Client

    @Multipart
    @POST(APIEndPoint.CLIENTS + "/{clientId}/images")
    fun uploadClientImage(
        @Path("clientId") clientId: Int,
        @Part file: MultipartBody.Part?,
    ): Observable<ResponseBody>

    @DELETE(APIEndPoint.CLIENTS + "/{clientId}/images")
    fun deleteClientImage(@Path("clientId") clientId: Int): Observable<ResponseBody>

    // TODO: Implement when API Fixed
    //    @GET("/clients/{clientId}/images")
    //    Observable<TypedString> getClientImage(@Path("clientId") int clientId);
    @POST(APIEndPoint.CLIENTS)
    fun createClient(@Body clientPayload: ClientPayload?): Observable<Client>

    @get:GET(APIEndPoint.CLIENTS + "/template")
    val clientTemplate: Observable<ClientsTemplate>

    @GET(APIEndPoint.CLIENTS + "/{clientId}/accounts")
    suspend fun getClientAccounts(@Path("clientId") clientId: Int): ClientAccounts

    /**
     * This Service is for fetching the List of Identifiers.
     * REST END POINT:
     * https://demo.openmf.org/fineract-provider/api/v1/clients/{clientId}/identifiers
     *
     * @param clientId Client Id
     * @return List<Identifier>
     </Identifier> */
    @GET(APIEndPoint.CLIENTS + "/{clientId}/" + APIEndPoint.IDENTIFIERS)
    suspend fun getClientIdentifiers(@Path("clientId") clientId: Int): List<Identifier>

    /**
     * This Service is for Creating the Client Identifier.
     * REST END POINT:
     * https://demo.openmf.org/fineract-provider/api/v1/clients/{clientId}/identifiers
     *
     * @param clientId          Client Id
     * @param identifierPayload IdentifierPayload
     * @return IdentifierCreationResponse
     */
    @POST(APIEndPoint.CLIENTS + "/{clientId}/identifiers")
    fun createClientIdentifier(
        @Path("clientId") clientId: Int,
        @Body identifierPayload: IdentifierPayload,
    ): Flow<IdentifierCreationResponse>

    /**
     * This Service is for the Fetching the Client Identifier Template.
     * REST END POINT:
     * https://demo.openmf.org/fineract-provider/api/v1/clients/{clientId}/identifiers/template
     *
     * @param clientId Client Id
     * @return IdentifierTemplate
     */
    @GET(APIEndPoint.CLIENTS + "/{clientId}/identifiers/template")
    fun getClientIdentifierTemplate(@Path("clientId") clientId: Int): Flow<IdentifierTemplate>

    /**
     * This Service for Deleting the Client Identifier.
     * REST END POINT:
     * https://demo.openmf.org/fineract-provider/api/v1/clients/{clientId}/identifiers/
     * {identifierId}
     *
     * @param clientId     Client Id
     * @param identifierId Identifier Id
     * @return GenericResponse
     */
    @DELETE(APIEndPoint.CLIENTS + "/{clientId}/" + APIEndPoint.IDENTIFIERS + "/{identifierId}")
    suspend fun deleteClientIdentifier(
        @Path("clientId") clientId: Int,
        @Path("identifierId") identifierId: Int,
    ): DeleteClientsClientIdIdentifiersIdentifierIdResponse

    /**
     * This is the service for fetching the client pinpoint locations from the dataTable
     * "client_pinpoint_location". This DataTable entries are
     * 1. Place Id
     * 2. Place Address
     * 3. latitude
     * 4. longitude
     * REST END POINT:
     * https://demo.openmf.org/fineract-provider/api/v1/datatables/client_pinpoint_location
     * /{appTableId}
     *
     * @param clientId Client Id
     * @return ClientAddressResponse
     */
    @GET(APIEndPoint.DATATABLES + "/client_pinpoint_location/{clientId}")
    suspend fun getClientPinpointLocations(
        @Path("clientId") clientId: Int,
    ): List<ClientAddressResponse>

    /**
     * This is the service for adding the new Client Pinpoint Location in dataTable
     * "client_pinpoint_location".
     * REST END POINT:
     * https://demo.openmf.org/fineract-provider/api/v1/datatables/client_pinpoint_location
     * /{appTableId}
     *
     * @param clientId             Client Id
     * @param clientAddressRequest ClientAddress
     * @return GenericResponse
     */
    @POST(APIEndPoint.DATATABLES + "/client_pinpoint_location/{clientId}")
    suspend fun addClientPinpointLocation(
        @Path("clientId") clientId: Int,
        @Body clientAddressRequest: ClientAddressRequest?,
    ): GenericResponse

    /**
     * This is the service for deleting the pinpoint location from the DataTable
     * "client_pinpoint_location".
     * REST END POINT:
     * https://demo.openmf.org/fineract-provider/api/v1/datatables/client_pinpoint_location
     * /{appTableId}/{datatableId}
     *
     * @param apptableId
     * @param datatableId
     * @return GenericResponse
     */
    @DELETE(APIEndPoint.DATATABLES + "/client_pinpoint_location/{apptableId}/{datatableId}")
    suspend fun deleteClientPinpointLocation(
        @Path("apptableId") apptableId: Int,
        @Path("datatableId") datatableId: Int,
    ): GenericResponse

    /**
     * This is the service for updating the pinpoint location from DataTable
     * "client_pinpoint_location"
     * REST ENT POINT:
     * https://demo.openmf.org/fineract-provider/api/v1/datatables/client_pinpoint_location
     * /{appTableId}/{datatableId}
     *
     * @param apptableId
     * @param datatableId
     * @param address     Client Address
     * @return GenericResponse
     */
    @PUT(APIEndPoint.DATATABLES + "/client_pinpoint_location/{apptableId}/{datatableId}")
    suspend fun updateClientPinpointLocation(
        @Path("apptableId") apptableId: Int,
        @Path("datatableId") datatableId: Int,
        @Body address: ClientAddressRequest?,
    ): GenericResponse

    /**
     * This is the service to activate the client
     * REST ENT POINT
     * https://demo.openmf.org/fineract-provider/api/v1/clients/{clientId}?command=activate
     *
     * @param clientId
     * @return GenericResponse
     */
    @POST(APIEndPoint.CLIENTS + "/{clientId}")
    suspend fun activateClient(
        @Path("clientId") clientId: Long,
        @Body clientActivate: PostClientsClientIdRequest,
        @Query("command") command: String? = null,
    ): PostClientsClientIdResponse

    /**
     * Retrieves address configuration from Global Configuration.
     *
     * @return The AddressConfiguration object
     */
    @GET("configurations/name/enable-address")
    suspend fun getAddressConfiguration(): AddressConfiguration

    /**
     * Retrieves an address template.
     * This template can be used to pre-fill address forms or guide users in providing address information.
     *
     * @return An [AddressTemplate] object containing the structure for an address.
     */
    @GET("client/addresses/template")
    suspend fun getAddressTemplate(): AddressTemplate
}
