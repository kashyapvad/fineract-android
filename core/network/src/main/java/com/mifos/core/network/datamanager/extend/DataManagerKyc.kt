/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.network.datamanager.extend

import com.mifos.core.databasehelper.extend.DatabaseHelperKyc
import com.mifos.core.datastore.PrefManager
import com.mifos.core.network.GenericResponse
import com.mifos.core.network.BaseApiManager
import com.mifos.core.network.mappers.clients.extend.ClientKycMapper
import com.mifos.core.network.mappers.clients.extend.GuarantorKycMapper
import com.mifos.core.objects.client.extend.ClientKycPayload
import com.mifos.core.objects.client.extend.GuarantorKycPayload
import com.mifos.core.objects.client.extend.kyc.ClientKycData
import com.mifos.core.objects.client.extend.kyc.ClientKycRequest
import com.mifos.core.objects.client.extend.kyc.GuarantorKycData
import com.mifos.core.objects.client.extend.kyc.GuarantorKycRequest
import com.mifos.core.objects.client.extend.kyc.OtpGenerationRequest
import com.mifos.core.objects.client.extend.kyc.OtpSubmissionRequest
import com.mifos.core.objects.client.extend.kyc.GuarantorOtpGenerationRequest
import com.mifos.core.objects.client.extend.kyc.GuarantorOtpSubmissionRequest
import rx.Observable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data manager for KYC operations following DataManagerClient pattern.
 * 
 * Handles online/offline logic for Client KYC and Guarantor KYC operations
 * with proper entity storage and payload management for sync.
 */
