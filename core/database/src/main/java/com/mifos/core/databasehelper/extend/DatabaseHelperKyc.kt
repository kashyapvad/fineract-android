/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.databasehelper.extend

import android.util.Log
import com.mifos.core.objects.client.extend.ClientKycPayload
import com.mifos.core.objects.client.extend.ClientKycPayload_Table
import com.mifos.core.objects.client.extend.GuarantorKycPayload
import com.mifos.core.objects.client.extend.GuarantorKycPayload_Table
import com.mifos.core.objects.client.extend.kyc.ClientKycData
import com.mifos.core.objects.client.extend.kyc.ClientKycData_Table
import com.mifos.core.objects.client.extend.kyc.GuarantorKycData
import com.mifos.core.objects.client.extend.kyc.GuarantorKycData_Table
import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.SQLite
import rx.Observable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Database helper for KYC operations following DatabaseHelperClient pattern.
 * 
 * This helper manages BOTH entity operations AND payload operations (unified pattern):
 * - Entity operations (ClientKycData, GuarantorKycData) - for offline viewing
 * - Payload operations (ClientKycPayload, GuarantorKycPayload) - for offline sync
 * 
 * This follows the exact same pattern as DatabaseHelperClient.
 */
@Singleton
class DatabaseHelperKyc @Inject constructor() {

    // ============================================================================
    // ENTITY OPERATIONS (for offline viewing and template management)
    // ============================================================================

    /**
     * Save Client KYC entity to database for offline viewing
     */
    fun saveClientKyc(clientKyc: ClientKycData): Observable<ClientKycData> {
        return Observable.defer {
            clientKyc.save()
            Observable.just(clientKyc)
        }
    }

    /**
     * Get Client KYC entity by client ID
     */
    fun getClientKyc(clientId: Int): Observable<ClientKycData?> {
        return Observable.create { subscriber ->
            val clientKyc = SQLite.select()
                .from(ClientKycData::class.java)
                .where(ClientKycData_Table.clientId.eq(clientId.toLong()))
                .querySingle()
            subscriber.onNext(clientKyc)
            subscriber.onCompleted()
        }
    }

    /**
     * Get all Client KYC entities
     */
    fun getAllClientKyc(): Observable<List<ClientKycData>> {
        return Observable.defer {
            val clientKycList = SQLite.select()
                .from(ClientKycData::class.java)
                .queryList()
            Observable.just(clientKycList)
        }
    }

    /**
     * Save Guarantor KYC entity to database for offline viewing
     */
    fun saveGuarantorKyc(guarantorKyc: GuarantorKycData): Observable<GuarantorKycData> {
        return Observable.defer {
            guarantorKyc.save()
            Observable.just(guarantorKyc)
        }
    }

    /**
     * Get Guarantor KYC entities by client ID
     */
    fun getGuarantorKycList(clientId: Int): Observable<List<GuarantorKycData>> {
        return Observable.defer {
            val guarantorKycList = SQLite.select()
                .from(GuarantorKycData::class.java)
                .where(GuarantorKycData_Table.clientId.eq(clientId.toLong()))
                .queryList()
            Observable.just(guarantorKycList)
        }
    }

    /**
     * Get specific Guarantor KYC entity by ID
     */
    fun getGuarantorKyc(guarantorKycId: Long): Observable<GuarantorKycData?> {
        return Observable.create { subscriber ->
            val guarantorKyc = SQLite.select()
                .from(GuarantorKycData::class.java)
                .where(GuarantorKycData_Table.id.eq(guarantorKycId))
                .querySingle()
            subscriber.onNext(guarantorKyc)
            subscriber.onCompleted()
        }
    }

    /**
     * Save Client KYC template for offline forms
     */
    fun saveClientKycTemplate(template: ClientKycData): Observable<ClientKycData> {
        return Observable.defer {
            // Mark as template and save
            val templateCopy = template.copy(
                id = TEMPLATE_CLIENT_KYC_ID,
                clientId = 0L // Template marker
            )
            templateCopy.save()
            Observable.just(templateCopy)
        }
    }

    /**
     * Read Client KYC template for offline forms
     */
    fun readClientKycTemplate(): Observable<ClientKycData> {
        return Observable.defer {
            val template = SQLite.select()
                .from(ClientKycData::class.java)
                .where(ClientKycData_Table.id.eq(TEMPLATE_CLIENT_KYC_ID))
                .querySingle()
                ?: createDefaultClientKycTemplate()
            Observable.just(template)
        }
    }

