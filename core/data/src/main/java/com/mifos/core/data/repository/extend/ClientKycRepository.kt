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

import com.mifos.core.network.GenericResponse
import com.mifos.core.objects.client.extend.kyc.ClientKycData
import com.mifos.core.objects.client.extend.kyc.ClientKycRequest
import com.mifos.core.objects.client.extend.kyc.OtpGenerationRequest
import com.mifos.core.objects.client.extend.kyc.OtpSubmissionRequest

/**
 * Repository interface for Client KYC operations.
 *
 * Follows the offline-first architecture pattern used throughout the app.
 * Provides a clean abstraction between the UI layer and data sources.
 */
interface ClientKycRepository {

    /**
     * Get client KYC details.
     *
     * @param clientId The client ID
     * @return ClientKycData or template if no KYC exists
     */
    suspend fun getClientKyc(clientId: Int): ClientKycData

    /**
     * Get client KYC template for creating new KYC.
     *
     * @param clientId The client ID
     * @return ClientKycData template
     */
    suspend fun getClientKycTemplate(clientId: Int): ClientKycData

    /**
     * Create new client KYC details.
     *
     * @param clientId The client ID
     * @param request The KYC creation request
     * @return GenericResponse with creation result
     */
    suspend fun createClientKyc(clientId: Int, request: ClientKycRequest): GenericResponse

    /**
     * Update existing client KYC details.
     *
     * @param clientId The client ID
     * @param kycId The KYC record ID
     * @param request The KYC update request
     * @return GenericResponse with update result
     */
    suspend fun updateClientKyc(
        clientId: Int,
        kycId: Long,
        request: ClientKycRequest,
    ): GenericResponse

    /**
     * Delete client KYC details.
     *
     * @param clientId The client ID
     * @param kycId The KYC record ID
     * @return GenericResponse with deletion result
     */
    suspend fun deleteClientKyc(clientId: Int, kycId: Long): GenericResponse

    /**
     * Generate OTP for Aadhaar verification.
     *
     * @param clientId The client ID
     * @param request The OTP generation request
     * @return GenericResponse with OTP generation result
     */
    suspend fun generateOtpForAadhaarVerification(
        clientId: Int,
        request: OtpGenerationRequest,
    ): GenericResponse

    /**
     * Submit OTP for Aadhaar verification.
     *
     * @param clientId The client ID
     * @param request The OTP submission request
     * @return GenericResponse with verification result
     */
    suspend fun submitOtpForAadhaarVerification(
        clientId: Int,
        request: OtpSubmissionRequest,
    ): GenericResponse
} 