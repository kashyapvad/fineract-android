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
import com.mifos.core.objects.client.extend.kyc.GuarantorKycData
import com.mifos.core.objects.client.extend.kyc.GuarantorKycRequest
import com.mifos.core.objects.client.extend.kyc.GuarantorOtpGenerationRequest
import com.mifos.core.objects.client.extend.kyc.GuarantorOtpSubmissionRequest
import com.mifos.core.objects.client.extend.kyc.RelationshipType
import com.mifos.core.data.repository.extend.GuarantorKycRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Guarantor KYC functionality
 * Follows the simple offline-first pattern with direct creation flow
 */
@HiltViewModel
class GuarantorKycViewModel @Inject constructor(
    private val guarantorKycRepository: GuarantorKycRepository,
    private val prefManager: PrefManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<GuarantorKycUiState>(GuarantorKycUiState.Loading)
    val uiState: StateFlow<GuarantorKycUiState> = _uiState.asStateFlow()

    fun loadClientDetails(clientId: Int, isOffline: Boolean = false) {
        viewModelScope.launch {
            try {
                _uiState.value = GuarantorKycUiState.Loading

                // Create a client for display (in real implementation, this would be passed from parent screen)
                val client = Client().apply {
                    this.id = clientId
                    this.displayName = "Client $clientId"
                    this.accountNo = "ACC$clientId"
                    this.active = true
                }

                // Load existing guarantors
                val guarantors = try {
                    println("DEBUG: GuarantorKycViewModel loading guarantors for clientId: $clientId")
                    val result = guarantorKycRepository.getGuarantorKycList(clientId)
                    println("DEBUG: GuarantorKycViewModel loaded ${result.size} guarantors: $result")
                    result
                } catch (e: Exception) {
                    println("DEBUG: GuarantorKycViewModel error loading guarantors: ${e.message}")
                    e.printStackTrace()
                    emptyList()
                }

                _uiState.value = GuarantorKycUiState.GuarantorsList(
                    client = client,
                    guarantors = guarantors
                )
            } catch (e: Exception) {
                _uiState.value = GuarantorKycUiState.Error(
                    e.message ?: "Failed to load client details",
                )
            }
        }
    }

    fun showAddForm() {
        val currentState = _uiState.value
        if (currentState is GuarantorKycUiState.GuarantorsList) {
            _uiState.value = GuarantorKycUiState.FormReady(
                client = currentState.client,
                formData = GuarantorKycFormData(),
                isEditMode = false
            )
        }
    }

    fun editGuarantor(guarantor: GuarantorKycData) {
        val currentState = _uiState.value
        if (currentState is GuarantorKycUiState.GuarantorsList) {
            val formData = GuarantorKycFormData(
                fullName = guarantor.fullName ?: "",
                mobileNumber = guarantor.mobileNumber ?: "",
                relationshipToClient = guarantor.relationshipToClient ?: "",
                panNumber = guarantor.panNumber ?: "",
                aadhaarNumber = guarantor.aadhaarNumber ?: "",
                isPrimaryGuarantor = guarantor.isPrimaryGuarantor,
                isActive = guarantor.isActive,
                verificationNotes = guarantor.verificationNotes ?: "",
                editingId = guarantor.id
            )
            
            _uiState.value = GuarantorKycUiState.FormReady(
                client = currentState.client,
                formData = formData,
                isEditMode = true
            )
        }
    }

    fun deleteGuarantor(guarantor: GuarantorKycData) {
        viewModelScope.launch {
            try {
                guarantorKycRepository.deleteGuarantorKyc(guarantor.clientId?.toInt() ?: 0, guarantor.id ?: 0L)
                
                // Reload the list
                val currentState = _uiState.value
                if (currentState is GuarantorKycUiState.GuarantorsList) {
                    loadClientDetails(currentState.client.id)
                }
            } catch (e: Exception) {
                _uiState.value = GuarantorKycUiState.Error(
                    e.message ?: "Failed to delete guarantor"
                )
            }
        }
    }

    fun initiateOtpVerification(guarantor: GuarantorKycData) {
        val currentState = _uiState.value
        if (currentState is GuarantorKycUiState.GuarantorsList) {
            // Check if Aadhaar number exists for OTP verification
            if (guarantor.aadhaarNumber.isNullOrBlank()) {
                _uiState.value = GuarantorKycUiState.Error(
                    "Aadhaar number is required for OTP verification. Please edit guarantor KYC details to add Aadhaar number."
                )
                return
            }
            
            // Show OTP input form
            _uiState.value = GuarantorKycUiState.OtpVerificationForm(
                client = currentState.client,
                guarantor = guarantor,
                isGeneratingOtp = false,
                otpGenerated = false,
                isSubmittingOtp = false
            )
        }
    }

    fun generateOtp() {
        val currentState = _uiState.value
        if (currentState is GuarantorKycUiState.OtpVerificationForm) {
        viewModelScope.launch {
            try {
                    _uiState.value = currentState.copy(isGeneratingOtp = true)
                    
                    val request = GuarantorOtpGenerationRequest(
                        aadhaarNumber = currentState.guarantor.aadhaarNumber ?: ""
                    )
                    
                    guarantorKycRepository.generateOtpForGuarantorAadhaarVerification(
                        currentState.client.id,
                        currentState.guarantor.id ?: 0L,
                        request
                    )
                    
                    _uiState.value = currentState.copy(
                        isGeneratingOtp = false,
                        otpGenerated = true
                )
            } catch (e: Exception) {
                _uiState.value = GuarantorKycUiState.Error(
                        e.message ?: "Failed to generate OTP"
                    )
                }
            }
        }
    }

    fun submitOtp(otp: String, notes: String = "") {
        val currentState = _uiState.value
        if (currentState is GuarantorKycUiState.OtpVerificationForm) {
            viewModelScope.launch {
                try {
                    _uiState.value = currentState.copy(isSubmittingOtp = true)
                    
                    val request = GuarantorOtpSubmissionRequest(
                        otp = otp,
                        notes = notes.ifBlank { null }
                    )
                    
                    guarantorKycRepository.submitOtpForGuarantorAadhaarVerification(
                        currentState.client.id,
                        currentState.guarantor.id ?: 0L,
                        request
                    )
                    
                    _uiState.value = GuarantorKycUiState.Success(
                        client = currentState.client,
                        message = "Aadhaar verification completed successfully for ${currentState.guarantor.fullName} via OTP"
                )
                } catch (e: Exception) {
                    _uiState.value = GuarantorKycUiState.Error(
                        e.message ?: "Failed to verify OTP"
                    )
        }
    }
        }
    }

    fun cancelOtpVerification() {
        val currentState = _uiState.value
        if (currentState is GuarantorKycUiState.OtpVerificationForm) {
            // Reload guarantors list
            loadClientDetails(currentState.client.id)
        }
    }

    fun updateFormData(formData: GuarantorKycFormData) {
        val currentState = _uiState.value
        if (currentState is GuarantorKycUiState.FormReady) {
            _uiState.value = currentState.copy(formData = formData)
        }
    }

    fun submitGuarantorKyc(formData: GuarantorKycFormData) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is GuarantorKycUiState.FormReady) {
                    _uiState.value = GuarantorKycUiState.Loading

                    // Validate required fields
                    if (formData.fullName.isBlank()) {
                        _uiState.value = GuarantorKycUiState.Error("Guarantor name is required")
                        return@launch
                    }
                    if (formData.mobileNumber.isBlank()) {
                        _uiState.value = GuarantorKycUiState.Error("Mobile number is required")
                        return@launch
                    }

                    // Create guarantor KYC request
                    val guarantorKycRequest = GuarantorKycRequest(
                        fullName = formData.fullName,
                        mobileNumber = formData.mobileNumber,
                        relationshipToClient = formData.relationshipToClient.ifBlank { null },
                        panNumber = formData.panNumber.ifBlank { null },
                        aadhaarNumber = formData.aadhaarNumber.ifBlank { null },
                        isPrimaryGuarantor = formData.isPrimaryGuarantor,
                        verificationNotes = formData.verificationNotes.ifBlank { null },
                    )

                    if (currentState.isEditMode && formData.editingId != null) {
                        // Update existing guarantor
                        guarantorKycRepository.updateGuarantorKyc(
                            currentState.client.id,
                            formData.editingId,
                            guarantorKycRequest,
                        )
                        
                        // Show success message for update
                        _uiState.value = GuarantorKycUiState.Success(
                            client = currentState.client,
                            message = if (isOfflineMode()) {
                                "Guarantor KYC updated and saved for sync when online"
                            } else {
                                "Guarantor KYC updated successfully for ${formData.fullName}"
                            }
                        )
                    } else {
                        // Create new guarantor
                        guarantorKycRepository.createGuarantorKyc(
                            currentState.client.id,
                            guarantorKycRequest,
                        )
                        
                        // Show success message for creation
                        _uiState.value = GuarantorKycUiState.Success(
                            client = currentState.client,
                            message = if (isOfflineMode()) {
                                "Guarantor KYC created and saved for sync when online"
                            } else {
                                "Guarantor KYC created successfully for ${formData.fullName}"
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = GuarantorKycUiState.Error(
                    e.message ?: "Failed to ${if ((uiState.value as? GuarantorKycUiState.FormReady)?.isEditMode == true) "update" else "create"} guarantor KYC",
                )
            }
        }
    }

    private fun isOfflineMode(): Boolean {
        return prefManager.userStatus
    }
}

