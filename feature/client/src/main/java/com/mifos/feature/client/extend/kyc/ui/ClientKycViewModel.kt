/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.feature.client.extend.kyc.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mifos.core.datastore.PrefManager
import com.mifos.core.objects.client.Client
import com.mifos.core.objects.client.extend.kyc.ClientKycData
import com.mifos.core.objects.client.extend.kyc.ClientKycRequest
import com.mifos.core.objects.client.extend.kyc.OtpGenerationRequest
import com.mifos.core.objects.client.extend.kyc.OtpSubmissionRequest
import com.mifos.core.data.repository.extend.ClientKycRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing Client KYC workflow.
 * Handles both creation and editing of KYC details with proper offline support.
 * 
 * Fixed references to KycSyncStatus and improved OTP verification logic
 */
@HiltViewModel
class ClientKycViewModel @Inject constructor(
    private val clientKycRepository: ClientKycRepository,
    private val prefManager: PrefManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ClientKycUiState>(ClientKycUiState.Loading)
    val uiState: StateFlow<ClientKycUiState> = _uiState.asStateFlow()

    fun initializeKyc(clientId: Int, isOffline: Boolean = false) {
        viewModelScope.launch {
            try {
                _uiState.value = ClientKycUiState.Loading

                // Create client for display
                val client = Client().apply {
                    id = clientId
                    displayName = "Client $clientId"
                    accountNo = "ACC$clientId"
                    active = true
                }

                // Try to get existing KYC data first
                val existingKyc = try {
                    clientKycRepository.getClientKyc(clientId)
                } catch (e: Exception) {
                    null
                }

                if (existingKyc != null && hasKycData(existingKyc)) {
                    // KYC already exists, show appropriate view based on sync status
                    
                    client.displayName = existingKyc.clientDisplayName ?: "Client $clientId"
                    
                    // Unified: Use KycExists for both online and offline
                    _uiState.value = ClientKycUiState.KycExists(
                        client = client,
                        kycData = existingKyc,
                    )
                } else {
                    // No KYC exists, show form
                    val formState = ClientKycUiState.FormReady(
                        client = client,
                        formData = ClientKycFormData(),
                        syncStatus = existingKyc?.syncStatus,
                        isLoading = false,
                        isEditMode = false,
                    )
                    _uiState.value = formState
                }
            } catch (e: Exception) {
                println("DEBUG: ClientKycViewModel initializeKyc error: ${e.message}")
                _uiState.value = ClientKycUiState.Error(
                    e.message ?: "Failed to initialize KYC",
                )
            }
        }
    }

    private fun hasKycData(kycData: ClientKycData): Boolean {
        // Check if KYC has any meaningful data beyond just client info
        return !kycData.panNumber.isNullOrBlank() ||
                !kycData.aadhaarNumber.isNullOrBlank() ||
                !kycData.voterIdNumber.isNullOrBlank() ||
                !kycData.drivingLicenseNumber.isNullOrBlank() ||
                !kycData.passportNumber.isNullOrBlank()
    }

    fun enableEditMode() {
        val currentState = _uiState.value
        if (currentState is ClientKycUiState.KycExists) {
            val formData = ClientKycFormData(
                panNumber = currentState.kycData.panNumber ?: "",
                aadhaarNumber = currentState.kycData.aadhaarNumber ?: "",
                drivingLicenseNumber = currentState.kycData.drivingLicenseNumber ?: "",
                voterId = currentState.kycData.voterIdNumber ?: "",
                passportNumber = currentState.kycData.passportNumber ?: "",
                verificationNotes = currentState.kycData.manualVerificationNotes ?: "",
            )
            
            _uiState.value = ClientKycUiState.FormReady(
                client = currentState.client,
                formData = formData,
                syncStatus = currentState.kycData.syncStatus,
                isLoading = false,
                isEditMode = true,
            )
        }
    }

    fun initiateOtpVerification() {
        val currentState = _uiState.value
        when (currentState) {
            is ClientKycUiState.KycExists -> {
                // Validate Aadhaar number exists before initiating OTP
                if (currentState.kycData.aadhaarNumber.isNullOrBlank()) {
                    _uiState.value = ClientKycUiState.Error(
                        "Aadhaar number is required for OTP verification. Please edit KYC details to add Aadhaar number."
                    )
                    return
                }
                
                _uiState.value = ClientKycUiState.OtpVerificationForm(
                    client = currentState.client,
                    kycData = currentState.kycData,
                    isGeneratingOtp = false,
                    otpGenerated = false,
                    isSubmittingOtp = false
                )
            }
            is ClientKycUiState.FormReady -> {
                // Validate form has Aadhaar number
                if (currentState.formData.aadhaarNumber.isBlank()) {
                    _uiState.value = ClientKycUiState.Error(
                        "Aadhaar number is required for OTP verification. Please add Aadhaar number in the form."
                    )
                    return
                }
                
                // Create KYC data from form for OTP verification
                val kycData = ClientKycData(
                    clientId = currentState.client.id.toLong(),
                    clientDisplayName = currentState.client.displayName,
                    panNumber = currentState.formData.panNumber.ifBlank { null },
                    aadhaarNumber = currentState.formData.aadhaarNumber,
                    voterIdNumber = currentState.formData.voterId.ifBlank { null },
                    drivingLicenseNumber = currentState.formData.drivingLicenseNumber.ifBlank { null },
                    passportNumber = currentState.formData.passportNumber.ifBlank { null },
                    manualVerificationNotes = currentState.formData.verificationNotes.ifBlank { null }
                )
                
                _uiState.value = ClientKycUiState.OtpVerificationForm(
                    client = currentState.client,
                    kycData = kycData,
                    isGeneratingOtp = false,
                    otpGenerated = false,
                    isSubmittingOtp = false
                )
            }
            else -> {
                _uiState.value = ClientKycUiState.Error(
                    "Invalid state for OTP verification"
                )
            }
        }
    }

    fun generateOtp() {
        val currentState = _uiState.value
        if (currentState is ClientKycUiState.OtpVerificationForm) {
            viewModelScope.launch {
                try {
                    _uiState.value = currentState.copy(isGeneratingOtp = true)
                    
                    // Validate Aadhaar number exists
                    val aadhaarNumber = currentState.kycData.aadhaarNumber
                    if (aadhaarNumber.isNullOrBlank()) {
                        _uiState.value = ClientKycUiState.Error(
                            "Aadhaar number is required for OTP verification. Please edit KYC details to add Aadhaar number."
                        )
                        return@launch
                    }
                    
                    val request = OtpGenerationRequest(
                        aadhaarNumber = aadhaarNumber
                    )
                    
                    clientKycRepository.generateOtpForAadhaarVerification(
                        currentState.client.id,
                        request
                    )
                    
                    _uiState.value = currentState.copy(
                        isGeneratingOtp = false,
                        otpGenerated = true
                    )
                } catch (e: Exception) {
                    println("DEBUG: ClientKycViewModel generateOtp error: ${e.message}")
                    _uiState.value = ClientKycUiState.Error(
                        e.message ?: "Failed to generate OTP"
                    )
                }
            }
        }
    }

    fun submitOtp(otp: String, notes: String = "") {
        val currentState = _uiState.value
        if (currentState is ClientKycUiState.OtpVerificationForm) {
            viewModelScope.launch {
                try {
                    _uiState.value = currentState.copy(isSubmittingOtp = true)
                    
                    val request = OtpSubmissionRequest(
                        otp = otp,
                        notes = notes.ifBlank { null }
                    )
                    
                    clientKycRepository.submitOtpForAadhaarVerification(
                        currentState.client.id,
                        request
                    )
                    
                    _uiState.value = ClientKycUiState.Success(
                        client = currentState.client,
                        message = "Aadhaar verification completed successfully via OTP"
                    )
                } catch (e: Exception) {
                    println("DEBUG: ClientKycViewModel submitOtp error: ${e.message}")
                    _uiState.value = ClientKycUiState.Error(
                        e.message ?: "Failed to verify OTP"
                    )
                }
            }
        }
    }

    fun cancelOtpVerification() {
        val currentState = _uiState.value
        if (currentState is ClientKycUiState.OtpVerificationForm) {
            _uiState.value = ClientKycUiState.KycExists(
                client = currentState.client,
                kycData = currentState.kycData
            )
        }
    }

    fun updateFormData(formData: ClientKycFormData) {
        val currentState = _uiState.value
        if (currentState is ClientKycUiState.FormReady) {
            _uiState.value = currentState.copy(formData = formData)
        }
    }

    fun submitKyc(formData: ClientKycFormData) {
        val currentState = _uiState.value
        if (currentState is ClientKycUiState.FormReady) {
            viewModelScope.launch {
                try {
                    _uiState.value = currentState.copy(isLoading = true)

                    // Validate that at least one document field is provided
                    if (!isFormValid(formData)) {
                        _uiState.value = ClientKycUiState.Error(
                            "Please provide at least one KYC document number"
                        )
                        return@launch
                    }

                    val request = ClientKycRequest(
                        panNumber = formData.panNumber.ifBlank { null },
                        aadhaarNumber = formData.aadhaarNumber.ifBlank { null },
                        drivingLicenseNumber = formData.drivingLicenseNumber.ifBlank { null },
                        voterId = formData.voterId.ifBlank { null },
                        passportNumber = formData.passportNumber.ifBlank { null },
                        verificationNotes = formData.verificationNotes.ifBlank { null },
                    )

                    // Create/update KYC (works both online and offline)
                    clientKycRepository.createClientKyc(
                        currentState.client.id,
                        request,
                    )

                    val successMessage = if (isOfflineMode()) {
                        "KYC saved for sync when online"
                    } else {
                        "KYC ${if (currentState.syncStatus != null) "updated" else "created"} successfully"
                    }

                    _uiState.value = ClientKycUiState.Success(
                        client = currentState.client,
                        message = successMessage,
                    )
                } catch (e: Exception) {
                    println("DEBUG: ClientKycViewModel submitKyc error: ${e.message}")
                    _uiState.value = ClientKycUiState.Error(
                        e.message ?: "Failed to submit KYC",
                    )
                }
            }
        }
    }

    private fun isFormValid(formData: ClientKycFormData): Boolean {
        return formData.panNumber.isNotBlank() ||
            formData.aadhaarNumber.isNotBlank() ||
            formData.drivingLicenseNumber.isNotBlank() ||
            formData.voterId.isNotBlank() ||
            formData.passportNumber.isNotBlank()
    }

    private fun isOfflineMode(): Boolean {
        return prefManager.userStatus
    }
}

/**
 * Form data for Client KYC matching web app implementation
 */
data class ClientKycFormData(
    val panNumber: String = "",
    val aadhaarNumber: String = "",
    val drivingLicenseNumber: String = "",
    val voterId: String = "",
    val passportNumber: String = "",
    val verificationNotes: String = "",
)

/**
 * UI state for Client KYC creation flow - simplified
 */
sealed class ClientKycUiState {
    object Loading : ClientKycUiState()

    data class FormReady(
        val client: Client,
        val formData: ClientKycFormData,
        val syncStatus: String?,
        val isLoading: Boolean,
        val isEditMode: Boolean = false,
    ) : ClientKycUiState()

    data class KycExists(
        val client: Client,
        val kycData: ClientKycData,
    ) : ClientKycUiState()

    data class OtpVerificationForm(
        val client: Client,
        val kycData: ClientKycData,
        val isGeneratingOtp: Boolean,
        val otpGenerated: Boolean,
        val isSubmittingOtp: Boolean,
    ) : ClientKycUiState()

    data class Success(
        val client: Client,
        val message: String,
    ) : ClientKycUiState()

    data class Error(
        val message: String,
    ) : ClientKycUiState()
}

 
