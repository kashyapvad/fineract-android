/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.network.services.extend

import com.mifos.core.model.APIEndPoint
import com.mifos.core.network.GenericResponse
import com.mifos.core.network.model.extend.ClientKycResponse
import com.mifos.core.network.model.extend.GuarantorKycResponse
import com.mifos.core.objects.client.extend.ClientKycPayload
import com.mifos.core.objects.client.extend.GuarantorKycPayload
import com.mifos.core.objects.client.extend.kyc.ClientKycRequest
import com.mifos.core.objects.client.extend.kyc.GuarantorKycRequest
import com.mifos.core.objects.client.extend.kyc.OtpGenerationRequest
import com.mifos.core.objects.client.extend.kyc.OtpSubmissionRequest
import com.mifos.core.objects.client.extend.kyc.GuarantorOtpGenerationRequest
import com.mifos.core.objects.client.extend.kyc.GuarantorOtpSubmissionRequest
import retrofit2.http.*
import rx.Observable

/**
 * KYC service interface for API operations.
 * 
 * Defines REST API endpoints for Client KYC operations following the backend API specification
 * and using APIEndPoint constants like other upstream services.
 */
interface KycService {

    // ==============================================================================
    // CLIENT KYC OPERATIONS
    // ==============================================================================

    /**
     * Retrieve Client KYC Details
     * GET /clients/{clientId}/extend/kyc
     */
    @GET(APIEndPoint.CLIENTS + "/{clientId}/" + APIEndPoint.EXTEND_KYC)
    suspend fun getClientKyc(@Path("clientId") clientId: Int): ClientKycResponse

    /**
     * Create KYC Details
     * POST /clients/{clientId}/extend/kyc
     */
    @POST(APIEndPoint.CLIENTS + "/{clientId}/" + APIEndPoint.EXTEND_KYC)
    fun createClientKyc(
        @Path("clientId") clientId: Int,
        @Body clientKycPayload: ClientKycPayload,
    ): Observable<GenericResponse>

    /**
     * Update KYC Details
     * PUT /clients/{clientId}/extend/kyc/{kycId}
     */
    @PUT(APIEndPoint.CLIENTS + "/{clientId}/" + APIEndPoint.EXTEND_KYC + "/{kycId}")
    fun updateClientKyc(
        @Path("clientId") clientId: Int,
        @Path("kycId") kycId: Long,
        @Body clientKycRequest: ClientKycRequest,
    ): Observable<GenericResponse>

    /**
     * Delete KYC Details
     * DELETE /clients/{clientId}/extend/kyc/{kycId}
     */
    @DELETE(APIEndPoint.CLIENTS + "/{clientId}/" + APIEndPoint.EXTEND_KYC + "/{kycId}")
    fun deleteClientKyc(
        @Path("clientId") clientId: Int,
        @Path("kycId") kycId: Long,
    ): Observable<GenericResponse>

    /**
     * Generate OTP for Aadhaar Verification
     * POST /clients/{clientId}/extend/kyc/verify/otp/generate
     */
    @POST(APIEndPoint.CLIENTS + "/{clientId}/" + APIEndPoint.EXTEND_KYC + "/verify/otp/generate")
    fun generateOtpForAadhaarVerification(
        @Path("clientId") clientId: Int,
        @Body otpRequest: OtpGenerationRequest,
    ): Observable<GenericResponse>

    /**
     * Submit OTP for Aadhaar Verification
     * POST /clients/{clientId}/extend/kyc/verify/otp/submit
     */
    @POST(APIEndPoint.CLIENTS + "/{clientId}/" + APIEndPoint.EXTEND_KYC + "/verify/otp/submit")
    fun submitOtpForAadhaarVerification(
        @Path("clientId") clientId: Int,
        @Body otpSubmission: OtpSubmissionRequest,
    ): Observable<GenericResponse>

    // ==============================================================================
    // GUARANTOR KYC OPERATIONS
    // ==============================================================================

    /**
     * Retrieve Guarantor KYC List
     * GET /clients/{clientId}/extend/guarantor-kyc
     */
    @GET(APIEndPoint.CLIENTS + "/{clientId}/" + APIEndPoint.EXTEND_GUARANTOR_KYC)
    suspend fun getGuarantorKyc(@Path("clientId") clientId: Int): List<GuarantorKycResponse>

    /**
     * Create Guarantor KYC
     * POST /clients/{clientId}/extend/guarantor-kyc
     */
    @POST(APIEndPoint.CLIENTS + "/{clientId}/" + APIEndPoint.EXTEND_GUARANTOR_KYC)
    fun createGuarantorKyc(
        @Path("clientId") clientId: Int,
        @Body guarantorKycPayload: GuarantorKycPayload,
    ): Observable<GenericResponse>

    /**
     * Update Guarantor KYC
     * PUT /clients/{clientId}/extend/guarantor-kyc/{guarantorKycId}
     */
    @PUT(APIEndPoint.CLIENTS + "/{clientId}/" + APIEndPoint.EXTEND_GUARANTOR_KYC + "/{guarantorKycId}")
    fun updateGuarantorKyc(
        @Path("clientId") clientId: Int,
        @Path("guarantorKycId") guarantorKycId: Long,
        @Body guarantorKycRequest: GuarantorKycRequest,
    ): Observable<GenericResponse>

    /**
     * Delete Guarantor KYC
     * DELETE /clients/{clientId}/extend/guarantor-kyc/{guarantorKycId}
     */
    @DELETE(APIEndPoint.CLIENTS + "/{clientId}/" + APIEndPoint.EXTEND_GUARANTOR_KYC + "/{guarantorKycId}")
    fun deleteGuarantorKyc(
        @Path("clientId") clientId: Int,
        @Path("guarantorKycId") guarantorKycId: Long,
    ): Observable<GenericResponse>

    /**
     * Generate OTP for Guarantor Aadhaar Verification
     * POST /clients/{clientId}/extend/guarantor-kyc/{guarantorKycId}/verify/otp/generate
     */
    @POST(APIEndPoint.CLIENTS + "/{clientId}/" + APIEndPoint.EXTEND_GUARANTOR_KYC + "/{guarantorKycId}/verify/otp/generate")
    fun generateGuarantorOtpForAadhaarVerification(
        @Path("clientId") clientId: Int,
        @Path("guarantorKycId") guarantorKycId: Long,
        @Body otpRequest: GuarantorOtpGenerationRequest,
    ): Observable<GenericResponse>

    /**
     * Submit OTP for Guarantor Aadhaar Verification
     * POST /clients/{clientId}/extend/guarantor-kyc/{guarantorKycId}/verify/otp/submit
     */
    @POST(APIEndPoint.CLIENTS + "/{clientId}/" + APIEndPoint.EXTEND_GUARANTOR_KYC + "/{guarantorKycId}/verify/otp/submit")
    fun submitGuarantorOtpForAadhaarVerification(
        @Path("clientId") clientId: Int,
        @Path("guarantorKycId") guarantorKycId: Long,
        @Body otpSubmission: GuarantorOtpSubmissionRequest,
    ): Observable<GenericResponse>
} 