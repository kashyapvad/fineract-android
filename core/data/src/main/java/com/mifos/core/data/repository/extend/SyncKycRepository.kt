/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.data.repository.extend

import com.mifos.core.objects.client.extend.ClientKycPayload
import com.mifos.core.objects.client.extend.GuarantorKycPayload
import rx.Observable

/**
 * Repository interface for KYC sync operations.
 * 
 * Handles synchronization of Client KYC and Guarantor KYC data
 * between local database and server API.
 */
interface SyncKycRepository {

    // Database operations
    fun getAllClientKycPayloads(): Observable<List<ClientKycPayload>>
    fun getAllGuarantorKycPayloads(): Observable<List<GuarantorKycPayload>>
    
    // API operations
    fun syncClientKyc(clientKycPayload: ClientKycPayload): Observable<Map<String, Any>>
    fun syncGuarantorKyc(guarantorKycPayload: GuarantorKycPayload): Observable<Map<String, Any>>
    
    // Database update operations after sync
    fun updateClientKycPayload(clientKycPayload: ClientKycPayload): Observable<ClientKycPayload>
    fun updateGuarantorKycPayload(guarantorKycPayload: GuarantorKycPayload): Observable<GuarantorKycPayload>
    fun deleteClientKycPayload(id: Int): Observable<List<ClientKycPayload>>
    fun deleteGuarantorKycPayload(id: Int): Observable<List<GuarantorKycPayload>>
    
    // Dependency resolution operations
    fun updateKycPayloadsWithNewClientId(oldClientId: Int, newClientId: Int): Observable<Boolean>
} 