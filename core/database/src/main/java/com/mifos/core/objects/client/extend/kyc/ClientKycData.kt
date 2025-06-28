/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.objects.client.extend.kyc

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.mifos.core.database.MifosDatabase
import com.mifos.core.model.MifosBaseModel
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ModelContainer
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import kotlinx.parcelize.Parcelize

/**
 * Client KYC Data model for core layer.
 */
@Parcelize
@Table(database = MifosDatabase::class, useBooleanGetterSetters = false)
@ModelContainer
data class ClientKycData(
    // Basic Information
    @PrimaryKey
    var id: Long? = null,
    
    @Column
    var clientId: Long? = null,
    
    @Column
    var clientName: String? = null,
    
    @Column
    var clientDisplayName: String? = null,

    // Document Information
    @Column
    var panNumber: String? = null,
    
    @Column
    var aadhaarNumber: String? = null,
    
    @Column
    var voterIdNumber: String? = null,
    
    @Column
    var drivingLicenseNumber: String? = null,
    
    @Column
    var passportNumber: String? = null,

    // Individual Document Verification Status
    @Column
    var panVerified: Boolean = false,
    
    @Column
    var aadhaarVerified: Boolean = false,
    
    @Column
    var voterIdVerified: Boolean = false,
    
    @Column
    var drivingLicenseVerified: Boolean = false,
    
    @Column
    var passportVerified: Boolean = false,

    // Verification Metadata
    @Column
    var verificationMethod: String? = null,
    
    @Column
    var verificationMethodCode: String? = null,
    
    @Column
    var verificationMethodDescription: String? = null,

    @Column
    @SerializedName("lastVerifiedOn")
    var lastVerifiedOn: String? = null,
    
    @Column
    var lastVerifiedByUserId: Long? = null,
    
    @Column
    var lastVerifiedByUsername: String? = null,

    // Manual Verification Details
    @Column
    var manualVerificationNotes: String? = null,
    
    @Column
    @SerializedName("manualVerificationDate")
    var manualVerificationDate: String? = null,
    
    @Column
    var manualVerifiedByUserId: Long? = null,
    
    @Column
    var manualVerifiedByUsername: String? = null,

    // OTP Verification Fields
    @Column
    var aadhaarOtpVerified: Boolean = false,
    
    @Column
    var otpClientId: String? = null,
    
    @Column
    @SerializedName("otpLastRequestedOn")
    var otpLastRequestedOn: String? = null,
    
    @Column
    @SerializedName("otpVerifiedOn") 
    var otpVerifiedOn: String? = null,

    // API Response Data
    @Column
    var apiResponseData: String? = null,

    // Status Fields (DBFlow issue with "is" prefix)
    var isActive: Boolean = true,

    // Audit Information
    @Column
    @SerializedName("createdDate")
    var createdDate: String? = null,
    
    @Column
    @SerializedName("lastModifiedDate")
    var lastModifiedDate: String? = null,
    
    @Column
    var createdByUserId: Long? = null,
    
    @Column
    var lastModifiedByUserId: Long? = null,

    // Offline Support Fields
    @Column
    var syncStatus: String? = null,
    
    @Column
    var errorMessage: String? = null,

    // Offline state tracking
    @Column
    var offlineData: Boolean = false,

    // Additional fields
    @Column
    var createdAt: String? = null,
    
    @Column
    var updatedAt: String? = null,
) : MifosBaseModel(), Parcelable

/**
 * Client KYC Create/Update Request model.
 */
@Parcelize
data class ClientKycRequest(
    val panNumber: String? = null,
    val aadhaarNumber: String? = null,
    val voterId: String? = null,
    val drivingLicenseNumber: String? = null,
    val passportNumber: String? = null,
    val verificationNotes: String? = null,
) : Parcelable

/**
 * OTP Generation Request model.
 */
@Parcelize
data class OtpGenerationRequest(
    val aadhaarNumber: String? = null,
) : Parcelable

/**
 * OTP Submission Request model.
 */
@Parcelize
data class OtpSubmissionRequest(
    val otp: String,
    val notes: String? = null,
) : Parcelable 