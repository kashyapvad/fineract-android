/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.network.mappers.clients.extend

import com.mifos.core.network.model.extend.ClientKycResponse
import com.mifos.core.network.model.extend.GuarantorKycResponse
import com.mifos.core.objects.client.extend.kyc.ClientKycData
import com.mifos.core.objects.client.extend.kyc.GuarantorKycData
import org.mifos.core.data.AbstractMapper

/**
 * Mapper for Client KYC API responses, following the same pattern as ClientMapper.
 * 
 * Maps between network DTOs and core domain models using AbstractMapper.
 */
object ClientKycMapper : AbstractMapper<ClientKycResponse, ClientKycData>() {

    override fun mapFromEntity(entity: ClientKycResponse): ClientKycData {
        return ClientKycData(
            id = entity.id,
            clientId = entity.clientId,
            clientName = entity.clientName,
            clientDisplayName = entity.clientDisplayName,
            panNumber = entity.panNumber,
            aadhaarNumber = entity.aadhaarNumber,
            voterIdNumber = entity.voterIdNumber,
            drivingLicenseNumber = entity.drivingLicenseNumber,
            passportNumber = entity.passportNumber,
            panVerified = entity.panVerified ?: false,
            aadhaarVerified = entity.aadhaarVerified ?: false,
            voterIdVerified = entity.voterIdVerified ?: false,
            drivingLicenseVerified = entity.drivingLicenseVerified ?: false,
            passportVerified = entity.passportVerified ?: false,
            verificationMethod = entity.verificationMethod,
            verificationMethodCode = entity.verificationMethodCode,
            verificationMethodDescription = entity.verificationMethodDescription,
            lastVerifiedOn = entity.lastVerifiedOn,
            lastVerifiedByUserId = entity.lastVerifiedByUserId,
            lastVerifiedByUsername = entity.lastVerifiedByUsername,
            manualVerificationNotes = entity.manualVerificationNotes,
            manualVerificationDate = entity.manualVerificationDate,
            manualVerifiedByUserId = entity.manualVerifiedByUserId,
            manualVerifiedByUsername = entity.manualVerifiedByUsername,
            aadhaarOtpVerified = entity.aadhaarOtpVerified ?: false,
            otpClientId = null, // Not in response DTO
            otpLastRequestedOn = null, // Not in response DTO
            otpVerifiedOn = null, // Not in response DTO
            apiResponseData = entity.apiResponseData,
            isActive = true, // Default for active KYC records
            createdDate = entity.createdDate,
            lastModifiedDate = entity.lastModifiedDate,
            createdByUserId = entity.createdByUserId,
            lastModifiedByUserId = entity.lastModifiedByUserId,
        )
    }

    override fun mapToEntity(domainModel: ClientKycData): ClientKycResponse {
        return ClientKycResponse(
            id = domainModel.id,
            clientId = domainModel.clientId,
            clientName = domainModel.clientName,
            clientDisplayName = domainModel.clientDisplayName,
            panNumber = domainModel.panNumber,
            aadhaarNumber = domainModel.aadhaarNumber,
            voterIdNumber = domainModel.voterIdNumber,
            drivingLicenseNumber = domainModel.drivingLicenseNumber,
            passportNumber = domainModel.passportNumber,
            panVerified = domainModel.panVerified,
            aadhaarVerified = domainModel.aadhaarVerified,
            voterIdVerified = domainModel.voterIdVerified,
            drivingLicenseVerified = domainModel.drivingLicenseVerified,
            passportVerified = domainModel.passportVerified,
            aadhaarOtpVerified = domainModel.aadhaarOtpVerified,
            verificationMethod = domainModel.verificationMethod,
            verificationMethodCode = domainModel.verificationMethodCode,
            verificationMethodDescription = domainModel.verificationMethodDescription,
            lastVerifiedOn = domainModel.lastVerifiedOn,
            lastVerifiedByUserId = domainModel.lastVerifiedByUserId,
            lastVerifiedByUsername = domainModel.lastVerifiedByUsername,
            manualVerificationNotes = domainModel.manualVerificationNotes,
            manualVerificationDate = domainModel.manualVerificationDate,
            manualVerifiedByUserId = domainModel.manualVerifiedByUserId,
            manualVerifiedByUsername = domainModel.manualVerifiedByUsername,
            apiResponseData = domainModel.apiResponseData,
            createdDate = domainModel.createdDate,
            lastModifiedDate = domainModel.lastModifiedDate,
            createdByUserId = domainModel.createdByUserId,
            lastModifiedByUserId = domainModel.lastModifiedByUserId,
        )
    }


}

