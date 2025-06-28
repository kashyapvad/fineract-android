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

import com.mifos.core.data.repository.extend.ClientKycRepository
import com.mifos.core.network.GenericResponse
import com.mifos.core.network.datamanager.extend.DataManagerKyc
import com.mifos.core.objects.client.extend.ClientKycPayload
import com.mifos.core.objects.client.extend.kyc.ClientKycData
import com.mifos.core.objects.client.extend.kyc.ClientKycRequest
import com.mifos.core.objects.client.extend.kyc.OtpGenerationRequest
import com.mifos.core.objects.client.extend.kyc.OtpSubmissionRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Implementation of ClientKycRepository following the established upstream pattern.
 *
 * This implementation:
 * - Delegates all operations to DataManagerKyc (following upstream pattern)
 * - Adapts between Observable (DataManager) and suspend functions (Repository)
 * - Handles data transformation between Map<String, Any> and proper data classes
 * - Maintains proper offline-first architecture through DataManager
 */
class ClientKycRepositoryImp @Inject constructor(
    private val dataManagerKyc: DataManagerKyc,
) : ClientKycRepository {

    override suspend fun getClientKyc(clientId: Int): ClientKycData {
        return dataManagerKyc.getClientKyc(clientId)
    }

    override suspend fun getClientKycTemplate(clientId: Int): ClientKycData {
        return dataManagerKyc.getClientKycTemplate(clientId)
    }

    override suspend fun createClientKyc(clientId: Int, request: ClientKycRequest): GenericResponse {
        return suspendCancellableCoroutine { continuation ->
            val payload = ClientKycPayload(
                clientId = clientId,
                panNumber = request.panNumber,
                aadhaarNumber = request.aadhaarNumber,
                voterId = request.voterId,
                drivingLicenseNumber = request.drivingLicenseNumber,
                passportNumber = request.passportNumber,
                verificationNotes = request.verificationNotes
            )
            dataManagerKyc.createClientKyc(payload)
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

    override suspend fun updateClientKyc(
        clientId: Int,
        kycId: Long,
        request: ClientKycRequest,
    ): GenericResponse {
        return suspendCancellableCoroutine { continuation ->
            dataManagerKyc.updateClientKyc(
                clientId = clientId,
                kycId = kycId,
                panNumber = request.panNumber,
                aadhaarNumber = request.aadhaarNumber,
                voterId = request.voterId,
                drivingLicenseNumber = request.drivingLicenseNumber,
                passportNumber = request.passportNumber,
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

    override suspend fun deleteClientKyc(clientId: Int, kycId: Long): GenericResponse {
        return suspendCancellableCoroutine { continuation ->
            dataManagerKyc.deleteClientKyc(clientId, kycId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { response -> continuation.resume(response) },
                    { error -> continuation.resumeWithException(error) }
                )
        }
    }

    override suspend fun generateOtpForAadhaarVerification(
        clientId: Int,
        request: OtpGenerationRequest,
    ): GenericResponse {
        return suspendCancellableCoroutine { continuation ->
            dataManagerKyc.generateOtpForAadhaarVerification(clientId, request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { response -> continuation.resume(response) },
                    { error -> continuation.resumeWithException(error) }
                )
        }
    }

    override suspend fun submitOtpForAadhaarVerification(
        clientId: Int,
        request: OtpSubmissionRequest,
    ): GenericResponse {
        return suspendCancellableCoroutine { continuation ->
            dataManagerKyc.submitOtpForAadhaarVerification(clientId, request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { response -> continuation.resume(response) },
                    { error -> continuation.resumeWithException(error) }
                )
        }
    }


} 