@Singleton
class DataManagerKyc @Inject constructor(
    private val baseApiManager: BaseApiManager,
    private val databaseHelperKyc: DatabaseHelperKyc,
    private val prefManager: PrefManager,
) {

    // ============================================================================
    // NEW SUSPEND FUNCTIONS FOR REPOSITORY LAYER (WITH ONLINE/OFFLINE LOGIC)
    // ============================================================================

    /**
     * Get Client KYC data with online/offline logic following DataManagerClient pattern
     */
    suspend fun getClientKyc(clientId: Int): ClientKycData {
        return when (prefManager.userStatus) {
            false -> { // Online
                try {
                    val response = baseApiManager.kycApi.getClientKyc(clientId)
                    ClientKycMapper.mapFromEntity(response)
                } catch (e: Exception) {
                    // Return template if not found online
                    getClientKycTemplate(clientId)
                }
            }
            true -> { // Offline
                try {
                    databaseHelperKyc.getClientKyc(clientId).toBlocking().first()
                        ?: getClientKycTemplate(clientId)
                } catch (e: Exception) {
                    getClientKycTemplate(clientId)
                }
            }
        }
    }

    /**
     * Get Client KYC template (simple template for new KYC)
     */
    suspend fun getClientKycTemplate(clientId: Int): ClientKycData {
        return ClientKycData(clientId = clientId.toLong(), isActive = true)
    }

    /**
     * Get Guarantor KYC list with online/offline logic following DataManagerClient pattern
     */
    suspend fun getGuarantorKycList(clientId: Int): List<GuarantorKycData> {
        return when (prefManager.userStatus) {
            false -> { // Online
                try {
                    val response = baseApiManager.kycApi.getGuarantorKyc(clientId)
                    response.map { GuarantorKycMapper.mapFromEntity(it) }
                } catch (e: Exception) {
                    // Return empty list if not found online
                    emptyList()
                }
            }
            true -> { // Offline
                try {
                    databaseHelperKyc.getGuarantorKycList(clientId).toBlocking().first()
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
    }



    // ============================================================================
    // SYNC METHODS (ALWAYS MAKE NETWORK CALLS - FOR SYNC OPERATIONS)
    // ============================================================================

    /**
     * Sync Client KYC to server (always online - used during sync operations)
     */
    fun syncClientKycToServer(clientKycPayload: ClientKycPayload): Observable<Map<String, Any>> {
        return baseApiManager.kycApi.createClientKyc(
            clientId = clientKycPayload.clientId!!,
            clientKycPayload = clientKycPayload
        ).map { response ->
            // Convert GenericResponse to Map<String, Any> for sync compatibility
            mapOf(
                "id" to (response.responseFields["resourceId"] ?: 0),
                "clientId" to clientKycPayload.clientId!!,
                "status" to "created"
            )
        }
    }

    /**
     * Sync Guarantor KYC to server (always online - used during sync operations)
     */
    fun syncGuarantorKycToServer(guarantorKycPayload: GuarantorKycPayload): Observable<Map<String, Any>> {
        return baseApiManager.kycApi.createGuarantorKyc(
            clientId = guarantorKycPayload.clientId!!,
            guarantorKycPayload = guarantorKycPayload
        ).map { response ->
            // Convert GenericResponse to Map<String, Any> for sync compatibility
            mapOf(
                "id" to (response.responseFields["resourceId"] ?: 0),
                "clientId" to guarantorKycPayload.clientId!!,
                "status" to "created"
            )
        }
    }

    // ============================================================================
    // CREATION METHODS (WITH ONLINE/OFFLINE LOGIC - FOR USER OPERATIONS)
    // ============================================================================

    /**
     * Create Client KYC with online/offline logic following DataManagerClient pattern
     */
    fun createClientKyc(clientKycPayload: ClientKycPayload): Observable<Map<String, Any>> {
        return when (prefManager.userStatus) {
            false -> { // Online
                baseApiManager.kycApi.createClientKyc(
                    clientId = clientKycPayload.clientId!!,
                    clientKycPayload = clientKycPayload
                ).map { response ->
                    // Convert GenericResponse to Map<String, Any> for sync compatibility
                    mapOf(
                        "id" to (response.responseFields["resourceId"] ?: 0),
                        "clientId" to clientKycPayload.clientId!!,
                        "status" to "created"
                    )
                }
            }
            true -> { // Offline - save both payload (for sync) AND entity (for viewing)
                databaseHelperKyc.saveClientKycPayloadToDB(clientKycPayload)
                    .concatMap { 
                        // Also save as entity for immediate offline viewing
                        val kycEntity = ClientKycData(
                            clientId = clientKycPayload.clientId!!.toLong(),
                            clientDisplayName = "Client ${clientKycPayload.clientId}",
                            panNumber = clientKycPayload.panNumber,
                            aadhaarNumber = clientKycPayload.aadhaarNumber,
                            voterIdNumber = clientKycPayload.voterId,
                            drivingLicenseNumber = clientKycPayload.drivingLicenseNumber,
                            passportNumber = clientKycPayload.passportNumber,
                            manualVerificationNotes = clientKycPayload.verificationNotes,
                            isActive = true,
                            offlineData = true,
                            syncStatus = "PENDING_SYNC"
                        )
                        databaseHelperKyc.saveClientKyc(kycEntity)
                    }
                    .map { 
                        mapOf(
                            "id" to 0, // Temporary ID for offline
                            "clientId" to clientKycPayload.clientId!!,
                            "status" to "created",
                            "offline" to true
                        )
                    }
            }
        }
    }

    /**
     * Create Guarantor KYC with online/offline logic following DataManagerClient pattern
     */
    fun createGuarantorKyc(guarantorKycPayload: GuarantorKycPayload): Observable<Map<String, Any>> {
        return when (prefManager.userStatus) {
            false -> { // Online
                baseApiManager.kycApi.createGuarantorKyc(
                    clientId = guarantorKycPayload.clientId!!,
                    guarantorKycPayload = guarantorKycPayload
                ).map { response ->
                    // Convert GenericResponse to Map<String, Any> for sync compatibility
                    mapOf(
                        "id" to (response.responseFields["resourceId"] ?: 0),
                        "clientId" to guarantorKycPayload.clientId!!,
                        "status" to "created"
                    )
                }
            }
            true -> { // Offline - save both payload (for sync) AND entity (for viewing)
                databaseHelperKyc.saveGuarantorKycPayloadToDB(guarantorKycPayload)
                    .concatMap { 
                        // Also save as entity for immediate offline viewing
                        val guarantorEntity = GuarantorKycData(
                            clientId = guarantorKycPayload.clientId!!.toLong(),
                            fullName = guarantorKycPayload.fullName,
                            mobileNumber = guarantorKycPayload.mobileNumber,
                            relationshipToClient = guarantorKycPayload.relationshipToClient,
                            panNumber = guarantorKycPayload.panNumber,
                            aadhaarNumber = guarantorKycPayload.aadhaarNumber,
                            verificationNotes = guarantorKycPayload.verificationNotes,
                            isActive = true,
                            offlineData = true,
                            syncStatus = "PENDING_SYNC"
                        )
                        databaseHelperKyc.saveGuarantorKyc(guarantorEntity)
                    }
                    .map { 
                        mapOf(
                            "id" to 0, // Temporary ID for offline
                            "clientId" to guarantorKycPayload.clientId!!,
                            "status" to "created",
                            "offline" to true
                        )
                    }
            }
        }
    }

    /**
     * Update Client KYC data with online/offline logic
     */
    fun updateClientKyc(
        clientId: Int,
        kycId: Long,
        panNumber: String?,
        aadhaarNumber: String?,
        voterId: String?,
        drivingLicenseNumber: String?,
        passportNumber: String?,
        verificationNotes: String?,
    ): Observable<GenericResponse> {
        return when (prefManager.userStatus) {
            false -> { // Online
                val request = ClientKycRequest(
                    panNumber = panNumber,
                    aadhaarNumber = aadhaarNumber,
                    voterId = voterId,
                    drivingLicenseNumber = drivingLicenseNumber,
                    passportNumber = passportNumber,
                    verificationNotes = verificationNotes
                )
                baseApiManager.kycApi.updateClientKyc(
                    clientId = clientId,
                    kycId = kycId,
                    clientKycRequest = request
                )
            }
            true -> { // Offline - update local entity
                val updatedKyc = ClientKycData(
                    id = kycId,
                    clientId = clientId.toLong(),
                    clientDisplayName = "Client $clientId",
                    panNumber = panNumber,
                    aadhaarNumber = aadhaarNumber,
                    voterIdNumber = voterId,
                    drivingLicenseNumber = drivingLicenseNumber,
                    passportNumber = passportNumber,
                    manualVerificationNotes = verificationNotes,
                    isActive = true,
                    offlineData = true,
                    syncStatus = "PENDING_UPDATE"
                )
                databaseHelperKyc.updateClientKyc(updatedKyc)
                    .map { 
                        GenericResponse().apply {
                            responseFields["offline"] = true
                            responseFields["status"] = "updated"
                        }
                    }
            }
        }
    }

    /**
     * Update Guarantor KYC data with online/offline logic
     */
    fun updateGuarantorKyc(
        clientId: Int,
        guarantorKycId: Long,
        fullName: String?,
        mobileNumber: String?,
        relationshipToClient: String?,
        panNumber: String?,
        aadhaarNumber: String?,
        verificationNotes: String?,
    ): Observable<GenericResponse> {
        return when (prefManager.userStatus) {
            false -> { // Online
                val request = GuarantorKycRequest(
                    fullName = fullName ?: "",
                    mobileNumber = mobileNumber,
                    relationshipToClient = relationshipToClient,
                    panNumber = panNumber,
                    aadhaarNumber = aadhaarNumber,
                    verificationNotes = verificationNotes
                )
                baseApiManager.kycApi.updateGuarantorKyc(
                    clientId = clientId,
                    guarantorKycId = guarantorKycId,
                    guarantorKycRequest = request
                )
            }
            true -> { // Offline - update local entity
                val updatedGuarantor = GuarantorKycData(
                    id = guarantorKycId,
                    clientId = clientId.toLong(),
                    fullName = fullName,
                    mobileNumber = mobileNumber,
                    relationshipToClient = relationshipToClient,
                    panNumber = panNumber,
                    aadhaarNumber = aadhaarNumber,
                    verificationNotes = verificationNotes,
                    isActive = true,
                    offlineData = true,
                    syncStatus = "PENDING_UPDATE"
                )
                databaseHelperKyc.updateGuarantorKyc(updatedGuarantor)
                    .map { 
                        GenericResponse().apply {
                            responseFields["offline"] = true
                            responseFields["status"] = "updated"
                        }
                    }
            }
        }
    }

    /**
     * Delete Client KYC data with online/offline logic
     */
    fun deleteClientKyc(clientId: Int, kycId: Long): Observable<GenericResponse> {
        return when (prefManager.userStatus) {
            false -> { // Online
                baseApiManager.kycApi.deleteClientKyc(clientId, kycId)
            }
            true -> { // Offline - mark for deletion
                databaseHelperKyc.deleteClientKyc(kycId)
                    .map { 
                        GenericResponse().apply {
                            responseFields["offline"] = true
                            responseFields["status"] = "deleted"
                        }
                    }
            }
        }
    }

    /**
     * Delete Guarantor KYC data with online/offline logic
     */
    fun deleteGuarantorKyc(clientId: Int, guarantorKycId: Long): Observable<GenericResponse> {
        return when (prefManager.userStatus) {
            false -> { // Online
                baseApiManager.kycApi.deleteGuarantorKyc(clientId, guarantorKycId)
            }
            true -> { // Offline - delete both entity and payload
                // In offline mode, we can only delete guarantors from offline-created clients
                // So we just need to remove from local storage (both entity and payload)
                databaseHelperKyc.getGuarantorKycList(clientId)
                    .flatMap { entities ->
                        val guarantorEntity = entities.find { it.id == guarantorKycId }
                        
                        if (guarantorEntity != null) {
                            // Find matching payload and delete it
                            databaseHelperKyc.getGuarantorKycPayloadsByClientId(clientId)
                                .flatMap { payloads ->
                                    val matchingPayload = payloads.find { payload ->
                                        payload.fullName == guarantorEntity.fullName &&
                                        payload.mobileNumber == guarantorEntity.mobileNumber
                                    }
                                    
                                    if (matchingPayload != null) {
                                        // Delete both payload and entity
                                        databaseHelperKyc.deleteGuarantorKycPayload(matchingPayload.id!!)
                                            .flatMap { 
                                                databaseHelperKyc.deleteGuarantorKyc(guarantorKycId)
                                            }
                                            .map { 
                                                GenericResponse().apply {
                                                    responseFields["offline"] = true
                                                    responseFields["status"] = "deleted"
                                                }
                                            }
                                    } else {
                                        // No payload - just delete entity
                                        databaseHelperKyc.deleteGuarantorKyc(guarantorKycId)
                                            .map { 
                                                GenericResponse().apply {
                                                    responseFields["offline"] = true
                                                    responseFields["status"] = "deleted"
                                                }
                                            }
                                    }
                                }
                        } else {
                            Observable.just(GenericResponse().apply {
                                responseFields["offline"] = true
                                responseFields["status"] = "error"
                                responseFields["message"] = "Guarantor not found"
                            })
                        }
                    }
            }
        }
    }

    // ============================================================================
    // OTP VERIFICATION METHODS
    // ============================================================================

    /**
     * Generate OTP for Client Aadhaar verification (requires online connection)
     */
    fun generateOtpForAadhaarVerification(
        clientId: Int,
        request: OtpGenerationRequest,
    ): Observable<GenericResponse> {
        return when (prefManager.userStatus) {
            false -> { // Online
                baseApiManager.kycApi.generateOtpForAadhaarVerification(clientId, request)
            }
            true -> { // Offline - OTP verification requires internet
                Observable.error(Exception("OTP verification requires internet connection. Please go online to verify Aadhaar."))
            }
        }
    }

    /**
     * Submit OTP for Client Aadhaar verification (requires online connection)
     */
    fun submitOtpForAadhaarVerification(
        clientId: Int,
        request: OtpSubmissionRequest,
    ): Observable<GenericResponse> {
        return when (prefManager.userStatus) {
            false -> { // Online
                baseApiManager.kycApi.submitOtpForAadhaarVerification(clientId, request)
            }
            true -> { // Offline - OTP verification requires internet
                Observable.error(Exception("OTP verification requires internet connection. Please go online to verify Aadhaar."))
            }
        }
    }

    /**
     * Generate OTP for Guarantor Aadhaar verification (requires online connection)
     */
    fun generateGuarantorOtpForAadhaarVerification(
        clientId: Int,
        guarantorKycId: Long,
        request: GuarantorOtpGenerationRequest,
    ): Observable<GenericResponse> {
        return when (prefManager.userStatus) {
            false -> { // Online
                baseApiManager.kycApi.generateGuarantorOtpForAadhaarVerification(clientId, guarantorKycId, request)
            }
            true -> { // Offline - OTP verification requires internet
                Observable.error(Exception("OTP verification requires internet connection. Please go online to verify Aadhaar."))
            }
        }
    }

    /**
     * Submit OTP for Guarantor Aadhaar verification (requires online connection)
     */
    fun submitGuarantorOtpForAadhaarVerification(
        clientId: Int,
        guarantorKycId: Long,
        request: GuarantorOtpSubmissionRequest,
    ): Observable<GenericResponse> {
        return when (prefManager.userStatus) {
            false -> { // Online
                baseApiManager.kycApi.submitGuarantorOtpForAadhaarVerification(clientId, guarantorKycId, request)
            }
            true -> { // Offline - OTP verification requires internet
                Observable.error(Exception("OTP verification requires internet connection. Please go online to verify Aadhaar."))
            }
        }
    }

} 