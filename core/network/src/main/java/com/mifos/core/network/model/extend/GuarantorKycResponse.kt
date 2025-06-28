/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.network.model.extend

import com.google.gson.annotations.SerializedName

/**
 * Network DTO for Guarantor KYC API responses.
 * 
 * This exactly matches the backend GuarantorKycData.java structure
 * for proper serialization/deserialization.
 */
data class GuarantorKycResponse(
    // Basic Information
    val id: Long? = null,
    val clientId: Long? = null,
    val clientName: String? = null,
    val clientDisplayName: String? = null,

    // Guarantor Core Identity
    val fullName: String? = null,
    val mobileNumber: String? = null,
    val relationshipToClient: String? = null,

    // KYC Document Information
    val panNumber: String? = null,
    val aadhaarNumber: String? = null,

    // Verification Status
    val panVerified: Boolean? = null,
    val aadhaarVerified: Boolean? = null,

    // OTP Verification Fields
    val aadhaarOtpVerified: Boolean? = null,
    val otpClientId: String? = null,
    @SerializedName("otpLastRequestedOn")
    val otpLastRequestedOn: String? = null, // DateArrayDeserializer converts array to string
    @SerializedName("otpVerifiedOn")
    val otpVerifiedOn: String? = null,

    // Manual Verification Fields
    val panManuallyVerified: Boolean? = null,
    val aadhaarManuallyVerified: Boolean? = null,
    val manualVerificationReason: String? = null,

    // Verification Metadata
    val verificationMethod: String? = null, // Will be enum name from backend
    val verificationMethodCode: String? = null,
    val verificationMethodDescription: String? = null,
    @SerializedName("lastVerifiedOn")
    val lastVerifiedOn: String? = null, // DateArrayDeserializer converts array to string
    val verificationProvider: String? = null,
    val verifiedByUserId: Long? = null,
    val verifiedByUsername: String? = null,

    // API Response Data (as String to avoid JsonNode serialization issues)
    val apiResponseData: String? = null,
    val verificationNotes: String? = null,

    // Status Fields
    val isActive: Boolean? = null,
    val isPrimaryGuarantor: Boolean? = null,

    // Audit Information
    @SerializedName("createdDate")
    val createdDate: String? = null, // DateArrayDeserializer converts array to string
    @SerializedName("lastModifiedDate")
    val lastModifiedDate: String? = null,
    val createdByUserId: Long? = null,
    val lastModifiedByUserId: Long? = null,
) 