/**
 * Mapper for Guarantor KYC API responses, following the same pattern as ClientMapper.
 */
object GuarantorKycMapper : AbstractMapper<GuarantorKycResponse, GuarantorKycData>() {

    override fun mapFromEntity(entity: GuarantorKycResponse): GuarantorKycData {
        return GuarantorKycData(
            id = entity.id,
            clientId = entity.clientId,
            fullName = entity.fullName,
            mobileNumber = entity.mobileNumber,
            relationshipToClient = entity.relationshipToClient,
            panNumber = entity.panNumber,
            aadhaarNumber = entity.aadhaarNumber,
            panVerified = entity.panVerified ?: false,
            aadhaarVerified = entity.aadhaarVerified ?: false,
            verificationMethod = entity.verificationMethod,
            verificationMethodCode = entity.verificationMethodCode,
            verificationMethodDescription = entity.verificationMethodDescription,
            verificationNotes = entity.verificationNotes,
            lastVerifiedOn = entity.lastVerifiedOn, // DateArrayDeserializer handles conversion
            lastVerifiedByUserId = entity.verifiedByUserId,
            lastVerifiedByUsername = entity.verifiedByUsername,
            manualVerificationNotes = entity.manualVerificationReason,
            manualVerificationDate = null, // Not in guarantor response
            manualVerifiedByUserId = null, // Not in guarantor response
            manualVerifiedByUsername = null, // Not in guarantor response
            aadhaarOtpVerified = entity.aadhaarOtpVerified ?: false,
            otpClientId = entity.otpClientId,
            otpLastRequestedOn = entity.otpLastRequestedOn, // DateArrayDeserializer handles conversion
            otpVerifiedOn = entity.otpVerifiedOn, // DateArrayDeserializer handles conversion
            isPrimaryGuarantor = entity.isPrimaryGuarantor ?: false,
            guarantorType = null, // Derived field, not in response
            isActive = entity.isActive ?: true,
            createdDate = entity.createdDate, // DateArrayDeserializer handles conversion
            lastModifiedDate = entity.lastModifiedDate, // DateArrayDeserializer handles conversion
            createdByUserId = entity.createdByUserId,
            lastModifiedByUserId = entity.lastModifiedByUserId,
        )
    }

    override fun mapToEntity(domainModel: GuarantorKycData): GuarantorKycResponse {
        return GuarantorKycResponse(
            id = domainModel.id,
            clientId = domainModel.clientId,
            fullName = domainModel.fullName,
            mobileNumber = domainModel.mobileNumber,
            relationshipToClient = domainModel.relationshipToClient,
            panNumber = domainModel.panNumber,
            aadhaarNumber = domainModel.aadhaarNumber,
            panVerified = domainModel.panVerified,
            aadhaarVerified = domainModel.aadhaarVerified,
            aadhaarOtpVerified = domainModel.aadhaarOtpVerified,
            otpClientId = domainModel.otpClientId,
            otpLastRequestedOn = domainModel.otpLastRequestedOn, // Network layer expects strings
            otpVerifiedOn = domainModel.otpVerifiedOn, // Network layer expects strings
            verificationMethod = domainModel.verificationMethod,
            verificationMethodCode = domainModel.verificationMethodCode,
            verificationMethodDescription = domainModel.verificationMethodDescription,
            lastVerifiedOn = domainModel.lastVerifiedOn, // Network layer expects strings
            verifiedByUserId = domainModel.lastVerifiedByUserId,
            verifiedByUsername = domainModel.lastVerifiedByUsername,
            verificationNotes = domainModel.verificationNotes,
            isActive = domainModel.isActive,
            isPrimaryGuarantor = domainModel.isPrimaryGuarantor,
            createdDate = domainModel.createdDate, // Network layer expects strings
            lastModifiedDate = domainModel.lastModifiedDate, // Network layer expects strings
            createdByUserId = domainModel.createdByUserId,
            lastModifiedByUserId = domainModel.lastModifiedByUserId,
        )
    }
} 