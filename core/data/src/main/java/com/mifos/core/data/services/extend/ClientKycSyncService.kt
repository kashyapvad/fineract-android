/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.data.services.extend

import android.util.Log
import com.mifos.core.common.utils.FileUtils.LOG_TAG
import com.mifos.core.data.repository.extend.SyncKycRepository
import rx.Observable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service to handle KYC sync operations after client creation.
 * This keeps the main sync logic clean and focused.
 */
@Singleton
class ClientKycSyncService @Inject constructor(
    private val syncKycRepository: SyncKycRepository
) {

    /**
     * Sync all KYC payloads for a specific client after client creation.
     * Simple approach: Find KYC payloads by old local client ID and update them with new server client ID.
     */
    fun syncKycForClient(serverClientId: Int, localClientId: Int): Observable<Boolean> {
        Log.d(LOG_TAG, "syncKycForClient called - serverClientId: $serverClientId, localClientId: $localClientId")
        
        // Simple approach: Update KYC payloads that have the old local client ID with the new server client ID
        return syncKycRepository.updateKycPayloadsWithNewClientId(localClientId, serverClientId)
            .flatMap { success ->
                Log.d(LOG_TAG, "KYC payloads updated with server client ID: $serverClientId, success: $success")
                
                // Now sync the updated KYC payloads for this client
                syncKycRepository.getAllClientKycPayloads()
                    .flatMap { allClientKycPayloads ->
                        Log.d(LOG_TAG, "Total client KYC payloads found: ${allClientKycPayloads.size}")
                        val clientKycPayloads = allClientKycPayloads.filter { it.clientId == serverClientId }
                        Log.d(LOG_TAG, "Client KYC payloads for server client $serverClientId: ${clientKycPayloads.size}")
                        
                        if (clientKycPayloads.isNotEmpty()) {
                            // Sync client KYC payloads first
                            syncClientKycList(clientKycPayloads)
                                .flatMap { syncGuarantorKycForClient(serverClientId) }
                        } else {
                            // No client KYC, go directly to guarantor KYC
                            Log.d(LOG_TAG, "No client KYC payloads found, checking guarantor KYC")
                            syncGuarantorKycForClient(serverClientId)
                        }
                    }
            }
            .onErrorReturn { error ->
                Log.e(LOG_TAG, "Failed to update KYC payloads with server client ID", error)
                true // Continue anyway
            }
    }

    private fun syncClientKycList(payloads: List<com.mifos.core.objects.client.extend.ClientKycPayload>): Observable<Boolean> {
        if (payloads.isEmpty()) return Observable.just(true)
        
        return Observable.concat(
            payloads.map { payload ->
                syncKycRepository.syncClientKyc(payload)
                    .map { response ->
                        Log.d(LOG_TAG, "Client KYC synced: ${response["id"]}")
                        payload.syncStatus = "SYNCED"
                        true
                    }
                    .flatMap { syncKycRepository.deleteClientKycPayload(payload.id!!) }
                    .map { true }
                    .onErrorReturn { error ->
                        Log.e(LOG_TAG, "Client KYC sync failed: ${error.message}")
                        true // Continue with other payloads
                    }
            }
        ).lastOrDefault(true)
    }

    private fun syncGuarantorKycForClient(clientId: Int): Observable<Boolean> {
        Log.d(LOG_TAG, "syncGuarantorKycForClient called for client: $clientId")
        return syncKycRepository.getAllGuarantorKycPayloads()
            .flatMap { allGuarantorKycPayloads ->
                Log.d(LOG_TAG, "Total guarantor KYC payloads found: ${allGuarantorKycPayloads.size}")
                val guarantorKycPayloads = allGuarantorKycPayloads.filter { it.clientId == clientId }
                Log.d(LOG_TAG, "Guarantor KYC payloads for client $clientId: ${guarantorKycPayloads.size}")
                
                if (guarantorKycPayloads.isNotEmpty()) {
                    syncGuarantorKycList(guarantorKycPayloads)
                } else {
                    Log.d(LOG_TAG, "No guarantor KYC payloads found for client $clientId")
                    Observable.just(true)
                }
            }
    }

    private fun syncGuarantorKycList(payloads: List<com.mifos.core.objects.client.extend.GuarantorKycPayload>): Observable<Boolean> {
        if (payloads.isEmpty()) return Observable.just(true)
        
        return Observable.concat(
            payloads.map { payload ->
                syncKycRepository.syncGuarantorKyc(payload)
                    .map { response ->
                        Log.d(LOG_TAG, "Guarantor KYC synced: ${response["id"]}")
                        payload.syncStatus = "SYNCED"
                        true
                    }
                    .flatMap { syncKycRepository.deleteGuarantorKycPayload(payload.id!!) }
                    .map { true }
                    .onErrorReturn { error ->
                        Log.e(LOG_TAG, "Guarantor KYC sync failed: ${error.message}")
                        true // Continue with other payloads
                    }
            }
        ).lastOrDefault(true)
    }
} 