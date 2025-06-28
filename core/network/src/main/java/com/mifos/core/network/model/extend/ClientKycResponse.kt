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
 * Network DTO for Client KYC API responses.
 * 
 * This exactly matches the backend ClientKycData.java structure
 * for proper serialization/deserialization.
 */
data class ClientKycResponse(
    // Basic Information
    val id: Long? = null,
    val clientId: Long? = null,
    val clientName: String? = null,
    val clientDisplayName: String? = null,

    // Document Information
    val panNumber: String? = null,
    val aadhaarNumber: String? = null,
    val voterIdNumber: String? = null,
    val drivingLicenseNumber: String? = null,
    val passportNumber: String? = null,

    // Document Storage URLs
    val panDocumentUrl: String? = null,
    val aadhaarDocumentUrl: String? = null,
    val voterIdDocumentUrl: String? = null,
    val drivingLicenseDocumentUrl: String? = null,
    val passportDocumentUrl: String? = null,

    // Individual Document Verification Status
    val panVerified: Boolean? = null,
    val aadhaarVerified: Boolean? = null,
    val voterIdVerified: Boolean? = null,
    val drivingLicenseVerified: Boolean? = null,
    val passportVerified: Boolean? = null,

    // OTP Verification Fields
    val aadhaarOtpVerified: Boolean? = null,

    // Verification Metadata
    val verificationMethod: String? = null, // Will be enum name from backend
    val verificationMethodCode: String? = null,
    val verificationMethodDescription: String? = null,

    @SerializedName("lastVerifiedOn")
    val lastVerifiedOn: String? = null, // DateArrayDeserializer converts array to string
    val lastVerifiedByUserId: Long? = null,
    val lastVerifiedByUsername: String? = null,

    // API Verification Details
    val apiProvider: String? = null,
    val apiResponseData: String? = null,
    val apiVerificationId: String? = null,

    // Manual Verification Details
    val manualVerificationNotes: String? = null,
    @SerializedName("manualVerificationDate")
    val manualVerificationDate: String? = null, // DateArrayDeserializer converts array to string
    val manualVerifiedByUserId: Long? = null,
    val manualVerifiedByUsername: String? = null,

    // Manual Unverification Details
    @SerializedName("manualUnverificationDate")
    val manualUnverificationDate: String? = null,
    val manualUnverifiedByUserId: Long? = null,
    val manualUnverifiedByUsername: String? = null,
    val manualUnverificationReason: String? = null,

    // Audit Information
    @SerializedName("createdDate")
    val createdDate: String? = null, // DateArrayDeserializer converts array to string
    @SerializedName("lastModifiedDate")
    val lastModifiedDate: String? = null,
    val createdByUserId: Long? = null,
    val lastModifiedByUserId: Long? = null,
) 