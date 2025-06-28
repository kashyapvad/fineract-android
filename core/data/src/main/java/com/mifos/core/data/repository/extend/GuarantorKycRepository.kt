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
import com.mifos.core.objects.client.extend.kyc.GuarantorKycData
import com.mifos.core.objects.client.extend.kyc.GuarantorKycRequest
import com.mifos.core.objects.client.extend.kyc.GuarantorOtpGenerationRequest
import com.mifos.core.objects.client.extend.kyc.GuarantorOtpSubmissionRequest

/**
 * Repository interface for Guarantor KYC operations.
 *
 * Follows the offline-first architecture pattern used throughout the app.
 * Provides a clean abstraction between the UI layer and data sources.
 */
interface GuarantorKycRepository {

    /**
     * Get all guarantor KYC details for a client.
     *
     * @param clientId The client ID
     * @return List of GuarantorKycData
     */
    suspend fun getGuarantorKycList(clientId: Int): List<GuarantorKycData>

    /**
     * Get specific guarantor KYC details.
     *
     * @param clientId The client ID
     * @param guarantorKycId The guarantor KYC ID
     * @return GuarantorKycData
     */
    suspend fun getGuarantorKyc(clientId: Int, guarantorKycId: Long): GuarantorKycData

    /**
     * Create new guarantor KYC details.
     *
     * @param clientId The client ID
     * @param request The guarantor KYC creation request
     * @return GenericResponse with creation result
     */
    suspend fun createGuarantorKyc(
        clientId: Int,
        request: GuarantorKycRequest,
    ): GenericResponse

    /**
     * Update existing guarantor KYC details.
     *
     * @param clientId The client ID
     * @param guarantorKycId The guarantor KYC ID
     * @param request The guarantor KYC update request
     * @return GenericResponse with update result
     */
    suspend fun updateGuarantorKyc(
        clientId: Int,
        guarantorKycId: Long,
        request: GuarantorKycRequest,
    ): GenericResponse

    /**
     * Delete guarantor KYC details.
     *
     * @param clientId The client ID
     * @param guarantorKycId The guarantor KYC ID
     * @return GenericResponse with deletion result
     */
    suspend fun deleteGuarantorKyc(clientId: Int, guarantorKycId: Long): GenericResponse

    /**
     * Generate OTP for guarantor Aadhaar verification.
     *
     * @param clientId The client ID
     * @param guarantorKycId The guarantor KYC ID
     * @param request The OTP generation request
     * @return GenericResponse with OTP generation result
     */
    suspend fun generateOtpForGuarantorAadhaarVerification(
        clientId: Int,
        guarantorKycId: Long,
        request: GuarantorOtpGenerationRequest,
    ): GenericResponse

    /**
     * Submit OTP for guarantor Aadhaar verification.
     *
     * @param clientId The client ID
     * @param guarantorKycId The guarantor KYC ID
     * @param request The OTP submission request
     * @return GenericResponse with verification result
     */
    suspend fun submitOtpForGuarantorAadhaarVerification(
        clientId: Int,
        guarantorKycId: Long,
        request: GuarantorOtpSubmissionRequest,
    ): GenericResponse
} 