/**
 * Form data for Guarantor KYC matching web app implementation
 */
data class GuarantorKycFormData(
    val fullName: String = "",
    val mobileNumber: String = "",
    val relationshipToClient: String = "",
    val panNumber: String = "",
    val aadhaarNumber: String = "",
    val isPrimaryGuarantor: Boolean = false,
    val isActive: Boolean = true,
    val verificationNotes: String = "",
    val availableRelationships: List<String> = RelationshipType.getAllOptions(),
    val editingId: Long? = null // For edit mode
)

/**
 * UI state for Guarantor KYC flow - supporting multiple guarantors
 */
sealed class GuarantorKycUiState {
    object Loading : GuarantorKycUiState()

    data class GuarantorsList(
        val client: Client,
        val guarantors: List<GuarantorKycData>,
    ) : GuarantorKycUiState()

    data class FormReady(
        val client: Client,
        val formData: GuarantorKycFormData,
        val isEditMode: Boolean = false,
    ) : GuarantorKycUiState()

    data class OtpVerificationForm(
        val client: Client,
        val guarantor: GuarantorKycData,
        val isGeneratingOtp: Boolean = false,
        val otpGenerated: Boolean = false,
        val isSubmittingOtp: Boolean = false,
    ) : GuarantorKycUiState()

    data class Success(
        val client: Client,
        val message: String,
    ) : GuarantorKycUiState()

    data class Error(
        val message: String,
    ) : GuarantorKycUiState()
} 
