/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.data.repositoryImp.extend

import com.mifos.core.data.repository.extend.SyncKycRepository
import com.mifos.core.databasehelper.extend.DatabaseHelperKyc
import com.mifos.core.network.datamanager.extend.DataManagerKyc
import com.mifos.core.objects.client.extend.ClientKycPayload
import com.mifos.core.objects.client.extend.GuarantorKycPayload
import rx.Observable
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Implementation of SyncKycRepository that integrates with DatabaseHelperKyc
 * to provide proper offline KYC sync functionality.
 * 
 * This fixes the "no such table: ClientKycPayload" error by properly implementing
 * the database operations through the existing database helper pattern.
 */
@Singleton
class SyncKycRepositoryImp @Inject constructor(
    private val databaseHelperKyc: DatabaseHelperKyc,
    private val dataManagerKyc: DataManagerKyc
) : SyncKycRepository {

    /**
     * Get all Client KYC payloads from database
     */
    override fun getAllClientKycPayloads(): Observable<List<ClientKycPayload>> {
        return databaseHelperKyc.readAllClientKycPayloads()
    }

    /**
     * Get all Guarantor KYC payloads from database
     */
    override fun getAllGuarantorKycPayloads(): Observable<List<GuarantorKycPayload>> {
        return databaseHelperKyc.readAllGuarantorKycPayloads()
    }

    /**
     * Sync Client KYC payload to server
     */
    override fun syncClientKyc(clientKycPayload: ClientKycPayload): Observable<Map<String, Any>> {
        println("🔄 SyncKycRepositoryImp: Syncing Client KYC to server - clientId: ${clientKycPayload.clientId}")
        return dataManagerKyc.syncClientKycToServer(clientKycPayload).map { response: Map<String, Any> ->
            println("✅ SyncKycRepositoryImp: Client KYC sync successful - response: $response")
            mapOf(
                "id" to (response["id"] ?: 0),
                "clientId" to clientKycPayload.clientId!!,
                "status" to "success"
            )
        }.doOnError { error ->
            println("❌ SyncKycRepositoryImp: Client KYC sync failed for clientId: ${clientKycPayload.clientId} - ${error.message}")
        }
    }

    /**
     * Sync Guarantor KYC payload to server
     */
    override fun syncGuarantorKyc(guarantorKycPayload: GuarantorKycPayload): Observable<Map<String, Any>> {
        println("🔄 SyncKycRepositoryImp: Syncing Guarantor KYC to server - clientId: ${guarantorKycPayload.clientId}, name: ${guarantorKycPayload.fullName}")
        return dataManagerKyc.syncGuarantorKycToServer(guarantorKycPayload).map { response: Map<String, Any> ->
            println("✅ SyncKycRepositoryImp: Guarantor KYC sync successful - response: $response")
            mapOf(
                "id" to (response["id"] ?: 0),
                "clientId" to guarantorKycPayload.clientId!!,
                "status" to "success"
            )
        }.doOnError { error ->
            println("❌ SyncKycRepositoryImp: Guarantor KYC sync failed for clientId: ${guarantorKycPayload.clientId} - ${error.message}")
        }
    }

    /**
     * Update Client KYC payload in database
     */
    override fun updateClientKycPayload(clientKycPayload: ClientKycPayload): Observable<ClientKycPayload> {
        return databaseHelperKyc.updateClientKycPayload(clientKycPayload)
    }

    /**
     * Update Guarantor KYC payload in database
     */
    override fun updateGuarantorKycPayload(guarantorKycPayload: GuarantorKycPayload): Observable<GuarantorKycPayload> {
        return databaseHelperKyc.updateGuarantorKycPayload(guarantorKycPayload)
    }

    /**
     * Delete Client KYC payload from database
     */
    override fun deleteClientKycPayload(id: Int): Observable<List<ClientKycPayload>> {
        return databaseHelperKyc.deleteClientKycPayload(id)
    }

    /**
     * Delete Guarantor KYC payload from database
     */
    override fun deleteGuarantorKycPayload(id: Int): Observable<List<GuarantorKycPayload>> {
        return databaseHelperKyc.deleteGuarantorKycPayload(id)
    }

    /**
     * Update KYC payloads with new client ID (simplified approach)
     * This replaces the complex clientCreationTime dependency tracking
     */
    override fun updateKycPayloadsWithNewClientId(oldClientId: Int, newClientId: Int): Observable<Boolean> {
        return Observable.zip(
            databaseHelperKyc.updateClientKycPayloadsWithNewClientId(oldClientId, newClientId),
            databaseHelperKyc.updateGuarantorKycPayloadsWithNewClientId(oldClientId, newClientId)
        ) { clientKycPayloads: List<com.mifos.core.objects.client.extend.ClientKycPayload>, 
            guarantorKycPayloads: List<com.mifos.core.objects.client.extend.GuarantorKycPayload> ->
            // Return true if both updates succeeded
            println("🔄 SyncKycRepositoryImp: Updated ${clientKycPayloads.size} client KYC and ${guarantorKycPayloads.size} guarantor KYC payloads")
            true
        }
    }
} 