    /**
     * Update Client KYC entity in database
     */
    fun updateClientKyc(clientKyc: ClientKycData): Observable<ClientKycData> {
        return Observable.defer {
            clientKyc.update()
            Observable.just(clientKyc)
        }
    }

    /**
     * Update Guarantor KYC entity in database
     */
    fun updateGuarantorKyc(guarantorKyc: GuarantorKycData): Observable<GuarantorKycData> {
        return Observable.defer {
            guarantorKyc.update()
            Observable.just(guarantorKyc)
        }
    }

    /**
     * Delete Client KYC entity from database
     */
    fun deleteClientKyc(clientKycId: Long): Observable<Boolean> {
        return Observable.defer {
            Delete.table(ClientKycData::class.java, ClientKycData_Table.id.eq(clientKycId))
            Observable.just(true)
        }
    }

    /**
     * Delete Guarantor KYC entity from database
     */
    fun deleteGuarantorKyc(guarantorKycId: Long): Observable<Boolean> {
        return Observable.defer {
            Delete.table(GuarantorKycData::class.java, GuarantorKycData_Table.id.eq(guarantorKycId))
            Observable.just(true)
        }
    }

    /**
     * Create default Client KYC template when none exists
     */
    private fun createDefaultClientKycTemplate(): ClientKycData {
        return ClientKycData(
            id = TEMPLATE_CLIENT_KYC_ID,
            clientId = 0L,
            isActive = true
        )
    }

    companion object {
        const val TEMPLATE_CLIENT_KYC_ID = -1L // Special ID for template
    }

    // ============================================================================
    // PAYLOAD OPERATIONS (for offline sync - consolidated from DatabaseHelperKycSync)
    // ============================================================================

    /**
     * Save Client KYC payload to database for offline sync
     */
    fun saveClientKycPayloadToDB(clientKycPayload: ClientKycPayload): Observable<ClientKycPayload> {
        return Observable.defer {
            val currentTime = System.currentTimeMillis()
            clientKycPayload.kycCreationTime = currentTime
            clientKycPayload.save()
            Observable.just(clientKycPayload)
        }
    }

    /**
     * Save Guarantor KYC payload to database for offline sync
     */
    fun saveGuarantorKycPayloadToDB(guarantorKycPayload: GuarantorKycPayload): Observable<GuarantorKycPayload> {
        return Observable.defer {
            val currentTime = System.currentTimeMillis()
            guarantorKycPayload.guarantorKycCreationTime = currentTime
            guarantorKycPayload.save()
            Observable.just(guarantorKycPayload)
        }
    }

    /**
     * Read all Client KYC payloads from database
     */
    fun readAllClientKycPayloads(): Observable<List<ClientKycPayload>> {
        return Observable.defer {
            val clientKycPayloads = SQLite.select()
                .from(ClientKycPayload::class.java)
                .where(ClientKycPayload_Table.syncStatus.notEq("SYNCED"))
                .orderBy(ClientKycPayload_Table.kycCreationTime, true)
                .queryList()
            Observable.just(clientKycPayloads)
        }
    }

    /**
     * Read all Guarantor KYC payloads from database
     */
    fun readAllGuarantorKycPayloads(): Observable<List<GuarantorKycPayload>> {
        return Observable.defer {
            val guarantorKycPayloads = SQLite.select()
                .from(GuarantorKycPayload::class.java)
                .where(GuarantorKycPayload_Table.syncStatus.notEq("SYNCED"))
                .orderBy(GuarantorKycPayload_Table.guarantorKycCreationTime, true)
                .queryList()
            Observable.just(guarantorKycPayloads)
        }
    }



    /**
     * Get Client KYC payload for a specific client by client ID
     */
    fun getClientKycPayloadByClientId(clientId: Int): Observable<ClientKycPayload?> {
        return Observable.defer {
            val clientKycPayload = SQLite.select()
                .from(ClientKycPayload::class.java)
                .where(ClientKycPayload_Table.clientId.eq(clientId))
                .and(ClientKycPayload_Table.syncStatus.notEq("SYNCED"))
                .orderBy(ClientKycPayload_Table.kycCreationTime, false) // Latest first
                .querySingle()
            Observable.just(clientKycPayload)
        }
    }



