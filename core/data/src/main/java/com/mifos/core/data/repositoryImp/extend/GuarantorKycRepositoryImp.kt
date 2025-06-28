/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.core.data.repositoryImp.extend

import com.mifos.core.data.repository.extend.GuarantorKycRepository
import com.mifos.core.network.GenericResponse
import com.mifos.core.network.datamanager.extend.DataManagerKyc
import com.mifos.core.objects.client.extend.GuarantorKycPayload
import com.mifos.core.objects.client.extend.kyc.GuarantorKycData
import com.mifos.core.objects.client.extend.kyc.GuarantorKycRequest
import com.mifos.core.objects.client.extend.kyc.GuarantorOtpGenerationRequest
import com.mifos.core.objects.client.extend.kyc.GuarantorOtpSubmissionRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Implementation of GuarantorKycRepository following the established upstream pattern.
 *
 * This implementation:
 * - Delegates all operations to DataManagerKyc (following upstream pattern)
 * - Adapts between Observable (DataManager) and suspend functions (Repository)
 * - Handles data transformation between List<Map<String, Any>> and proper data classes
 * - Maintains proper offline-first architecture through DataManager
 */
class GuarantorKycRepositoryImp @Inject constructor(
    private val dataManagerKyc: DataManagerKyc,
) : GuarantorKycRepository {

    override suspend fun getGuarantorKycList(clientId: Int): List<GuarantorKycData> {
        return dataManagerKyc.getGuarantorKycList(clientId)
    }

    override suspend fun getGuarantorKyc(clientId: Int, guarantorKycId: Long): GuarantorKycData {
        // Since DataManagerKyc only has getGuarantorKyc(clientId) that returns a list,
        // we need to get the list and filter by guarantorKycId
        val guarantorKycList = getGuarantorKycList(clientId)
        return guarantorKycList.firstOrNull { it.id == guarantorKycId }
            ?: throw Exception("Guarantor KYC with ID $guarantorKycId not found for client $clientId")
    }

    override suspend fun createGuarantorKyc(
        clientId: Int,
        request: GuarantorKycRequest,
    ): GenericResponse {
        return suspendCancellableCoroutine { continuation ->
            val payload = GuarantorKycPayload(
                clientId = clientId,
                fullName = request.fullName,
                mobileNumber = request.mobileNumber,
                relationshipToClient = request.relationshipToClient,
                panNumber = request.panNumber,
                aadhaarNumber = request.aadhaarNumber,
                verificationNotes = request.verificationNotes
            )
            dataManagerKyc.createGuarantorKyc(payload)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { response -> 
                        val genericResponse = GenericResponse().apply {
                            responseFields["resourceId"] = (response["resourceId"] ?: response["id"]) ?: 0
                            responseFields["offline"] = response["offline"] ?: false
                        }
                        continuation.resume(genericResponse)
                    },
                    { error -> continuation.resumeWithException(error) }
                )
        }
    }

    override suspend fun updateGuarantorKyc(
        clientId: Int,
        guarantorKycId: Long,
        request: GuarantorKycRequest,
    ): GenericResponse {
        return suspendCancellableCoroutine { continuation ->
            dataManagerKyc.updateGuarantorKyc(
                clientId = clientId,
                guarantorKycId = guarantorKycId,
                fullName = request.fullName,
                mobileNumber = request.mobileNumber,
                relationshipToClient = request.relationshipToClient,
                panNumber = request.panNumber,
                aadhaarNumber = request.aadhaarNumber,
                verificationNotes = request.verificationNotes
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { response -> continuation.resume(response) },
                    { error -> continuation.resumeWithException(error) }
                )
        }
    }

    override suspend fun deleteGuarantorKyc(clientId: Int, guarantorKycId: Long): GenericResponse {
        return suspendCancellableCoroutine { continuation ->
            dataManagerKyc.deleteGuarantorKyc(clientId, guarantorKycId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { response -> continuation.resume(response) },
                    { error -> continuation.resumeWithException(error) }
                )
        }
    }

    override suspend fun generateOtpForGuarantorAadhaarVerification(
        clientId: Int,
        guarantorKycId: Long,
        request: GuarantorOtpGenerationRequest,
    ): GenericResponse {
        return suspendCancellableCoroutine { continuation ->
            dataManagerKyc.generateGuarantorOtpForAadhaarVerification(clientId, guarantorKycId, request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { response -> continuation.resume(response) },
                    { error -> continuation.resumeWithException(error) }
                )
        }
    }

    override suspend fun submitOtpForGuarantorAadhaarVerification(
        clientId: Int,
        guarantorKycId: Long,
        request: GuarantorOtpSubmissionRequest,
    ): GenericResponse {
        return suspendCancellableCoroutine { continuation ->
            dataManagerKyc.submitGuarantorOtpForAadhaarVerification(clientId, guarantorKycId, request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { response -> continuation.resume(response) },
                    { error -> continuation.resumeWithException(error) }
                )
        }
    }


} 