    /**
     * Get Guarantor KYC payloads for a specific client by client ID
     */
    fun getGuarantorKycPayloadsByClientId(clientId: Int): Observable<List<GuarantorKycPayload>> {
        return Observable.defer {
            val guarantorKycPayloads = SQLite.select()
                .from(GuarantorKycPayload::class.java)
                .where(GuarantorKycPayload_Table.clientId.eq(clientId))
                .and(GuarantorKycPayload_Table.syncStatus.notEq("SYNCED"))
                .orderBy(GuarantorKycPayload_Table.guarantorKycCreationTime, false) // Latest first
                .queryList()
            Observable.just(guarantorKycPayloads)
        }
    }

    /**
     * Update Client KYC payload status after sync
     */
    fun updateClientKycPayload(clientKycPayload: ClientKycPayload): Observable<ClientKycPayload> {
        return Observable.defer {
            clientKycPayload.update()
            Observable.just(clientKycPayload)
        }
    }

    /**
     * Update Guarantor KYC payload status after sync
     */
    fun updateGuarantorKycPayload(guarantorKycPayload: GuarantorKycPayload): Observable<GuarantorKycPayload> {
        return Observable.defer {
            guarantorKycPayload.update()
            Observable.just(guarantorKycPayload)
        }
    }

    /**
     * Delete Client KYC payload after successful sync
     */
    fun deleteClientKycPayload(id: Int): Observable<List<ClientKycPayload>> {
        return Observable.defer {
            Delete.table(ClientKycPayload::class.java, ClientKycPayload_Table.id.eq(id))
            readAllClientKycPayloads()
        }
    }

    /**
     * Delete Guarantor KYC payload after successful sync
     */
    fun deleteGuarantorKycPayload(id: Int): Observable<List<GuarantorKycPayload>> {
        return Observable.defer {
            Delete.table(GuarantorKycPayload::class.java, GuarantorKycPayload_Table.id.eq(id))
            readAllGuarantorKycPayloads()
        }
    }

    /**
     * Update Client KYC payloads with new client ID (simplified approach)
     * This replaces the complex clientCreationTime dependency tracking
     */
    fun updateClientKycPayloadsWithNewClientId(
        oldClientId: Int,
        newClientId: Int
    ): Observable<List<ClientKycPayload>> {
        return Observable.defer {
            Log.d("DatabaseHelperKyc", "Updating client KYC payloads - oldClientId: $oldClientId, newClientId: $newClientId")
            val clientKycPayloads = SQLite.select()
                .from(ClientKycPayload::class.java)
                .where(ClientKycPayload_Table.clientId.eq(oldClientId))
                .and(ClientKycPayload_Table.syncStatus.notEq("SYNCED"))
                .queryList()

            Log.d("DatabaseHelperKyc", "Found ${clientKycPayloads.size} client KYC payloads to update")
            clientKycPayloads.forEach { payload ->
                Log.d("DatabaseHelperKyc", "Updating payload ID: ${payload.id}, old clientId: ${payload.clientId}, new clientId: $newClientId")
                payload.clientId = newClientId
                payload.update()
            }

            Observable.just(clientKycPayloads)
        }
    }

    /**
     * Update Guarantor KYC payloads with new client ID (simplified approach)
     * This replaces the complex clientCreationTime dependency tracking
     */
    fun updateGuarantorKycPayloadsWithNewClientId(
        oldClientId: Int,
        newClientId: Int
    ): Observable<List<GuarantorKycPayload>> {
        return Observable.defer {
            Log.d("DatabaseHelperKyc", "Updating guarantor KYC payloads - oldClientId: $oldClientId, newClientId: $newClientId")
            val guarantorKycPayloads = SQLite.select()
                .from(GuarantorKycPayload::class.java)
                .where(GuarantorKycPayload_Table.clientId.eq(oldClientId))
                .and(GuarantorKycPayload_Table.syncStatus.notEq("SYNCED"))
                .queryList()

            Log.d("DatabaseHelperKyc", "Found ${guarantorKycPayloads.size} guarantor KYC payloads to update")
            guarantorKycPayloads.forEach { payload ->
                Log.d("DatabaseHelperKyc", "Updating payload ID: ${payload.id}, old clientId: ${payload.clientId}, new clientId: $newClientId")
                payload.clientId = newClientId
                payload.update()
            }

            Observable.just(guarantorKycPayloads)
        }
    }
} 